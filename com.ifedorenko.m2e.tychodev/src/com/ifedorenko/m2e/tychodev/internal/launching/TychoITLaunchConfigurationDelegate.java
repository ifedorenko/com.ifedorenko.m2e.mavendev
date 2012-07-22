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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;
import org.eclipse.jdt.internal.junit.launcher.ITestKind;
import org.eclipse.jdt.internal.junit.launcher.JUnitLaunchConfigurationConstants;
import org.eclipse.jdt.internal.junit.launcher.JUnitRuntimeClasspathEntry;
import org.eclipse.jdt.internal.launching.JavaSourceLookupDirector;
import org.eclipse.jdt.junit.launcher.JUnitLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.m2e.core.embedder.MavenRuntime;
import org.eclipse.m2e.core.internal.MavenPluginActivator;
import org.eclipse.m2e.internal.launch.IMavenLaunchParticipant;
import org.eclipse.m2e.internal.launch.MavenLaunchUtils;
import org.eclipse.osgi.internal.baseadaptor.DevClassPathHelper;
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
    private ILaunch launch;

    private IProgressMonitor monitor;

    private static final SourceLookupMavenLaunchParticipant sourcelookup = new SourceLookupMavenLaunchParticipant();

    private static final MavenLaunchParticipant launchParicipant = new MavenLaunchParticipant();

    @Override
    public synchronized void launch( ILaunchConfiguration configuration, String mode, ILaunch launch,
                                     IProgressMonitor monitor )
        throws CoreException
    {
        this.launch = launch;
        this.monitor = monitor;
        try
        {
            setSourceLocator( configuration, launch );

            super.launch( configuration, mode, launch, monitor );
        }
        finally
        {
            this.launch = null;
            this.monitor = null;
        }
    }

    private void setSourceLocator( ILaunchConfiguration configuration, ILaunch launch )
        throws CoreException
    {
        JavaSourceLookupDirector sourceLocator = new JavaSourceLookupDirector();
        sourceLocator.setSourcePathComputer( getLaunchManager().getSourcePathComputer( "org.eclipse.jdt.launching.sourceLookup.javaSourcePathComputer" ) );
        sourceLocator.initializeDefaults( configuration );

        // default java source lookup participant is broken, https://bugs.eclipse.org/bugs/show_bug.cgi?id=368212
        sourceLocator.removeParticipants( sourceLocator.getParticipants() );

        addSourceLookupParticipants( configuration, launch, sourceLocator, sourcelookup );

        launch.setSourceLocator( sourceLocator );
    }

    void addSourceLookupParticipants( ILaunchConfiguration configuration, ILaunch launch,
                                      JavaSourceLookupDirector sourceLocator, IMavenLaunchParticipant participant )
    {
        List<ISourceLookupParticipant> sourceLookupParticipants =
            participant.getSourceLookupParticipants( configuration, launch, monitor );
        if ( sourceLookupParticipants != null )
        {
            sourceLocator.addParticipants( sourceLookupParticipants.toArray( new ISourceLookupParticipant[sourceLookupParticipants.size()] ) );
        }
    }

    @Override
    public String getVMArguments( ILaunchConfiguration configuration )
        throws CoreException
    {
        StringBuilder vmargs = new StringBuilder();
        append( vmargs, super.getVMArguments( configuration ) );

        // m2e workspace dependency resolution
        File state = MavenPluginActivator.getDefault().getMavenProjectManager().getWorkspaceStateFile();
        MavenRuntime runtime = MavenLaunchUtils.getMavenRuntime( configuration );
        append( vmargs, "-Dm2eclipse.workspace.state=" + state.getAbsolutePath() );
        append( vmargs, "-Dtychodev-maven.home=" + runtime.getLocation() );
        append( vmargs, "-Dtychodev-maven.ext.class.path=" + MavenLaunchUtils.getCliResolver( runtime ) );
        append( vmargs, "-Dtychodev-cp=" + getTestClasspath( configuration ) );

        append( vmargs, sourcelookup.getVMArguments( configuration, launch, monitor ) );
        append( vmargs, launchParicipant.getVMArguments( configuration, launch, monitor ) );
        return vmargs.toString();
    }

    void append( StringBuilder result, String str )
    {
        if ( str != null )
        {
            str = str.trim();
        }
        if ( str != null && str.length() > 0 )
        {
            if ( result.length() > 0 )
            {
                result.append( ' ' );
            }
            result.append( str );
        }
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

    void addClasspath( StringBuilder cp, Set<String> set, String location )
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
}
