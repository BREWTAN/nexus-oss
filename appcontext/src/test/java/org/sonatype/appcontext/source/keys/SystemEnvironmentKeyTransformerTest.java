package org.sonatype.appcontext.source.keys;

import junit.framework.Assert;
import junit.framework.TestCase;

public class SystemEnvironmentKeyTransformerTest
    extends TestCase
{

    public void testSimple()
    {
        final SystemEnvironmentKeyTransformer transformer = new SystemEnvironmentKeyTransformer();

        Assert.assertEquals( "doodle", transformer.transform( "DOODLE" ) );
        Assert.assertEquals( "mavenHome", transformer.transform( "MAVEN_HOME" ) );
        Assert.assertEquals( "oneTwoThree", transformer.transform( "ONE_TWO_THREE" ) );
    }

    public void testSimpleWithPrefix()
    {
        final SystemEnvironmentKeyTransformer sysEnv = new SystemEnvironmentKeyTransformer();
        final PrefixRemovingKeyTransformer transformer = new PrefixRemovingKeyTransformer( "plexus" );

        Assert.assertEquals( "foo", transformer.transform( sysEnv.transform( "PLEXUS_FOO" ) ) );
        Assert.assertEquals( "someSetting", transformer.transform( sysEnv.transform( "PLEXUS_SOME_SETTING" ) ) );
        Assert.assertEquals( "oneTwoThree", transformer.transform( sysEnv.transform( "PLEXUS_ONE_TWO_THREE" ) ) );
    }

}
