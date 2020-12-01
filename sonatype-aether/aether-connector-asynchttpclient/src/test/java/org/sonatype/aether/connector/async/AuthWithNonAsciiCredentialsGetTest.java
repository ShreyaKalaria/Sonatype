package org.sonatype.aether.connector.async;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import org.junit.Before;
import org.junit.runner.RunWith;
import org.sonatype.aether.repository.Authentication;
import org.sonatype.tests.http.runner.annotations.ConfiguratorList;
import org.sonatype.tests.http.runner.junit.ConfigurationRunner;
import org.sonatype.tests.http.server.api.ServerProvider;

/**
 */
@RunWith( ConfigurationRunner.class )
@ConfiguratorList( "AuthSuiteConfigurator.list" )
public class AuthWithNonAsciiCredentialsGetTest
    extends GetTest
{

    @Override
    public void configureProvider( ServerProvider provider )
    {
        super.configureProvider( provider );
        provider.addUser( "user-non-ascii", "\u00E4\u00DF" );
    }

    @Before
    @Override
    public void before()
        throws Exception
    {
        super.before();

        repository().setAuthentication( new Authentication( "user-non-ascii", "\u00E4\u00DF" ) );
    }

    @Override
    public void testDownloadArtifactWhoseSizeExceedsMaxHeapSize()
        throws Exception
    {
        // this one is slow and doesn't bring anything new to the table in this context so just skip
    }

}
