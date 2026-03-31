package com.revenera.gcs;

public final class BuildVersion {
  private final String sequence;
  private final String date;
  private final String release;

  public BuildVersion(String sequence, String date, String release) {
    this.sequence = sequence;
    this.date = date;
    this.release = release;
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
    return String.format("%s | %s | %s", this.date, this.sequence, this.release);
  }
}