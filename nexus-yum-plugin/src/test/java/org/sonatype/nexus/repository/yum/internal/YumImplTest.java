/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.yum.internal;

import static junit.framework.Assert.assertNotSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URL;
import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.repository.yum.YumRegistry;
import org.sonatype.nexus.repository.yum.YumRepository;
import org.sonatype.nexus.repository.yum.internal.utils.AbstractYumNexusTestCase;
import junit.framework.Assert;

public class YumImplTest
    extends AbstractYumNexusTestCase
{

    private static final String REPO_BASE_URL = "http://localhost:8081/nexus/service/local/snapshots/1.0";

    private static final String VERSION_1_0 = "1.0";

    private static final String SNAPSHOTS = "snapshots";

    @Inject
    private YumRegistry yumRegistry;

    private YumImpl yum;

    @Before
    public void activateService()
    {
        yum = (YumImpl) yumRegistry.register( createRepository( SNAPSHOTS ) );
    }

    @Test
    public void shouldCacheRepository()
        throws Exception
    {
        final YumRepository repo1 = yum.getYumRepository( VERSION_1_0, new URL( REPO_BASE_URL ) );
        final YumRepository repo2 = yum.getYumRepository( VERSION_1_0, new URL( REPO_BASE_URL ) );

        Assert.assertEquals( repo1, repo2 );
    }

    @Test
    public void shouldRecreateRepository()
        throws Exception
    {
        final YumRepository repo1 = yum.getYumRepository( VERSION_1_0, new URL( REPO_BASE_URL ) );

        yum.markDirty( VERSION_1_0 );

        YumRepository repo2 = yum.getYumRepository( VERSION_1_0, new URL( REPO_BASE_URL ) );

        assertNotSame( repo1, repo2 );
    }

    @Test
    public void shouldNotFindRepository()
        throws Exception
    {
        Assert.assertNull( yumRegistry.get( "blablup" ) );
    }

    @Test
    public void shouldFindRepository()
        throws Exception
    {
        yumRegistry.register( createRepository( SNAPSHOTS ) );
        Assert.assertNotNull( yumRegistry.get( SNAPSHOTS ) );
    }

    public static MavenRepository createRepository( String id )
    {
        final MavenRepository repo = mock( MavenRepository.class );
        when( repo.getId() ).thenReturn( id );
        when( repo.getLocalUrl() ).thenReturn( getTempUrl() );
        when( repo.getProviderRole() ).thenReturn( Repository.class.getName() );
        when( repo.getProviderHint() ).thenReturn( "maven2" );
        return repo;
    }

    private static String getTempUrl()
    {
        return new File( System.getProperty( "java.io.tmpdir" ) ).toURI().toString();
    }
}
