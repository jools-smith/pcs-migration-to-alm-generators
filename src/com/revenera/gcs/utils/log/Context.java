package com.revenera.gcs.utils.log;

import java.time.Instant;

public class Context {
  final StackTraceElement element;
  final Level level;
  final Instant time = Instant.now();

  Context(final Level level, final StackTraceElement element) {
    this.level = level;
    this.element = element; 
  }
  
  public Level getLevel() {
    //
    return level;
  }

  //UTC
  public String getTimeUtc() {
    return time.toString()
        .replace("T", " ")
        .replace("Z", "");
  }

  public String getClassName() {
    //
    return this.element.getClassName();
  }

  public String getMethodName () {
    //
    return this.element.getMethodName();
  }

  int getLineNumber(){
    //
    return this.element.getLineNumber();
  }
}
