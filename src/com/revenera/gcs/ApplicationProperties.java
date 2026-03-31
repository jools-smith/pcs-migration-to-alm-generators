package com.revenera.gcs;

import org.apache.commons.lang3.SystemProperties;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.stream.Collectors;


public class ApplicationProperties {

  public final Map<Object,Object> properties;
  public final Map<Object,Object> os;
  public final Map<Object,Object> java;
  public final Map<Object,Object> environment;

  public final String hostName = SystemUtils.getHostName();

  public final String userName = SystemProperties.getUserName("unknown");

  public static Map<Object, Object> getBuildProperties() {
    final Properties props = new Properties();

    try {
      props.load(ApplicationProperties.class.getResourceAsStream("/revenera.properties"));

      // no reliance on actual property names
      return props.stringPropertyNames().stream()
          .collect(Collectors.toMap(
              name -> name,
              props::getProperty, (e1, e2) -> e1,
              TreeMap::new
          ));
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  ApplicationProperties() {

    // no reliance on actual property names
    this.properties = getBuildProperties();

    final Runtime runtime = Runtime.getRuntime();
    this.environment = new LinkedHashMap<Object, Object>() {
      {
        put("availableProcessors", runtime.availableProcessors());
        put("freeMemory", runtime.freeMemory());
        put("totalMemory", runtime.totalMemory());
        put("maxMemory", runtime.maxMemory());
      }
    };

    final Properties systemProps = System.getProperties();
    this.os = systemProps.stringPropertyNames().stream()
        .filter(p -> p.startsWith("os."))
        .collect(Collectors.toMap(
            name -> name.substring("os.".length()).replace(".", " "),
            systemProps::getProperty, (e1, e2) -> e1,
            TreeMap::new
        ));

    this.java = systemProps.stringPropertyNames().stream()
        .filter(p -> p.startsWith("java."))
        .collect(Collectors.toMap(
            name -> name.substring("java.".length()).replace(".", " "),
            systemProps::getProperty, (e1, e2) -> e1,
            TreeMap::new
        ));


  }

  public static ApplicationProperties create() {
    return new ApplicationProperties();
  }

}
