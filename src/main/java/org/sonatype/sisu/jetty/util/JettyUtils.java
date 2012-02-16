/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.sisu.jetty.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.interpolation.Interpolator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.xml.XmlConfiguration;
import org.sonatype.appcontext.AppContext;
import org.sonatype.appcontext.AppContextRequest;
import org.sonatype.appcontext.Factory;
import org.sonatype.appcontext.source.MapEntrySource;
import org.sonatype.appcontext.source.PropertiesFileEntrySource;
import org.sonatype.appcontext.source.Sources;

/**
 * Utility that configures Jetty server from passed in jetty.xml file.
 * 
 * @author cstamas
 */
public final class JettyUtils
{
    /**
     * System property key for "context" file location. If not present, this class will default to use
     * "jetty.properties" next to (in same directory) the Jetty XML configuration file. If property is set, and it
     * contains a relative file path, then it will be "resolved" against Jetty's XML configuration file. Otherwise, the
     * absolute path will be used (as is).
     */
    public static final String JETTY_CONTEXT_FILE_KEY = "jettyContext";

    /**
     * System property key for "direct inclusions" of other keys by AppContext.
     */
    public static final String JETTY_CONTEXT_INCLUDE_KEYS_KEY = "jettyContextIncludeKeys";

    /**
     * A system property key to turn on "appcontext dump", when the contents of appcontext is dumped using one of the
     * publishers available. Dump happens only if this property has value of "true" (Boolean.TRUE.toString()).
     */
    public static final String JETTY_CONTEXT_DUMP = "jettyContextDump";

    /**
     * A system property key to turn on "plexus compatibility", and use "plexus" alias for AppContext too, to pick up
     * env variables with "PLEXUS_" prefix and system properties with "plexus." prefix. Needed by Nexus instances for
     * example.
     */
    public static final String PLEXUS_COMPATIBILITY_KEY = "jettyPlexusCompatibility";

    private JettyUtils()
    {
    }

    public static AppContext configureServer( Server server, File jettyXml, Map<?, ?>... contexts )
        throws InterpolationException, IOException
    {
        final FileInputStream fis = new FileInputStream( jettyXml );

        String rawConfig;

        try
        {
            rawConfig = IO.toString( fis, "UTF-8" );
        }
        finally
        {
            fis.close();
        }

        // for historical reasons, honor the "plexus" prefix too
        AppContextRequest appContextReq = null;

        if ( Boolean.getBoolean( PLEXUS_COMPATIBILITY_KEY ) )
        {
            appContextReq = Factory.getDefaultRequest( "jetty", null, Arrays.asList( "plexus" ) );
        }
        else
        {
            appContextReq = Factory.getDefaultRequest( "jetty" );
        }

        if ( !Boolean.valueOf( System.getProperty( JETTY_CONTEXT_DUMP ) ) )
        {
            // we do not publish anything (defaultReq does contain one "terminal" publisher)
            appContextReq.getPublishers().clear();
        }

        // fill in passed in contexts too
        int ctxNo = 1;
        for ( Map<?, ?> context : contexts )
        {
            appContextReq.getSources().add( 0, new MapEntrySource( "ctx" + ( ctxNo++ ), context ) );
        }

        // fill in inclusions if any
        final String includedKeysString = System.getProperty( JETTY_CONTEXT_INCLUDE_KEYS_KEY );
        if ( !isBlank( includedKeysString ) )
        {
            final String[] keys = includedKeysString.split( "," );
            if ( keys != null && keys.length > 0 )
            {
                appContextReq.getSources().addAll( 0, Sources.getDefaultSelectTargetedSources( keys ) );
            }
        }

        // try jetty.properties next to XML file, if found, add it as ultimate source
        final File jettyContextFile = getContextFile( jettyXml );

        if ( jettyContextFile.isFile() )
        {
            appContextReq.getSources().add( 0, new PropertiesFileEntrySource( jettyContextFile ) );
        }

        AppContext appContext = Factory.create( appContextReq );

        // Interpolate jetty.xml
        Interpolator interpolator = appContext.getInterpolator();

        String interpolatedConfig;

        // interpolate
        interpolatedConfig = interpolator.interpolate( rawConfig );

        try
        {
            new XmlConfiguration( new ByteArrayInputStream( interpolatedConfig.getBytes( "UTF-8" ) ) ).configure( server );
        }
        catch ( Exception e )
        {
            final IOException ex =
                new IOException( "Failed to configure Jetty server using XML configuration at: " + jettyXml );
            ex.initCause( e );
            throw ex;
        }

        return appContext;
    }

    protected static File getContextFile( final File jettyXml )
    {
        final String jettyContext = System.getProperty( JETTY_CONTEXT_FILE_KEY );

        if ( !isBlank( jettyContext ) )
        {
            final File jettyContextFile = new File( jettyContext );

            if ( jettyContextFile.isAbsolute() )
            {
                return jettyContextFile;
            }
            else
            {
                return new File( jettyXml.getParentFile(), jettyContext );
            }
        }
        else
        {
            // fallback to defaults, a "jetty.properties" file next to jetty's XML configuration file
            return new File( jettyXml.getParentFile(), "jetty.properties" );
        }
    }

    public static boolean isBlank( final String string )
    {
        return ( ( string == null ) || ( string.trim().length() == 0 ) );
    }
}
