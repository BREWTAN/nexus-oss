package org.sonatype.appcontext.source;

import java.util.ArrayList;
import java.util.List;

import org.sonatype.appcontext.source.filter.FilteredEntrySource;
import org.sonatype.appcontext.source.filter.KeyEqualityEntryFilter;
import org.sonatype.appcontext.source.filter.KeyPrefixEntryFilter;
import org.sonatype.appcontext.source.keys.ConfigurableSystemEnvironmentKeyTransformer;
import org.sonatype.appcontext.source.keys.KeyTransformer;
import org.sonatype.appcontext.source.keys.KeyTransformingEntrySource;
import org.sonatype.appcontext.source.keys.NoopKeyTransformer;
import org.sonatype.appcontext.source.keys.PrefixRemovingKeyTransformer;

/**
 * Helper to create various EntrySources.
 * 
 * @author cstamas
 */
public final class Sources
{
    private Sources()
    {
    }

    /**
     * Creates a properly ordered list of EntrySources with variations to be used as "default" entry sources. Order is
     * system env then system properties. It also takes care of "aliases", ordering the before "id" to make "id"
     * override aliases if needed.
     * 
     * @param context
     * @return
     */
    public static List<EntrySource> getDefaultSources( final String id, final List<String> aliases )
    {
        ArrayList<EntrySource> result = new ArrayList<EntrySource>( 2 );

        for ( String alias : aliases )
        {
            result.add( getPrefixTargetedEntrySource( new SystemEnvironmentEntrySource(),
                new ConfigurableSystemEnvironmentKeyTransformer( '-' ), alias + "-" ) );
        }

        result.add( getPrefixTargetedEntrySource( new SystemEnvironmentEntrySource(),
            new ConfigurableSystemEnvironmentKeyTransformer( '-' ), id + "-" ) );

        for ( String alias : aliases )
        {
            result.add( getPrefixTargetedEntrySource( new SystemPropertiesEntrySource(), alias + "." ) );
        }

        result.add( getPrefixTargetedEntrySource( new SystemPropertiesEntrySource(), id + "." ) );

        return result;
    }

    /**
     * Gets a targeted entry source (see {@link #getTargetedEntrySource(EntrySource, KeyTransformer, String)} for
     * complete description) that uses {@link NoopKeyTransformer}.
     * 
     * @param source
     * @param prefix
     * @return
     */
    public static EntrySource getPrefixTargetedEntrySource( final EntrySource source, final String prefix )
    {
        return getPrefixTargetedEntrySource( source, new NoopKeyTransformer(), prefix );
    }

    /**
     * Returns a "targeted entry source" that does:
     * <ul>
     * <li>performs key transformation with supplied key transformer</li>
     * <li>filters keys with supplied prefix</li>
     * <li>performs a key transformation with prefixRemoving transformer</li>
     * </ul>
     * Hence, if you have an entry source having keys "myapp.foo" and "bar", and you pass in prefix "myapp." (not the
     * ending dot!), the resulting entry sources will deliver only one key, the "foo" (filtering discareded "bar", it
     * does not have prefix "myapp.", and prefixRemoving key transformation stripped off "myapp." prefix). This is a
     * special case of FilteredEntrySource that "cherry-picks" based on prefix (so far does same as FilteredEntrySource
     * with KeyPrefixEntryFilter), but also removes the matched prefix from the matched keys before putting it into
     * result.
     * 
     * @param source
     * @param transformer
     * @param prefix
     * @return
     */
    public static EntrySource getPrefixTargetedEntrySource( final EntrySource source, final KeyTransformer transformer,
                                                            final String prefix )
    {
        final KeyTransformingEntrySource transformingEntrySource = new KeyTransformingEntrySource( source, transformer );

        final FilteredEntrySource filteredEntrySource =
            new FilteredEntrySource( transformingEntrySource, new KeyPrefixEntryFilter( prefix ) );

        return new KeyTransformingEntrySource( filteredEntrySource, new PrefixRemovingKeyTransformer( prefix ) );
    }

    /**
     * Returns a "targeted entry source" that does:
     * <ul>
     * <li>performs key transformation with supplied key transformer</li>
     * <li>filters (cherry-picks) keys with supplied keys (equality)</li>
     * <li>does not performs a key transformation</li>
     * </ul>
     * Hence, if you have an entry source having keys "foo" and "bar", and you pass in keys [foo], the resulting entry
     * sources will deliver only one key, the "foo" (filtering discareded "bar"). This is a special case of
     * FilteredEntrySource that "cherry-picks" based on keys (so far does same as FilteredEntrySource with
     * KeyEqualityEntryFilter).
     * 
     * @param source
     * @param transformer
     * @param prefix
     * @return
     */
    public static EntrySource getSelectTargetedEntrySource( final EntrySource source, final KeyTransformer transformer,
                                                            final String... keys )
    {
        if ( transformer != null )
        {
            final KeyTransformingEntrySource transformingEntrySource =
                new KeyTransformingEntrySource( source, transformer );

            return new FilteredEntrySource( transformingEntrySource, new KeyEqualityEntryFilter( keys ) );
        }
        else
        {
            return new FilteredEntrySource( source, new KeyEqualityEntryFilter( keys ) );
        }
    }
}
