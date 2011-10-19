/*******************************************************************************
 * Copyright (c) 2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.sisu.locks;

import java.util.concurrent.Semaphore;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Local {@link ResourceLockFactory} implementation.
 */
@Named
@Singleton
public final class DefaultResourceLockFactory
    extends AbstractResourceLockFactory
{
    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    @Inject
    public DefaultResourceLockFactory()
    {
        this( true );
    }

    public DefaultResourceLockFactory( final boolean jmxEnabled )
    {
        super( jmxEnabled );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    @Override
    protected String category()
    {
        return "DefaultResourceLocks";
    }

    @Override
    protected ResourceLock createResourceLock( final String name )
    {
        return new DefaultResourceLock();
    }
}

/**
 * {@link ResourceLock} implemented on top of a JDK {@link Semaphore}.
 */
final class DefaultResourceLock
    extends AbstractSemaphoreResourceLock
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Semaphore sem;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    DefaultResourceLock()
    {
        sem = new Semaphore( Integer.MAX_VALUE, true );
    }

    // ----------------------------------------------------------------------
    // Semaphore methods
    // ----------------------------------------------------------------------

    @Override
    protected void acquire( final int permits )
    {
        sem.acquireUninterruptibly( permits );
    }

    @Override
    protected void release( final int permits )
    {
        sem.release( permits );
    }

    @Override
    protected int availablePermits()
    {
        return sem.availablePermits();
    }
}
