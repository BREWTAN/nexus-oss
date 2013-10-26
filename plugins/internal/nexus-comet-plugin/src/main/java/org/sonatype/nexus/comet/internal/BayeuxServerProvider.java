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
package org.sonatype.nexus.comet.internal;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.sisu.goodies.common.ComponentSupport;

import org.cometd.bayeux.server.BayeuxServer;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * {@link BayeuxServer} provider.
 *
 * @since 2.7
 */
@Named
@Singleton
public class BayeuxServerProvider
  extends ComponentSupport
  implements Provider<BayeuxServer>
{
  private final CometdServletImpl cometdServlet;

  @Inject
  public BayeuxServerProvider(final CometdServletImpl cometdServlet) {
    this.cometdServlet = checkNotNull(cometdServlet);
  }

  @Override
  public BayeuxServer get() {
    BayeuxServer server = cometdServlet.getBayeux();
    checkState(server != null);
    return server;
  }
}
