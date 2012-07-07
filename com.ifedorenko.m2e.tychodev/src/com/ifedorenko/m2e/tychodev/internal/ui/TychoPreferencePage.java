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
package com.ifedorenko.m2e.tychodev.internal.ui;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.ifedorenko.m2e.tychodev.internal.TychoInsituActivator;

public class TychoPreferencePage
    extends PreferencePage
    implements IWorkbenchPreferencePage
{
    private Button btnEnableTychoIn;

    public TychoPreferencePage()
    {
    }

    @Override
    public Control createContents( Composite parent )
    {
        Composite container = new Composite( parent, SWT.NULL );
        container.setLayout( new GridLayout( 1, false ) );

        btnEnableTychoIn = new Button( container, SWT.CHECK );
        btnEnableTychoIn.setToolTipText( "When enabled, all Maven launch configurations will be instrumented with additional configuration parameters required to enable workspace dependency resolution of Tycho p2 runtime. Only works with Tycho 0.16 or newer." );
        btnEnableTychoIn.setText( "Enable workspace dependency resolution" );
        btnEnableTychoIn.setSelection( TychoInsituActivator.isInstrumentationEnabled() );

        return container;
    }

    public void init( IWorkbench workbench )
    {
        // Initialize the preference page
    }

    @Override
    public boolean performOk()
    {
        boolean changed = TychoInsituActivator.isInstrumentationEnabled() != btnEnableTychoIn.getSelection();

        if ( changed )
        {
            TychoInsituActivator.setInstrumentationEnabled( btnEnableTychoIn.getSelection() );
        }

        return true;
    }

    @Override
    protected void performDefaults()
    {
        btnEnableTychoIn.setSelection( false );
        super.performDefaults();
    }
}
