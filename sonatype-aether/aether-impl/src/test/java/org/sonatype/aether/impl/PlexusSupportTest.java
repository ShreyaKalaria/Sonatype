package org.sonatype.aether.impl;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.util.List;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.impl.ArtifactDescriptorReader;
import org.sonatype.aether.impl.VersionRangeResolver;
import org.sonatype.aether.impl.VersionResolver;
import org.sonatype.aether.spi.localrepo.LocalRepositoryManagerFactory;

/**
 */
public class PlexusSupportTest
    extends PlexusTestCase
{

    public void testExistenceOfPlexusComponentMetadata()
        throws Exception
    {
        getContainer().addComponent( new StubVersionRangeResolver(), VersionRangeResolver.class, null );
        getContainer().addComponent( new StubVersionResolver(), VersionResolver.class, null );
        getContainer().addComponent( new StubArtifactDescriptorReader(), ArtifactDescriptorReader.class, null );

        RepositorySystem repoSystem = lookup( RepositorySystem.class );
        assertNotNull( repoSystem );
        assertSame( repoSystem, lookup( RepositorySystem.class ) );

        List<LocalRepositoryManagerFactory> lrmfs = getContainer().lookupList( LocalRepositoryManagerFactory.class );
        assertNotNull( lrmfs );
        assertEquals( 2, lrmfs.size() );
    }

}
