/**
 * Copyright (C) 2008 Sonatype Inc.
 * Sonatype Inc, licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.sonatype.plexus.maven.plugin;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.util.cli.StreamPumper;
import org.sonatype.appbooter.ForkedAppBooter;
import org.sonatype.appbooter.PlexusContainerHost;
import org.sonatype.appbooter.ctl.AppBooterServiceException;
import org.sonatype.appbooter.ctl.ControllerClient;
import org.sonatype.plexus.classworlds.model.ClassworldsRealmConfiguration;

/**
 * Start a Plexus application, and optionally wait for Ctl-C to shut it down. Otherwise, complete the mojo's execution
 * with the application still running (useful for integration testing). The application is started in a separate
 * process, with a control port listening for administrative commands.
 * 
 * @author Jason van Zyl
 * @author John Casey
 * @execute phase="test"
 * @goal run
 * @requiresDependencyResolution test
 */
public class PlexusRunMojo
    implements Mojo, Contextualizable /* , Service */
{
    // ------------------------------------------------------------------------
    // Maven Parameters
    // ------------------------------------------------------------------------

    /**
     * If true, do NOT wait for CTL-C to terminate the application, just start it and return. Future calls to plx:stop
     * or direct use of the {@link ControllerClient} API can manage the application once started.
     * 
     * @parameter default-value="false" expression="${plx.disableBlocking}"
     */
    private boolean disableBlocking;

    /**
     * If <code>disableBlocking</code> is true <code>sleepAfterStart</code> is the number of milliseconds to wait
     * for the application to start up.
     * 
     * @parameter default-value="5000" expression="${plx.sleepAfterStart}"
     */
    private int sleepAfterStart;

    /**
     * Turns on debug mode, which uses the debugJavaCmd to start the plexus application instead of the normal javaCmd.
     * 
     * @parameter default-value="false" expression="${plx.debug}"
     */
    private boolean debug;

    /**
     * Output diagnostic information for the command line and classworlds configuration file generated by this mojo in
     * order to start the application.
     * 
     * @parameter default-value="false" expression="${plx.debugOutput}"
     */
    private boolean debugOutput;

    /**
     * Java command used to start the Plexus application under normal (non-debug) circumstances.
     * 
     * @parameter default-value="java"
     */
    private String javaCmd;

    /**
     * Substitutes the given port into the expression '@DEBUG_PORT@' in your debugJavaCmd.
     * 
     * @parameter default-value="5005" expression="${plx.debugPort}"
     */
    private int debugPort;

    /**
     * Substitutes 'y' or 'n' into the expression '@DEBUG_SUSPEND@' in your debugJavaCmd.
     * 
     * @parameter default-value="true" expression="${plx.debugSuspend}"
     */
    private boolean debugSuspend;

    /**
     * Java command used to start the Plexus application into debugging mode, which is meant to allow attachment of a
     * remote application debugger via JPDA, etc.
     * 
     * @parameter default-value="java -Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=y,suspend=@DEBUG_SUSPEND@,address=@DEBUG_PORT@  -Djava.compiler=NONE"  expression="${plx.debugJavaCmd}"
     */
    private String debugJavaCmd;

    /**
     * The class containing the main method that will be used to start up the Plexus container to initialize the
     * application. <br/> CAUTION! Be sure you understand the ramifications before changing this!
     * 
     * @parameter default-value="org.sonatype.appbooter.PlexusContainerHost"
     */
    private String launcherClass;

    /**
     * System properties passed on to the new java process.
     * 
     * @parameter
     */
    private Map<String, String> systemProperties;

    /** @parameter expression="${project}" */
    private MavenProject project;

    /** @parameter expression="${configuration}" default-value="${basedir}/src/main/plexus/plexus.xml" */
    private File configuration;

    /** @parameter expression="${basedir}" */
    private File basedir;

    /**
     * @parameter default-value="${project.build.directory}"
     */
    private File targetDir;

    /** @parameter expression="${project.build.outputDirectory}" */
    private File classes;

    /** @parameter expression="${project.build.testOutputDirectory}" */
    private File testClasses;

    /** @parameter default-value="false" */
    private boolean includeTestClasspath;

    /**
     * Artifact coordinate containing the platform classes for the application. These should include a plexus container,
     * along with the launcherClass. <br/> Default is
     * org.sonatype.appbooter.plexus-platforms:plexus-platform-base:1.0-SNAPSHOT
     * 
     * @parameter
     */
    private PlatformArtifact platformArtifact = PlatformArtifact.DEFAULT;

    /**
     * List of class paths to prepend to the classworlds configuration.
     * 
     * @parameter
     */
    private List<String> prependClasspaths;

    /**
     * @component
     */
    private ArtifactResolver resolver;

    /**
     * @component
     */
    private ArtifactFactory factory;

    /**
     * @parameter default-value="${localRepository}"
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * @parameter default-value="${project.remoteArtifactRepositories}"
     * @readonly
     */
    private List<ArtifactRepository> remoteRepositories;

    /**
     * Uses DEFAULT_CONTROL_PORT from {@link PlexusContainerHost} by default. <br/> This is the port used to administer
     * the remote application. If you execute with disableBlocking == true, you may need to know this port to use the
     * {@link ControllerClient} API directly (from integration-test JUnit code, for instance).
     * 
     * @parameter expression="${plx.controlPort}" default-value="-1"
     */
    private int controlPort;

    private ControllerClient controlClient;

    private Log log;

    private PlexusContainer container;

    @SuppressWarnings( "unchecked" )
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {

        // it would be WAY better to get this from a container already configured, but its really a pain to push the
        // config values back into it. It makes the complexity of the code grow.... so what I did, is added setters and
        // getters to the component....
        MavenForkedAppBooter mavenForkedAppBooter = new MavenForkedAppBooter();

        // grab the logger..
        try
        {
            LoggerManager lm = (LoggerManager) this.container.lookup( LoggerManager.ROLE );
            Logger logger = lm.getLoggerForComponent( ForkedAppBooter.ROLE );
            mavenForkedAppBooter.enableLogging( logger );
        }
        catch ( ComponentLookupException e )
        {
            throw new MojoExecutionException( "Failed to pass a logger to the Plexus Component: "+ e.getMessage(), e );
        }

        // configure it...
        mavenForkedAppBooter.setClassworldsRealmConfig( this.getClassworldsRealmConfig() );
        mavenForkedAppBooter.setPlatformFile( this.getPlatformFile() );

        mavenForkedAppBooter.setBasedir( this.basedir );
        mavenForkedAppBooter.setConfiguration( this.configuration );
        mavenForkedAppBooter.setControlClient( this.controlClient );
        mavenForkedAppBooter.setControlPort( this.controlPort );
        mavenForkedAppBooter.setDebug( this.debug );
        mavenForkedAppBooter.setDebugJavaCmd( this.debugJavaCmd );
        mavenForkedAppBooter.setDebugPort( this.debugPort );
        mavenForkedAppBooter.setDebugSuspend( this.debugSuspend );
        mavenForkedAppBooter.setJavaCmd( this.javaCmd );
        mavenForkedAppBooter.setLauncherClass( this.launcherClass );
        mavenForkedAppBooter.setSleepAfterStart( this.sleepAfterStart );
        mavenForkedAppBooter.setSystemProperties( this.systemProperties );
        mavenForkedAppBooter.setTempDir( this.targetDir );
        mavenForkedAppBooter.setDisableBlocking( this.disableBlocking );

        // run it!
        try
        {
            mavenForkedAppBooter.start();
        }
        catch ( AppBooterServiceException e )
        {
            throw new MojoFailureException( e.getMessage(), e );
        }

    }

    @SuppressWarnings( "unchecked" )
    private File getPlatformFile()
        throws MojoFailureException, MojoExecutionException
    {
        String platformVersion = platformArtifact.getVersion();
        Map<String, Artifact> managedVersionMap = project.getManagedVersionMap();
        if ( managedVersionMap != null )
        {
            Artifact managed = managedVersionMap.get( platformArtifact.getManagementKey() );
            if ( managed != null )
            {
                platformVersion = managed.getVersion();
            }
        }
        Artifact platform =
            factory.createArtifact( platformArtifact.getGroupId(), platformArtifact.getArtifactId(), platformVersion,
                                    null, platformArtifact.getType() );
        try
        {
            resolver.resolve( platform, remoteRepositories, localRepository );
        }
        catch ( ArtifactResolutionException e )
        {
            throw new MojoExecutionException( "Failed to resolve platform artifact: " + platform.getId(), e );
        }
        catch ( ArtifactNotFoundException e )
        {
            throw new MojoExecutionException( "Cannot find platform artifact: " + platform.getId(), e );
        }

        File platformFile = platform.getFile();
        if ( outputDebugMessages() )
        {
            getLog().info( "Using plexus platform: " + platformArtifact + "\nFile: " + platformFile.getAbsolutePath() );
        }
        return platformFile;
    }

    protected boolean outputDebugMessages()
    {
        return debug || debugOutput || getLog().isDebugEnabled();
    }

    private ClassworldsRealmConfiguration getClassworldsRealmConfig()
    {
        ClassworldsRealmConfiguration rootRealmConfig = new ClassworldsRealmConfiguration( "plexus" );

        if ( prependClasspaths != null && !prependClasspaths.isEmpty() )
        {
            rootRealmConfig.addLoadPatterns( prependClasspaths );
        }

        if ( includeTestClasspath )
        {
            rootRealmConfig.addLoadPattern( testClasses.getAbsolutePath() );
        }

        rootRealmConfig.addLoadPattern( classes.getAbsolutePath() );

        rootRealmConfig.addLoadPatterns( getDependencyPaths() );

        return rootRealmConfig;
    }

    @SuppressWarnings( "unchecked" )
    private LinkedHashSet<String> getDependencyPaths()
    {
        LinkedHashSet<String> paths = new LinkedHashSet<String>();

        if ( includeTestClasspath )
        {
            for ( Artifact artifact : (List<Artifact>) project.getTestArtifacts() )
            {
                paths.add( artifact.getFile().getAbsolutePath() );
            }
        }
        else
        {
            // NOTE: We're including compile, runtime, and provided scopes here
            // since the platform may be assumed to be provided by the distro base,
            // where this might only be executing the app that runs inside that base.
            for ( Artifact artifact : (List<Artifact>) project.getTestArtifacts() )
            {
                if ( Artifact.SCOPE_COMPILE.equals( artifact.getScope() )
                    || Artifact.SCOPE_RUNTIME.equals( artifact.getScope() )
                    || Artifact.SCOPE_PROVIDED.equals( artifact.getScope() ) )
                {
                    paths.add( artifact.getFile().getAbsolutePath() );
                }
            }
        }

        return paths;
    }

    public Log getLog()
    {
        return log;
    }

    public void setLog( Log log )
    {
        this.log = log;
    }

    public void contextualize( Context context )
        throws ContextException
    {
        this.container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );

    }

}
