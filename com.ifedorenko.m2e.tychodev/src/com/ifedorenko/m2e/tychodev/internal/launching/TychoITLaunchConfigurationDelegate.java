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
package com.ifedorenko.m2e.tychodev.internal.launching;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.internal.runtime.DevClassPathHelper;
import org.eclipse.core.resources.IProject;
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
import org.eclipse.m2e.core.embedder.MavenRuntime;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.internal.launch.MavenLaunchUtils;
import org.eclipse.m2e.internal.launch.MavenRuntimeLaunchSupport;
import org.eclipse.m2e.internal.launch.MavenRuntimeLaunchSupport.VMArguments;
import org.eclipse.osgi.service.datalocation.Location;
import org.osgi.framework.Bundle;

import com.ifedorenko.m2e.sourcelookup.internal.SourceLookupMavenLaunchParticipant;
import com.ifedorenko.m2e.tychodev.internal.MavenLaunchParticipant;

/**
 * Launches Tycho Integration Tests in development mode.
 * <p/>
 * To enable debugging, test jvm will run both integration test code and maven runtime executed from the tests. The
 * latter is achieved by using Maven Verifier no-fork mode, which will execute Maven in a separate classloader.
 * <p/>
 * Maven uses system classloader as parent of maven plugin class realms (see http://jira.codehaus.org/browse/MNG-4747).
 * This requires special test launcher that loads test classes in a separate classloader and allows almost clean system
 * classloader.
 */
@SuppressWarnings( "restriction" )
public class TychoITLaunchConfigurationDelegate
    extends JUnitLaunchConfigurationDelegate
    implements ILaunchConfigurationDelegate
{
    public static final String ATTR_TEST_TARGETPLATFORM = "tychodev-testTargetPlatform";

    private ILaunch launch;

    private IProgressMonitor monitor;

    private MavenRuntimeLaunchSupport launchSupport;

    private static final MavenLaunchParticipant launchParicipant = new MavenLaunchParticipant();

    @Override
    public synchronized void launch( ILaunchConfiguration configuration, String mode, ILaunch launch,
                                     IProgressMonitor monitor )
        throws CoreException
    {
        this.launch = launch;
        this.monitor = monitor;
        this.launchSupport = MavenRuntimeLaunchSupport.create( configuration, launch, monitor );
        try
        {
            launch.setSourceLocator( SourceLookupMavenLaunchParticipant.newSourceLocator( mode ) );

            super.launch( configuration, mode, launch, monitor );
        }
        finally
        {
            this.launch = null;
            this.monitor = null;
        }
    }

    @Override
    public String getVMArguments( ILaunchConfiguration configuration )
        throws CoreException
    {
        VMArguments arguments = launchSupport.getVMArguments();

        // force Verifier to use embedded maven launcher, required by m2e workspace resolution
        arguments.appendProperty( "verifier.forkMode", "embedded" );

        // TODO deprecate in Tycho and eventually remove
        MavenRuntime runtime = MavenLaunchUtils.getMavenRuntime( configuration );
        arguments.appendProperty( "tychodev-maven.home", runtime.getLocation() );
        arguments.appendProperty( "tychodev-maven.ext.class.path", MavenLaunchUtils.getCliResolver( runtime ) );

        // actual test classpath, see RemoteTestRunner
        arguments.appendProperty( "tychodev-cp", getTestClasspath( configuration ) );

        String testTargetPlatform = configuration.getAttribute( ATTR_TEST_TARGETPLATFORM, (String) null );
        if ( testTargetPlatform != null )
        {
            arguments.appendProperty( "tychodev-testTargetPlatform", testTargetPlatform );
        }

        arguments.append( SourceLookupMavenLaunchParticipant.getVMArguments() );
        arguments.append( launchParicipant.getVMArguments( configuration, launch, monitor ) );

        // last, so user can override standard arguments
        arguments.append( super.getVMArguments( configuration ) );

        return arguments.toString();
    }

    @Override
    public String verifyMainTypeName( ILaunchConfiguration configuration )
        throws CoreException
    {
        return "com.ifedorenko.m2e.tychodev.junit.runtime.internal.RemoteTestRunner";
    }

    @Override
    public String[] getClasspath( ILaunchConfiguration configuration )
        throws CoreException
    {
        List<String> cp = getBundleEntries( "com.ifedorenko.m2e.tychodev.junit.runtime", null );
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

    static String getDefaultTestTargetPlatform()
    {
        Location platformLocation = Platform.getInstallLocation();

        if ( platformLocation == null )
        {
            return null;
        }

        URL url = platformLocation.getURL();

        if ( "file".equals( url.getProtocol() ) )
        {
            try
            {
                return new File( url.toURI() ).getAbsolutePath();
            }
            catch ( URISyntaxException e )
            {
                // ignored
            }
        }

        return null;
    }

    @Override
    public IVMRunner getVMRunner( ILaunchConfiguration configuration, String mode )
        throws CoreException
    {
        return launchSupport.decorateVMRunner( super.getVMRunner( configuration, mode ) );
    }

    @Override
    public File getDefaultWorkingDirectory( ILaunchConfiguration configuration )
        throws CoreException
    {
        IJavaProject javaProject = JavaRuntime.getJavaProject( configuration );
        IProject project = javaProject.getProject();
        IMavenProjectFacade projectFacade = MavenPlugin.getMavenProjectRegistry().getProject( project );
        if ( projectFacade != null )
        {
            return project.getFolder( projectFacade.getTestOutputLocation().removeFirstSegments( 1 ) ).getLocation().toFile();
        }
        else
        {
            return super.getWorkingDirectory( configuration );
        }
    }
}
