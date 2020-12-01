package org.codehaus.plexus.archiver.diags;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.codehaus.plexus.archiver.*;
import org.codehaus.plexus.components.io.resources.PlexusIoResource;
import org.codehaus.plexus.components.io.resources.PlexusIoResourceCollection;
import org.codehaus.plexus.util.StringUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A diagnostic archiver that keeps track of stuff that has been added.
 */
public class TrackingArchiver
    implements Archiver
{
    private File destFile;

    public final List<Addition> added = new ArrayList<Addition>();

    private boolean useJvmChmod;

    private boolean ignorePermissions;

    public void createArchive()
        throws ArchiverException, IOException
    {
    }

    public void addDirectory( final @Nonnull File directory )
        throws ArchiverException
    {
        added.add( new Addition( directory, null, null, null, -1 ) );
    }

    public void addDirectory( final @Nonnull File directory, final String prefix )
        throws ArchiverException
    {
        added.add( new Addition( directory, prefix, null, null, -1 ) );
    }

    public void addDirectory( final @Nonnull File directory, final String[] includes, final String[] excludes )
        throws ArchiverException
    {
        added.add( new Addition( directory, null, includes, excludes, -1 ) );
    }

    public void addDirectory( final @Nonnull File directory, final String prefix, final String[] includes,
                              final String[] excludes )
        throws ArchiverException
    {
        added.add( new Addition( directory, prefix, includes, excludes, -1 ) );
    }

    public void addFileSet( final @Nonnull FileSet fileSet )
        throws ArchiverException
    {
        added.add( new Addition( fileSet, null, null, null, -1 ) );
    }

    public void addFile( final @Nonnull File inputFile, final @Nonnull String destFileName )
        throws ArchiverException
    {
        added.add( new Addition( inputFile, destFileName, null, null, -1 ) );
    }

    public void addFile( final @Nonnull File inputFile, final @Nonnull String destFileName, final int permissions )
        throws ArchiverException
    {
        added.add( new Addition( inputFile, destFileName, null, null, permissions ) );
    }

    public void addArchivedFileSet( final @Nonnull File archiveFile )
        throws ArchiverException
    {
        added.add( new Addition( archiveFile, null, null, null, -1 ) );
    }

    public void addArchivedFileSet( final @Nonnull File archiveFile, final String prefix )
        throws ArchiverException
    {
        added.add( new Addition( archiveFile, prefix, null, null, -1 ) );
    }

    public void addSymlink( String s, String s2 )
        throws ArchiverException
    {
        added.add( new Addition( s, null, null, null, -1 ) );
    }

    public void addSymlink( String s, int i, String s2 )
        throws ArchiverException
    {
        added.add( new Addition( s, null, null, null, -1 ) );

    }

    public void addArchivedFileSet( final File archiveFile, final String[] includes, final String[] excludes )
        throws ArchiverException
    {
        added.add( new Addition( archiveFile, null, includes, excludes, -1 ) );
    }

    public void addArchivedFileSet( final @Nonnull File archiveFile, final String prefix, final String[] includes,
                                    final String[] excludes )
        throws ArchiverException
    {
        added.add( new Addition( archiveFile, prefix, includes, excludes, -1 ) );
    }

    public void addArchivedFileSet( final ArchivedFileSet fileSet )
        throws ArchiverException
    {
        added.add( new Addition( fileSet, null, null, null, -1 ) );
    }

    public void addResource( final PlexusIoResource resource, final String destFileName, final int permissions )
        throws ArchiverException
    {
        added.add( new Addition( resource, destFileName, null, null, permissions ) );
    }

    public void addResources( final PlexusIoResourceCollection resources )
        throws ArchiverException
    {
        added.add( new Addition( resources, null, null, null, -1 ) );
    }

    public File getDestFile()
    {
        return destFile;
    }

    public void setDestFile( final File destFile )
    {
        this.destFile = destFile;
    }

    public void setFileMode( final int mode )
    {
    }

    public int getFileMode()
    {
        return Integer.parseInt( "0644", 8 );
    }

    public int getOverrideFileMode()
    {
        return Integer.parseInt( "0644", 8 );
    }

    public void setDefaultFileMode( final int mode )
    {
    }

    public int getDefaultFileMode()
    {
        return Integer.parseInt( "0644", 8 );
    }

    public void setDirectoryMode( final int mode )
    {
    }

    public int getDirectoryMode()
    {
        return Integer.parseInt( "0755", 8 );
    }

    public int getOverrideDirectoryMode()
    {
        return Integer.parseInt( "0755", 8 );
    }

    public void setDefaultDirectoryMode( final int mode )
    {
    }

    public int getDefaultDirectoryMode()
    {
        return Integer.parseInt( "0755", 8 );
    }

    public boolean getIncludeEmptyDirs()
    {
        return false;
    }

    public void setIncludeEmptyDirs( final boolean includeEmptyDirs )
    {
    }

    public void setDotFileDirectory( final File dotFileDirectory )
    {
    }

    public
    @Nonnull
    ResourceIterator getResources()
        throws ArchiverException
    {
        throw new RuntimeException("Not implemented");
    }

    @SuppressWarnings( "rawtypes" )
    public Map<String, ArchiveEntry> getFiles()
    {
        return new HashMap<String, ArchiveEntry>();
    }

    public boolean isForced()
    {
        return false;
    }

    public void setForced( final boolean forced )
    {
    }

    public boolean isSupportingForced()
    {
        return true;
    }

    public String getDuplicateBehavior()
    {
        return null;
    }

    public void setDuplicateBehavior( final String duplicate )
    {
    }

    public class Addition
    {
        /**
         * {@inheritDoc}
         *
         * @see Object#toString()
         */
        @Override
        public String toString()
        {
            return "Addition (\n    resource= " + resource + "\n    directory= " + directory + "\n    destination= "
                + destination + "\n    permissions= " + permissions + "\n    includes= " + ( includes == null
                ? "-none-"
                : StringUtils.join( includes, ", " ) ) + "\n    excludes= " + ( excludes == null
                ? "-none-"
                : StringUtils.join( excludes, ", " ) ) + "\n)";
        }

        public final Object resource;

        public final File directory;

        public final String destination;

        public final int permissions;

        public final String[] includes;

        public final String[] excludes;

        public Addition( final Object resource, final String destination, final String[] includes,
                         final String[] excludes, final int permissions )
        {
            this.resource = resource;
            if ( resource instanceof FileSet)
            {
                final FileSet fs = (FileSet) resource;
                directory = fs.getDirectory();
                this.destination = fs.getPrefix();
                this.includes = fs.getIncludes();
                this.excludes = fs.getExcludes();
                this.permissions = permissions;
            }
            else
            {
                if ( resource instanceof File && ( (File) resource ).isDirectory() )
                {
                    directory = (File) resource;
                }
                else
                {
                    directory = null;
                }

                this.destination = destination;
                this.includes = includes;
                this.excludes = excludes;
                this.permissions = permissions;
            }
        }
    }

    public boolean isUseJvmChmod()
    {
        return useJvmChmod;
    }

    public void setUseJvmChmod( final boolean useJvmChmod )
    {
        this.useJvmChmod = useJvmChmod;
    }

    public boolean isIgnorePermissions()
    {
        return ignorePermissions;
    }

    public void setIgnorePermissions( final boolean ignorePermissions )
    {
        this.ignorePermissions = ignorePermissions;
    }
}
