package org.sonatype.aether.util.filter;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import org.sonatype.aether.graph.DependencyFilter;
import org.sonatype.aether.graph.DependencyNode;

/**
 * A dependency filter that combines zero or more other filters using a logical {@code OR}.
 * 
 * @author Benjamin Bentmann
 */
public class OrDependencyFilter
    implements DependencyFilter
{

    private final Collection<DependencyFilter> filters = new LinkedHashSet<DependencyFilter>();

    /**
     * Creates a new filter from the specified filters.
     * 
     * @param filters The filters to combine, may be {@code null}.
     */
    public OrDependencyFilter( DependencyFilter... filters )
    {
        if ( filters != null )
        {
            Collections.addAll( this.filters, filters );
        }
    }

    /**
     * Creates a new filter from the specified filters.
     * 
     * @param filters The filters to combine, may be {@code null}.
     */
    public OrDependencyFilter( Collection<DependencyFilter> filters )
    {
        if ( filters != null )
        {
            this.filters.addAll( filters );
        }
    }

    /**
     * Creates a new filter from the specified filters.
     * 
     * @param filter1 The first filter to combine, may be {@code null}.
     * @param filter2 The first filter to combine, may be {@code null}.
     * @return The combined filter or {@code null} if both filter were {@code null}.
     */
    public static DependencyFilter newInstance( DependencyFilter filter1, DependencyFilter filter2 )
    {
        if ( filter1 == null )
        {
            return filter2;
        }
        else if ( filter2 == null )
        {
            return filter1;
        }
        return new OrDependencyFilter( filter1, filter2 );
    }

    public boolean accept( DependencyNode node, List<DependencyNode> parents )
    {
        for ( DependencyFilter filter : filters )
        {
            if ( filter.accept( node, parents ) )
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }

        if ( obj == null || !getClass().equals( obj.getClass() ) )
        {
            return false;
        }

        OrDependencyFilter that = (OrDependencyFilter) obj;

        return this.filters.equals( that.filters );
    }

    @Override
    public int hashCode()
    {
        int hash = getClass().hashCode();
        hash = hash * 31 + filters.hashCode();
        return hash;
    }

}
