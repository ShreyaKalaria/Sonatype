package org.sonatype.aether.version;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

/**
 * A range of versions.
 * 
 * @author Benjamin Bentmann
 */
public interface VersionRange
{

    /**
     * Determines whether the specified version is contained within this range.
     * 
     * @param version The version to test, must not be {@code null}.
     * @return {@code true} if this range contains the specified version, {@code false} otherwise.
     */
    boolean containsVersion( Version version );

}
