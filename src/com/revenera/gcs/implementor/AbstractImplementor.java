package com.revenera.gcs.implementor;

import com.flexnet.external.type.*;
import com.flexnet.external.webservice.keygenerator.LicGeneratorException;
import com.flexnet.external.webservice.keygenerator.LicenseGeneratorServiceInterface;
import com.revenera.gcs.Application;
import com.revenera.gcs.ApplicationData;
import com.revenera.gcs.utils.Utils;
import com.revenera.gcs.utils.log.LoggingFactory;
import org.apache.commons.lang3.SystemProperties;
import org.apache.commons.lang3.SystemUtils;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractImplementor implements TechnologyProperties, LicenseGeneratorServiceInterface {

  protected final LoggingFactory logger = LoggingFactory.create(this.getClass());

  @SuppressWarnings("unused")
  static protected <T> T raiseLicGeneratorException(final Throwable t) throws LicGeneratorException {
    throw new LicGeneratorException("unexpected exception", new SvcException() {
      {
        this.message = t.getMessage();
        this.name = t.getClass().getSimpleName();
      }
    });
  }

  protected List<LicenseFileMapItem> makeLicenseFiles(final List<LicenseFileDefinition> files, final String text, final byte[] bytes) {
    return new ArrayList<LicenseFileMapItem>() {
      {
        files.forEach(lfd -> {
          switch (lfd.getLicenseStorageType()) {
            case TEXT:
              Optional.ofNullable(text).ifPresent(license -> this.add(new LicenseFileMapItem() {
                {
                  this.name = lfd.getName();
                  this.value = license;
                }
              }));
              break;
            case BINARY:
              Optional.ofNullable(bytes).ifPresent(license -> this.add(new LicenseFileMapItem() {
                {
                  this.name = lfd.getName();
                  this.value = license;
                }
              }));
              break;
            default:
              throw new RuntimeException("invalid license file type");
          }
        });
      }
    };
  }

  private static class Technology {
    protected String name;
    protected String id;
  }

  private final Technology technology = new Technology();
  // TECHNOLOGY PROPS

  @Override
  public void configureTechnologyProperties(final String id, final String name) {
    this.technology.id = id;
    this.technology.name = name;
  }

  @Override
  public String technologyId() {
   return this.technology.id;
  }

  @Override
  public String technologyName() {
    return this.technology.name;
  }

  @Override
  public PingResponse ping(final PingRequest request) {
    try {
      logger.in();

      return new PingResponse() {
        {
          final ApplicationData props = ApplicationData.create();

          this.info = Utils.safeSerializeYaml(props);

          class Bag {
            final Map<String, Object> elements = new LinkedHashMap<>();

            Bag with(final String key, final Object... values) {

              this.elements.put(key, Arrays
                  .stream(values)
                  .map(Object::toString)
                  .collect(Collectors.joining(" | ")));

              return this;
            }

            String build() {
              return this.elements.entrySet()
                  .stream()
                  .map(e -> e.getKey() + ": " + e.getValue())
                  .collect(Collectors.joining("\n"));
            }
          }

          this.str = new Bag()
              .with("imp", logger.getType().getSimpleName(), technologyId())
              .with("ver",
                  Application.getApplicationProperties().getVersion(),
                  Application.getApplicationProperties().getDate(),
                  Application.getApplicationProperties().getTime())
              .with("sys",
                  SystemProperties.getOsName(),
                  SystemProperties.getOsVersion(),
                  SystemProperties.getOsArch()
                  )
              .with("host",
                  SystemUtils.getHostName(),
                  SystemProperties.getUserName("unknown"))
              .with("path", Application.getInstance().getResourcePath())
              .with("up",props.getUpTime())
              .build();

          this.processedTime = Instant.now().toString();
        }
      };
    }
    catch (final Throwable t) {
      return new PingResponse() {
        {
          this.info = t.getClass().getName();
          this.str = t.getMessage();
          this.processedTime = Instant.now().toString();
        }
      };
    }
  }

  @Override
  public Status validateProduct(final ProductRequest product) throws LicGeneratorException {
    return new Status() {
      {
        this.message = "product is validated | " + product.getName() + " | " + product.getVersion();
        this.code = 0;
      }
    };
  }

  @Override
  public Status validateLicenseModel(final LicenseModelRequest model) throws LicGeneratorException {
    return new Status() {
      {
        this.message = "license model is validated | " + model.getName();
        this.code = 0;
      }
    };
  }

  @Override
  public ConsolidatedLicense consolidateFulfillments(final FulfillmentRecordSet fulfillmentRecordset) throws LicGeneratorException {
    final String license = fulfillmentRecordset.getFulfillments().stream().flatMap(fulfilment -> fulfilment.getLicenseFiles().stream()).filter(lfd -> String.class.isAssignableFrom(lfd.getValue().getClass())).map(lfd -> lfd.getValue().toString()).collect(Collectors.joining("\n"));

    return new ConsolidatedLicense() {
      {
        this.fulfillments = fulfillmentRecordset.getFulfillments();

        fulfillmentRecordset.getFulfillments().stream().findAny().ifPresent(fid -> this.licFiles =
                makeLicenseFiles(fid.getLicenseTechnology().getLicenseFileDefinitions(), license, null));
      }
    };
  }

  private <T> T except(final Class<T> type, final String message) {
    throw new RuntimeException(message + " | " + type.getName());
  }

  @Override
  public LicenseFileDefinitionMap generateLicenseFilenames(final GeneratorRequest fileRec) throws LicGeneratorException {

    return except(LicenseFileDefinitionMap.class, "generateLicenseFilenames not implemented");
  }

  @Override
  public LicenseFileDefinitionMap generateConsolidatedLicenseFilenames(final ConsolidatedLicenseResquest clRec) throws LicGeneratorException {
    return except(LicenseFileDefinitionMap.class, "generateConsolidatedLicenseFilenames not implemented");
  }

  @Override
  public String generateCustomHostIdentifier(final HostIdRequest hostIdReq) throws LicGeneratorException {
    return except(String.class, "generateCustomHostIdentifier not implemented");
  }
}
