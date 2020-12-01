package org.codehaus.plexus.archiver.gzip;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

import org.codehaus.plexus.components.io.attributes.Java7FileAttributes;
import org.codehaus.plexus.components.io.attributes.PlexusIoResourceAttributes;
import org.codehaus.plexus.components.io.resources.PlexusIoCompressedFileResourceCollection;
import org.codehaus.plexus.util.IOUtil;

import javax.annotation.Nonnull;


/**
 * Abstract base class for compressed files, aka singleton
 * resource collections.
 */
public class PlexusIoGzipResourceCollection
    extends PlexusIoCompressedFileResourceCollection
{
    protected String getDefaultExtension()
    {
        return ".gz";
    }

    @Nonnull
    protected InputStream getInputStream( File file )
        throws IOException
    {
        InputStream fis = new FileInputStream( file );
        try
        {
            InputStream result = new GZIPInputStream( fis );
            fis = null;
            return result;
        }
        finally
        {
            IOUtil.close( fis );
        }
    }

	@Override protected PlexusIoResourceAttributes getAttributes(File file) throws IOException {
        return new Java7FileAttributes(file, new HashMap<Integer, String>(), new HashMap<Integer, String>());
	}
}
