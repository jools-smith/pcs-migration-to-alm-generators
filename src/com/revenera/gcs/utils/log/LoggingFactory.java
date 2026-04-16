package com.revenera.gcs.utils.log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.revenera.gcs.utils.Utils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class LoggingFactory {
  static final Object lock = new Object();

  final static Queue<String> messageQueue = new ConcurrentLinkedQueue<>();

  final static AtomicReference<Level> loggingLevel = new AtomicReference<>(Level.TRACE);

  final static AtomicReference<String> loggingRoot = new AtomicReference<>("c:\\revenera");

  public static boolean willLog(final Level level) {
    return loggingLevel.get().compare(level) >= 0;
//    return level.value <= loggingLevel.get().value;
  }

  public static Level setLoggingLevel(final Level level) {
    return loggingLevel.getAndSet(level);
  }

  public static String setLoggingRoot(final String root) {
    return loggingRoot.getAndSet(root);
  }

  public static boolean serializationPending() {
    return !messageQueue.isEmpty();
  }

  public static boolean serialize() {
    final String content = messageQueue.poll();
    if (content != null) {
      final String root = loggingRoot.get();

      if (Files.exists(Paths.get(root))) {
        try {
          final File file = Paths.get(root, LocalDate.now() + ".revenera.log").toAbsolutePath().toFile();

          FileUtils.writeLines(file, Collections.singletonList(content), true);

          return true;
        }
        catch (IOException e) {
          //exception(e);
          //TODO: what do we do here?
        }
      }
    }
    return false;
  }

  final Class<?> type;

  // implementor
  class SimpleLoggingImplementor implements ILogging {
    final Instant time = Instant.now();
    final Context context;
    
    private SimpleLoggingImplementor(final Context context) {
      this.context = context;
    }

    private void post(final String message) {
      synchronized (lock) {
        final String content = String.format("%s %5s [%s] {%s} %s.%s(%d) %s",
            this.context.getTimeUtc(),
            this.context.getLevel().getText(),
            Thread.currentThread().getName(),
            LoggingFactory.this.type.getSimpleName(),
            abbreviatePackageName(this.context.getClassName(), 32),
            this.context.getMethodName(),
            this.context.getLineNumber(),
            message);

        System.out.println(content);

        if (willLog(this.context.level)) {
          messageQueue.add(content);
        }
      }
    }

    @Override
    public void log(final String message) {
      post(message);
    }

    @Override
    public void log(final Object... params) {
      post(Arrays
          .stream(params)
          .map(Object::toString)
          .collect(Collectors.joining(" | ")));
    }

    @Override
    public void log(Throwable t) {
      final StackTraceElement frame = t.getStackTrace()[0];

      log(t.getClass().getName(),
          t.getLocalizedMessage(),
          frame.getFileName(),
          frame.getClassName(),
          frame.getMethodName(),
          frame.getLineNumber());
    }
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

  LoggingFactory(Class<?> type) {
    //
    this.type = type;
  }

  public ILogging error() {
    return new SimpleLoggingImplementor(
        new Context(Level.ERROR, new Throwable().getStackTrace()[1]));
  }

  public ILogging warning() {
    return new SimpleLoggingImplementor(
        new Context(Level.WARNING, new Throwable().getStackTrace()[1]));
  }

  public ILogging info() {
    return new SimpleLoggingImplementor(
        new Context(Level.INFO, new Throwable().getStackTrace()[1]));
  }

  public ILogging debug() {
    return new SimpleLoggingImplementor(
        new Context(Level.DEBUG, new Throwable().getStackTrace()[1]));
  }

  public ILogging verbose() {
    return new SimpleLoggingImplementor(
        new Context(Level.TRACE, new Throwable().getStackTrace()[1]));
  }

  public ILogging get(final Level level) {
    return new SimpleLoggingImplementor(
        new Context(level, new Throwable().getStackTrace()[1]));
  }

  public void in() {
    new SimpleLoggingImplementor(
        new Context(Level.TRACE, new Throwable().getStackTrace()[1])).log("-->");
  }

  public void in(final Object obj) {
    try {
      new SimpleLoggingImplementor(
          new Context(Level.TRACE, new Throwable().getStackTrace()[1])).log(
          "-->",
          obj.getClass().getName(),
          Utils.yaml_mapper.writeValueAsString(obj));
    }
    catch (final JsonProcessingException e) {
      exception(e);
    }
  }

  public void out() {
    new SimpleLoggingImplementor(
        new Context(Level.TRACE, new Throwable().getStackTrace()[1])).log("<--");
  }

  public void json(final Level level, final Object obj) {
    try {
      new SimpleLoggingImplementor(
          new Context(level, new Throwable().getStackTrace()[1])).log(
              obj.getClass().getName(),
              Utils.json_mapper_indented.writeValueAsString(obj));
    }
    catch (final JsonProcessingException e) {
      exception(e);
    }
  }

  public void yaml(final Level level, final Object obj) {
    try {
      new SimpleLoggingImplementor(
          new Context(level, new Throwable().getStackTrace()[1])).log(
            obj.getClass().getName(),
            Utils.yaml_mapper.writeValueAsString(obj));
    }
    catch (final JsonProcessingException e) {
      exception(e);
    }
  }

  public void me(final Object obj) {
    //TODO
  }

  public void exception(final Throwable t) {
    new SimpleLoggingImplementor(
        new Context(Level.ERROR, new Throwable().getStackTrace()[1])).log(t);
  }

  public Class<?> getType() {
    ///
    return this.type;
  }

  public static LoggingFactory create(final Class<?> type) {
    //
    return new LoggingFactory(type);
  }
}
