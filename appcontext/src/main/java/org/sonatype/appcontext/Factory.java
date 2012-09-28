package org.sonatype.appcontext;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.sonatype.appcontext.internal.InternalFactory;
import org.sonatype.appcontext.publisher.PrintStreamEntryPublisher;
import org.sonatype.appcontext.publisher.Slf4jLoggerEntryPublisher;
import org.sonatype.appcontext.source.Sources;

/**
 * A factory for creating {@link AppContext} instances.
 * 
 * @author cstamas
 */
public class Factory
{
    private static final List<String> EMPTY = Collections.emptyList();

    /**
     * Creates a "default" request with given ID. Creates a "default" request, with all the default sources and
     * publishers. The request will have passed in ID, default sources are coming from
     * {@link Sources#getDefaultSources(String, List)} and {@link PrintStreamEntryPublisher} or
     * {@link Slf4jLoggerEntryPublisher} publisher, depending is SLF4J detected on class path or not.
     * 
     * @param id
     * @return the request to continue work with
     */
    public static AppContextRequest getDefaultRequest( final String id )
    {
        return getDefaultRequest( id, null );
    }

    /**
     * Creates a "default" request with given ID and given parent app context. See {@link #getDefaultRequest(String)}
     * for sources and publishers.
     * 
     * @param id
     * @param parent
     * @return the request to continue work with
     */
    public static AppContextRequest getDefaultRequest( final String id, final AppContext parent )
    {
        return getDefaultRequest( id, parent, EMPTY );
    }

    /**
     * Creates a "default" request with given ID and given parent app context and given "aliases" (aliases are used in
     * harvesting the sources, for prefix matching only). See {@link #getDefaultRequest(String)} for sources and
     * publishers.
     * 
     * @param id
     * @param parent
     * @param aliases
     * @param keyInclusions
     * @return the request to continue work with
     */
    public static AppContextRequest getDefaultRequest( final String id, final AppContext parent,
                                                       final List<String> aliases, final String... keyInclusions )
    {
        return InternalFactory.getDefaultAppContextRequest( id, parent, aliases, keyInclusions );
    }

    /**
     * Creates AppContext instance from the given request.
     * 
     * @param request
     * @return the created {@link AppContext} instance.
     * @throws AppContextException
     */
    public static AppContext create( final AppContextRequest request )
        throws AppContextException
    {
        return InternalFactory.create( request );
    }

    /**
     * Creates AppContext instance out of the supplied map. This method is usable in tests or any other places where
     * quickly an AppContext is needed without all the fuss about sourcing and publishing the entries. This method will
     * NOT interpolate anything, it will just create a context from supplied map as is.
     * 
     * @param id the ID of the app context
     * @param parent the parent of the appcontext or {@code null}
     * @param map the map to use as source for app context.
     * @return the created {@link AppContext} instance.
     * @throws AppContextException
     */
    public static AppContext create( final String id, final AppContext parent, final Map<String, Object> map )
        throws AppContextException
    {
        return InternalFactory.create( id, parent, map );
    }
}
