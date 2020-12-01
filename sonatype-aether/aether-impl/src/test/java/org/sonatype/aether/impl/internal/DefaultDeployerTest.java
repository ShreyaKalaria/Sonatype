package org.sonatype.aether.impl.internal;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import static org.junit.Assert.*;
import static org.sonatype.aether.test.impl.RecordingRepositoryListener.Type.*;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.aether.RepositoryEvent;
import org.sonatype.aether.RepositoryException;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.deployment.DeployRequest;
import org.sonatype.aether.deployment.DeploymentException;
import org.sonatype.aether.impl.UpdateCheckManager;
import org.sonatype.aether.metadata.MergeableMetadata;
import org.sonatype.aether.metadata.Metadata;
import org.sonatype.aether.metadata.Metadata.Nature;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.spi.connector.ArtifactDownload;
import org.sonatype.aether.spi.connector.ArtifactUpload;
import org.sonatype.aether.spi.connector.MetadataDownload;
import org.sonatype.aether.spi.connector.MetadataUpload;
import org.sonatype.aether.spi.connector.RepositoryConnector;
import org.sonatype.aether.spi.connector.Transfer.State;
import org.sonatype.aether.test.impl.RecordingRepositoryListener;
import org.sonatype.aether.test.impl.RecordingRepositoryListener.EventWrapper;
import org.sonatype.aether.test.impl.TestFileProcessor;
import org.sonatype.aether.test.impl.TestRepositorySystemSession;
import org.sonatype.aether.test.util.TestFileUtils;
import org.sonatype.aether.transfer.ArtifactTransferException;
import org.sonatype.aether.transfer.MetadataNotFoundException;
import org.sonatype.aether.transfer.MetadataTransferException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.metadata.DefaultMetadata;

public class DefaultDeployerTest
{

    private Artifact artifact;

    private DefaultMetadata metadata;

    private TestRepositorySystemSession session;

    private StubRemoteRepositoryManager manager;

    private DefaultDeployer deployer;

    private DeployRequest request;

    private RecordingRepositoryConnector connector;

    private RecordingRepositoryListener listener;

    @Before
    public void setup()
        throws IOException
    {
        artifact = new DefaultArtifact( "gid", "aid", "jar", "ver" );
        artifact = artifact.setFile( TestFileUtils.createTempFile( "artifact" ) );
        metadata =
            new DefaultMetadata( "gid", "aid", "ver", "type", Nature.RELEASE_OR_SNAPSHOT,
                                 TestFileUtils.createTempFile( "metadata" ) );

        session = new TestRepositorySystemSession();
        manager = new StubRemoteRepositoryManager();

        deployer = new DefaultDeployer();
        deployer.setRemoteRepositoryManager( manager );
        deployer.setRepositoryEventDispatcher( new StubRepositoryEventDispatcher() );
        UpdateCheckManager updateCheckManager = new StaticUpdateCheckManager( true );
        deployer.setUpdateCheckManager( updateCheckManager );
        deployer.setFileProcessor( TestFileProcessor.INSTANCE );
        deployer.setSyncContextFactory( new StubSyncContextFactory() );

        request = new DeployRequest();
        request.setRepository( new RemoteRepository( "id", "default", "file:///" ) );
        connector = new RecordingRepositoryConnector();
        manager.setConnector( connector );

        listener = new RecordingRepositoryListener();
        session.setRepositoryListener( listener );
    }

    @After
    public void teardown()
        throws Exception
    {
        if ( session.getLocalRepository() != null )
        {
            TestFileUtils.delete( session.getLocalRepository().getBasedir() );
        }
        session = null;
        listener = null;
        connector = null;
        manager = null;
        deployer = null;
    }

    @Test
    public void testSuccessfulDeploy()
        throws DeploymentException
    {

        connector.setExpectPut( artifact );
        connector.setExpectPut( metadata );

        request.addArtifact( artifact );
        request.addMetadata( metadata );

        deployer.deploy( session, request );

        connector.assertSeenExpected();
    }

    @Test( expected = DeploymentException.class )
    public void testNullArtifactFile()
        throws DeploymentException
    {
        request.addArtifact( artifact.setFile( null ) );
        deployer.deploy( session, request );
    }

    @Test( expected = DeploymentException.class )
    public void testNullMetadataFile()
        throws DeploymentException
    {
        request.addArtifact( artifact.setFile( null ) );
        deployer.deploy( session, request );
    }

