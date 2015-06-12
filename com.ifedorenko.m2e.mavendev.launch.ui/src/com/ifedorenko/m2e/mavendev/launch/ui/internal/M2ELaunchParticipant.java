package com.ifedorenko.m2e.mavendev.launch.ui.internal;

import java.io.File;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;
import org.eclipse.m2e.internal.launch.IMavenLaunchParticipant;
import org.eclipse.m2e.internal.launch.MavenRuntimeLaunchSupport.VMArguments;

@SuppressWarnings("restriction")
public class M2ELaunchParticipant implements IMavenLaunchParticipant {

  private static final BuildProgressActivator CORE = BuildProgressActivator.getInstance();

  @Override
  public String getProgramArguments(ILaunchConfiguration configuration, ILaunch launch,
      IProgressMonitor monitor) {
    return null;
  }

  @Override
  public String getVMArguments(ILaunchConfiguration configuration, ILaunch launch,
      IProgressMonitor monitor) {
    VMArguments arguments = new VMArguments();
    arguments.appendProperty("maven.ext.class.path", getExtPath());

    // TODO use constants from ExecutionEventListener
    arguments.appendProperty("m2e.buildListener.port", Integer.toString(CORE.getListenerPort()));
    arguments.appendProperty("m2e.buildListener.launchId", CORE.registerLaunch(launch));
    return arguments.toString();
  }

  private String getExtPath() {
    StringBuilder sb = new StringBuilder();
    for (String entry : CORE.getEventspyClasspath()) {
      if (sb.length() > 0) {
        sb.append(File.pathSeparatorChar);
      }
      sb.append(entry);
    }
    return sb.toString();
  }

  @Override
  public List<ISourceLookupParticipant> getSourceLookupParticipants(
      ILaunchConfiguration configuration, ILaunch launch, IProgressMonitor monitor) {
    return null;
  }

}
