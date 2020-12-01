package org.sonatype.aether.connector.wagon;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.spi.connector.RepositoryConnector;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.spi.io.FileProcessor;
import org.sonatype.aether.spi.locator.Service;
import org.sonatype.aether.spi.locator.ServiceLocator;
import org.sonatype.aether.spi.log.Logger;
import org.sonatype.aether.spi.log.NullLogger;
import org.sonatype.aether.transfer.NoRepositoryConnectorException;

/**
 * A repository connector factory that uses Maven Wagon for the transfers.
 * 
 * @author Benjamin Bentmann
 */
@Component( role = RepositoryConnectorFactory.class, hint = "wagon" )
public class WagonRepositoryConnectorFactory
    implements RepositoryConnectorFactory, Service
{

    @Requirement
    private Logger logger = NullLogger.INSTANCE;

    @Requirement
    private FileProcessor fileProcessor;

    @Requirement
    private WagonProvider wagonProvider;

    @Requirement
    private WagonConfigurator wagonConfigurator;

    private int priority;

    public WagonRepositoryConnectorFactory()
    {
        // enables default constructor
    }

    public WagonRepositoryConnectorFactory( Logger logger, FileProcessor fileProcessor, WagonProvider wagonProvider,
                                            WagonConfigurator wagonConfigurator )
    {
        setLogger( logger );
        setFileProcessor( fileProcessor );
        setWagonProvider( wagonProvider );
        setWagonConfigurator( wagonConfigurator );
    }

    public void initService( ServiceLocator locator )
    {
        setLogger( locator.getService( Logger.class ) );
        setFileProcessor( locator.getService( FileProcessor.class ) );
        setWagonProvider( locator.getService( WagonProvider.class ) );
        setWagonConfigurator( locator.getService( WagonConfigurator.class ) );
    }

    /**
     * Sets the logger to use for this component.
     * 
     * @param logger The logger to use, may be {@code null} to disable logging.
     * @return This component for chaining, never {@code null}.
     */
    public WagonRepositoryConnectorFactory setLogger( Logger logger )
    {
        this.logger = ( logger != null ) ? logger : NullLogger.INSTANCE;
        return this;
    }

    /**
     * Sets the file processor to use for this component.
     * 
     * @param fileProcessor The file processor to use, must not be {@code null}.
     * @return This component for chaining, never {@code null}.
     */
    public WagonRepositoryConnectorFactory setFileProcessor( FileProcessor fileProcessor )
    {
        if ( fileProcessor == null )
        {
            throw new IllegalArgumentException( "file processor has not been specified" );
        }
        this.fileProcessor = fileProcessor;
        return this;
    }

    /**
     * Sets the wagon provider to use to acquire and release wagon instances.
     * 
     * @param wagonProvider The wagon provider to use, may be {@code null}.
     * @return This factory for chaining, never {@code null}.
     */
    public WagonRepositoryConnectorFactory setWagonProvider( WagonProvider wagonProvider )
    {
        this.wagonProvider = wagonProvider;
        return this;
    }

    /**
     * Sets the wagon configurator to use to apply provider-specific configuration to wagon instances.
     * 
     * @param wagonConfigurator The wagon configurator to use, may be {@code null}.
     * @return This factory for chaining, never {@code null}.
     */
    public WagonRepositoryConnectorFactory setWagonConfigurator( WagonConfigurator wagonConfigurator )
    {
        this.wagonConfigurator = wagonConfigurator;
        return this;
    }

    public int getPriority()
    {
        return priority;
    }

    /**
     * Sets the priority of this component.
     * 
     * @param priority The priority.
     * @return This component for chaining, never {@code null}.
     */
    public WagonRepositoryConnectorFactory setPriority( int priority )
    {
        this.priority = priority;
        return this;
    }

    public RepositoryConnector newInstance( RepositorySystemSession session, RemoteRepository repository )
        throws NoRepositoryConnectorException
    {
        return new WagonRepositoryConnector( wagonProvider, wagonConfigurator, repository, session, fileProcessor,
                                             logger );
    }

}
