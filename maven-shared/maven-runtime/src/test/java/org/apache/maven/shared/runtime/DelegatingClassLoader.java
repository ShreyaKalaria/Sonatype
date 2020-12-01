package org.apache.maven.shared.runtime;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;

/**
 * A parent- or child-delegating class loader for use by tests.
 * 
 * @author <a href="mailto:markh@apache.org">Mark Hobson</a>
 * @version $Id$
 */
public class DelegatingClassLoader extends URLClassLoader
{
    // fields -----------------------------------------------------------------

    private final boolean childDelegating;

    // constructors -----------------------------------------------------------

    public DelegatingClassLoader( URL[] urls )
    {
        super( urls );

        childDelegating = false;
    }

    public DelegatingClassLoader( URL[] urls, ClassLoader parent )
    {
        this( urls, parent, false );
    }

    public DelegatingClassLoader( URL[] urls, ClassLoader parent, boolean childDelegating )
    {
        super( urls, parent );

        this.childDelegating = childDelegating;
    }

    // ClassLoader methods ----------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> loadClass( String name ) throws ClassNotFoundException
    {
        Class<?> klass;

        if ( childDelegating )
        {
            klass = findLoadedClass( name );

            if ( klass == null )
            {
                try
                {
                    klass = findClass( name );
                }
                catch ( ClassNotFoundException exception )
                {
                    if ( getParent() != null )
                    {
                        klass = getParent().loadClass( name );
                    }
                }
            }
        }
        else
        {
            klass = super.loadClass( name );
        }

        return klass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL getResource( String name )
    {
        URL url;

        if ( childDelegating )
        {
            url = findResource( name );

            if ( url == null && getParent() != null )
            {
                url = getParent().getResource( name );
            }
        }
        else
        {
            url = super.getResource( name );
        }

        return url;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Enumeration<URL> getResources( String name ) throws IOException
    {
        Enumeration<URL> urls;

        if ( childDelegating )
        {
            urls = findResources( name );

            if ( getParent() != null )
            {
                Enumeration<URL> parentURLs = getParent().getResources( name );

                urls = new CompositeEnumeration<URL>( urls, parentURLs );
            }
        }
        else
        {
            urls = super.getResources( name );
        }

        return urls;
    }
}
