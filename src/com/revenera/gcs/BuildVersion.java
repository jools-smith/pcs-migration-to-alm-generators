package com.revenera.gcs;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public final class BuildVersion {
  enum Items {
    TIMESTAMP("build.timestamp"),
    DATE("build.date"),
    TIME("build.time"),
    SEQUENCE("build.number"),
    USER("build.username"),
    RELEASE("build.category");
    final String name;
    Items(String name) {
      this.name = name;
    }
  }

  private final Map<Items,Object> properties = new LinkedHashMap<>();

  public BuildVersion() {
    try {
      final Properties props = new Properties();
      props.load(ApplicationProperties.class.getResourceAsStream("/revenera.properties"));

      Arrays.stream(Items.values()).forEach(item -> properties.put(item, props.getProperty(item.name)));
    }
    catch (final IOException ignored) {
      // just ignore this nothing can be done
    }
  }

  public String getTimeStamp() {
    return this.properties.get(Items.TIMESTAMP).toString();
  }

  public String getSequence() {
    return this.properties.get(Items.SEQUENCE).toString();
  }

  public String getDate() {
    return this.properties.get(Items.DATE).toString();
  }

  public String getTime() {
    return this.properties.get(Items.TIME).toString();
  }

  public String getUser() {
    return this.properties.get(Items.USER).toString();
  }

  public String getRelease() {
    return this.properties.get(Items.RELEASE).toString();
  }

  public String getVersionString() {
    final Instant inst = Instant.parse(getTimeStamp());
    return String.format("%s | %s | %s | %s | %08X", getDate(), getTime(), getSequence(), getRelease(), inst.toEpochMilli());
  }
}