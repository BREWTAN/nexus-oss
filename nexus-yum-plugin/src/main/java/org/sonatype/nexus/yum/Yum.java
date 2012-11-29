/*
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
package org.sonatype.nexus.yum;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.scheduling.ScheduledTask;

/**
 * Provides access to Yum functionality ariund a Nexus repository.
 *
 * @since 3.0
 */
public interface Yum
{

    static final long DEFAULT_DELETE_PROCESSING_DELAY = 10;

    /**
     * Configures if deletes from a Nexus repository should result in Yum metadata regeneration.
     *
     * @param processDeletes true if metadata should be regenerated
     * @return itself
     */
    Yum setProcessDeletes( boolean processDeletes );

    /**
     * Configures the delay between a delete and Yum metadata regeneration.
     *
     * @param numberOfSeconds delay in seconds
     * @return itself
     */
    Yum setDeleteProcessingDelay( final long numberOfSeconds );

    /**
     * @return true if metadata is regenerated after a delete from a Nexus repository
     */
    boolean shouldProcessDeletes();

    /**
     * @return number of seconds between a delete from a Nexus repository and metadata regeneration
     */
    long deleteProcessingDelay();

    /**
     * Configures an alias for a version.
     *
     * @param alias   alias name (cannot be null)
     * @param version to be aliased (cannot be null)
     * @return itself
     */
    Yum addAlias( String alias, String version );

    /**
     * Removes an alias.
     *
     * @param alias alias name (cannot be null)
     * @return itself
     */
    Yum removeAlias( String alias );

    /**
     * Resets aliases to provided mappings.
     *
     * @param aliases alias mappings (cannot be null)
     * @return itself
     */
    Yum setAliases( Map<String, String> aliases );

    /**
     * @param alias alias name
     * @return version mapped to provided alias, null if no mapping found
     */
    String getVersion( String alias );

    /**
     * @return associated Nexus repository (never null)
     */
    Repository getRepository();

    YumRepository getYumRepository( String version, URL repoBaseUrl )
        throws Exception;

    ScheduledTask<YumRepository> addToYumRepository( String path );

    void recreateRepository();

    void deleteRpm( String path );

    void deleteDirectory( String path );

    File getBaseDir();

    Set<String> getVersions();

    void addVersion( String version );

    void markDirty( String itemVersion );

}
