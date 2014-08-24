/*******************************************************************************
 * Copyright (c) 2014 Igor Fedorenko
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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.ArtifactRef;
import org.eclipse.m2e.core.project.IMavenProjectFacade;

class Verifiers
{
    public static boolean isApacheVerifierProject( ILaunchConfiguration configuration )
    {
        return hasMavenDependency( configuration, "org.apache.maven.shared", "maven-verifier" );
    }

    public static boolean isTakariVerifierProject( ILaunchConfiguration configuration )
    {
        return hasMavenDependency( configuration, "io.takari.maven.plugins", "takari-plugin-testing" );
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

}
