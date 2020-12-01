package org.sonatype.aether.collection;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import org.sonatype.aether.RepositoryException;
import org.sonatype.aether.graph.DependencyNode;

/**
 * Transforms a given dependency graph. <em>Note:</em> Dependency graphs may contain cycles, as such a graph transformer
 * needs to gracefully handle cyclic graphs, e.g. guard against infinite recursion.
 * 
 * @author Benjamin Bentmann
 * @see org.sonatype.aether.RepositorySystemSession#getDependencyGraphTransformer()
 */
public interface DependencyGraphTransformer
{

    /**
     * Transforms the dependency graph denoted by the specified root node. The transformer may directly change the
     * provided input graph or create a new graph.
     * 
     * @param node The root node of the (possibly cyclic!) graph to transform, must not be {@code null}.
     * @param context The graph transformation context, must not be {@code null}.
     * @return The result graph of the transformation, never {@code null}.
     * @throws RepositoryException If the transformation failed.
     */
    DependencyNode transformGraph( DependencyNode node, DependencyGraphTransformationContext context )
        throws RepositoryException;

}
