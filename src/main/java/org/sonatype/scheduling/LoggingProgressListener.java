package org.sonatype.scheduling;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple progress listener that logs messages into SLF4J API.
 * 
 * @author cstamas
 */
public class LoggingProgressListener
    implements ProgressListener
{
    private static final Workunit ROOT = new Workunit( "root", UNKNOWN_WORKUNITS );

    private final Logger logger;

    private final Deque<Workunit> workunits;

    private volatile boolean canceled = false;

    public LoggingProgressListener( final String name )
    {
        this( LoggerFactory.getLogger( name ) );
    }

    public LoggingProgressListener( final Logger logger )
    {
        this.logger = logger;
        this.workunits = new ArrayDeque<Workunit>( Arrays.asList( ROOT ) );
        this.canceled = false;
    }

    public void beginTask( final String name )
    {
        beginTask( name, UNKNOWN_WORKUNITS );
    }

    public void beginTask( final String name, final int toDo )
    {
        workunits.push( new Workunit( name, toDo ) );

        if ( UNKNOWN_WORKUNITS != toDo )
        {
            log( "{}: started ({} steps).", getStackedWorkunitNames(), toDo );
        }
        else
        {
            log( "{}: started.", getStackedWorkunitNames() );
        }
    }

    public void working( final int workDone )
    {
        working( null, workDone );
    }

    public void working( final String message )
    {
        working( message, 0 );
    }

    public void working( final String message, final int workDone )
    {
        final Workunit wu = workunits.peek();

        wu.done( workDone );

        if ( message != null && message.trim().length() > 0 )
        {
            if ( UNKNOWN_WORKUNITS != wu.getToDo() )
            {
                log(
                    "{}: {} ({}/{})",
                    new Object[] { getStackedWorkunitNames(), nvl( message ), String.valueOf( wu.getDone() ),
                        String.valueOf( wu.getToDo() ) } );
            }
            else
            {
                log( "{}: {} ({})", new Object[] { getStackedWorkunitNames(), nvl( message ), wu.getDone() } );
            }
        }
    }

    public void endTask( final String message )
    {
        log( "{}: finished: {}", getStackedWorkunitNames(), nvl( message ) );

        if ( workunits.size() > 1 )
        {
            workunits.pop();
        }
    }

    public boolean isCanceled()
    {
        return canceled;
    }

    public void cancel()
    {
        log( "{}: canceled, bailing out (may take a while).", getStackedWorkunitNames() );

        this.canceled = true;
    }

    // ==

    protected String nvl( final String str )
    {
        return String.valueOf( str );
    }

    protected String getStackedWorkunitNames()
    {
        Iterator<Workunit> wi = workunits.descendingIterator();

        // skip root
        wi.next();

        if ( wi.hasNext() )
        {
            final StringBuilder sb = new StringBuilder( wi.next().getName() );

            for ( ; wi.hasNext(); )
            {
                sb.append( " - " ).append( wi.next().getName() );
            }

            return sb.toString();
        }
        else
        {
            return "";
        }
    }

    protected void log( final String message, Object... param )
    {
        if ( logger != null )
        {
            logger.info( message, param );
        }
    }

    // ==

    public static class Workunit
    {
        private final String name;

        private final int toDo;

        private int done;

        public Workunit( final String name, final int toDo )
        {
            this.name = name;
            this.toDo = toDo;
            this.done = 0;
        }

        public String getName()
        {
            return name;
        }

        public int getToDo()
        {
            return toDo;
        }

        public int getDone()
        {
            return done;
        }

        public void done( int done )
        {
            this.done += done;
        }
    }
}
