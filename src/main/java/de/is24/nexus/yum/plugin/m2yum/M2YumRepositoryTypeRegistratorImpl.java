package de.is24.nexus.yum.plugin.m2yum;

import org.codehaus.plexus.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.guice.plexus.config.Strategies;
import org.sonatype.nexus.plugins.RepositoryType;
import org.sonatype.nexus.proxy.registry.RepositoryTypeDescriptor;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import com.google.inject.Inject;
import com.google.inject.Singleton;


@Component(role = M2YumRepositoryTypeRegistrator.class, instantiationStrategy = Strategies.LOAD_ON_START)
@Singleton
public class M2YumRepositoryTypeRegistratorImpl implements M2YumRepositoryTypeRegistrator {
  private static final Logger LOG = LoggerFactory.getLogger(M2YumRepositoryTypeRegistratorImpl.class);

  @Inject
  private RepositoryTypeRegistry repositoryTypeRegistry;

  @Inject
  public void registerRepositoryType() {
    LOG.info("Try register my M2YumRepository to the RepositoryTypeRegistry");

    RepositoryTypeDescriptor description = new RepositoryTypeDescriptor(Repository.class, M2YumRepository.ID,
      "repositories",
      RepositoryType.UNLIMITED_INSTANCES);
    repositoryTypeRegistry.registerRepositoryTypeDescriptors(description);
  }
}
