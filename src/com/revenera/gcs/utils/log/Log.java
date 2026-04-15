package com.revenera.gcs.utils.log;

import com.revenera.gcs.utils.Utils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;


public final class Log extends LogBase {

  private final Class<?> type;

  private Log(final Class<?> cls) {
    this.type = cls;
  }

  public static Log create(final Class<?> type) {
    return new Log(type);
  }

  public Class<?> type() {
    return this.type;
  }

  private void serialize(final String content) {
    synchronized (lock) {
      final String root = loggingRoot.get();

      if (Files.exists(Paths.get(root))) {
        try {
          final File file = Paths.get(root, LocalDate.now() + ".revenera.log").toAbsolutePath().toFile();

          FileUtils.writeLines(file, Collections.singletonList(content), true);
        }
        catch (IOException e) {
          exception(e);
        }
      }
    }
  }

  public void log(final Level level, final String message) {
    if (willLog(level)) {

      synchronized (lock) {
        final Context context = new Context();

        final String content = String.format("%s %s [%s] {%s} %s.%s(%d) %s",
            context.getTimeUtc(),
            level.toString().toUpperCase(),
            Thread.currentThread().getName(),
            type.getSimpleName(),
            Utils.abbreviatePackageName(context.getClassName(), 30),
            context.getMethod(),
            context.getLine(),
            message);

        // let tomcat sort it out
        System.out.println(content);

        serialize(content);
      }
    }
  }

  // NOT SYNCHRONIZED...

  public void yaml(final Level level, final Object obj) {
    try {
      array(level,
          obj.getClass().getName(),
          obj.getClass().getSimpleName(),
          Utils.safeSerializeYaml(obj));
    }
    catch (final Throwable e) {
      exception(e);
    }
  }

  public void json(final Level level, final Object obj) {
    try {
      array(level,
          obj.getClass().getName(),
          obj.getClass().getSimpleName(),
          Utils.safeSerializeJsonIndented(obj));
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
}

