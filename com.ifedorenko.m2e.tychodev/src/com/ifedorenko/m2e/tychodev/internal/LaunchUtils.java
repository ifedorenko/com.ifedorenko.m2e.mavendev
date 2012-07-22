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
package com.ifedorenko.m2e.tychodev.internal;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;

public class LaunchUtils
{
    public static void onTerminate( final ILaunch launch, final Runnable runnable )
    {
        DebugPlugin.getDefault().addDebugEventListener( new IDebugEventSetListener()
        {
            private Set<IProcess> processes = new HashSet<IProcess>();

            @Override
            public void handleDebugEvents( DebugEvent[] events )
            {
                for ( DebugEvent event : events )
                {
                    if ( event.getSource() instanceof IProcess )
                    {
                        IProcess process = (IProcess) event.getSource();
                        if ( process.getLaunch() == launch ) // TODO should it be #equals instead?
                        {
                            switch ( event.getKind() )
                            {
                                case DebugEvent.TERMINATE:
                                    processTerminated( process );
                                    break;
                                case DebugEvent.CREATE:
                                    processCreated( process );
                                    break;
                            }
                        }
                    }
                }
            }

            private void processCreated( IProcess process )
            {
                processes.add( process );
            }

            private void processTerminated( IProcess process )
            {
                processes.remove( process );
                if ( processes.isEmpty() )
                {
                    launchTerminated();
                }
            }

            private void launchTerminated()
            {
                DebugPlugin.getDefault().removeDebugEventListener( this );
                runnable.run();
            }
        } );

    }
}
