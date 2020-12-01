package org.sonatype.aether.transfer;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;

/**
 * Thrown when an artifact was not found in a particular repository.
 * 
 * @author Benjamin Bentmann
 */
public class ArtifactNotFoundException
    extends ArtifactTransferException
{

    public ArtifactNotFoundException( Artifact artifact, RemoteRepository repository )
    {
        super( artifact, repository, "Could not find artifact " + artifact + getString( " in ", repository )
            + getLocalPathInfo( artifact, repository ) );
    }

    private static String getLocalPathInfo( Artifact artifact, RemoteRepository repository )
    {
        String localPath = ( artifact != null ) ? artifact.getProperty( "localPath", null ) : null;
        if ( localPath != null && repository == null )
        {
            return " at specified path " + localPath;
        }
        else
        {
            return "";
        }
    }

    public ArtifactNotFoundException( Artifact artifact, RemoteRepository repository, String message )
    {
        super( artifact, repository, message );
    }

}
