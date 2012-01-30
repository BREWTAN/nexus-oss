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
package org.sonatype.nexus.plugins.capabilities.internal.condition;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sonatype.nexus.plugins.capabilities.CapabilityIdentity.capabilityIdentity;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.plugins.capabilities.CapabilityContext;
import org.sonatype.nexus.plugins.capabilities.CapabilityEvent;
import org.sonatype.nexus.plugins.capabilities.CapabilityIdentity;
import org.sonatype.nexus.plugins.capabilities.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.EventBusTestSupport;

/**
 * {@link PassivateCapabilityDuringUpdateCondition} UTs.
 *
 * @since 2.0
 */
public class PassivateCapabilityDuringUpdateConditionTest
    extends EventBusTestSupport
{

    private CapabilityReference reference;

    private PassivateCapabilityDuringUpdateCondition underTest;

    private CapabilityRegistry capabilityRegistry;

    @Override
    @Before
    public void setUp()
        throws Exception
    {
        super.setUp();

        final CapabilityIdentity id = capabilityIdentity( "test" );

        capabilityRegistry = mock( CapabilityRegistry.class );
        reference = mock( CapabilityReference.class );

        final CapabilityContext context = mock( CapabilityContext.class );
        when( context.id() ).thenReturn( id );

        when( reference.context() ).thenReturn( context );

        underTest = new PassivateCapabilityDuringUpdateCondition( eventBus, id );
        underTest.bind();

        verify( eventBus ).register( underTest );
    }

    /**
     * Condition should become unsatisfied before update and satisfied after update.
     */
    @Test
    public void passivateDuringUpdate()
    {
        underTest.handle( new CapabilityEvent.BeforeUpdate( capabilityRegistry, reference ) );
        underTest.handle( new CapabilityEvent.AfterUpdate( capabilityRegistry, reference ) );

        verifyEventBusEvents( unsatisfied( underTest ), satisfied( underTest ) );
    }

    /**
     * Event bus handler is removed when releasing.
     */
    @Test
    public void releaseRemovesItselfAsHandler()
    {
        underTest.release();

        verify( eventBus ).unregister( underTest );
    }

}
