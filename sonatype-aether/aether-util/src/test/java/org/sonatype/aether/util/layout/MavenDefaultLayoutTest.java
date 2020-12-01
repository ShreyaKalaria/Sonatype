package org.sonatype.aether.util.layout;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import static org.junit.Assert.*;

import java.net.URI;

import org.junit.Test;
import org.sonatype.aether.test.util.impl.StubArtifact;

/**
 * @author Benjamin Bentmann
 */
public class MavenDefaultLayoutTest
{

    @Test
    public void testArtifactPath()
    {
        MavenDefaultLayout layout = new MavenDefaultLayout();

        URI uri = layout.getPath( new StubArtifact( "g.i.d", "a-i.d", "cls", "ext", "1.0" ) );
        assertEquals( "g/i/d/a-i.d/1.0/a-i.d-1.0-cls.ext", uri.getPath() );

        uri = layout.getPath( new StubArtifact( "g.i.d", "aid", "", "ext", "1.0" ) );
        assertEquals( "g/i/d/aid/1.0/aid-1.0.ext", uri.getPath() );

        uri = layout.getPath( new StubArtifact( "g.i.d", "aid", "", "", "1.0" ) );
        assertEquals( "g/i/d/aid/1.0/aid-1.0", uri.getPath() );
    }

}
