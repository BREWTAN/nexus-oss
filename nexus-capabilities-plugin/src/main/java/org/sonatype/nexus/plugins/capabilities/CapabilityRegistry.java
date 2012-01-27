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
package org.sonatype.nexus.plugins.capabilities;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.sonatype.configuration.validation.InvalidConfigurationException;
import com.google.common.base.Predicate;

/**
 * Registry of current configured capabilities.
 */
public interface CapabilityRegistry
{

    /**
     * Creates a new capability.
     *
     * @param type       type of capability to be created
     * @param enabled    whether or not created capability should be enabled
     * @param notes      optional capability notes (can be null)
     * @param properties optional capability properties (can be null)
     * @return reference to created capability
     * @throws InvalidConfigurationException If validation failed
     * @throws IOException                   If capabilities could not be stored
     */
    CapabilityReference add( CapabilityType type,
                             boolean enabled,
                             String notes,
                             Map<String, String> properties )
        throws InvalidConfigurationException, IOException;

    /**
     * Updates a capability.
     *
     * @param id         of capability to be updated
     * @param enabled    whether or not updated capability should be enabled
     * @param notes      optional capability notes (can be null)
     * @param properties optional capability properties (can be null)
     * @return reference to updated capability
     * @throws InvalidConfigurationException If validation failed
     * @throws IOException                   If capabilities could not be stored
     */
    CapabilityReference update( CapabilityIdentity id,
                                boolean enabled,
                                String notes,
                                Map<String, String> properties )
        throws InvalidConfigurationException, IOException;

    /**
     * Removes a capability.
     *
     * @param id of capability to be removed
     * @return reference of removed capability
     * @throws IOException If capabilities could not be stored
     */
    CapabilityReference remove( CapabilityIdentity id )
        throws IOException;

    /**
     * Enables a capability.
     *
     * @param id of capability to be enabled
     * @return reference to enabled capability
     * @throws InvalidConfigurationException If validation failed
     * @throws IOException                   If capabilities could not be stored
     */
    CapabilityReference enable( CapabilityIdentity id )
        throws InvalidConfigurationException, IOException;

    /**
     * Disables a capability.
     *
     * @param id of capability to be disabled
     * @return reference to disabled capability
     * @throws InvalidConfigurationException If validation failed
     * @throws IOException                   If capabilities could not be stored
     */
    CapabilityReference disable( CapabilityIdentity id )
        throws InvalidConfigurationException, IOException;

    /**
     * Retrieves the capability from registry with specified id. If there is no capability with specified id in the
     * registry it will return null.
     *
     * @param id to retrieve
     * @return capability with specified id or null if not found
     * @since 2.0
     */
    CapabilityReference get( CapabilityIdentity id );

    /**
     * Retrieves all capabilities from registry that matches the specified filter. If no capability exists or matches,
     * result will be empty.
     *
     * @param filter capability reference filter
     * @return collection of capabilities, never null
     * @since 2.0
     */
    public Collection<? extends CapabilityReference> get( Predicate<CapabilityReference> filter );

    /**
     * Retrieves all capabilities from registry. If no capability exists, result will be empty.
     *
     * @return collection of capabilities, never null
     * @since 2.0
     */
    Collection<? extends CapabilityReference> getAll();

}
