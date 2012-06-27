package org.sonatype.sisu.jetty.thread;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

/**
 * A watch-dog meant to be run in separate thread, that will nag in case of unexpected Jetty pooled thread death being
 * detected. Experiments showed, that when one QTP thread dies due to OOM, not much is possible to be done from that
 * thread (not even {@link UncaughtExceptionHandler} of it is invoked, doing anything more complex might just induce
 * another OOM). But, other threads kept running happily with some sort of OOMs (see below). This will still not help
 * you if your code has memory leaks.
 * <p>
 * Note: OOM are not always same. This will work if OOM is not due to leak, but to some memory consumption peak (ie.
 * code that quickly allocates huge amounts of memory, but in next moment it is released too, same pattern as in
 * NXCM-4391 for P2 proxy repository).
 * 
 * @author cstamas
 * @since 1.3
 */
public class JettyWatchdog
    implements Runnable
{
    private static final Logger LOG = Log.getLogger( JettyWatchdog.class );

    public void run()
    {
        try
        {
            while ( true )
            {
                Thread.sleep( TimeUnit.SECONDS.toMillis( 10 ) );

                if ( RunnableWrapper.unexpectedThrowable )
                {
                    LOG.warn( "Jetty pooled thread death detected! System might be running low on memory!" );
                }
            }
        }
        catch ( InterruptedException e )
        {
            // nothing, will quit silently
        }
    }
}
