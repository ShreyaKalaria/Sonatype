package org.sonatype.aether.connector.async;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.spi.connector.ArtifactDownload;
import org.sonatype.aether.spi.connector.ArtifactUpload;
import org.sonatype.aether.test.impl.SysoutLogger;
import org.sonatype.aether.test.impl.TestFileProcessor;
import org.sonatype.aether.test.util.TestFileUtils;
import org.sonatype.aether.test.util.impl.StubArtifact;
import org.sonatype.aether.transfer.ArtifactNotFoundException;
import org.sonatype.aether.util.DefaultRepositorySystemSession;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AsyncHandlerExceptionTest
{

    private File baseDir;

    @Before
    public void setUp()
        throws IOException
    {
        baseDir = TestFileUtils.createTempDir( getClass().getSimpleName() );
    }

    @Test
    public void testIt()
        throws Exception
    {
        HttpServer server = new HttpServer();
        server.addResources( "/", baseDir.getAbsolutePath() );
        server.start();

        try
        {
            RemoteRepository repo = new RemoteRepository( "id", "default", server.getHttpUrl() + "/repo" );
            RepositorySystemSession session = new DefaultRepositorySystemSession();

            AsyncRepositoryConnector connector =
                new AsyncRepositoryConnector( repo, session, new TestFileProcessor(), new SysoutLogger() );

            try
            {
                StubArtifact artifact = new StubArtifact( "gid:aid:1.0" );
                for ( int i = 0; i < 16; i++ )
                {
                    System.out.println( "RUN #" + i );
                    TestFileUtils.delete( baseDir );

                    ArtifactDownload download =
                        new ArtifactDownload( artifact, "project", new File( baseDir, "a.jar" ), "ignore" );
                    System.out.println( "GET" );
                    connector.get( Arrays.asList( download ), null );
                    assertTrue( String.valueOf( download.getException() ),
                                download.getException() instanceof ArtifactNotFoundException );

                    ArtifactUpload upload = new ArtifactUpload( artifact, new File( "pom.xml" ) );
                    System.out.println( "PUT" );
                    connector.put( Arrays.asList( upload ), null );
                    if ( upload.getException() != null )
                    {
                        upload.getException().printStackTrace();
                    }
                    assertNull( String.valueOf( upload.getException() ), upload.getException() );
                }
            }
            finally
            {
                connector.close();
            }
        }
        finally
        {
            server.stop();
        }
    }

    @After
    public void tearDown()
        throws IOException
    {
        TestFileUtils.delete( baseDir );
    }

}
