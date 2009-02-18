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
package org.sonatype.nexus.proxy.maven;

import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;
import org.sonatype.nexus.proxy.repository.DefaultRepositoryConfigurator;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;

public class AbstractMavenRepositoryConfigurator
    extends DefaultRepositoryConfigurator
{
    @Override
    public Repository updateRepositoryFromModel( Repository old, ApplicationConfiguration configuration,
        CRepository repo, RemoteStorageContext rsc, LocalRepositoryStorage ls, RemoteRepositoryStorage rs )
        throws InvalidConfigurationException
    {
        MavenRepository repository = (MavenRepository) super.updateRepositoryFromModel(
            old,
            configuration,
            repo,
            rsc,
            ls,
            rs );

        if ( CRepository.REPOSITORY_POLICY_RELEASE.equals( repo.getRepositoryPolicy() ) )
        {
            repository.setRepositoryPolicy( RepositoryPolicy.RELEASE );
        }
        else
        {
            repository.setRepositoryPolicy( RepositoryPolicy.SNAPSHOT );
        }

        if ( repository.getRepositoryKind().isFacetAvailable( MavenProxyRepository.class ) )
        {
            MavenProxyRepository mpr = repository.adaptToFacet( MavenProxyRepository.class );

            mpr.setReleaseMaxAge( repo.getArtifactMaxAge() );
            mpr.setSnapshotMaxAge( repo.getArtifactMaxAge() );
            mpr.setMetadataMaxAge( repo.getMetadataMaxAge() );
            mpr.setCleanseRepositoryMetadata( repo.isMaintainProxiedRepositoryMetadata() );
            mpr.setChecksumPolicy( ChecksumPolicy.fromModel( repo.getChecksumPolicy() ) );
        }

        return repository;
    }
}
