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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.ArtifactRef;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.ui.internal.launch.MavenRuntimeSelector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

@SuppressWarnings( "restriction" )
public class MavenITLaunchConfigurationTab
    extends AbstractLaunchConfigurationTab
{

    private MavenRuntimeSelector runtimeSelector;

    private Button detectMavenCoreITs;

    private Button overrideTestMaven;

    /**
     * @wbp.parser.entryPoint
     */
    @Override
    public void createControl( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NONE );
        setControl( composite );
        composite.setLayout( new GridLayout( 1, false ) );

        overrideTestMaven = new Button( composite, SWT.CHECK );
        overrideTestMaven.addSelectionListener( new SelectionAdapter()
        {
            @Override
            public void widgetSelected( SelectionEvent e )
            {
                runtimeSelector.setEnabled( overrideTestMaven.getSelection() );
            }
        } );
        overrideTestMaven.setText( "Override test Maven runtime" );
        overrideTestMaven.addSelectionListener( new SelectionAdapter()
        {
            @Override
            public void widgetSelected( SelectionEvent e )
            {
                entriesChanged();
            }
        } );

        runtimeSelector = new MavenRuntimeSelector( composite );
        runtimeSelector.setEnabled( false );
        GridData gd_runtimeSelector = new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1 );
        gd_runtimeSelector.horizontalIndent = 20;
        runtimeSelector.setLayoutData( gd_runtimeSelector );
        runtimeSelector.addSelectionChangedListener( new ISelectionChangedListener()
        {
            public void selectionChanged( SelectionChangedEvent event )
            {
                entriesChanged();
            }
        } );

        detectMavenCoreITs = new Button( composite, SWT.CHECK );
        detectMavenCoreITs.setSelection( true );
        detectMavenCoreITs.setToolTipText( "As of version 3.1.2-SNAPSHOT, Maven Core ITs require -Dmaven.it.global-settings.dir system property to point at core-it-suite/target/test-classes. When this checkbox is enabled (the default), the property will be set automatically for org.apache.maven.its:core-it-suite project." );
        detectMavenCoreITs.setText( "Detect Maven core integration tests" );
        detectMavenCoreITs.addSelectionListener( new SelectionAdapter()
        {
            @Override
            public void widgetSelected( SelectionEvent e )
            {
                entriesChanged();
            }
        } );
    }

    void entriesChanged()
    {
        setDirty( true );
        updateLaunchConfigurationDialog();
    }

    @Override
    public void setDefaults( ILaunchConfigurationWorkingCopy configuration )
    {
        if ( hasMavenDependency( configuration, "org.apache.maven.shared", "maven-verifier" ) )
        {
            configuration.setAttribute( MavenITLaunchDelegate.ATTR_OVERRIDE_MAVEN, true );
        }
    }

    private static boolean hasMavenDependency( ILaunchConfiguration configuration, String groupId, String artifactId )
    {
        try
        {
            IJavaProject jproject = JavaRuntime.getJavaProject( configuration );
            if ( jproject == null )
            {
                return false;
            }
            IMavenProjectFacade facade = MavenPlugin.getMavenProjectRegistry().getProject( jproject.getProject() );
            if ( facade == null )
            {
                return false;
            }
            for ( ArtifactRef dependency : facade.getMavenProjectArtifacts() )
            {
                if ( groupId.equals( dependency.getGroupId() ) && artifactId.equals( dependency.getArtifactId() ) )
                {
                    return true;
                }
            }
        }
        catch ( CoreException e )
        {
            // maybe log
        }
        return false;
    }

    @Override
    public void initializeFrom( ILaunchConfiguration configuration )
    {
        runtimeSelector.initializeFrom( configuration );
        detectMavenCoreITs.setSelection( getAttribute( configuration, MavenITLaunchDelegate.ATTR_DETECT_CORE_ITS, true ) );
        overrideTestMaven.setSelection( getAttribute( configuration, MavenITLaunchDelegate.ATTR_OVERRIDE_MAVEN, false ) );
    }

    private static boolean getAttribute( ILaunchConfiguration configuration, String name, boolean defaultValue )
    {
        try
        {
            return configuration.getAttribute( name, defaultValue );
        }
        catch ( CoreException e )
        {
            // TODO log
        }
        return defaultValue;
    }

    @Override
    public void performApply( ILaunchConfigurationWorkingCopy configuration )
    {
        runtimeSelector.performApply( configuration );
        configuration.setAttribute( MavenITLaunchDelegate.ATTR_DETECT_CORE_ITS, detectMavenCoreITs.getSelection() );
        configuration.setAttribute( MavenITLaunchDelegate.ATTR_OVERRIDE_MAVEN, overrideTestMaven.getSelection() );
    }

    @Override
    public String getName()
    {
        return "Maven";
    }
}
