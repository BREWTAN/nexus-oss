/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.configuration.application.upgrade;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.configuration.upgrade.ConfigurationIsCorruptedException;
import org.sonatype.configuration.upgrade.SingleVersionUpgrader;
import org.sonatype.configuration.upgrade.UpgradeMessage;
import org.sonatype.nexus.configuration.model.v1_4_4.upgrade.BasicVersionConverter;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Upgrades configuration model from version 1.4.3 to 1.4.4.
 *
 * @author bdemers
 */
@Singleton
@Named("1.4.3")
public class Upgrade143to144
    extends ComponentSupport
    implements SingleVersionUpgrader
{
  private final File nexusWorkdir;

  @Inject
  public Upgrade143to144(final @Named("${nexus-work}") File nexusWorkdir) {
    this.nexusWorkdir = checkNotNull(nexusWorkdir);
  }

  @Override
  public Object loadConfiguration(File file)
      throws IOException, ConfigurationIsCorruptedException
  {
    FileReader fr = null;

    org.sonatype.nexus.configuration.model.v1_4_3.Configuration conf = null;

    try {
      // reading without interpolation to preserve user settings as variables
      fr = new FileReader(file);

      org.sonatype.nexus.configuration.model.v1_4_3.io.xpp3.NexusConfigurationXpp3Reader reader =
          new org.sonatype.nexus.configuration.model.v1_4_3.io.xpp3.NexusConfigurationXpp3Reader();

      conf = reader.read(fr);
    }
    catch (XmlPullParserException e) {
      throw new ConfigurationIsCorruptedException(file.getAbsolutePath(), e);
    }
    finally {
      if (fr != null) {
        fr.close();
      }
    }

    return conf;
  }

  @Override
  public void upgrade(UpgradeMessage message)
      throws ConfigurationIsCorruptedException
  {
    org.sonatype.nexus.configuration.model.v1_4_3.Configuration oldc =
        (org.sonatype.nexus.configuration.model.v1_4_3.Configuration) message.getConfiguration();

    org.sonatype.nexus.configuration.model.v1_4_4.Configuration newc =
        new BasicVersionConverter().convertConfiguration(oldc);

    // NEXUS-3833
    for (org.sonatype.nexus.configuration.model.v1_4_4.CScheduledTask task : newc.getTasks()) {
      if ("ReindexTask".equals(task.getType())) {
        task.setType("UpdateIndexTask");
      }
    }

    newc.setVersion(org.sonatype.nexus.configuration.model.v1_4_4.Configuration.MODEL_VERSION);
    message.setModelVersion(org.sonatype.nexus.configuration.model.v1_4_4.Configuration.MODEL_VERSION);
    message.setConfiguration(newc);
  }

  //

  /**
   * This "Frankenstein" method is actually redoing all that DefaultFSLocalRepositoryStorage class does, but it is
   * moved/copied here to be able to perform upgrade even if once in future we move that out to a plugin, or
   * fundamentally change it's behavior. This method here "freeze" the behavior of older Nexuses we are actually
   * upgrading.
   */
  protected File getRepositoryStorageBaseDirectory(String repositoryId, String urlStr)
      throws ConfigurationIsCorruptedException
  {
    if (StringUtils.isBlank(urlStr)) {
      // the  factory default
      return new File(new File( nexusWorkdir, "storage"), repositoryId);
    }
    else {
      // do the same as DefaultFSLocalRepositoryStorage does to interpret string url
      URL url;

      try {
        url = new URL(urlStr);
      }
      catch (MalformedURLException e) {
        try {
          url = new File(urlStr).toURI().toURL();
        }
        catch (MalformedURLException e1) {
          throw new ConfigurationIsCorruptedException("The local storage has a malformed URL as baseUrl!", e);
        }
      }

      File file;

      try {
        file = new File(url.toURI());
      }
      catch (Exception t) {
        file = new File(url.getPath());
      }

      if (file.exists()) {
        if (file.isFile()) {
          throw new ConfigurationIsCorruptedException("The repository (ID=\"" + repositoryId
              + "\") repository's baseDir is not a directory, path: " + file.getAbsolutePath());
        }
      }

      return file;
    }
  }

}
