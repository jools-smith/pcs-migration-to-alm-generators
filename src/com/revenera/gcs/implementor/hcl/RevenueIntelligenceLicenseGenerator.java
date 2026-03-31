package com.revenera.gcs.implementor.hcl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.flexnet.external.type.*;
import com.flexnet.external.webservice.keygenerator.LicGeneratorException;
import com.revenera.gcs.Application;
import com.revenera.gcs.implementor.AbstractImplementor;
import com.revenera.gcs.utils.GeneratorImplementor;
import com.revenera.gcs.utils.Log;
import com.revenera.gcs.utils.Utils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import javax.xml.datatype.XMLGregorianCalendar;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

class Resources {
  final String filename;
  final Path directory;
  final Path input;
  final Path executable;

  Resources() {
    this.filename = "raw." + Instant.now().toString().replace(":", ".").replace("-", ".");

    // working directory
    this.directory = Application.getInstance().getResourcePath("licenses");

    // input file for signer
    this.input = Application.getInstance().getResourcePath("licenses", filename);

    // path to signer
    this.executable = Application.getInstance().getResourcePath("executable", "Test.exe");

  }
}

class FeatureLine {
  @JsonIgnore
  public String key() {
    return String.format("%s|%s|%d|%s", this.featureName, this.featureVersion, this.expirationDate, this.ipAddress);
  }

  static long toLong(final XMLGregorianCalendar date) {
    return date == null ? 0L : date.toGregorianCalendar().getTime().getTime();
  }

  public String featureName;
  public String featureVersion;
  public long featureCount;
  public long expirationDate;
  public String ipAddress;

  public FeatureLine() {
  }

  public boolean hashSubnetMask() {
    return StringUtils.isNotEmpty(this.ipAddress);
  }

  public static FeatureLine create(final com.flexnet.external.type.Feature feature,
                                   final long fulfilmentCount,
                                   final long quantityPerCopy,
                                   final XMLGregorianCalendar expiration,
                                   final String subnet) {
    return new FeatureLine() {
      {
        this.featureName = feature.getName();
        this.featureVersion = feature.getVersion();
        this.featureCount = feature.getCount() * fulfilmentCount * quantityPerCopy;
        this.expirationDate = toLong(expiration);
        this.ipAddress = subnet;
      }
    };
  }

  @Override
  public String toString() {
    final StringBuilder bfr = new StringBuilder();
    bfr.append(featureName)
       .append(" ")
       .append(featureCount);


    if (this.expirationDate > 0) {
      bfr.append(" ")
         .append(new Date(this.expirationDate).toInstant().toString());
    }

    if (hashSubnetMask()) {
      bfr.append(" ")
         .append(this.ipAddress);
    }

    return bfr.toString();
  }

  public void add(final FeatureLine value) {
    if (key().equals(value.key())) {
      this.featureCount += value.featureCount;
    }
  }

  static final TypeReference<List<FeatureLine>> featureLineListType = new TypeReference<List<FeatureLine>>() {
  };

  @SuppressWarnings("unused")
  public static String serializeList(final List<FeatureLine> features) {
    try {
      return Utils.yaml_mapper.writeValueAsString(features);
    }
    catch (final Throwable t) {
      throw new RuntimeException(t);
    }
  }

  static List<FeatureLine>deserializeList(final String payload) {
    try {
      return Utils.yaml_mapper.readValue(payload, featureLineListType);
    }
    catch (final Throwable t) {
      throw new RuntimeException(t);
    }
  }
}

@SuppressWarnings("unused")
@GeneratorImplementor(technology = "RI")
public class RevenueIntelligenceLicenseGenerator extends AbstractImplementor {

  @Override
  public String technologyName() {
    return "Revenue Intelligence License Technology";
  }

  @Override
  public String technologyId() {
    return "RI";
  }

  private enum Strings {
    License,
    Signature,
    SUBNET_MASK,
    SUBNET_MASK_CIDR_FORMAT
  }

