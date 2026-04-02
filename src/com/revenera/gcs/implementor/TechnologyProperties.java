package com.revenera.gcs.implementor;

public interface TechnologyProperties {

  public abstract String technologyName();

  public abstract String technologyId();

  public abstract void configureTechnologyProperties(String id, String name);
}
