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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.eventbus.NexusEventBus;
import org.sonatype.nexus.plugins.capabilities.Condition;
import org.sonatype.nexus.plugins.capabilities.support.condition.ConditionSupport;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.nexus.proxy.events.NexusStoppedEvent;
import com.google.common.eventbus.Subscribe;

/**
 * A condition that is satisfied when nexus is active.
 *
 * @since 2.0
 */
@Named
@Singleton
public class NexusIsActiveCondition
    extends ConditionSupport
    implements Condition, NexusEventBus.LoadOnStart
{

    @Inject
    NexusIsActiveCondition( final NexusEventBus eventBus )
    {
        super( eventBus, false );
        bind();
    }

    @Subscribe
    public void handle( final NexusStartedEvent event )
    {
        setSatisfied( true );
    }

    @Subscribe
    public void handle( final NexusStoppedEvent event )
    {
        setSatisfied( false );
    }

    @Override
    protected void doBind()
    {
        getEventBus().register( this );
    }

    @Override
    protected void doRelease()
    {
        getEventBus().unregister( this );
    }

    @Override
    public String toString()
    {
        return "Nexus is active";
    }

    @Override
    public String explainSatisfied()
    {
        return "Nexus is active";
    }

    @Override
    public String explainUnsatisfied()
    {
        return "Nexus is not active";
    }
}
