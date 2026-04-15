package com.revenera.gcs.implementor;

public interface TechnologyProperties {

  String technologyName();

  String technologyId();

  void configureTechnologyProperties(String id, String name);
}
