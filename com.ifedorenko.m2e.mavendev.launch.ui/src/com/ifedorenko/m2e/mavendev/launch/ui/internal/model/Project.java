package com.ifedorenko.m2e.mavendev.launch.ui.internal.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class Project {

  private final String id;

  private Map<String, MojoExecution> executions = new LinkedHashMap<>();

  private Status status = Status.waiting;

  public Project(String projectId) {
    id = projectId;
  }

  public String getId() {
    return id;
  }

  public Collection<MojoExecution> getExecutions() {
    synchronized (executions) {
      return new ArrayList<>(executions.values());
    }
  }

  public void setExecution(MojoExecution execution) {
    synchronized (executions) {
      executions.put(execution.getId(), execution);
    }
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }
}
