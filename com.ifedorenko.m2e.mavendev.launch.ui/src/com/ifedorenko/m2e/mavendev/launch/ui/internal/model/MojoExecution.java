package com.ifedorenko.m2e.mavendev.launch.ui.internal.model;

public class MojoExecution {
  private final String id;
  private Status status = Status.inprogress;

  public MojoExecution(String executionId) {
    this.id = executionId;
  }

  public String getId() {
    return id;
  }

  public synchronized Status getStatus() {
    return status;
  }

  public synchronized void setStatus(Status status) {
    this.status = status;
  }

  public synchronized void terminated() {
    if (status == Status.inprogress) {
      status = Status.skipped;
    }
  }
}
