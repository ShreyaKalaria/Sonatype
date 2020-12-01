package org.sonatype.aether;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.util.Map;

/**
 * The keys and defaults for common configuration properties.
 * 
 * @author Benjamin Bentmann
 * @see RepositorySystemSession#getConfigProperties()
 */
public final class ConfigurationProperties
{

    private static final String PREFIX_AETHER = "aether.";

    private static final String PREFIX_CONNECTOR = PREFIX_AETHER + "connector.";

    /**
     * A flag indicating whether interaction with the user is allowed.
     * 
     * @see #DEFAULT_INTERACTIVE
     */
    public static final String INTERACTIVE = PREFIX_AETHER + "interactive";

    /**
     * The default interactive mode if {@link #INTERACTIVE} isn't set.
     */
    public static final boolean DEFAULT_INTERACTIVE = false;

    /**
     * The user agent that repository connectors should report to servers.
     * 
     * @see #DEFAULT_USER_AGENT
     */
    public static final String USER_AGENT = PREFIX_CONNECTOR + "userAgent";

    /**
     * The default user agent to use if {@link #USER_AGENT} isn't set.
     */
    public static final String DEFAULT_USER_AGENT = "Aether";

    /**
     * The timeout (in milliseconds) to wait for a successful connection to a remote server. Non-positive values
     * indicate no timeout.
     * 
     * @see #DEFAULT_CONNECT_TIMEOUT
     */
    public static final String CONNECT_TIMEOUT = PREFIX_CONNECTOR + "connectTimeout";

    /**
     * The default connect timeout to use if {@link #CONNECT_TIMEOUT} isn't set.
     */
    public static final int DEFAULT_CONNECT_TIMEOUT = 10 * 1000;

    /**
     * The timeout (in milliseconds) to wait for a response from a remote server. Non-positive values indicate no
     * timeout.
     * 
     * @see #DEFAULT_REQUEST_TIMEOUT
     */
    public static final String REQUEST_TIMEOUT = PREFIX_CONNECTOR + "requestTimeout";

    /**
     * The default request timeout to use if {@link #REQUEST_TIMEOUT} isn't set.
     */
    public static final int DEFAULT_REQUEST_TIMEOUT = 60 * 1000;

    /**
     * The request headers to use for HTTP-based repository connectors. The headers are specified using a
     * {@code Map<String, String>}, mapping a header name to its value. Besides this general key, clients may also
     * specify headers for a specific remote repository by appending the suffix {@code .<repoId>} to this key when
     * storing the headers map. The repository-specific headers map is supposed to be complete, i.e. is not merged with
     * the general headers map.
     */
    public static final String HTTP_HEADERS = PREFIX_CONNECTOR + "http.headers";

    /**
     * The encoding/charset to use when exchanging credentials with HTTP servers. Besides this general key, clients may
     * also specify the encoding for a specific remote repository by appending the suffix {@code .<repoId>} to this key
     * when storing the charset name.
     * 
     * @see #DEFAULT_HTTP_CREDENTIAL_ENCODING
     */
    public static final String HTTP_CREDENTIAL_ENCODING = PREFIX_CONNECTOR + "http.credentialEncoding";

    /**
     * The default encoding/charset to use if {@link #HTTP_CREDENTIAL_ENCODING} isn't set.
     */
    public static final String DEFAULT_HTTP_CREDENTIAL_ENCODING = "ISO-8859-1";

    private ConfigurationProperties()
    {
        // hide constructor
    }

    /**
     * Gets the specified configuration property.
     * 
     * @param properties The configuration properties to read, must not be {@code null}.
     * @param key The property to read, must not be {@code null}.
     * @param defaultValue The default value to return in case the property isn't set, may be {@code null}.
     * @return The property value or {@code null} if none.
     * @deprecated As of version 1.12, use {@code org.sonatype.aether.util.ConfigUtils} instead.
     */
    @Deprecated
    public static String get( Map<?, ?> properties, String key, String defaultValue )
    {
        Object value = properties.get( key );

        if ( !( value instanceof String ) )
        {
            return defaultValue;
        }

        return (String) value;
    }

    /**
     * Gets the specified configuration property.
     * 
     * @param session The repository system session from which to read the configuration property, must not be
     *            {@code null}.
     * @param key The property to read, must not be {@code null}.
     * @param defaultValue The default value to return in case the property isn't set, may be {@code null}.
     * @return The property value or {@code null} if none.
     * @deprecated As of version 1.12, use {@code org.sonatype.aether.util.ConfigUtils} instead.
     */
    @Deprecated
    public static String get( RepositorySystemSession session, String key, String defaultValue )
    {
        return get( session.getConfigProperties(), key, defaultValue );
    }

    /**
     * Gets the specified configuration property.
     * 
     * @param properties The configuration properties to read, must not be {@code null}.
     * @param key The property to read, must not be {@code null}.
     * @param defaultValue The default value to return in case the property isn't set.
     * @return The property value.
     * @deprecated As of version 1.12, use {@code org.sonatype.aether.util.ConfigUtils} instead.
     */
    @Deprecated
    public static int get( Map<?, ?> properties, String key, int defaultValue )
    {
        Object value = properties.get( key );

        if ( value instanceof Number )
        {
            return ( (Number) value ).intValue();
        }

        try
        {
            return Integer.valueOf( (String) value );
        }
        catch ( Exception e )
        {
            return defaultValue;
        }
    }

    /**
     * Gets the specified configuration property.
     * 
     * @param session The repository system session from which to read the configuration property, must not be
     *            {@code null}.
     * @param key The property to read, must not be {@code null}.
     * @param defaultValue The default value to return in case the property isn't set.
     * @return The property value.
     * @deprecated As of version 1.12, use {@code org.sonatype.aether.util.ConfigUtils} instead.
     */
    @Deprecated
    public static int get( RepositorySystemSession session, String key, int defaultValue )
    {
        return get( session.getConfigProperties(), key, defaultValue );
    }

    /**
     * Gets the specified configuration property.
     * 
     * @param properties The configuration properties to read, must not be {@code null}.
     * @param key The property to read, must not be {@code null}.
     * @param defaultValue The default value to return in case the property isn't set.
     * @return The property value.
     * @deprecated As of version 1.12, use {@code org.sonatype.aether.util.ConfigUtils} instead.
     */
    @Deprecated
    public static boolean get( Map<?, ?> properties, String key, boolean defaultValue )
    {
        Object value = properties.get( key );

        if ( value instanceof Boolean )
        {
            return ( (Boolean) value ).booleanValue();
        }
        else if ( !( value instanceof String ) )
        {
            return defaultValue;
        }

        return Boolean.parseBoolean( (String) value );
    }

    /**
     * Gets the specified configuration property.
     * 
     * @param session The repository system session from which to read the configuration property, must not be
     *            {@code null}.
     * @param key The property to read, must not be {@code null}.
     * @param defaultValue The default value to return in case the property isn't set.
     * @return The property value.
     * @deprecated As of version 1.12, use {@code org.sonatype.aether.util.ConfigUtils} instead.
     */
    @Deprecated
    public static boolean get( RepositorySystemSession session, String key, boolean defaultValue )
    {
        return get( session.getConfigProperties(), key, defaultValue );
    }

}
