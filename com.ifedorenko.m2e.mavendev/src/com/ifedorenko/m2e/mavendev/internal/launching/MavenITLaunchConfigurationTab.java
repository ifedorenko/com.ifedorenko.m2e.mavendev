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
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
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

    /**
     * @wbp.parser.entryPoint
     */
    @Override
    public void createControl( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NONE );
        setControl( composite );
        composite.setLayout( new GridLayout( 1, false ) );

        runtimeSelector = new MavenRuntimeSelector( composite );
        runtimeSelector.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1 ) );
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
        detectMavenCoreITs.setText( "Detect Maven Core Integration Tests" );
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
    }

    @Override
    public void initializeFrom( ILaunchConfiguration configuration )
    {
        runtimeSelector.initializeFrom( configuration );
        try
        {
            detectMavenCoreITs.setSelection( configuration.getAttribute( MavenITLaunchDelegate.ATTR_DETECT_CORE_ITS,
                                                                         true ) );
        }
        catch ( CoreException e )
        {
            // TODO log
        }
    }

    @Override
    public void performApply( ILaunchConfigurationWorkingCopy configuration )
    {
        runtimeSelector.performApply( configuration );
        configuration.setAttribute( MavenITLaunchDelegate.ATTR_DETECT_CORE_ITS, detectMavenCoreITs.getSelection() );
    }

    @Override
    public String getName()
    {
        return "Maven IT";
    }
}