    @Test
    public void testSuccessfulArtifactEvents()
        throws DeploymentException
    {
        request.addArtifact( artifact );

        deployer.deploy( session, request );

        List<EventWrapper> events = listener.getEvents();
        assertEquals( 2, events.size() );

        EventWrapper wrapper = events.get( 0 );
        assertEquals( ARTIFACT_DEPLOYING, wrapper.getType() );

        RepositoryEvent event = wrapper.getEvent();
        assertEquals( artifact, event.getArtifact() );
        assertNull( event.getException() );

        wrapper = events.get( 1 );
        assertEquals( ARTIFACT_DEPLOYED, wrapper.getType() );

        event = wrapper.getEvent();
        assertEquals( artifact, event.getArtifact() );
        assertNull( event.getException() );
    }

    @Test
    public void testFailingArtifactEvents()
    {
        connector = new RecordingRepositoryConnector()
        {

            @Override
            public void get( Collection<? extends ArtifactDownload> artifactDownloads,
                             Collection<? extends MetadataDownload> metadataDownloads )
            {
                metadataDownloads =
                    metadataDownloads == null ? Collections.<MetadataDownload> emptyList() : metadataDownloads;
                artifactDownloads =
                    artifactDownloads == null ? Collections.<ArtifactDownload> emptyList() : artifactDownloads;
                for ( MetadataDownload d : metadataDownloads )
                {
                    d.setState( State.ACTIVE );
                    d.setException( new MetadataTransferException( d.getMetadata(), null, "failed" ) );
                    d.setState( State.DONE );
                }
                for ( ArtifactDownload d : artifactDownloads )
                {
                    d.setState( State.ACTIVE );
                    d.setException( new ArtifactTransferException( d.getArtifact(), null, "failed" ) );
                    d.setState( State.DONE );
                }
            }

            @Override
            public void put( Collection<? extends ArtifactUpload> artifactUploads,
                             Collection<? extends MetadataUpload> metadataUploads )
            {
                metadataUploads = metadataUploads == null ? Collections.<MetadataUpload> emptyList() : metadataUploads;
                artifactUploads = artifactUploads == null ? Collections.<ArtifactUpload> emptyList() : artifactUploads;
                for ( MetadataUpload d : metadataUploads )
                {
                    d.setState( State.ACTIVE );
                    d.setException( new MetadataTransferException( d.getMetadata(), null, "failed" ) );
                    d.setState( State.DONE );
                }
                for ( ArtifactUpload d : artifactUploads )
                {
                    d.setState( State.ACTIVE );
                    d.setException( new ArtifactTransferException( d.getArtifact(), null, "failed" ) );
                    d.setState( State.DONE );
                }
            }

        };

        manager.setConnector( connector );

        request.addArtifact( artifact );

        try
        {
            deployer.deploy( session, request );
            fail( "expected exception" );
        }
        catch ( DeploymentException e )
        {
            List<EventWrapper> events = listener.getEvents();
            assertEquals( 2, events.size() );

            EventWrapper wrapper = events.get( 0 );
            assertEquals( ARTIFACT_DEPLOYING, wrapper.getType() );

            RepositoryEvent event = wrapper.getEvent();
            assertEquals( artifact, event.getArtifact() );
            assertNull( event.getException() );

            wrapper = events.get( 1 );
            assertEquals( ARTIFACT_DEPLOYED, wrapper.getType() );

            event = wrapper.getEvent();
            assertEquals( artifact, event.getArtifact() );
            assertNotNull( event.getException() );
        }
    }

    @Test
    public void testSuccessfulMetadataEvents()
        throws DeploymentException
    {
        request.addMetadata( metadata );

        deployer.deploy( session, request );

        List<EventWrapper> events = listener.getEvents();
        assertEquals( 2, events.size() );

        EventWrapper wrapper = events.get( 0 );
        assertEquals( METADATA_DEPLOYING, wrapper.getType() );

        RepositoryEvent event = wrapper.getEvent();
        assertEquals( metadata, event.getMetadata() );
        assertNull( event.getException() );

        wrapper = events.get( 1 );
        assertEquals( METADATA_DEPLOYED, wrapper.getType() );

        event = wrapper.getEvent();
        assertEquals( metadata, event.getMetadata() );
        assertNull( event.getException() );
    }

