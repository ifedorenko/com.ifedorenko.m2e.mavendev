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

import org.eclipse.jdt.junit.launcher.JUnitLaunchShortcut;

public class MavenITLaunchShortcut
    extends JUnitLaunchShortcut
{
    protected String getLaunchConfigurationTypeId()
    {
        return "com.ifedorenko.m2e.mavendev.itLaunchType";
    }
}
