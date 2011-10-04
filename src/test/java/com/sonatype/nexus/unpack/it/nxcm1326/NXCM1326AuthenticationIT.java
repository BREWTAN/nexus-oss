/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.unpack.it.nxcm1326;

import java.io.IOException;

import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.test.utils.PrivilegesMessageUtil;
import org.sonatype.nexus.test.utils.RoleMessageUtil;
import org.sonatype.nexus.test.utils.UserMessageUtil;
import org.sonatype.security.rest.model.RoleResource;
import org.sonatype.security.rest.model.UserResource;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sonatype.nexus.unpack.it.AbstractUnpackIT;
import com.thoughtworks.xstream.XStream;

public class NXCM1326AuthenticationIT
    extends AbstractUnpackIT
{
    @BeforeClass
    public void setSecurity()
    {
        TestContainer.getInstance().getTestContext().setSecureTest( true );
        XStream xstream = this.getXMLXStream();

        this.userUtil = new UserMessageUtil( this, xstream, MediaType.APPLICATION_XML );
        this.roleUtil = new RoleMessageUtil( this, xstream, MediaType.APPLICATION_XML );
        this.privUtil = new PrivilegesMessageUtil( this, xstream, MediaType.APPLICATION_XML );
    }

    @BeforeMethod
    public void setupUserPrivs()
        throws IOException
    {
        UserResource user = this.userUtil.getUser( "test-user" );
        user.addRole( "repo-all-full" );
        this.userUtil.updateUser( user );

    }

    @Test
    public void invalidUser()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().setUsername( "dummy" );
        TestContainer.getInstance().getTestContext().setPassword( "dummy" );

        try
        {
            getDeployUtils().deployWithWagon( "http",
                                              nexusBaseUrl + "service/local/repositories/" + REPO_TEST_HARNESS_REPO
                                                  + "/content-compressed", getTestFile( "bundle.zip" ), "" );
            Assert.fail( "Authentication should fail!!!" );
        }
        catch ( org.apache.maven.wagon.TransferFailedException e )
        {
            Assert.assertTrue( e.getMessage().contains( "401" ) );
        }
    }

    @Test( dependsOnMethods = "invalidUser" )
    public void withoutPrivsUser()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        try
        {
            getDeployUtils().deployWithWagon( "http",
                                              nexusBaseUrl + "service/local/repositories/" + REPO_TEST_HARNESS_REPO
                                                  + "/content-compressed", getTestFile( "bundle.zip" ), "" );
            Assert.fail( "Authentication should fail!!!" );
        }
        catch ( org.apache.maven.wagon.authorization.AuthorizationException e )
        {
        }
    }

    @Test( dependsOnMethods = { "withoutPrivsUser" } )
    public void okUser()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        addPrivilege( "test-user", "repository-" + REPO_TEST_HARNESS_REPO, "content-compressed" );

        getDeployUtils().deployWithWagon( "http",
                                          nexusBaseUrl + "service/local/repositories/" + REPO_TEST_HARNESS_REPO
                                              + "/content-compressed", getTestFile( "bundle.zip" ), "" );
    }

    protected void addPrivilege( String userId, String privilege, String... privs )
        throws IOException
    {
        TestContainer.getInstance().getTestContext().useAdminForRequests();

        RoleResource role = roleUtil.findRole( privilege + "-role" );
        boolean create = false;
        if ( role == null )
        {
            role = new RoleResource();
            create = true;
        }
        role.setId( privilege + "-role" );
        role.setName( privilege + "-name" );
        role.addPrivilege( privilege );
        for ( String priv : privs )
        {
            role.addPrivilege( priv );
        }
        role.setDescription( privilege );
        role.setSessionTimeout( 100 );
        if ( create )
        {
            this.roleUtil.createRole( role );
        }
        else
        {
            RoleMessageUtil.update( role );
        }

        UserResource testUser = this.userUtil.getUser( userId );
        testUser.addRole( role.getId() );
        this.userUtil.updateUser( testUser );
    }

    protected UserMessageUtil userUtil;

    protected RoleMessageUtil roleUtil;

    protected PrivilegesMessageUtil privUtil;
}
