package com.revenera.gcs.implementor;

import com.flexnet.external.webservice.keygenerator.LicenseGeneratorServiceInterface;
import com.revenera.gcs.utils.log.Level;
import com.revenera.gcs.utils.log.Log;

import java.util.HashMap;
import java.util.Map;

public class ImplementorFactory {
  private final static Log logger = Log.create(ImplementorFactory.class);

  private final Map<String, LicenseGeneratorServiceInterface> implementors = new HashMap<>();

  private LicenseGeneratorServiceInterface defaultImplementor = null;

  public void addImplementor(final AbstractImplementor imp, final boolean isDefault) {

    logger.log(Level.debug, imp.technologyId() + " -> " + imp.getClass().getSimpleName());

    this.implementors.put(imp.technologyId(), imp);

    if (isDefault) {
      this.defaultImplementor = imp;
    }
  }

  public LicenseGeneratorServiceInterface getDefaultImplementor() {
    return this.defaultImplementor;
  }

  public LicenseGeneratorServiceInterface getImplementor(final String id) {

    if (this.implementors.containsKey(id)) {
      final LicenseGeneratorServiceInterface impl = this.implementors.get(id);

//      logger.me(impl);

      return this.implementors.get(id);
    }
    else {
      return getDefaultImplementor();
    }
  }
}
