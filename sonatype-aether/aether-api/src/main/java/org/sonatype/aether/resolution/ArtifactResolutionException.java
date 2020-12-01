package org.sonatype.aether.resolution;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.util.Collections;
import java.util.List;

import org.sonatype.aether.RepositoryException;
import org.sonatype.aether.transfer.ArtifactNotFoundException;

/**
 * Thrown in case of a unresolvable artifacts.
 * 
 * @author Benjamin Bentmann
 */
public class ArtifactResolutionException
    extends RepositoryException
{

    private final List<ArtifactResult> results;

    public ArtifactResolutionException( List<ArtifactResult> results )
    {
        super( getMessage( results ), getCause( results ) );
        this.results = ( results != null ) ? results : Collections.<ArtifactResult> emptyList();
    }

    public List<ArtifactResult> getResults()
    {
        return results;
    }

    public ArtifactResult getResult()
    {
        return !results.isEmpty() ? results.get( 0 ) : null;
    }

    private static String getMessage( List<? extends ArtifactResult> results )
    {
        StringBuilder buffer = new StringBuilder( 256 );

        buffer.append( "The following artifacts could not be resolved: " );

        int unresolved = 0;

        String sep = "";
        for ( ArtifactResult result : results )
        {
            if ( !result.isResolved() )
            {
                unresolved++;

                buffer.append( sep );
                buffer.append( result.getRequest().getArtifact() );
                sep = ", ";
            }
        }

        Throwable cause = getCause( results );
        if ( cause != null )
        {
            if ( unresolved == 1 )
            {
                buffer.setLength( 0 );
                buffer.append( cause.getMessage() );
            }
            else
            {
                buffer.append( ": " ).append( cause.getMessage() );
            }
        }

        return buffer.toString();
    }

    private static Throwable getCause( List<? extends ArtifactResult> results )
    {
        for ( ArtifactResult result : results )
        {
            if ( !result.isResolved() )
            {
                Throwable nf = null;
                for ( Throwable t : result.getExceptions() )
                {
                    if ( t instanceof ArtifactNotFoundException )
                    {
                        if ( nf == null )
                        {
                            nf = t;
                        }
                    }
                    else
                    {
                        return t;
                    }

                }
                if ( nf != null )
                {
                    return nf;
                }
            }
        }
        return null;
    }

}
