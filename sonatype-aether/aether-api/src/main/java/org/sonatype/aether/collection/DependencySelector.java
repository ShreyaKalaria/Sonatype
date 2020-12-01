package org.sonatype.aether.collection;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import org.sonatype.aether.graph.Dependency;

/**
 * Decides what dependencies to include in the dependency graph. <em>Note:</em> For the sake of good performance during
 * dependency collection, implementations should provide a semantic {@link Object#equals(Object) equals()} method.
 * 
 * @author Benjamin Bentmann
 * @see org.sonatype.aether.RepositorySystemSession#getDependencySelector()
 * @see org.sonatype.aether.RepositorySystem#collectDependencies(org.sonatype.aether.RepositorySystemSession,
 *      CollectRequest)
 */
public interface DependencySelector
{

    /**
     * Applies exclusions to the specified dependency.
     * 
     * @param dependency The dependency to filter, must not be {@code null}.
     * @return {@code false} if the dependency should be excluded from the children of the current node, {@code true}
     *         otherwise.
     */
    boolean selectDependency( Dependency dependency );

    /**
     * Derives a dependency selector for the specified collection context. When calculating the child selector,
     * implementors are strongly advised to simply return the current instance if nothing changed to help save memory.
     * 
     * @param context The dependency collection context, must not be {@code null}.
     * @return The dependency filter for the target node, must not be {@code null}.
     */
    DependencySelector deriveChildSelector( DependencyCollectionContext context );

}
