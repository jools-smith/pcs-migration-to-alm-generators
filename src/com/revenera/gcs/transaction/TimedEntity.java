package com.revenera.gcs.transaction;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Duration;
import java.time.Instant;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public abstract class TimedEntity {
  protected final Instant start = Instant.now();

  protected Duration duration;

  @JsonIgnore
  public String key() {
    return this.start.toString().replace(":",".").replace("-",".");
  }
  @JsonIgnore
  protected void stop() {
    if (isStopped()) {
      throw new TransactionException("Already stopped");
    }
    this.duration = Duration.between(this.start, Instant.now());
  }

  @JsonIgnore
  public boolean isStopped() {
    return this.duration != null;
  }
}
