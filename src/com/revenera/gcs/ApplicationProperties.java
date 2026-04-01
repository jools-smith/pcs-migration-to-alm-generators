package com.revenera.gcs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.SystemProperties;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

public class ApplicationProperties {
  private final static Instant started = Instant.now();

  public final Map<String,Object> sys;
  public final Map<String,Object> env;
  public final Map<String,Object> os;
  public final Map<String,Object> java;
  public final Map<String,Object> build;

  ApplicationProperties() {

    this.build = new LinkedHashMap<String, Object>() {
      {
        final BuildVersion bv = Application.getBuildVersion();

        put("TIMESTAMP", bv.getTimeStamp());
        put("DATE", bv.getDate());
        put("TIME", bv.getTime());
        put("RELEASE", bv.getRelease());
        put("NUMBER", bv.getSequence());
        put("USER", bv.getUser());
      }
    };

    this.sys = new LinkedHashMap<String, Object>() {
      {
        put("TIMESTAMP", Instant.now().toString());
        put("UP_TIME", getUpTime().toString());
        put("USER_NAME", SystemProperties.getUserName("unknown"));
        put("HOST_NAME", SystemUtils.getHostName());
        put("RESOURCE_PATH", Application.getInstance().getResourcePath().toAbsolutePath().toString());
      }
    };

    final Runtime runtime = Runtime.getRuntime();
    this.env = new LinkedHashMap<String, Object>() {
      {
        Function<Long, String> tomb = v -> (v / (1024 * 1024)) + "MB";

        put("PROCESSORS", runtime.availableProcessors());
        put("FREE_MEMORY", tomb.apply(runtime.freeMemory()));
        put("TOTAL_MEMORY", tomb.apply(runtime.totalMemory()));
        put("MAX_MEMORY", tomb.apply(runtime.maxMemory()));
      }
    };

    this.os = new LinkedHashMap<String, Object>() {
      {
        put("OS_NAME", SystemUtils.OS_NAME);
        put("OS_VERSION", SystemUtils.OS_VERSION);
        put("OS_ARCH", SystemUtils.OS_ARCH);
      }
    };

    this.java = new LinkedHashMap<String, Object>() {
      {
        put("JAVA_VERSION", SystemUtils.JAVA_VERSION);
        put("JAVA_VENDOR", SystemUtils.JAVA_VENDOR);
        put("JAVA_CLASS_VERSION", SystemUtils.JAVA_CLASS_VERSION);
        put("JAVA_VM_NAME", SystemUtils.JAVA_VM_NAME);
        put("JAVA_VM_INFO", SystemUtils.JAVA_VM_INFO);
      }
    };
  }

  @JsonIgnore
  public Duration getUpTime() {
    return Duration.between(started, Instant.now());
  }

  public static ApplicationProperties create() {
    return new ApplicationProperties();
  }

}
