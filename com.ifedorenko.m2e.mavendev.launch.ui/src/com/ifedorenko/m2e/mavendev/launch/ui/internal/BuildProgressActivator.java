package com.ifedorenko.m2e.mavendev.launch.ui.internal;

import static org.eclipse.m2e.core.internal.Bundles.findDependencyBundle;
import static org.eclipse.m2e.core.internal.Bundles.getClasspathEntries;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.ifedorenko.m2e.mavendev.launch.ui.internal.model.IBuildProgressListener;
import com.ifedorenko.m2e.mavendev.launch.ui.internal.model.Launch;
import com.ifedorenko.m2e.mavendev.launch.ui.internal.model.MojoExecution;
import com.ifedorenko.m2e.mavendev.launch.ui.internal.model.Project;
import com.ifedorenko.m2e.mavendev.launch.ui.internal.model.Status;

@SuppressWarnings("restriction")
public class BuildProgressActivator extends AbstractUIPlugin {

  public static final String PLUGINID = "com.ifedorenko.m2e.mavendev.launch.ui";

  private static final String KEY_LAUNCHID = "m2e.buildMonitor.launchId";

  private static BuildProgressActivator INSTANCE;

  private LocalResourceManager resourceManager;

  private final Map<String, Launch> launches = new ConcurrentHashMap<>();

  private final List<IBuildProgressListener> listeners = new CopyOnWriteArrayList<>();

  private BuildLogWriter logWriter;

  private final IDebugEventSetListener debugListener = new IDebugEventSetListener() {
    @Override
    public void handleDebugEvents(DebugEvent[] events) {}
  };

  private final ILaunchesListener2 launchesListener = new ILaunchesListener2() {

    @Override
    public void launchesRemoved(ILaunch[] launches) {
      for (ILaunch ilaunch : launches) {
        String launchId = ilaunch.getAttribute(KEY_LAUNCHID);
        if (launchId == null) {
          continue;
        }
        Launch launch = BuildProgressActivator.this.launches.remove(launchId);
        if (launch != null) {
          // TODO notify listeners
        }
        logWriter.removeLaunch(launchId);
      }
    }

    @Override
    public void launchesChanged(ILaunch[] launches) {}

    @Override
    public void launchesAdded(ILaunch[] launches) {}

    @Override
    public void launchesTerminated(ILaunch[] launches) {}
  };

  private final BuildProgressListenerServer buildListener = new BuildProgressListenerServer() {

    @Override
    protected void onMessage(Map<String, Object> data) {
      switch ((String) data.get("messageId")) {
        case "sessionStarted":
          onSessionStarted(data);
          break;
        case "mojoStarted":
          onMojoStarted(data);
          break;
        case "mojoCompleted":
          onMojoCompleted(data);
          break;
        case "projectStarted":
          onProjectStarted(data);
          break;
        case "projectCompleted":
          onProjectCompleted(data);
          break;
        case "logEvent":
          onLogEvent(data);
          break;
      }
    }

  };

  @Override
  public void start(BundleContext context) throws Exception {
    INSTANCE = this;

    DebugPlugin.getDefault().addDebugEventListener(debugListener);
    DebugPlugin.getDefault().getLaunchManager().addLaunchListener(launchesListener);

    logWriter = new BuildLogWriter(getStateLocation().append("logs").toFile());
    buildListener.start();
  }

  protected void onLogEvent(Map<String, Object> data) {
    String launchId = (String) data.get("launchId");
    String projectId = (String) data.get("projectId");
    String message = (String) data.get("message");

    logWriter.write(launchId, projectId, message);
  }

  protected void onProjectCompleted(Map<String, Object> data) {
    String launchId = (String) data.get("launchId");
    String projectId = (String) data.get("projectId");

    Launch launch = launches.get(launchId);
    Project project = launch.getProject(projectId);

    Status status;
    switch ((String) data.get("projectStatus")) {
      case "ProjectSucceeded":
        status = Status.succeeded;
        break;
      case "ProjectFailed":
        status = Status.failed;
        break;
      case "ProjectSkipped":
        status = Status.skipped;
        break;
      default:
        // TODO log
        return;
    }

    project.setStatus(status);

    notifyListeners(project);

    logWriter.projectCompleted(launchId, projectId);
  }

