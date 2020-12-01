package org.sonatype.aether.connector.async;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.tests.http.server.api.ServerProvider;
import org.sonatype.tests.http.server.jetty.behaviour.Redirect;

/**
 * @author Benjamin Hanzelmann
 *
 */
public class RedirectGetTest
    extends GetTest
{

    @Override
    protected RemoteRepository repository()
    {
        return super.repository().setUrl( url( "redirect" ) );
    }

    @Override
    public void configureProvider( ServerProvider provider )
    {
        super.configureProvider( provider );
        provider().addBehaviour( "/redirect/*", new Redirect( "^", "/repo" ) );
    }

}
