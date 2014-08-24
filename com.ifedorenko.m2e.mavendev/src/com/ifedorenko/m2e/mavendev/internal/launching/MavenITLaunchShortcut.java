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

import static org.eclipse.m2e.actions.MavenLaunchConstants.ATTR_WORKSPACE_RESOLUTION;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.junit.launcher.JUnitLaunchShortcut;

@SuppressWarnings( "restriction" )
public class MavenITLaunchShortcut
    extends JUnitLaunchShortcut
{
    protected String getLaunchConfigurationTypeId()
    {
        return "com.ifedorenko.m2e.mavendev.itLaunchType";
    }

    @Override
    protected ILaunchConfigurationWorkingCopy createLaunchConfiguration( IJavaElement element )
        throws CoreException
    {
        ILaunchConfigurationWorkingCopy configuration = super.createLaunchConfiguration( element );
        configuration.setAttribute( ATTR_WORKSPACE_RESOLUTION, true );
        return configuration;
    }
}