    @Test
    public void testFailingMetdataEvents()
    {
        connector = new RecordingRepositoryConnector()
        {

            @Override
            public void get( Collection<? extends ArtifactDownload> artifactDownloads,
                             Collection<? extends MetadataDownload> metadataDownloads )
            {
                metadataDownloads =
                    metadataDownloads == null ? Collections.<MetadataDownload> emptyList() : metadataDownloads;
                artifactDownloads =
                    artifactDownloads == null ? Collections.<ArtifactDownload> emptyList() : artifactDownloads;
                for ( MetadataDownload d : metadataDownloads )
                {
                    d.setState( State.ACTIVE );
                    d.setException( new MetadataTransferException( d.getMetadata(), null, "failed" ) );
                    d.setState( State.DONE );
                }
                for ( ArtifactDownload d : artifactDownloads )
                {
                    d.setState( State.ACTIVE );
                    d.setException( new ArtifactTransferException( d.getArtifact(), null, "failed" ) );
                    d.setState( State.DONE );
                }
            }

            @Override
            public void put( Collection<? extends ArtifactUpload> artifactUploads,
                             Collection<? extends MetadataUpload> metadataUploads )
            {
                metadataUploads = metadataUploads == null ? Collections.<MetadataUpload> emptyList() : metadataUploads;
                artifactUploads = artifactUploads == null ? Collections.<ArtifactUpload> emptyList() : artifactUploads;
                for ( MetadataUpload d : metadataUploads )
                {
                    d.setState( State.ACTIVE );
                    d.setException( new MetadataTransferException( d.getMetadata(), null, "failed" ) );
                    d.setState( State.DONE );
                }
                for ( ArtifactUpload d : artifactUploads )
                {
                    d.setState( State.ACTIVE );
                    d.setException( new ArtifactTransferException( d.getArtifact(), null, "failed" ) );
                    d.setState( State.DONE );
                }
            }

        };

        manager.setConnector( connector );

        request.addMetadata( metadata );

        try
        {
            deployer.deploy( session, request );
            fail( "expected exception" );
        }
        catch ( DeploymentException e )
        {
            List<EventWrapper> events = listener.getEvents();
            assertEquals( 2, events.size() );

            EventWrapper wrapper = events.get( 0 );
            assertEquals( METADATA_DEPLOYING, wrapper.getType() );

            RepositoryEvent event = wrapper.getEvent();
            assertEquals( metadata, event.getMetadata() );
            assertNull( event.getException() );

            wrapper = events.get( 1 );
            assertEquals( METADATA_DEPLOYED, wrapper.getType() );

            event = wrapper.getEvent();
            assertEquals( metadata, event.getMetadata() );
            assertNotNull( event.getException() );
        }
    }

    @Test
    public void testStaleLocalMetadataCopyGetsDeletedBeforeMergeWhenMetadataIsNotCurrentlyPresentInRemoteRepo()
        throws Exception
    {
        MergeableMetadata metadata = new MergeableMetadata()
        {

            public Metadata setFile( File file )
            {
                return this;
            }

            public String getVersion()
            {
                return "";
            }

            public String getType()
            {
                return "test.properties";
            }

            public Nature getNature()
            {
                return Nature.RELEASE;
            }

            public String getGroupId()
            {
                return "org";
            }

            public File getFile()
            {
                return null;
            }

            public String getArtifactId()
            {
                return "aether";
            }

            public void merge( File current, File result )
                throws RepositoryException
            {
                Properties props = new Properties();

                try
                {
                    if ( current.isFile() )
                    {
                        TestFileUtils.read( props, current );
                    }

                    props.setProperty( "new", "value" );

                    TestFileUtils.write( props, result );
                }
                catch ( IOException e )
                {
                    throw new RepositoryException( e.getMessage(), e );
                }
            }

            public boolean isMerged()
            {
                return false;
            }
        };

        manager.setConnector( new RepositoryConnector()
        {

            public void put( Collection<? extends ArtifactUpload> artifactUploads,
                             Collection<? extends MetadataUpload> metadataUploads )
            {
            }

            public void get( Collection<? extends ArtifactDownload> artifactDownloads,
                             Collection<? extends MetadataDownload> metadataDownloads )
            {
                if ( metadataDownloads != null )
                {
                    for ( MetadataDownload download : metadataDownloads )
                    {
                        download.setException( new MetadataNotFoundException( download.getMetadata(), null, null ) );
                    }
                }
            }

            public void close()
            {
            }
        } );

        request.addMetadata( metadata );

        File metadataFile =
            new File( session.getLocalRepository().getBasedir(),
                      session.getLocalRepositoryManager().getPathForRemoteMetadata( metadata, request.getRepository(),
                                                                                    "" ) );
        Properties props = new Properties();
        props.setProperty( "old", "value" );
        TestFileUtils.write( props, metadataFile );

        deployer.deploy( session, request );

        props = new Properties();
        TestFileUtils.read( props, metadataFile );
        assertNull( props.toString(), props.get( "old" ) );
    }

}
