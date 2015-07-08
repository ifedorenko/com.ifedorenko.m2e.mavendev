package com.ifedorenko.m2e.mavendev.launch.ui.internal.model;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

public class Launch {
  private final String id;

  private Map<String, Project> projects = Collections.emptyMap();

  public Launch(String launchId) {
    id = launchId;
  }

  public Collection<Project> getProjects() {
    return projects.values();
  }

  public void setProjects(List<Project> projects) {
    Map<String, Project> map = new LinkedHashMap<>();

    for (Project project : projects) {
      map.put(project.getId(), project);
    }

    this.projects = ImmutableMap.copyOf(map);
  }

  public Project getProject(String id) {
    return projects.get(id);
  }

  public BuildStatus getStatus() {
    Collection<Project> projects = this.projects.values();
    int succeded = 0, failed = 0, skipped = 0;
    for (Project project : projects) {
      switch (project.getStatus()) {
        case succeeded:
          succeded++;
          break;
        case failed:
          failed++;
          break;
        case skipped:
          skipped++;
          break;
        default:
          break;
      }
    }
    return new BuildStatus(projects.size(), succeded, failed, skipped);
  }

  public String getId() {
    return id;
  }

  public void terminated() {
    for (Project project : projects.values()) {
      project.terminated();
    }
  }
}
