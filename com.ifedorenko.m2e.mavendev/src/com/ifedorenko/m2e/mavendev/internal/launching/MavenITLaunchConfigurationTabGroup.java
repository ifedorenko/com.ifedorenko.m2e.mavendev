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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;
import org.eclipse.jdt.junit.launcher.JUnitLaunchConfigurationTab;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.m2e.actions.MavenLaunchConstants;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.ResolverConfiguration;
import org.eclipse.m2e.internal.launch.MavenLaunchParticipantInfo;
import org.eclipse.m2e.ui.internal.launch.MavenLaunchExtensionsTab;

@SuppressWarnings( "restriction" )
public class MavenITLaunchConfigurationTabGroup
    extends AbstractLaunchConfigurationTabGroup
{

    @Override
    public void createTabs( ILaunchConfigurationDialog dialog, String mode )
    {
        List<ILaunchConfigurationTab> tabs = new ArrayList<ILaunchConfigurationTab>();

        tabs.add( new JUnitLaunchConfigurationTab() );
        tabs.add( new MavenITLaunchConfigurationTab() );
        tabs.add( new JavaArgumentsTab() );
        tabs.add( new JavaClasspathTab() );
        tabs.add( new JavaJRETab() );
        tabs.add( new SourceLookupTab() );

        List<MavenLaunchParticipantInfo> participants = MavenLaunchParticipantInfo.readParticipantsInfo();
        if ( !participants.isEmpty() )
        {
            tabs.add( new MavenLaunchExtensionsTab( participants ) );
        }

        tabs.add( new EnvironmentTab() );
        tabs.add( new CommonTab() );

        setTabs( tabs.toArray( new ILaunchConfigurationTab[tabs.size()] ) );
    }

    @Override
    public void performApply( ILaunchConfigurationWorkingCopy configuration )
    {
        super.performApply( configuration );

        String projectName;
        try
        {
            projectName =
                configuration.getAttribute( IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String) null );
        }
        catch ( CoreException e )
        {
            return;
        }

        if ( projectName == null || projectName.trim().isEmpty() )
        {
            return;
        }

        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject( projectName );
        if ( project == null || JavaCore.create( project ) == null )
        {
            return;
        }

        IMavenProjectFacade facade = //
            MavenPlugin.getMavenProjectRegistry().create( project, new NullProgressMonitor() ); // XXX yikes

        if ( facade == null )
        {
            return;
        }

        ResolverConfiguration resolverConfiguration = facade.getResolverConfiguration();

        configuration.setAttribute( MavenLaunchConstants.ATTR_WORKSPACE_RESOLUTION,
                                    resolverConfiguration.shouldResolveWorkspaceProjects() );

        configuration.setAttribute( IJavaLaunchConfigurationConstants.ATTR_CLASSPATH_PROVIDER,
                                    MavenITClasspathProvider.MAVENIT_CLASSPATH_PROVIDER );
        configuration.setAttribute( IJavaLaunchConfigurationConstants.ATTR_SOURCE_PATH_PROVIDER,
                                    MavenITSourcepathProvider.MAVENIT_SOURCEPATH_PROVIDER );
    }
}
