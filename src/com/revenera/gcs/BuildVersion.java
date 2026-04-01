package com.revenera.gcs;

public final class BuildVersion {
  private final String sequence;
  private final String date;
  private final String release;
  private final long epoch;

  public BuildVersion(final String date, final String sequence, final String release, final long epoch) {
    this.sequence = sequence;
    this.date = date;
    this.release = release;
    this.epoch = epoch;
  }

  public String getSequence() {
    return sequence;
  }

  public String getDate() {
    return date;
  }

  public String getRelease() {
    return release;
  }

  public String getVersionString() {
    return String.format("%s | %s | %s | %08X", this.date, this.sequence, this.release, this.epoch);
  }
}