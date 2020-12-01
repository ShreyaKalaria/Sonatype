package demo.plexus;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import org.codehaus.plexus.DefaultPlexusContainer;
import org.sonatype.aether.RepositorySystem;

/**
 * A factory for repository system instances that employs Plexus to wire up the system's components.
 */
public class PlexusRepositorySystemFactory
{

    public static RepositorySystem newRepositorySystem()
    {
        /*
         * Aether's components are equipped with plexus-specific metadata to enable discovery and wiring of components
         * by a Plexus container so this is as easy as looking up the implementation.
         */
        try
        {
            return new DefaultPlexusContainer().lookup( RepositorySystem.class );
        }
        catch ( Exception e )
        {
            throw new IllegalStateException( "dependency injection failed", e );
        }
    }

}