  private String signLicense(final List<String> lines) {
    logger.in();
    try {
      final Resources res = new Resources();

      logger.yaml(Log.Level.debug, res);

      FileUtils.writeLines(res.input.toFile(), StandardCharsets.UTF_8.name(), lines);

      final ProcessBuilder pb = new ProcessBuilder(res.executable.toAbsolutePath().toString(), res.filename);
      logger.log(Log.Level.debug, "created process");

      pb.directory(res.directory.toFile());

      final Process proc = pb.start();
      logger.log(Log.Level.debug, "started process");

      final boolean status = proc.waitFor(30, TimeUnit.SECONDS);

      if (status) {
        logger.log(Log.Level.debug, "completed process");

        return String.join("\n", lines);
      }
      else {
        return "ERROR";
      }
    }
    catch (final Throwable e) {
      logger.exception(e);

      return e.getMessage();
    }
    finally {
      // cleanup
      logger.out();
    }
  }

  static final Function<CustomAttributeDescriptor, Boolean> attribute_is_valid = x ->
          StringUtils.isNotEmpty(x.getValue()) &&
          StringUtils.equalsAny(x.getName(), Strings.SUBNET_MASK.toString(), Strings.SUBNET_MASK_CIDR_FORMAT.toString());

  @Override
  public GeneratorResponse generateLicense(final GeneratorRequest request) throws LicGeneratorException {
    logger.in();

    final AtomicReference<String> subnet = new AtomicReference<>();

    request.getLicenseModel()
           .getFulfillmentTimeAttributes()
           .getAttributes()
           .stream()
           .filter(attribute_is_valid::apply)
           .findAny()
           .ifPresent(att -> subnet.set(att.getValue()));

    final List<FeatureLine> licenseElements = new ArrayList<>();

    request.getEntitledProducts()
           .forEach(prod -> prod.getFeatures()
                                .forEach(feature -> licenseElements.add(FeatureLine.create(
                                        feature,
                                        request.getFulfillCount(),
                                        prod.getQuantityPerCopy(),
                                        request.getExpirationDate(),
                                        subnet.get()))));

    return new GeneratorResponse() {
      {
        this.licenseFiles = Collections.singletonList(new LicenseFileMapItem() {
          {
            name = Strings.License.toString();
            value = Utils.safeSerializeYaml(licenseElements);
          }
        });

        this.complete = true;

        // debug
        this.licenseFiles.forEach(file -> logger.array(Log.Level.debug, file.getName(), file.getValue()));
      }
    };
  }


  @Override
  public ConsolidatedLicense consolidateFulfillments(final FulfillmentRecordSet request) throws LicGeneratorException {

    logger.array(Log.Level.debug, Application.getBuildVersion().getDate(), Application.getBuildVersion().getSequence());

    final Map<String, FeatureLine> licenseElements = new TreeMap<>();

    request.getFulfillments().stream()
           .flatMap(fid -> fid.getLicenseFiles().stream())
           .filter(file -> file.getName().equals(Strings.License.toString()))
           .forEach(file -> {
              final List<FeatureLine> lines = FeatureLine.deserializeList(file.getValue().toString());

              lines.forEach(line -> {
                final String key = line.key();

                if (!licenseElements.containsKey(key)) {
                  licenseElements.put(key, line);
                }
                else {
                  licenseElements.get(key).featureCount += line.featureCount;
                }
              });
            });

    return new ConsolidatedLicense() {
      {
        this.fulfillments = request.getFulfillments();

        this.licFiles = Collections.singletonList(new LicenseFileMapItem() {
          {
            this.name = Strings.License.toString();
            // build intermediate format
            this.value = licenseElements.values().stream()
                                        .map(FeatureLine::toString)
                                        .collect(Collectors.joining("\n"));
          }
        });

        // debug
        this.licFiles.forEach(file -> logger.array(Log.Level.debug, file.getName(), file.getValue()));
      }
    };
  }
}
