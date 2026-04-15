package com.revenera.gcs.utils.log;

import java.time.Instant;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class Context {
  final StackTraceElement element = new Throwable().getStackTrace()[3];
  final Instant time = Instant.now();

  //UTC
  String getTimeUtc() {
    return time.toString()
        .replace("T", " ")
        .replace("Z", "");
  }

  String getClassName() {
    return this.element.getClassName();
  }

  String getMethod() {
    return this.element.getMethodName();
  }

  int getLine() {
    return this.element.getLineNumber();
  }
}
