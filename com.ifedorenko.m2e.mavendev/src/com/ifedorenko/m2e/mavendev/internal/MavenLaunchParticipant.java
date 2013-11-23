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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.m2e.actions.MavenLaunchConstants;
import org.eclipse.m2e.internal.launch.IMavenLaunchParticipant;
import org.eclipse.pde.core.plugin.IPluginBase;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.project.IBundleClasspathEntry;
import org.eclipse.pde.core.project.IBundleProjectDescription;
import org.eclipse.pde.core.project.IBundleProjectService;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.PluginModelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.tycho.m2e.internal.M2ETychoActivator;

@SuppressWarnings( "restriction" )
public class MavenLaunchParticipant
    implements IMavenLaunchParticipant
{
    /**
     * Key suffix corresponding to project basedir, i.e. parent directory of .project and pom.xml files
     */
    private static final String SUFFIX_BASEDIR = ":basedir";

    /**
     * Key suffix corresponding to bundle location, i.e. parent directory of META-INF/MANIFEST.MF
     */
    private static final String SUFFIX_LOCATION = ":location";

    /**
     * Key suffix corresponding to comma-separated list of additional bundle classpath entries
     */
    private static final String SUFFIX_ENTRIES = ":entries";

    /**
     * Location of m2e.tycho workspace state.
     * <p/>
     * Value must match among tycho-insitu, DevelopmentWorkspaceState and AbstractTychoIntegrationTest.
     */
    private static final String SYSPROP_STATELOCATION = "tychodev.workspace.state";

    private static final String FILE_WORKSPACESTATE = "workspacestate.properties";

    private static final Logger log = LoggerFactory.getLogger( MavenLaunchParticipant.class );

    @Override
    public String getProgramArguments( ILaunchConfiguration configuration, ILaunch launch, IProgressMonitor monitor )
    {
        return null;
    }

    @Override
    public String getVMArguments( ILaunchConfiguration configuration, ILaunch launch, IProgressMonitor monitor )
    {
        try
        {
            if ( !configuration.getAttribute( MavenLaunchConstants.ATTR_WORKSPACE_RESOLUTION, false ) )
            {
                return null;
            }
        }
        catch ( CoreException e )
        {
            log.error( e.getStatus().getMessage(), e.getStatus().getException() );
            return null;
        }

        // TODO ideally we want shared workspace state file and per-launch working area

        IBundleProjectService service = M2ETychoActivator.getDefault().getProjectService();

        Properties properties = new Properties();

        PluginModelManager modelManager = PDECore.getDefault().getModelManager();
        for ( IPluginModelBase plugin : modelManager.getActiveModels() )
        {
            String stringKey = toStringKey( plugin.getPluginBase() );
            properties.put( stringKey + SUFFIX_LOCATION, plugin.getInstallLocation() );
            if ( plugin.getUnderlyingResource() != null )
            {
                IProject project = plugin.getUnderlyingResource().getProject();
                properties.put( stringKey + SUFFIX_BASEDIR, project.getLocation().toOSString() );
                try
                {
                    StringBuilder deventries = new StringBuilder();
                    IBundleProjectDescription description = service.getDescription( project );
                    IBundleClasspathEntry[] cp = description.getBundleClasspath();
                    if ( cp == null )
                    {
                        deventries.append( getJavaDefaultOutputLocation( project ).toOSString() );
                    }
                    else
                    {
                        ArrayList<IPath> entries = new ArrayList<IPath>();
                        for ( IBundleClasspathEntry cpe : cp )
                        {
                            IPath path = cpe.getBinaryPath();
                            if ( path == null )
                            {
                                path = getJavaDefaultOutputLocation( project );
                            }
                            else
                            {
                                path = project.getFolder( path ).getLocation();
                            }
                            if ( !entries.contains( path ) )
                            {
                                entries.add( path );
                            }
                        }
                        for ( int i = 0; i < entries.size(); i++ )
                        {
                            if ( i > 1 )
                            {
                                deventries.append( ',' );
                            }
                            deventries.append( entries.get( i ).toOSString() );
                        }
                    }
                    properties.put( stringKey + SUFFIX_ENTRIES, deventries.toString() );
                }
                catch ( CoreException e )
                {
                    log.error( e.getStatus().getMessage(), e.getStatus().getException() );
                }
            }
        }

        try
        {
            final File stateLocation = File.createTempFile( "m2e-tycho", null );
            if ( !stateLocation.delete() || !stateLocation.mkdirs() )
            {
                throw new IOException( "Could not create temporary folder " + stateLocation.getAbsolutePath() );
            }

            OutputStream os =
                new BufferedOutputStream( new FileOutputStream( new File( stateLocation, FILE_WORKSPACESTATE ) ) );
            try
            {
                properties.store( os, null );
            }
            finally
            {
                IOUtil.close( os );
            }

            cleanupOnTerminate( launch, stateLocation );

            return "-D" + SYSPROP_STATELOCATION + "=" + stateLocation.getAbsolutePath();
        }
        catch ( IOException e )
        {
            log.error( "Could not write m2e-tycho workspace state file", e );
        }

        return null;
    }

    /**
     * Absolute filesystem path of java project default output folder
     */
    private IPath getJavaDefaultOutputLocation( IProject project )
        throws JavaModelException
    {
        IPath path = JavaCore.create( project ).getOutputLocation(); // workspace-related path
        IFolder folder = project.getWorkspace().getRoot().getFolder( path );
        return folder.getLocation();
    }

    private static void cleanupOnTerminate( final ILaunch launch, final File stateLocation )
    {
        LaunchUtils.onTerminate( launch, new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    FileUtils.deleteDirectory( stateLocation );
                }
                catch ( IOException e )
                {
                    log.error( "Could not cleanup m2e-tycho launch state directory", e );
                }
            }
        } );
    }

    @Override
    public List<ISourceLookupParticipant> getSourceLookupParticipants( ILaunchConfiguration configuration,
                                                                       ILaunch launch, IProgressMonitor monitor )
    {
        return null;
    }

    private String toStringKey( IPluginBase plugin )
    {
        StringBuilder sb = new StringBuilder();
        sb.append( "eclipse-plugin" );
        sb.append( ':' ).append( plugin.getId() ).append( ':' ).append( plugin.getVersion() );
        return sb.toString();
    }

}
