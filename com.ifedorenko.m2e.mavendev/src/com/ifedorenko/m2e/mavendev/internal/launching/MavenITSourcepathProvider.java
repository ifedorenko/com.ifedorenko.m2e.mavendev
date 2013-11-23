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
import org.eclipse.m2e.jdt.IClasspathManager;
import org.eclipse.m2e.jdt.internal.launch.MavenRuntimeClasspathProvider;

@SuppressWarnings( "restriction" )
public class MavenITSourcepathProvider
    extends MavenRuntimeClasspathProvider
{
    public static final String MAVENIT_SOURCEPATH_PROVIDER = "com.ifedorenko.m2e.mavendev.itSourcepathProvider";

    protected int getArtifactScope( ILaunchConfiguration configuration )
        throws CoreException
    {
        return IClasspathManager.CLASSPATH_TEST;
    }

}
