package com.revenera.gcs.utils.log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.atomic.AtomicReference;

abstract class LogBase {
  final static Object lock = new Object();

  final static AtomicReference<Level> loggingLevel = new AtomicReference<>(Level.trace);

  final static AtomicReference<String> loggingRoot = new AtomicReference<>("c:\\revenera");

  public static boolean willLog(final Level level) {
    return level.value >= loggingLevel.get().value;
  }

  public static Level setLoggingLevel(final Level level) {
    return loggingLevel.getAndSet(level);
  }

  public static String setLoggingRoot(final String root) {
    return loggingRoot.getAndSet(root);
  }
}
