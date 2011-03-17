package org.sonatype.security;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.shiro.ShiroException;
import org.apache.shiro.authc.pam.FirstSuccessfulStrategy;
import org.apache.shiro.authz.permission.RolePermissionResolver;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.apache.shiro.util.Initializable;
import org.slf4j.Logger;
import org.sonatype.inject.Nullable;
import org.sonatype.security.authentication.FirstSuccessfulModularRealmAuthenticator;
import org.sonatype.security.authorization.ExceptionCatchingModularRealmAuthorizer;

/**
 * Componentize the Shiro DefaultSecurityManager, and sets up caching.
 * 
 * @author Brian Demers
 */
@Singleton
@Typed( value = RealmSecurityManager.class )
@Named( value = "default" )
public class DefaultRealmSecurityManager
    extends DefaultSecurityManager
    implements Initializable
{
    private Logger logger;
    private RolePermissionResolver rolePermissionResolver;
    
    @Inject
    public DefaultRealmSecurityManager( Logger logger, @Nullable RolePermissionResolver rolePermissionResolver )
    {
        super();
        this.logger = logger;
        this.rolePermissionResolver = rolePermissionResolver;
        init();
    }
    
    @Override
    public void init()
        throws ShiroException
    {
        this.setSessionManager( new DefaultSessionManager() );

        // This could be injected
        // Authorizer
        ExceptionCatchingModularRealmAuthorizer authorizer =
            new ExceptionCatchingModularRealmAuthorizer( this.getRealms() );

        // if we have a Role Permission Resolver, set it, if not, don't worry about it
        if ( rolePermissionResolver != null )
        {
            authorizer.setRolePermissionResolver( rolePermissionResolver );
            logger.debug( "RolePermissionResolver was set to " + authorizer.getRolePermissionResolver() );
        }
        else
        {
            logger.warn( "No RolePermissionResolver is set" );
        }
        this.setAuthorizer( authorizer );

        // set the realm authenticator, that will automatically deligate the authentication to all the realms.
        FirstSuccessfulModularRealmAuthenticator realmAuthenticator = new FirstSuccessfulModularRealmAuthenticator();
        realmAuthenticator.setAuthenticationStrategy( new FirstSuccessfulStrategy() );

        // Authenticator
        this.setAuthenticator( realmAuthenticator );
    }
}
