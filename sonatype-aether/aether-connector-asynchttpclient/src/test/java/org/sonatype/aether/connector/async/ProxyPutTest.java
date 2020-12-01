package org.sonatype.aether.connector.async;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.sonatype.aether.repository.Authentication;
import org.sonatype.aether.repository.Proxy;
import org.sonatype.tests.http.runner.annotations.Configurators;
import org.sonatype.tests.http.runner.junit.ConfigurationRunner;
import org.sonatype.tests.http.server.jetty.configurations.HttpProxyAuthConfigurator;
import org.sonatype.tests.http.server.jetty.configurations.HttpProxyConfigurator;

/**
 * @author Benjamin Hanzelmann
 */
@RunWith( ConfigurationRunner.class )
@Configurators( { HttpProxyConfigurator.class, HttpProxyAuthConfigurator.class } )
public class ProxyPutTest
    extends PutTest
{

    @Override
    @Before
    public void before()
        throws Exception
    {
        super.before();

        Authentication auth = new Authentication( "puser", "password" );
        Proxy proxy = new Proxy( "http", "localhost", provider().getPort(), auth );
        repository().setProxy( proxy );
    }

    @Override
    public String url()
    {
        URL orig;
        try
        {
            orig = new URL( super.url() );
            return new URL( orig.getProtocol(), "proxiedhost", orig.getPort(), "" ).toString();
        }
        catch ( MalformedURLException e )
        {
            e.printStackTrace();
            throw new IllegalStateException( e.getMessage(), e );
        }
    }

}
