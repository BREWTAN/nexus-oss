/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.rapture.internal;

import java.io.IOException;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.web.BaseUrlHolder;
import org.sonatype.sisu.goodies.template.TemplateParameters;

/**
 * Provides {@code /index.html}.
 *
 * @since 3.0
 */
@Named
@Singleton
public class IndexHtmlWebResource
    extends TemplateWebResource
{
  @Override
  public String getPath() {
    return "/index.html";
  }

  @Override
  public String getContentType() {
    return HTML;
  }

  @Override
  protected byte[] generate() throws IOException {
    return render("index.vm", new TemplateParameters()
        .set("baseUrl", BaseUrlHolder.get())
    );
  }
}
