package org.sonatype.aether.util.graph;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sonatype.aether.graph.DependencyFilter;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.graph.DependencyVisitor;

/**
 * A dependency visitor that records all paths leading to nodes matching a certain filter criteria.
 * 
 * @author Benjamin Bentmann
 */
public class PathRecordingDependencyVisitor
    implements DependencyVisitor
{

    private final DependencyFilter filter;

    private final List<List<DependencyNode>> paths;

    private final Stack<DependencyNode> parents;

    private final boolean excludeChildrenOfMatches;

    /**
     * Creates a new visitor that uses the specified filter to identify terminal nodes of interesting paths. The visitor
     * will not search for paths going beyond an already matched node.
     * 
     * @param filter The filter used to select terminal nodes of paths to record, may be {@code null} to match any node.
     */
    public PathRecordingDependencyVisitor( DependencyFilter filter )
    {
        this( filter, true );
    }

    /**
     * Creates a new visitor that uses the specified filter to identify terminal nodes of interesting paths.
     * 
     * @param filter The filter used to select terminal nodes of paths to record, may be {@code null} to match any node.
     * @param excludeChildrenOfMatches Flag controlling whether children of matched nodes should be excluded from the
     *            traversal, thereby ignoring any potential paths to other matching nodes beneath a matching ancestor
     *            node. If {@code true}, all recorded paths will have only one matching node (namely the terminal node),
     *            if {@code false} a recorded path can consist of multiple matching nodes.
     */
    public PathRecordingDependencyVisitor( DependencyFilter filter, boolean excludeChildrenOfMatches )
    {
        this.filter = filter;
        this.excludeChildrenOfMatches = excludeChildrenOfMatches;
        paths = new ArrayList<List<DependencyNode>>();
        parents = new Stack<DependencyNode>();
    }

    /**
     * Gets the filter being used to select terminal nodes.
     * 
     * @return The filter being used or {@code null} if none.
     */
    public DependencyFilter getFilter()
    {
        return filter;
    }

    /**
     * Gets the paths leading to nodes matching the filter that have been recorded during the graph visit. A path is
     * given as a sequence of nodes, starting with the root node of the graph and ending with the node that matched the
     * filter.
     * 
     * @return The recorded paths, never {@code null}.
     */
    public List<List<DependencyNode>> getPaths()
    {
        return paths;
    }

    public boolean visitEnter( DependencyNode node )
    {
        boolean accept = filter == null || filter.accept( node, parents );

        parents.push( node );

        if ( accept )
        {
            DependencyNode[] path = new DependencyNode[parents.size()];
            int i = parents.size() - 1;
            for ( DependencyNode n : parents )
            {
                path[i] = n;
                i--;
            }
            paths.add( Arrays.asList( path ) );
        }

        return !( excludeChildrenOfMatches && accept );
    }

    public boolean visitLeave( DependencyNode node )
    {
        parents.pop();

        return true;
    }

}
