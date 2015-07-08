package com.ifedorenko.m2e.mavendev.launch.ui.internal.model;

public class BuildStatus {

  private final int total;

  private final int succeeded;

  private final int failed;

  private final int skipped;

  public BuildStatus(int total, int succeeded, int failed, int skipped) {
    this.total = total;
    this.succeeded = succeeded;
    this.failed = failed;
    this.skipped = skipped;
  }

  public boolean hasFailures() {
    return failed + skipped > 0;
  }

  public int getTotal() {
    return total;
  }

  public int getCompleted() {
    return succeeded + failed;
  }

  public int getInprogress() {
    return total - failed - succeeded - skipped;
  }
}
