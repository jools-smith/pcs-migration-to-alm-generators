package com.revenera.gcs.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


public final class Log {

  static final DateFormat df = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss.zzz");

  static AtomicReference<Log.Level> loggingLevel = new AtomicReference<>(Log.Level.trace);
  private final Class<?> type;

  private Log(final Class<?> cls) {
    this.type = cls;
  }

  public static boolean willLog(final Log.Level level) {
    return level.value >= loggingLevel.get().value;
  }

  public static Log.Level setLoggingLevel(final Log.Level level) {
    return loggingLevel.getAndSet(level);
  }

  public static Log create(final Class<?> type) {
    return new Log(type);
  }

  public Class<?> type() {
    return this.type;
  }

  public void dump(final String content) {
    final String root = "c:\\revenera";

    if (Files.exists(Paths.get(root))) {
      try {
        final File file = Paths.get(root, LocalDate.now() + "-revenera.log").toAbsolutePath().toFile();

        FileUtils.writeLines(file, Collections.singletonList(content), true);
      }
      catch (IOException e) {
        exception(e);
      }
    }
  }

  public void log(final Level level, final String message) {
    if (willLog(level)) {
      final Context context = new Context();

      final String content = String.format("%s %s [%s] {%s} %s.%s(%d) %s",
                                           context.getTime(),
                                           level.toString().toUpperCase(),
                                           Thread.currentThread().getName(),
                                           type.getSimpleName(),
                                           Utils.abbreviatePackageName(context.getClassName(), 30),
                                           context.getMethod(),
                                           context.getLine(),
                                           message);

      System.out.println(content);

      dump(content);
    }
  }

  public void yaml(final Level level, final Object obj) {
    try {
      log(level, Utils.safeSerializeYaml(obj));
    }
    catch (final Throwable e) {
      exception(e);
    }
  }

  public void json(final Level level, final Object obj) {
    try {
      log(level, Utils.safeSerializeJsonIndented(obj));
    }
    catch (final Throwable e) {
      exception(e);
    }
  }

  public void in() {
    log(Level.trace, "->");
  }

  public void me(final Object self) {
    array(Level.trace, self.getClass().getSimpleName(), Integer.toHexString(self.hashCode()));
  }

  public void out() {
    log(Level.trace, "<-");
  }

  public void exception(final Throwable t) {
    t.printStackTrace(System.err);

    final StackTraceElement frame = t.getStackTrace()[0];

    array(Level.severe,
          t.getClass().getName(),
          t.getLocalizedMessage(),
          frame.getFileName(),
          frame.getClassName(),
          frame.getMethodName(),
          frame.getLineNumber());
  }

  public void array(final Level level, final Object... params) {

    log(level, Arrays.stream(params).map(Object::toString).collect(Collectors.joining(" | ")));
  }

  /**
   * LEVEL
   */
  public enum Level {

    trace(0),
    debug(1),
    info(2),
    warning(3),
    error(4),
    severe(5);

    public final int value;

    Level(final int value) {
      this.value = value;
    }
  }

  static class Context {
    final StackTraceElement element = new Throwable().getStackTrace()[3];
    final Calendar calendar = GregorianCalendar.getInstance();

    String getTime() {
      return df.format(this.calendar.getTime());
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
}