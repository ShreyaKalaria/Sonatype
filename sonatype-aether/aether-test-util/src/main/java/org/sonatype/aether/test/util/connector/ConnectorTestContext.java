package org.sonatype.aether.test.util.connector;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.test.impl.RecordingTransferListener;

public class ConnectorTestContext
{

    private RemoteRepository repository;

    private RepositorySystemSession session;

    public ConnectorTestContext( RemoteRepository repository, RepositorySystemSession session )
    {
        this.repository = repository;
        this.session = session;
    }

    public ConnectorTestContext()
    {
    }

    public RemoteRepository getRepository()
    {
        return repository;
    }

    public RepositorySystemSession getSession()
    {
        return session;
    }

    public void setRepository( RemoteRepository repository )
    {
        this.repository = repository;
    }

    public void setSession( RepositorySystemSession session )
    {
        this.session = session;
    }

    public RecordingTransferListener getRecordingTransferListener()
    {
        if ( session.getTransferListener() instanceof RecordingTransferListener )
        {
            return (RecordingTransferListener) session.getTransferListener();
        }
        else
        {
            return new RecordingTransferListener( session.getTransferListener() );
        }
    }

}