  protected void onProjectStarted(Map<String, Object> data) {
    Launch launch = launches.get((String) data.get("launchId"));
    Project project = launch.getProject((String) data.get("projectId"));

    project.setStatus(Status.inprogress);
    notifyListeners(project);
  }

  protected void onMojoStarted(Map<String, Object> data) {
    Launch launch = launches.get((String) data.get("launchId"));
    Project project = launch.getProject((String) data.get("projectId"));
    String executionId = (String) data.get("executionId");
    project.setExecution(new MojoExecution(executionId));

    notifyListeners(project);
  }

  protected void onMojoCompleted(Map<String, Object> data) {
    String launchId = (String) data.get("launchId");
    String projectId = (String) data.get("projectId");
    String executionId = (String) data.get("executionId");

    Launch launch = launches.get(launchId);
    Project project = launch.getProject(projectId);
    MojoExecution execution = project.getExecution(executionId);

    Status status;
    switch ((String) data.get("executionStatus")) {
      case "MojoSucceeded":
        status = Status.succeeded;
        break;
      case "MojoFailed":
        status = Status.failed;
        break;
      case "MojoSkipped":
        status = Status.skipped;
        break;
      default:
        // TODO log
        return;
    }

    execution.setStatus(status);

    notifyListeners(project);
  }

  @SuppressWarnings("unchecked")
  protected void onSessionStarted(Map<String, Object> data) {
    Launch launch = launches.get((String) data.get("launchId"));
    if (launch == null) {
      return;
    }
    List<Project> projects = new ArrayList<>();
    for (Map<String, Object> projectData : (List<Map<String, Object>>) data.get("projects")) {
      String projectId = (String) projectData.get("projectId");
      projects.add(new Project(projectId));
    }
    launch.setProjects(projects);

    notifyListeners(launch);
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    buildListener.stop();
    logWriter.stop();

    DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(launchesListener);
    DebugPlugin.getDefault().removeDebugEventListener(debugListener);

    if (resourceManager != null) {
      resourceManager.dispose();
    }

    INSTANCE = null;
  }

  public static BuildProgressActivator getInstance() {
    return INSTANCE;
  }

  public int getListenerPort() {
    return buildListener.getPort();
  }

  public String registerLaunch(ILaunch launch) {
    String launchId = UUID.randomUUID().toString();
    launch.setAttribute(KEY_LAUNCHID, launchId);
    launches.put(launchId, new Launch(launchId));
    return launchId;
  }

  public void addListener(IBuildProgressListener listener) {
    this.listeners.add(listener);
  }

  public void removeListener(IBuildProgressListener listener) {
    this.listeners.remove(listener);
  }

  private void notifyListeners(Object source) {
    for (IBuildProgressListener listener : listeners) {
      listener.onUpdate(source);
    }
  }

  public LocalResourceManager getResourceManager() {
    if (resourceManager == null) {
      // no need to synchronize, this can only be called successfully from Display thread
      resourceManager = new LocalResourceManager(JFaceResources.getResources());
    }
    return resourceManager;
  }

  public List<String> getEventspyClasspath() {
    Set<String> entries = new LinkedHashSet<>();
    String[] dependencies =
        new String[] {"com.ifedorenko.m2e.mavendev.launch.ui.eventspy", "com.google.gson"};
    for (String dependency : dependencies) {
      Bundle dependencyBundle = findDependencyBundle(getBundle(), dependency);
      entries.addAll(getClasspathEntries(dependencyBundle));
    }
    return new ArrayList<>(entries);
  }

  public File getLogFile(String launchId, String projectId) {
    return logWriter.getProjectFile(launchId, projectId);
  }

  public Collection<Launch> getLaunches() {
    return launches.values();
  }
}
