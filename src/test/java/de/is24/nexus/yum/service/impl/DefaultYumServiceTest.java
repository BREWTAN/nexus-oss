package de.is24.nexus.yum.service.impl;

import static junit.framework.Assert.assertNotSame;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import java.io.File;
import java.net.URL;
import javax.inject.Inject;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import de.is24.nexus.yum.AbstractYumNexusTestCase;
import de.is24.nexus.yum.plugin.RepositoryRegistry;
import de.is24.nexus.yum.repository.YumRepository;
import de.is24.nexus.yum.service.YumService;


public class DefaultYumServiceTest extends AbstractYumNexusTestCase {
  private static final String REPO_BASE_URL = "http://localhost:8081/nexus/service/local/snapshots/1.0";
  private static final String VERSION_1_0 = "1.0";
  private static final String SNAPSHOTS = "snapshots";

  @Inject
  private YumService yumService;

  @Inject
  private RepositoryRegistry repositoryRegistry;

  @Before
  public void activateService() {
    yumService.activate();
  }

  @Test
  public void shouldCacheRepository() throws Exception {
    YumRepository repo1 = yumService.getRepository(createRepository(SNAPSHOTS), VERSION_1_0, new URL(REPO_BASE_URL));
    YumRepository repo2 = yumService.getRepository(createRepository(SNAPSHOTS), VERSION_1_0, new URL(REPO_BASE_URL));
    Assert.assertEquals(repo1, repo2);
  }

  @Test
  public void shouldRecreateRepository() throws Exception {
    YumRepository repo1 = yumService.getRepository(createRepository(SNAPSHOTS), VERSION_1_0, new URL(REPO_BASE_URL));

    yumService.markDirty(createRepository(SNAPSHOTS), VERSION_1_0);

    YumRepository repo2 = yumService.getRepository(createRepository(SNAPSHOTS), VERSION_1_0, new URL(REPO_BASE_URL));

    assertNotSame(repo1, repo2);
  }

  @Test
  public void shouldNotCreateYumRepo() throws Exception {
    yumService.deactivate();
    Assert.assertNull(yumService.createYumRepository(createRepository(SNAPSHOTS)));
  }

  @Test
  public void shouldNotFindRepository() throws Exception {
    Assert.assertNull(repositoryRegistry.findRepositoryForId("blablup"));
  }

  @Test
  public void shouldFindRepository() throws Exception {
    repositoryRegistry.registerRepository(createRepository(SNAPSHOTS));
    Assert.assertNotNull(repositoryRegistry.findRepositoryForId(SNAPSHOTS));
  }

  public static MavenRepository createRepository(String id) {
    MavenRepository repo = createMock(MavenRepository.class);
    expect(repo.getId()).andReturn(id).anyTimes();
    expect(repo.getLocalUrl()).andReturn(getTempUrl()).anyTimes();
    replay(repo);
    return repo;
  }

  private static String getTempUrl() {
    return new File(System.getProperty("java.io.tmpdir")).toURI().toString();
  }
}
