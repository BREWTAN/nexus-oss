/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.proxy.repository;

/**
 * This is default implementation of a repository. It supports age calculation, a repeated retrieval if item is found
 * locally but it's age is more then allowed.
 * 
 * @author cstamas
 */
public abstract class DefaultRepository
    extends AbstractProxyRepository
{
    private MutableProxyRepositoryKind repositoryKind;

    public RepositoryKind getRepositoryKind()
    {
        if ( repositoryKind == null )
        {
            repositoryKind = new MutableProxyRepositoryKind( this, null, new DefaultRepositoryKind(
                HostedRepository.class,
                null ), new DefaultRepositoryKind( ProxyRepository.class, null ) );
        }

        return repositoryKind;
    }
}
