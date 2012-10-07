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
package org.sonatype.nexus.plugins.yum.plugin.client.subsystem;

/**
 * Interface to access nexus-yum-plugin functionality in tests
 * 
 * @author sherold
 */
public interface YumClient
{

    /**
     * Retrieves the version behind the given alias.
     * 
     * @param repositoryId
     * @param alias
     * @return
     */
    String getAliasVersion( String repositoryId, String alias );

    /**
     * Creates for the given repository an alias to a specific
     * 
     * @param repositoryId
     * @param alias
     * @param version
     */
    void createOrUpdateAlias( String repositoryId, String alias, String version );

    /**
     * Retrieves the given metadata type (primary.xml, repomd.xml, etc.) from the repository
     * 
     * @param repositoryId
     * @param metadataType
     * @param typically {@link String} or <code>byte[].class</code>
     * @return the content of the metadata file. If the file is gzipped or bzipped the returning content is uncompressed
     *         automatically
     */
    <T> T getMetadata( String repositoryId, MetadataType metadataType, Class<T> returnType );

    /**
     * Retrieves the given metadata type (primary.xml, repomd.xml, etc.) from the repository and version
     * 
     * @param repositoryId
     * @param version
     * @param metadataType
     * @param typically {@link String} or <code>byte[].class</code>
     * @return the content of the metadata file. If the file is gzipped or bzipped the returning content is uncompressed
     *         automatically
     */
    <T> T getMetadata( String repositoryId, String version, MetadataType metadataType, Class<T> returnType );

    /**
     * Retrieves the given metadata type (primary.xml, repomd.xml, etc.) from the group repository
     * 
     * @param groupRepositoryId
     * @param metadataType
     * @param typically {@link String} or <code>byte[].class</code>
     * @return the content of the metadata file. If the file is gzipped or bzipped the returning content is uncompressed
     *         automatically
     */
    <T> T getGroupMetadata( String groupRepositoryId, MetadataType metadataType, Class<T> returnType );
}
