/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.proxy.storage.remote;

import org.sonatype.nexus.proxy.repository.RemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.RemoteConnectionSettings;
import org.sonatype.nexus.proxy.repository.RemoteProxySettings;
import org.sonatype.nexus.proxy.storage.StorageContext;

/**
 * The remote storage settings and context.
 * 
 * @author cstamas
 */
public interface RemoteStorageContext
    extends StorageContext
{
    boolean hasRemoteConnectionSettings();

    RemoteConnectionSettings getRemoteConnectionSettings();

    void setRemoteConnectionSettings( RemoteConnectionSettings settings );

    void removeRemoteConnectionSettings();

    boolean hasRemoteAuthenticationSettings();

    RemoteAuthenticationSettings getRemoteAuthenticationSettings();

    void setRemoteAuthenticationSettings( RemoteAuthenticationSettings settings );

    void removeRemoteAuthenticationSettings();

    boolean hasRemoteProxySettings();

    RemoteProxySettings getRemoteProxySettings();

    void setRemoteProxySettings( RemoteProxySettings settings );

    void removeRemoteProxySettings();
}
