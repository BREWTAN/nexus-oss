package org.sonatype.appcontext;

import java.io.PrintStream;
import java.util.Map;

import org.codehaus.plexus.interpolation.Interpolator;

/**
 * The generic app context, which is actually a Map. For modification, you can use only the {@link #put(String, Object)}
 * , {@link #putAll(Map)} and {@link #clear()} methods, since all the {@link #keySet()} {@link #values()} and
 * {@link #entrySet()} returns unmodifiable "views" only!
 * 
 * @author cstamas
 */
public interface AppContext
    extends Map<String, Object>
{
    /**
     * Returns the time stamp in milliseconds when this context was created.
     * 
     * @return the creation time in milliseconds.
     */
    long getCreated();

    /**
     * Returns the time stamp in milliseconds when this context was last modified.
     * 
     * @return the creation time in milliseconds.
     */
    long getModified();

    /**
     * Returns the id of this context.
     * 
     * @return
     */
    String getId();

    /**
     * Returns the parent app context if any, or {@code null} if this context is root context.
     * 
     * @return
     */
    AppContext getParent();

    /**
     * Flattens this AppContext (calculates "visible" entries from this and it's parent and returns a plain Map. This
     * map is not connected to AppContext anymore, and not modifiable! It is just a "snapshot".
     * 
     * @return
     */
    Map<String, Object> flatten();

    /**
     * Returns an interpolator using this app context as source.
     * 
     * @return
     */
    Interpolator getInterpolator();

    /**
     * Returns the entry value, used in creation of this context. Gives access to source marker and raw (uninterpolated)
     * values. Low level method!
     * 
     * @return
     */
    AppContextEntry getAppContextEntry( String key );

    /**
     * Flattens this AppContext (calculates "visible" entries from this and it's parent and returns a plain Map but with
     * AppContextEntries as values. This map is not connected to AppContext anymore, and not modifiable! It is just a
     * "snapshot". Low level method!
     * 
     * @return
     */
    Map<String, AppContextEntry> flattenAppContextEntries();

    /**
     * Dumps the complete AppContext (with hierarchy, sources) to default {@code System.out}. Low level method!
     */
    void dump();

    /**
     * Dumps the complete AppContext (with hierarchy, sources) to given PrintStream. Low level method!
     */
    void dump( PrintStream ps );
}
