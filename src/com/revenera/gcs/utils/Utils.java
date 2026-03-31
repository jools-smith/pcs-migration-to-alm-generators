package com.revenera.gcs.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import javax.xml.datatype.XMLGregorianCalendar;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

public class Utils {

  public static final  ObjectMapper json_mapper_indented = new ObjectMapper()
          .enable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
          .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
          .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
          .enable(SerializationFeature.INDENT_OUTPUT);

  public static final  ObjectMapper json_mapper = new ObjectMapper()
          .enable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
          .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
          .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

  public static final  ObjectMapper yaml_mapper = new ObjectMapper(new YAMLFactory())
          .enable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
          .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
          .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

  static {
    json_mapper_indented.findAndRegisterModules();
    json_mapper.findAndRegisterModules();
    yaml_mapper.findAndRegisterModules();
  }

  public static final Function<Integer, String[]> frameDetails = (offset) -> {
    final StackTraceElement frame = Thread.currentThread().getStackTrace()[offset];

    return new String[] {
            frame.getClassName(),
            frame.getMethodName(),
            String.valueOf(frame.getLineNumber())
    };
  };

  public static final SimpleDateFormat yyyy_mm_dd = new SimpleDateFormat("yyyy-MM-dd");

  public static Date gregorianCalendarToDate(final XMLGregorianCalendar calendar) {
    return calendar.toGregorianCalendar().getTime();
  }

  public static String format(final XMLGregorianCalendar calendar, final DateFormat df) {
    return df.format(calendar.toGregorianCalendar().getTime());
  }

  public static String abbreviatePackageName(final String name, final int limit) {

    String str = name;

    if (str.length() > limit) {
      final String[] parts = name.split("\\.");

      for (int i = 0; i < parts.length; i++) {
        parts[i] = parts[i].substring(0, 1);

        str = String.join(".", parts);
        if (str.length() <= limit) {
          break;
        }
      }
    }

    return str;
  }

  static String serialize(final ObjectMapper mapper, final Object payload) {
    try {
      return mapper.writeValueAsString(payload);
    }
    catch (final Throwable t) {
      throw new RuntimeException(t);
    }
  }



  public static String safeSerializeJson(final Object payload) {
    return serialize(json_mapper, payload);
  }

  public static String safeSerializeJsonIndented(final Object payload) {
    return serialize(json_mapper_indented, payload);
  }

  public static String safeSerializeYaml(final Object payload) {
    return serialize(yaml_mapper, payload);
  }

  public static String frameDetails(final StackTraceElement frame) {
    return  String.join("|",
            frame.getFileName(),
            frame.getClassName(),
            frame.getMethodName(),
            java.lang.String.valueOf(frame.getLineNumber()));
  }

  public static String jsonToBase64(final Object payload) {
    return Base64.getEncoder().encodeToString(safeSerializeJsonIndented(payload).getBytes());
  }

  public static String yamlToBase64(final Object payload) {
    return Base64.getEncoder().encodeToString(safeSerializeYaml(payload).getBytes());
  }
}
