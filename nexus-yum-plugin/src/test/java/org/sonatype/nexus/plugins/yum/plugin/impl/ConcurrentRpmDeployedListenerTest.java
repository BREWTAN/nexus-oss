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
 package org.sonatype.nexus.plugins.yum.plugin.impl;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;
import static org.sonatype.nexus.plugins.yum.repository.task.YumMetadataGenerationTask.ID;
import static org.sonatype.scheduling.TaskState.RUNNING;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.plugins.yum.AbstractRepositoryTester;
import org.sonatype.nexus.plugins.yum.config.YumConfiguration;
import org.sonatype.nexus.plugins.yum.plugin.ItemEventListener;
import org.sonatype.nexus.plugins.yum.plugin.RepositoryRegistry;
import org.sonatype.nexus.plugins.yum.repository.utils.RepositoryTestUtils;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStoreCreate;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.scheduling.ScheduledTask;

import com.google.code.tempusfugit.concurrency.ConcurrentRule;
import com.google.code.tempusfugit.concurrency.RepeatingRule;
import com.google.code.tempusfugit.concurrency.annotations.Concurrent;


/**
 * @author sherold
 * @author bvoss
 */
public class ConcurrentRpmDeployedListenerTest extends AbstractRepositoryTester {
  private static final Logger log = LoggerFactory.getLogger(ConcurrentRpmDeployedListenerTest.class);

  @Rule
  public ConcurrentRule concurrently = new ConcurrentRule();

  @Rule
  public RepeatingRule repeatedly = new RepeatingRule();

  @Inject
	private NexusScheduler nexusScheduler;

	@Inject
  private ItemEventListener listener;

  @Inject
  private RepositoryRegistry repositoryRegistry;

  @Inject
  private YumConfiguration yumConfig;

  @Before
  public void activateRepo() {
    yumConfig.setActive(true);
  }

  @After
  public void reactivateRepo() {
    yumConfig.setActive(true);
  }

  @Concurrent(count = 1)
  @Test
  public void shouldCreateRepoForPom() throws Exception {
    for (int j = 0; j < 5; j++) {
      shouldCreateRepoForRpm(j);
    }
    log.info("done");
  }

  private void shouldCreateRepoForRpm(int index) throws URISyntaxException, MalformedURLException,
    NoSuchAlgorithmException, IOException {
    MavenRepository repo = createRepository(true, "repo" + index);
    repositoryRegistry.registerRepository(repo);
    for (int version = 0; version < 5; version++) {
      assertNotMoreThan10ThreadForRpmUpload(repo, version);
    }
  }

  private void assertNotMoreThan10ThreadForRpmUpload(MavenRepository repo, int version) throws URISyntaxException,
    MalformedURLException, NoSuchAlgorithmException, IOException {
    String versionStr = version + ".1";
    File outputDirectory = new File(new URL(repo.getLocalUrl() + "/blalu/" +
        versionStr).toURI());
    File rpmFile = RepositoryTestUtils.createDummyRpm("test-artifact", versionStr, outputDirectory);

    StorageItem storageItem = createItem(versionStr, rpmFile.getName());

    listener.onEvent(new RepositoryItemEventStoreCreate(repo, storageItem));

		final int activeWorker = getRunningTasks();
		log.info("active worker: " + activeWorker);
    assertThat(activeWorker, is(lessThanOrEqualTo(10)));
  }

	private int getRunningTasks() {
		List<ScheduledTask<?>> tasks = nexusScheduler.getActiveTasks().get(ID);
		int count = 0;
		if (tasks != null) {
			for (ScheduledTask<?> task : tasks) {
				if (RUNNING.equals(task.getTaskState())) {
					count++;
				}
			}
		}
		return count;
	}
}
