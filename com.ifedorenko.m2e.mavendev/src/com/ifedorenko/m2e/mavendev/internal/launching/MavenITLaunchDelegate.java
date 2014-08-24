/*******************************************************************************
 * Copyright (c) 2012 Igor Fedorenko
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Igor Fedorenko - initial API and implementation
 *******************************************************************************/
package com.ifedorenko.m2e.mavendev.internal.launching;

import static org.eclipse.m2e.internal.launch.MavenLaunchUtils.quote;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.internal.runtime.DevClassPathHelper;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.junit.launcher.ITestKind;
import org.eclipse.jdt.internal.junit.launcher.JUnitLaunchConfigurationConstants;
import org.eclipse.jdt.internal.junit.launcher.JUnitRuntimeClasspathEntry;
import org.eclipse.jdt.junit.launcher.JUnitLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.internal.MavenPluginActivator;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.internal.launch.MavenLaunchExtensionsSupport;
import org.eclipse.m2e.internal.launch.MavenLaunchUtils;
import org.eclipse.m2e.internal.launch.MavenRuntimeLaunchSupport;
import org.eclipse.m2e.internal.launch.MavenRuntimeLaunchSupport.VMArguments;
import org.osgi.framework.Bundle;

/**
 * Launches Maven Integration Tests in development mode.
 * <p/>
 * To enable debugging, test jvm will run both integration test code and maven runtime executed from the tests. The
 * latter is achieved by using Maven Verifier no-fork mode, which will execute Maven in a separate classloader.
 * <p/>
 * Maven uses system classloader as parent of maven plugin class realms (see http://jira.codehaus.org/browse/MNG-4747).
 * This requires special test launcher that loads test classes in a separate classloader and allows almost clean system
 * classloader.
 */
