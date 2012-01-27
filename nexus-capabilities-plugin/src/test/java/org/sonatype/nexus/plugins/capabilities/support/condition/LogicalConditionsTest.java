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
package org.sonatype.nexus.plugins.capabilities.support.condition;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.plugins.capabilities.Condition;
import org.sonatype.nexus.plugins.capabilities.ConditionEvent;
import org.sonatype.nexus.plugins.capabilities.NexusEventBusTestSupport;

/**
 * {@link LogicalConditions} UTs.
 *
 * @since 2.0
 */
public class LogicalConditionsTest
    extends NexusEventBusTestSupport
{

    static final boolean UNSATISFIED = false;

    static final boolean SATISFIED = true;

    private LogicalConditions underTest;

    private Condition left;

    private Condition right;

    @Override
    @Before
    public void setUp()
        throws Exception
    {
        super.setUp();

        underTest = new LogicalConditions( eventBus );
        left = mock( Condition.class );
        right = mock( Condition.class );
    }

    public Condition prepare( final CompositeConditionSupport condition, boolean leftSatisfied,
                              boolean rightSatisfied )
    {
        condition.bind();

        when( left.isSatisfied() ).thenReturn( leftSatisfied );
        when( right.isSatisfied() ).thenReturn( rightSatisfied );

        if ( leftSatisfied )
        {
            condition.handle( new ConditionEvent.Satisfied( left ) );
        }
        else
        {
            condition.handle( new ConditionEvent.Unsatisfied( left ) );
        }

        if ( rightSatisfied )
        {
            condition.handle( new ConditionEvent.Satisfied( right ) );
        }
        else
        {
            condition.handle( new ConditionEvent.Unsatisfied( right ) );
        }

        return condition;
    }

    /**
     * Tests a logical AND between conditions.
     * <p/>
     * Condition is not satisfied when both operands are not satisfied.
     */
    @Test
    public void and01()
    {
        final Condition and =
            prepare( (CompositeConditionSupport) underTest.and( left, right ), UNSATISFIED, UNSATISFIED );
        assertThat( and.isSatisfied(), is( false ) );
    }

    /**
     * Tests a logical AND between conditions.
     * <p/>
     * Condition is not satisfied when left is unsatisfied and right is satisfied.
     */
    @Test
    public void and02()
    {
        final Condition and =
            prepare( (CompositeConditionSupport) underTest.and( left, right ), UNSATISFIED, SATISFIED );
        assertThat( and.isSatisfied(), is( false ) );
    }

    /**
     * Tests a logical AND between conditions.
     * <p/>
     * Condition is not satisfied when left is satisfied and right is unsatisfied.
     */
    @Test
    public void and03()
    {
        final Condition and =
            prepare( (CompositeConditionSupport) underTest.and( left, right ), SATISFIED, UNSATISFIED );
        assertThat( and.isSatisfied(), is( false ) );
    }

    /**
     * Tests a logical AND between conditions.
     * <p/>
     * Condition is satisfied when left is satisfied and right is satisfied.
     */
    @Test
    public void and04()
    {
        final Condition and =
            prepare( (CompositeConditionSupport) underTest.and( left, right ), SATISFIED, SATISFIED );
        assertThat( and.isSatisfied(), is( true ) );
    }

    /**
     * Tests a logical OR between conditions.
     * <p/>
     * Condition is not satisfied when both operands are not satisfied.
     */
    @Test
    public void or01()
    {
        final Condition or =
            prepare( (CompositeConditionSupport) underTest.or( left, right ), UNSATISFIED, UNSATISFIED );
        assertThat( or.isSatisfied(), is( false ) );
    }

    /**
     * Tests a logical OR between conditions.
     * <p/>
     * Condition is not satisfied when left is unsatisfied and right is satisfied.
     */
    @Test
    public void or02()
    {
        final Condition or =
            prepare( (CompositeConditionSupport) underTest.or( left, right ), UNSATISFIED, SATISFIED );
        assertThat( or.isSatisfied(), is( true ) );
    }

    /**
     * Tests a logical OR between conditions.
     * <p/>
     * Condition is satisfied when left is satisfied and right is unsatisfied.
     */
    @Test
    public void or03()
    {
        final Condition or =
            prepare( (CompositeConditionSupport) underTest.or( left, right ), SATISFIED, UNSATISFIED );
        assertThat( or.isSatisfied(), is( true ) );
    }

    /**
     * Tests a logical OR between conditions.
     * <p/>
     * Condition is satisfied when left is satisfied and right is satisfied.
     */
    @Test
    public void or04()
    {
        final Condition or = prepare( (CompositeConditionSupport) underTest.or( left, right ), SATISFIED, SATISFIED );
        assertThat( or.isSatisfied(), is( true ) );
    }

}
