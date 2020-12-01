package org.sonatype.aether.util.filter;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.test.util.NodeBuilder;

public class ScopeDependencyFilterTest
    extends AbstractDependencyFilterTest
{

    @Test
    public void acceptTest()
    {

        NodeBuilder builder = new NodeBuilder();
        builder.scope( "compile" ).artifactId( "test" );
        List<DependencyNode> parents = new LinkedList<DependencyNode>();

        // null or empty
        assertTrue( new ScopeDependencyFilter( null, null ).accept( builder.build(), parents ) );
        assertTrue( new ScopeDependencyFilter( new LinkedList<String>(), new LinkedList<String>() ).accept( builder.build(), parents ) );
        assertTrue( new ScopeDependencyFilter( (String[]) null ).accept( builder.build(), parents ) );
        
        //only excludes
        assertTrue( new ScopeDependencyFilter( "test" ).accept( builder.build(), parents ));
        assertFalse( new ScopeDependencyFilter( "compile" ).accept( builder.build(), parents ));
        assertFalse( new ScopeDependencyFilter( "compile", "test" ).accept( builder.build(), parents ));
        
        //Both
        String[] excludes1 = {"provided"};
        String[] includes1 = {"compile","test"}; 
        assertTrue( new ScopeDependencyFilter( Arrays.asList( includes1 ), Arrays.asList( excludes1 ) ).accept( builder.build() , parents ));
        assertTrue( new ScopeDependencyFilter( Arrays.asList( includes1 ), null ).accept( builder.build() , parents ));
        
        //exclude wins
        String[] excludes2 = {"compile"};
        String[] includes2 = {"compile"};
        assertFalse( new ScopeDependencyFilter( Arrays.asList( includes2 ), Arrays.asList( excludes2 ) ).accept( builder.build() , parents ));

    }

}
