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
package com.ifedorenko.m2e.mavendev.internal;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class MavenDevToolsActivator
    extends Plugin
{
    private static MavenDevToolsActivator plugin;

    public static final String PLUGIN_ID = "com.ifedorenko.m2e.mavendev";

    @Override
    public void start( BundleContext context )
        throws Exception
    {
        super.start( context );
        plugin = this;
    }

    @Override
    public void stop( BundleContext context )
        throws Exception
    {
        plugin = null;
        super.stop( context );
    }

    public static MavenDevToolsActivator getDefault()
    {
        return plugin;
    }
}
