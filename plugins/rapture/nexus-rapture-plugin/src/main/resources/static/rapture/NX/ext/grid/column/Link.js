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
/**
 * A {@link Ext.grid.column.Column} which renders its value as a link.
 *
 * @since 3.0
 */
Ext.define('NX.ext.grid.column.Link', {
  extend: 'Ext.grid.column.Column',
  alias: ['widget.nx-linkcolumn'],

  /**
   * Renders value as a link.
   */
  defaultRenderer: function (value) {
    var me = this;
    if (value) {
      return '<a href="' + value + '" target="' + me.target(value) + '">' + me.label(value) + '</a>';
    }
    return undefined;
  },

  /**
   * @protected
   */
  target: function (value) {
    return value;
  },

  /**
   * @protected
   */
  label: function (value) {
    return value;
  }

});