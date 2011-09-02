package org.sonatype.appcontext.source;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.sonatype.appcontext.AppContextException;
import org.sonatype.appcontext.AppContextRequest;
import org.sonatype.appcontext.internal.Preconditions;

/**
 * This is a "legacy" baseDir EntrySource that should not be used anymore. It was used mostly in Plexus applications,
 * that usually do depend on "baseDir".
 * 
 * @author cstamas
 * @deprecated Do not rely on system properties for stuff like these, use AppContext better.
 */
public class LegacyBasedirEntrySource
    implements EntrySource, EntrySourceMarker
{
    private final String basedirKey;

    private final boolean failIfNotFound;

    /**
     * Constructs the instance using "standard" key used in Plexus Applications. The constructed instance will fail if
     * key is not found!
     */
    public LegacyBasedirEntrySource()
    {
        this( "basedir", true );
    }

    /**
     * Constructs an instance with custom key.
     * 
     * @param basedirKey
     * @param failIfNotFound
     */
    public LegacyBasedirEntrySource( final String basedirKey, final boolean failIfNotFound )
    {
        this.basedirKey = Preconditions.checkNotNull( basedirKey );

        this.failIfNotFound = failIfNotFound;
    }

    public String getDescription()
    {
        return "legacyBasedir(key:\"" + basedirKey + "\")";
    }

    public EntrySourceMarker getEntrySourceMarker()
    {
        return this;
    }

    public Map<String, Object> getEntries( AppContextRequest request )
        throws AppContextException
    {
        final File baseDir = discoverBasedir( basedirKey );

        if ( failIfNotFound && !baseDir.isDirectory() )
        {
            throw new AppContextException(
                "LegacyBasedirEntrySource was not able to find existing basedir! It discovered \""
                    + baseDir.getAbsolutePath() + "\", but it does not exists or is not a directory!" );
        }

        final HashMap<String, Object> result = new HashMap<String, Object>();

        result.put( basedirKey, baseDir.getAbsolutePath() );

        return result;
    }

    // ==

    /**
     * The essence how old Plexus application was expecting to have "basedir" discovered. Usually using system property
     * that contained a file path, or fall back to current working directory.
     * 
     * @param basedirKey
     * @return
     */
    public File discoverBasedir( final String basedirKey )
    {
        String basedirPath = System.getProperty( basedirKey );

        if ( basedirPath == null )
        {
            return new File( "" ).getAbsoluteFile();
        }
        else
        {
            return new File( basedirPath ).getAbsoluteFile();
        }
    }
}
