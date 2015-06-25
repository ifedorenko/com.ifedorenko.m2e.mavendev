package com.ifedorenko.m2e.mavendev.launch.ui.internal;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.util.FileUtils;

import com.google.common.base.Charsets;

public class BuildLogWriter {

  private final File basedir;

  private final Map<Object, Writer> writers = new ConcurrentHashMap<>();

  public BuildLogWriter(File basedir) {
    this.basedir = basedir;
  }

  public void write(String launchId, String projectId, String message) {
    try {
      Object key = key(launchId, projectId);
      Writer writer = writers.get(key);
      if (writer == null) {
        writer = new BufferedWriter(
            new OutputStreamWriter(newOutputStream(launchId, projectId), Charsets.UTF_8));
        writers.put(key, writer);
      }
      writer.write(message + "\n");
      writer.flush();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private OutputStream newOutputStream(String launchId, String projectId) throws IOException {
    File projectFile = getProjectFile(launchId, projectId);
    projectFile.getParentFile().mkdirs();
    return new FileOutputStream(projectFile);
  }

  public File getProjectFile(String launchId, String projectId) {
    return new File(getLaunchDir(launchId), projectId.replace(':', '_'));
  }

  private File getLaunchDir(String launchId) {
    return new File(basedir, launchId);
  }

  public void projectCompleted(String launchId, String projectId) {
    Object key = key(launchId, projectId);
    Writer writer = writers.remove(key);
    if (writer != null) {
      try {
        writer.close();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  public void removeLaunch(String launchId) {
    try {
      FileUtils.deleteDirectory(getLaunchDir(launchId));
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void stop() {
    for (Writer writer : writers.values()) {
      try {
        writer.close();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    try {
      FileUtils.deleteDirectory(basedir);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private Object key(String launchId, String projectId) {
    return launchId + ":" + projectId;
  }
}
