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

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.m2e.ui.internal.launch.MavenRuntimeSelector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

@SuppressWarnings( "restriction" )
public class MavenITLaunchConfigurationTab
    extends AbstractLaunchConfigurationTab
{

    private MavenRuntimeSelector runtimeSelector;

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
    }

    @Override
    public void performApply( ILaunchConfigurationWorkingCopy configuration )
    {
        runtimeSelector.performApply( configuration );
    }

    @Override
    public String getName()
    {
        return "Maven IT";
    }
}
