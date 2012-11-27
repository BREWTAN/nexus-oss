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
package org.sonatype.nexus.plugins.capabilities.testsuite.client.internal;

import org.sonatype.nexus.capabilities.client.support.JerseyCapability;
import org.sonatype.nexus.client.rest.jersey.JerseyNexusClient;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityListItemResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityResource;
import org.sonatype.nexus.plugins.capabilities.testsuite.client.CapabilityA;

/**
 * @since 2.2
 */
public class JerseyCapabilityA
    extends JerseyCapability<CapabilityA>
    implements CapabilityA
{

    public JerseyCapabilityA( final JerseyNexusClient nexusClient )
    {
        super( nexusClient, "[a]" );
    }

    public JerseyCapabilityA( final JerseyNexusClient nexusClient, final CapabilityListItemResource resource )
    {
        super( nexusClient, resource );
    }

    @Override
    public String propertyA1()
    {
        return property( "a1" );
    }

    @Override
    public CapabilityA withPropertyA1( final String value )
    {
        withProperty( "a1", value );
        return this;
    }


}