@SuppressWarnings( "restriction" )
public class MavenITLaunchDelegate
    extends JUnitLaunchConfigurationDelegate
    implements ILaunchConfigurationDelegate
{
    public static final String ATTR_DETECT_CORE_ITS = "mavendev.detectCoreITs";

    public static final String ATTR_OVERRIDE_MAVEN = "mavendev.overrideMaven";

    private ILaunch launch;

    private IProgressMonitor monitor;

    private MavenRuntimeLaunchSupport launchSupport;

    private MavenLaunchExtensionsSupport extensionsSupport;

    @Override
    public synchronized void launch( ILaunchConfiguration configuration, String mode, ILaunch launch,
                                     IProgressMonitor monitor )
        throws CoreException
    {
        this.launch = launch;
        this.monitor = monitor;
        try
        {
            this.launchSupport = MavenRuntimeLaunchSupport.create( configuration, launch, monitor );
            this.extensionsSupport = MavenLaunchExtensionsSupport.create( configuration, launch );

            extensionsSupport.configureSourceLookup( configuration, launch, monitor );

            super.launch( configuration, mode, launch, monitor );
        }
        finally
        {
            this.launch = null;
            this.monitor = null;
            this.launchSupport = null;
            this.extensionsSupport = null;
        }
    }

    @Override
    public String getVMArguments( ILaunchConfiguration configuration )
        throws CoreException
    {
        final VMArguments arguments;
        if ( configuration.getAttribute( ATTR_OVERRIDE_MAVEN, false ) )
        {
            arguments = launchSupport.getVMArguments();
        }
        else
        {
            arguments = new VMArguments();
            File state = MavenPluginActivator.getDefault().getMavenProjectManager().getWorkspaceStateFile();
            // TODO replace with launchSupport.applyWorkspaceArtifacts(arguments) when require m2e 1.6
            arguments.appendProperty( "m2e.workspace.state", quote( state.getAbsolutePath() ) );
        }

        // force Verifier to use embedded maven launcher, required by m2e workspace resolution
        arguments.appendProperty( "verifier.forkMode", "embedded" );

        // maven bootclasspath, i.e. classworlds jar.
        arguments.appendProperty( "maven.bootclasspath",
                                  quote( MavenLaunchUtils.toPath( launchSupport.getBootClasspath() ) ) );

        // actual test classpath, see RemoteTestRunner
        arguments.appendProperty( "mavendev.testclasspath", getTestClasspath( configuration ) );

        if ( configuration.getAttribute( ATTR_DETECT_CORE_ITS, true ) )
        {
            IJavaProject jProject = JavaRuntime.getJavaProject( configuration );
            IProject project = jProject.getProject();
            IMavenProjectFacade facade = MavenPlugin.getMavenProjectRegistry().getProject( project );
            if ( "org.apache.maven.its".equals( facade.getArtifactKey().getGroupId() )
                && "core-it-suite".equals( facade.getArtifactKey().getArtifactId() ) )
            {
                // TODO need to introduce helpers to do this kind of stuff
                final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
                IFolder output = root.getFolder( facade.getTestOutputLocation() );
                arguments.appendProperty( "maven.it.global-settings.dir", output.getLocation().toOSString() );
            }
        }

        extensionsSupport.appendVMArguments( arguments, configuration, launch, monitor );

        // user configured entries
        arguments.append( super.getVMArguments( configuration ) );

        return arguments.toString();
    }

    @Override
    public String verifyMainTypeName( ILaunchConfiguration configuration )
        throws CoreException
    {
        return "com.ifedorenko.m2e.mavendev.junit.runtime.internal.RemoteTestRunner";
    }

    @Override
    public String[] getClasspath( ILaunchConfiguration configuration )
        throws CoreException
    {
        List<String> cp = getBundleEntries( "com.ifedorenko.m2e.mavendev.junit.runtime", null );
        return cp.toArray( new String[cp.size()] );
    }

    private List<String> getBundleEntries( String bundleId, String bundleRelativePath )
        throws CoreException
    {
        ArrayList<String> cp = new ArrayList<String>();
        if ( bundleRelativePath == null )
        {
            bundleRelativePath = "/";
        }
        Bundle bundle = Platform.getBundle( bundleId );
        cp.add( MavenLaunchUtils.getBundleEntry( bundle, bundleRelativePath ) );
        if ( DevClassPathHelper.inDevelopmentMode() )
        {
            for ( String cpe : DevClassPathHelper.getDevClassPath( bundleId ) )
            {
                cp.add( MavenLaunchUtils.getBundleEntry( bundle, cpe ) );
            }
        }
        return cp;
    }

    public String getTestClasspath( ILaunchConfiguration configuration )
        throws CoreException
    {
        IRuntimeClasspathEntry[] entries = JavaRuntime.computeUnresolvedRuntimeClasspath( configuration );
        entries = JavaRuntime.resolveRuntimeClasspath( entries, configuration );
        StringBuilder cp = new StringBuilder();
        Set<String> set = new HashSet<String>( entries.length );
        for ( IRuntimeClasspathEntry cpe : entries )
        {
            if ( cpe.getClasspathProperty() == IRuntimeClasspathEntry.USER_CLASSES )
            {
                addClasspath( cp, set, cpe.getLocation() );
            }
        }

        ITestKind kind = JUnitLaunchConfigurationConstants.getTestRunnerKind( configuration );
        for ( JUnitRuntimeClasspathEntry cpe : kind.getClasspathEntries() )
        {
            for ( String location : getBundleEntries( cpe.getPluginId(), cpe.getPluginRelativePath() ) )
            {
                addClasspath( cp, set, location );
            }
        }

        return cp.toString();
    }

    private void addClasspath( StringBuilder cp, Set<String> set, String location )
    {
        if ( location != null && set.add( location ) )
        {
            if ( cp.length() > 0 )
            {
                cp.append( File.pathSeparatorChar );
            }
            cp.append( location );
        }
    }

    @Override
    public IVMRunner getVMRunner( ILaunchConfiguration configuration, String mode )
        throws CoreException
    {
        return launchSupport.decorateVMRunner( super.getVMRunner( configuration, mode ) );
    }

}
