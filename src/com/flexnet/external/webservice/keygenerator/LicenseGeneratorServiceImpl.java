package com.flexnet.external.webservice.keygenerator;

import com.flexnet.external.type.*;
import com.revenera.gcs.Application;
import com.revenera.gcs.ServiceBase;
import com.revenera.gcs.utils.Diagnostics.Token;

import javax.jws.WebService;

@WebService(
        endpointInterface = "com.flexnet.external.webservice.keygenerator.LicenseGeneratorServiceInterface",
        wsdlLocation = "WEB-INF/wsdl/schema/LicenseGeneratorService.wsdl"
)
public class LicenseGeneratorServiceImpl extends ServiceBase implements LicenseGeneratorServiceInterface {

  @Override
  public PingResponse ping(final PingRequest payload) throws LicGeneratorException {
    logger.in(payload);
    final Token token = createDiagnosticsToken();
    try {
//      logger.json(Level.DEBUG, payload);
      return Application
          .getInstance()
          .getImplementorFactory()
          .getDefaultImplementor()
          .ping(payload);
    }
    catch (final Throwable t) {
      throw new LicGeneratorException(t.getMessage(), this.serviceException.apply(t));
    }
    finally {
      token.commit();
//      logger.out();
    }
  }

  @Override
  public Status validateProduct(final ProductRequest payload) throws LicGeneratorException {
    logger.in(payload);
    final Token token = createDiagnosticsToken();
    try {
//      logger.yaml(Level.DEBUG, payload);
      final String tech = super.getLicenseTechnology(payload);

      return Application.getInstance()
          .getImplementorFactory()
          .getImplementor(tech)
          .validateProduct(payload);
    }
    catch (final Throwable t) {
      throw new LicGeneratorException(t.getMessage(), this.serviceException.apply(t));
    }
    finally {
      token.commit();
//      logger.out();
    }
  }

  @Override
  public Status validateLicenseModel(final LicenseModelRequest payload) throws LicGeneratorException {
    logger.in(payload);
    final Token token = createDiagnosticsToken();
    try {
//      logger.yaml(Level.DEBUG, payload);
      final String tech = super.getLicenseTechnology(payload);

      return Application.getInstance()
          .getImplementorFactory()
          .getImplementor(tech)
          .validateLicenseModel(payload);
    }
    catch (final Throwable t) {
      throw new LicGeneratorException(t.getMessage(), this.serviceException.apply(t));
    }
    finally {
      token.commit();
//      logger.out();
    }
  }

  @Override
  public GeneratorResponse generateLicense(final GeneratorRequest payload) throws LicGeneratorException {
    logger.in(payload);
    final Token token = createDiagnosticsToken();
    try {
//      logger.yaml(Level.DEBUG, payload);

      final String tech = super.getLicenseTechnology(payload);

      return Application.getInstance()
          .getImplementorFactory()
          .getImplementor(tech)
          .generateLicense(payload);
    }
    catch (final Throwable t) {
      throw new LicGeneratorException(t.getMessage(), this.serviceException.apply(t));
    }
    finally {
      token.commit();
//      logger.out();
    }
  }

  @Override
  public ConsolidatedLicense consolidateFulfillments(final FulfillmentRecordSet payload) throws LicGeneratorException {
    logger.in(payload);
    final Token token = createDiagnosticsToken();
    try {
//      logger.yaml(Level.DEBUG, payload);
      final String tech = super.getLicenseTechnology(payload);

      return Application.getInstance()
          .getImplementorFactory()
          .getImplementor(tech)
          .consolidateFulfillments(payload);
    }
    catch (final Throwable t) {
      throw new LicGeneratorException(t.getMessage(), this.serviceException.apply(t));
    }
    finally {
      token.commit();
//      logger.out();
    }
  }

  @Override
  public LicenseFileDefinitionMap generateLicenseFilenames(final GeneratorRequest payload) throws LicGeneratorException {
    logger.in(payload);
    final Token token = createDiagnosticsToken();
    try {
//      logger.yaml(Level.DEBUG, payload);
      final String tech = super.getLicenseTechnology(payload);

      return Application.getInstance()
          .getImplementorFactory()
          .getImplementor(tech)
          .generateLicenseFilenames(payload);
    }
    catch (final Throwable t) {
      throw new LicGeneratorException(t.getMessage(), this.serviceException.apply(t));
    }
    finally {
      token.commit();
//      logger.out();
    }
  }


  @Override
  public LicenseFileDefinitionMap generateConsolidatedLicenseFilenames(final ConsolidatedLicenseResquest payload)
      throws LicGeneratorException {
    logger.in(payload);
    final Token token = createDiagnosticsToken();
    try {
//      logger.yaml(Level.DEBUG, payload);
      final String tech = super.getLicenseTechnology(payload);

      return Application.getInstance()
          .getImplementorFactory()
          .getImplementor(tech)
          .generateConsolidatedLicenseFilenames(payload);
    }
    catch (final Throwable t) {
      throw new LicGeneratorException(t.getMessage(), this.serviceException.apply(t));
    }
    finally {
      token.commit();
//      logger.out();
    }
  }

  @Override
  public String generateCustomHostIdentifier(final HostIdRequest payload) throws LicGeneratorException {
    logger.in(payload);
    final Token token = createDiagnosticsToken();
    try {
//      logger.yaml(Level.DEBUG, payload);
      return Application.getInstance()
          .getImplementorFactory()
          .getDefaultImplementor()
          .generateCustomHostIdentifier(payload);
    }
    catch (final Throwable t) {
      throw new LicGeneratorException(t.getMessage(), this.serviceException.apply(t));
    }
    finally {
      token.commit();
//      logger.out();
    }
  }
}
