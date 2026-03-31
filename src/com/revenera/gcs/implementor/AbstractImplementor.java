package com.revenera.gcs.implementor;

import com.flexnet.external.type.*;
import com.flexnet.external.webservice.keygenerator.LicGeneratorException;
import com.flexnet.external.webservice.keygenerator.LicenseGeneratorServiceInterface;
import com.revenera.gcs.Application;
import com.revenera.gcs.ApplicationProperties;
import com.revenera.gcs.utils.Log;
import com.revenera.gcs.utils.Utils;
import org.apache.commons.lang3.SystemProperties;
import org.apache.commons.lang3.SystemUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class AbstractImplementor implements LicenseGeneratorServiceInterface {

  protected final Log logger = Log.create(this.getClass());

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

  @Override
  public PingResponse ping(final PingRequest request) {
    try {
      logger.in();

      return new PingResponse() {
        {
          this.info = Utils.safeSerializeYaml(ApplicationProperties.create());

          this.str = String.format("%s | %s | %s | %s | %s | %s | %s | %s | %s",
              logger.type().getSimpleName(),
              Application.getBuildVersion().getVersionString(),
              technologyId(),
              SystemProperties.getOsName(),
              SystemProperties.getOsVersion(),
              SystemProperties.getOsArch(),
              SystemUtils.getHostName(),
              SystemProperties.getUserName("unknown"),
              Application.getInstance().getResourcePath().toString());

          this.processedTime = Instant.now().toString();
        }
      };
    }
    finally {
      logger.out();
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

  @SuppressWarnings("unused")
  public abstract String technologyName();

  public abstract String technologyId();
}
