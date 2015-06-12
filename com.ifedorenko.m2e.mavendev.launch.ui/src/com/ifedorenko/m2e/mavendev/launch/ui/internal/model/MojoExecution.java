package com.ifedorenko.m2e.mavendev.launch.ui.internal.model;

public class MojoExecution {
  private final String id;

  public MojoExecution(String executionId) {
    this.id = executionId;
  }

  public String getId() {
    return id;
  }
}
