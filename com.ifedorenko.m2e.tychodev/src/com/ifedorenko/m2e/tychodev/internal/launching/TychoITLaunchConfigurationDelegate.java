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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;
import org.eclipse.jdt.internal.launching.JavaSourceLookupDirector;
import org.eclipse.jdt.junit.launcher.JUnitLaunchConfigurationDelegate;
import org.eclipse.m2e.internal.launch.IMavenLaunchParticipant;

import com.ifedorenko.m2e.sourcelookup.internal.SourceLookupMavenLaunchParticipant;

@SuppressWarnings( "restriction" )
public class TychoITLaunchConfigurationDelegate
    extends JUnitLaunchConfigurationDelegate
    implements ILaunchConfigurationDelegate
{
    private ILaunch launch;

    private IProgressMonitor monitor;

    private static final SourceLookupMavenLaunchParticipant sourcelookup = new SourceLookupMavenLaunchParticipant();

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
        append( vmargs, sourcelookup.getVMArguments( configuration, launch, monitor ) );
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

}
