package org.sonatype.nexus.plugins.mac;

import junit.framework.Assert;

import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.apache.maven.index.ArtifactContext;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.NexusIndexer;

public class MacPluginTest
    extends AbstractMacPluginTest
{
    @Override
    protected void prepareNexusIndexer( NexusIndexer nexusIndexer )
        throws Exception
    {
        context =
            nexusIndexer.addIndexingContext( "test-default", "test", repoDir, indexLuceneDir, null, null,
                DEFAULT_CREATORS );

        assertNull( context.getTimestamp() ); // unknown upon creation

        nexusIndexer.scan( context );

        assertNotNull( context.getTimestamp() );
    }

    public void testCatalog()
        throws Exception
    {
        ArchetypeCatalog catalog;

        MacRequest request = new MacRequest( context.getRepositoryId() );

        // get catalog
        catalog = macPlugin.listArcherypesAsCatalog( request, context );

        // repo has 3 artifacts indexed (plus 3 "internal" fields)
        Assert.assertTrue( "We have at least 3 Lucene documents in there for 3 artifacts!", context.getSize() >= 6 );

        // repo has only 1 archetype
        Assert.assertEquals( "Catalog not exact!", 1, catalog.getArchetypes().size() );

        // add one archetype
        ArtifactInfo artifactInfo =
            new ArtifactInfo( context.getRepositoryId(), "org.sonatype.nexus.plugins", "nexus-archetype-plugin", "1.0",
                null );
        artifactInfo.packaging = "maven-archetype";
        ArtifactContext ac = new ArtifactContext( null, null, null, artifactInfo, artifactInfo.calculateGav() );
        nexusIndexer.addArtifactToIndex( ac, context );

        // get catalog again
        catalog = macPlugin.listArcherypesAsCatalog( request, context );

        // repo has 4 artifacts indexed (plus 3 "internal" fields)
        Assert.assertTrue( "We have at least 4 Lucene documents in there for 3 artifacts!", context.getSize() >= 7 );

        // repo has only 2 archetypes
        Assert.assertEquals( "Catalog not exact!", 2, catalog.getArchetypes().size() );
    }
}
