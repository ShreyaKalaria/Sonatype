package org.sonatype.aether.collection;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import org.sonatype.aether.RepositoryException;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.version.VersionConstraint;

/**
 * Thrown in case of an unsolvable conflict between different version constraints for a dependency.
 * 
 * @author Benjamin Bentmann
 */
public class UnsolvableVersionConflictException
    extends RepositoryException
{

    private final Object dependencyConflictId;

    private final Collection<String> versions;

    private final Collection<List<DependencyNode>> paths;

    public UnsolvableVersionConflictException( Object dependencyConflictId, Collection<String> versions )
    {
        super( "Could not resolve version conflict for " + dependencyConflictId + " with requested versions "
            + toList( versions ) );
        this.dependencyConflictId = ( dependencyConflictId != null ) ? dependencyConflictId : "";
        this.versions = ( versions != null ) ? versions : Collections.<String> emptyList();
        this.paths = Collections.emptyList();
    }

    private static String toList( Collection<String> versions )
    {
        StringBuilder buffer = new StringBuilder( 256 );
        if ( versions != null )
        {
            for ( String version : versions )
            {
                if ( buffer.length() > 0 )
                {
                    buffer.append( ", " );
                }
                buffer.append( version );
            }
        }
        return buffer.toString();
    }

    public UnsolvableVersionConflictException( Collection<List<DependencyNode>> paths, Object dependencyConflictId )
    {
        super( "Could not resolve version conflict among " + toPaths( paths ) );
        this.dependencyConflictId = ( dependencyConflictId != null ) ? dependencyConflictId : "";
        if ( paths == null )
        {
            this.paths = Collections.emptyList();
            this.versions = Collections.emptyList();
        }
        else
        {
            this.paths = paths;
            this.versions = new LinkedHashSet<String>();
            for ( List<DependencyNode> path : paths )
            {
                VersionConstraint constraint = path.get( path.size() - 1 ).getVersionConstraint();
                if ( constraint != null && !constraint.getRanges().isEmpty() )
                {
                    versions.add( constraint.toString() );
                }
            }
        }
    }

    private static String toPaths( Collection<List<DependencyNode>> paths )
    {
        String result = "";

        if ( paths != null )
        {
            Collection<String> strings = new LinkedHashSet<String>();

            for ( List<DependencyNode> path : paths )
            {
                strings.add( toPath( path ) );
            }

            result = strings.toString();
        }

        return result;
    }

    private static String toPath( List<DependencyNode> path )
    {
        StringBuilder buffer = new StringBuilder( 256 );

        for ( Iterator<DependencyNode> it = path.iterator(); it.hasNext(); )
        {
            DependencyNode node = it.next();
            if ( node.getDependency() == null )
            {
                continue;
            }

            Artifact artifact = node.getDependency().getArtifact();
            buffer.append( artifact.getGroupId() );
            buffer.append( ':' ).append( artifact.getArtifactId() );
            buffer.append( ':' ).append( artifact.getExtension() );
            if ( artifact.getClassifier().length() > 0 )
            {
                buffer.append( ':' ).append( artifact.getClassifier() );
            }
            buffer.append( ':' ).append( node.getVersionConstraint() );

            if ( it.hasNext() )
            {
                buffer.append( " -> " );
            }
        }

        return buffer.toString();
    }

    /**
     * Gets the conflict id of the dependency that encountered the version conflict.
     * 
     * @return The conflict id, never {@code null}.
     */
    public Object getDependencyConflictId()
    {
        return dependencyConflictId;
    }

    /**
     * Gets the paths leading to the conflicting dependencies.
     * 
     * @return The (read-only) paths leading to the conflicting dependencies, never {@code null}.
     */
    public Collection<List<DependencyNode>> getPaths()
    {
        return paths;
    }

    /**
     * Gets the conflicting version constraints of the dependency.
     * 
     * @return The (read-only) conflicting version constraints, never {@code null}.
     */
    public Collection<String> getVersions()
    {
        return versions;
    }

}
