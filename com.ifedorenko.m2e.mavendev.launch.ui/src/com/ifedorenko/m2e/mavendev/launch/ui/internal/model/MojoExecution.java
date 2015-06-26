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

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

}
