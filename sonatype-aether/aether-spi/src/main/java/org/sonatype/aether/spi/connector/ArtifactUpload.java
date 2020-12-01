package org.sonatype.aether.spi.connector;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.io.File;

import org.sonatype.aether.RequestTrace;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.transfer.ArtifactTransferException;

/**
 * An upload of an artifact to a remote repository. A repository connector processing this upload has to use
 * {@link #setState(State)} and {@link #setException(ArtifactTransferException)} to report the results of the transfer.
 * 
 * @author Benjamin Bentmann
 */
public class ArtifactUpload
    extends ArtifactTransfer
{

    /**
     * Creates a new uninitialized upload.
     */
    public ArtifactUpload()
    {
        // enables default constructor
    }

    /**
     * Creates a new upload with the specified properties.
     * 
     * @param artifact The artifact to upload, may be {@code null}.
     * @param file The local file to upload the artifact from, may be {@code null}.
     */
    public ArtifactUpload( Artifact artifact, File file )
    {
        setArtifact( artifact );
        setFile( file );
    }

    @Override
    public ArtifactUpload setArtifact( Artifact artifact )
    {
        super.setArtifact( artifact );
        return this;
    }

    @Override
    public ArtifactUpload setFile( File file )
    {
        super.setFile( file );
        return this;
    }

    @Override
    public ArtifactUpload setException( ArtifactTransferException exception )
    {
        super.setException( exception );
        return this;
    }

    @Override
    public ArtifactUpload setTrace( RequestTrace trace )
    {
        super.setTrace( trace );
        return this;
    }

    @Override
    public String toString()
    {
        return getState() + " " + getArtifact() + " - " + getFile();
    }

}
