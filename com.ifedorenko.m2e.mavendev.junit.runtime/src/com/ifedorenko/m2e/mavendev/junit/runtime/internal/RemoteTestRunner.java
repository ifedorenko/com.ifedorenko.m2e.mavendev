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
package com.ifedorenko.m2e.mavendev.junit.runtime.internal;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * See MavenITLaunchConfigurationDelegate
 */
public class RemoteTestRunner
{
    public static void main( String[] args )
        throws Exception
    {
        new RemoteTestRunner().run( args );
    }

    private void run( String[] args )
        throws Exception
    {
        ClassLoader cl = getTestClassLoader();
        Class<?> c = cl.loadClass( "org.eclipse.jdt.internal.junit.runner.RemoteTestRunner" );
        Method m = c.getMethod( "main", String[].class );
        m.invoke( null, (Object) args );
    }

    protected ClassLoader getTestClassLoader()
    {
        String cp = System.getProperty( "tychodev-cp" );
        if ( cp == null )
        {
            throw new IllegalArgumentException();
        }

        List<URL> urls = new ArrayList<URL>();

        StringTokenizer st = new StringTokenizer( cp, File.pathSeparator );
        while ( st.hasMoreTokens() )
        {
            try
            {
                urls.add( new File( st.nextToken() ).getCanonicalFile().toURI().toURL() );
            }
            catch ( IOException e )
            {
                throw new IllegalArgumentException( e );
            }
        }

        return new URLClassLoader( urls.toArray( new URL[urls.size()] ), getClass().getClassLoader() );
    }
}
