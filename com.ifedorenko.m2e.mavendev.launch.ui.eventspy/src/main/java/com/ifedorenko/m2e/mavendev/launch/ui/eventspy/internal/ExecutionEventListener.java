package com.ifedorenko.m2e.mavendev.launch.ui.eventspy.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.eventspy.EventSpy;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.ExecutionEvent.Type;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;

import com.google.gson.Gson;

@Named
@Singleton
public class ExecutionEventListener implements EventSpy {

  public static final String KEY_PORT = "m2e.buildListener.port";
  public static final String KEY_LAUNCHID = "m2e.buildListener.launchId";

  private static final Gson GSON = new Gson();
  private static final Charset UTF_8 = Charset.forName("UTF-8");

  private String launchId;

  private Socket socket;
  private OutputStream output;

  @Override
  public void init(Context context) {
    launchId = System.getProperty(KEY_LAUNCHID);
    if (launchId == null) {
      return;
    }

    String portStr = System.getProperty(KEY_PORT);
    if (portStr != null) {
      try {
        InetAddress loopback = InetAddress.getLoopbackAddress();
        socket = new Socket(loopback, Integer.parseInt(portStr));
        socket.setTcpNoDelay(true);
        output = socket.getOutputStream();
      } catch (NumberFormatException | IOException e) {
        // TODO log
      }
    }

    if (socket == null || output == null) {
      launchId = null;
      socket = null; // TODO close
      output = null; // redundant
    }
  }

  @Override
  public void onEvent(Object event) {
    if (launchId == null) {
      return;
    }

    if (event instanceof ExecutionEvent) {
      try {
        ExecutionEvent executionEvent = (ExecutionEvent) event;
        switch (executionEvent.getType()) {
          case SessionStarted:
            reportSessionStart(executionEvent.getSession());
            break;
          case SessionEnded:
            reportSessionEnd(executionEvent.getSession());
            break;
          case MojoStarted:
            reportMojoStarted(executionEvent.getSession(), executionEvent.getMojoExecution());
            break;
          case ProjectStarted:
            reportProjectStarted(executionEvent.getProject(), executionEvent.getType());
            break;
          case ProjectSucceeded:
          case ProjectFailed:
          case ProjectSkipped:
            reportProjectCompleted(executionEvent.getProject(), executionEvent.getType());
            break;
          default:
            // do nothing
            break;
        }
      } catch (IOException e) {
        // TODO
        e.printStackTrace();
      }
    }
  }

  private void reportProjectCompleted(MavenProject project, Type type) throws IOException {
    Map<String, Object> data = new HashMap<>();
    data.put("launchId", launchId);
    data.put("messageId", "projectCompleted");
    data.put("projectId", projectGA(project));
    data.put("projectStatus", type.name());

    sendMessage(data);
  }

  private void reportProjectStarted(MavenProject project, Type type) throws IOException {
    Map<String, Object> data = new HashMap<>();
    data.put("launchId", launchId);
    data.put("messageId", "projectStarted");
    data.put("projectId", projectGA(project));

    sendMessage(data);
  }

  private void reportMojoStarted(MavenSession session, MojoExecution execution) throws IOException {

    MavenProject project = session.getCurrentProject();

    Map<String, Object> data = new HashMap<>();
    data.put("launchId", launchId);
    data.put("messageId", "mojoStarted");
    data.put("projectId", projectGA(project));
    data.put("executionId", execution.getExecutionId());

    sendMessage(data);
  }

  private void reportSessionStart(MavenSession session) throws IOException {
    List<Map<String, String>> projects = new ArrayList<>();
    for (MavenProject project : session.getProjects()) {
      Map<String, String> data = new HashMap<>();
      data.put("projectId", projectGA(project));

      projects.add(data);
    }

    Map<String, Object> data = new HashMap<>();
    data.put("launchId", launchId);
    data.put("messageId", "sessionStarted");
    data.put("projects", projects);

    sendMessage(data);
  }

  protected String projectGA(MavenProject project) {
    return project.getGroupId() + ":" + project.getArtifactId();
  }

  private void reportSessionEnd(MavenSession session) throws IOException {
    Map<String, Object> data = new HashMap<>();
    data.put("launchId", launchId);
    data.put("messageId", "sessionEnded");

    sendMessage(data);
  }

  protected void sendMessage(Map<String, Object> data) throws IOException {
    String message = GSON.toJson(data);
    message += "\n"; // apparently required to flush data to the socket
    output.write(message.getBytes(UTF_8));
    output.flush();
  }

  @Override
  public void close() {
    try {
      output.close();
    } catch (IOException e) {}
    output = null;
    try {
      socket.close();
    } catch (IOException e) {}
    socket = null;
    launchId = null;
  }

}
