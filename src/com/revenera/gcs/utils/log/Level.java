package com.revenera.gcs.utils.log;

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
