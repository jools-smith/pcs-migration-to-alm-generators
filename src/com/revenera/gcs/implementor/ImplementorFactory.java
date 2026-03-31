package com.revenera.gcs.implementor;

import com.flexnet.external.webservice.keygenerator.LicenseGeneratorServiceInterface;
import com.revenera.gcs.utils.Log;

import java.util.HashMap;
import java.util.Map;

public class ImplementorFactory {
  private final static Log logger = Log.create(ImplementorFactory.class);

  public final static String default_technology_id = "DEF";

  private final Map<String, LicenseGeneratorServiceInterface> implementors = new HashMap<>();

  public void addImplementor(final AbstractImplementor imp) {

    logger.log(Log.Level.debug, imp.technologyId() + " -> " + imp.getClass().getSimpleName());

    this.implementors.put(imp.technologyId(), imp);
  }

  public LicenseGeneratorServiceInterface getDefaultImplementor() {
    return this.implementors.get(default_technology_id);
  }

  public LicenseGeneratorServiceInterface getImplementor(final String id) {

    if (this.implementors.containsKey(id)) {
      final LicenseGeneratorServiceInterface impl = this.implementors.get(id);

      logger.me(impl);

      return this.implementors.get(id);
    }
    else {
      return getDefaultImplementor();
    }
  }
}
