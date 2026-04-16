package com.revenera.gcs.utils.log;

public interface ILogging {
  void log(String message);

  void log(Object... params);

  void log(Throwable t);
}

