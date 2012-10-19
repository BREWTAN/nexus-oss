/**
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.security.web.guice;

import java.lang.reflect.Constructor;
import java.util.Enumeration;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.authz.Authorizer;
import org.apache.shiro.config.ConfigurationException;
import org.apache.shiro.guice.web.ShiroWebModule;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.web.filter.mgt.DefaultFilterChainManager;
import org.apache.shiro.web.filter.mgt.FilterChainManager;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.sonatype.guice.bean.locators.BeanLocator;
import org.sonatype.inject.BeanEntry;
import org.sonatype.inject.Mediator;
import org.sonatype.security.authentication.FirstSuccessfulModularRealmAuthenticator;
import org.sonatype.security.authorization.ExceptionCatchingModularRealmAuthorizer;
import org.sonatype.security.web.ProtectedPathManager;

import com.google.common.base.Throwables;
import com.google.inject.Key;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.name.Names;

/**
 * Extends ShiroWebModule to configure commonly set commponents such as SessionDAO, Authenticator, Authorizer, etc.
 * <p>
 * When {@link #useFilterChainManager} is {@code true} the {@link #addFilterChain} method has no affect; instead all
 * named filters bound in this application are injected into the {@link FilterChainManager} so they can be added to
 * filter chains programatically.
 * 
 * @since 2.7
 */
public class SecurityWebModule
    extends ShiroWebModule
{
    private final boolean useFilterChainManager;

    public SecurityWebModule( ServletContext servletContext, boolean useFilterChainManager )
    {
        super( servletContext );
        this.useFilterChainManager = useFilterChainManager;
    }

    @Override
    protected void configureShiroWeb()
    {
        bindRealm().to( EmptyRealm.class ); // not used in practice, just here to keep Shiro module happy

        // configure our preferred security components
        bind( SessionDAO.class ).to( EnterpriseCacheSessionDAO.class ).asEagerSingleton();
        bind( Authenticator.class ).to( FirstSuccessfulModularRealmAuthenticator.class ).in( Singleton.class );
        bind( Authorizer.class ).to( ExceptionCatchingModularRealmAuthorizer.class ).in( Singleton.class );
        bind( ProtectedPathManager.class ).to( SimpleProtectedPathManager.class ).in( Singleton.class );

        if ( useFilterChainManager )
        {
            // override the default resolver with one backed by a FilterChainManager using an injected filter map
            bind( FilterChainResolver.class ).toConstructor( ctor( PathMatchingFilterChainResolver.class ) ).asEagerSingleton();
            bind( FilterChainManager.class ).toProvider( FilterChainManagerProvider.class ).in( Singleton.class );
        }

        // bindings used by external modules
        expose( ProtectedPathManager.class );
        expose( FilterChainResolver.class );
    }

    @Override
    protected void bindWebSecurityManager( AnnotatedBindingBuilder<? super WebSecurityManager> bind )
    {
        // prefer the default constructor; we'll set the realms programatically
        bind( DefaultWebSecurityManager.class ).toConstructor( ctor( DefaultWebSecurityManager.class ) ).asEagerSingleton();

        // bind RealmSecurityManager and WebSecurityManager to _same_ component
        bind( RealmSecurityManager.class ).to( DefaultWebSecurityManager.class );
        bind.to( DefaultWebSecurityManager.class );

        // bindings used by external modules
        expose( RealmSecurityManager.class );
        expose( WebSecurityManager.class );
    }

    @Override
    protected void bindSessionManager( AnnotatedBindingBuilder<SessionManager> bind )
    {
        // use native web session management instead of delegating to servlet container
        bind.toConstructor( ctor( DefaultWebSessionManager.class ) ).asEagerSingleton();
    }

    /**
     * Binds the named {@link Filter} instance and exposes this binding to other modules.
     * 
     * @param name The filter name
     * @param filter The filter instance
     */
    protected void bindNamedFilter( String name, Filter filter )
    {
        Key<Filter> key = Key.get( Filter.class, Names.named( name ) );
        bind( key ).toInstance( filter );
        expose( key );
    }

    /**
     * Empty {@link Realm} - only used to satisfy Shiro's need for an initial realm binding.
     */
    @Singleton
    private static final class EmptyRealm
        implements Realm
    {
        public String getName()
        {
            return getClass().getName();
        }

        public boolean supports( AuthenticationToken token )
        {
            return false;
        }

        public AuthenticationInfo getAuthenticationInfo( AuthenticationToken token )
        {
            return null;
        }
    }

    /**
     * @return Public constructor with given parameterTypes; wraps checked exceptions
     */
    private static final <T> Constructor<T> ctor( Class<T> clazz, Class<?>... parameterTypes )
    {
        try
        {
            return clazz.getConstructor( parameterTypes );
        }
        catch ( Exception e )
        {
            Throwables.propagateIfPossible( e );
            throw new ConfigurationException( e );
        }
    }

    /**
     * Constructs a {@link DefaultFilterChainManager} from an injected {@link Filter} map.
     */
    private static final class FilterChainManagerProvider
        implements Provider<FilterChainManager>, Mediator<Named, Filter, FilterChainManager>
    {
        private final FilterConfig filterConfig;

        private final BeanLocator beanLocator;

        @Inject
        private FilterChainManagerProvider( @Named( "SHIRO" ) ServletContext servletContext,
                                            BeanLocator beanLocator )
        {
            // simple configuration so we can initialize filters as we add them
            this.filterConfig = new SimpleFilterConfig( "SHIRO", servletContext );
            this.beanLocator = beanLocator;
        }

        public FilterChainManager get()
        {
            FilterChainManager filterChainManager = new DefaultFilterChainManager( filterConfig );
            beanLocator.watch( Key.get( Filter.class ), this, filterChainManager );
            return filterChainManager;
        }

        public void add( final BeanEntry<Named, Filter> entry, final FilterChainManager manager )
        {
            manager.addFilter( entry.getKey().value(), entry.getValue(), true );
        }

        public void remove( final BeanEntry<Named, Filter> filter, final FilterChainManager manager )
        {
            // no-op
        }
    }

    /**
     * Simple {@link FilterConfig} that delegates to the surrounding {@link ServletContext}.
     */
    private static final class SimpleFilterConfig
        implements FilterConfig
    {
        private final String filterName;

        private final ServletContext servletContext;

        SimpleFilterConfig( String filterName, ServletContext servletContext )
        {
            this.filterName = filterName;
            this.servletContext = servletContext;
        }

        public String getFilterName()
        {
            return filterName;
        }

        public ServletContext getServletContext()
        {
            return servletContext;
        }

        public String getInitParameter( String name )
        {
            return servletContext.getInitParameter( name );
        }

        public Enumeration<?> getInitParameterNames()
        {
            return servletContext.getInitParameterNames();
        }
    }

    /**
     * Simpler wrapper around Shiro's {@link FilterChainManager}.
     */
    private static final class SimpleProtectedPathManager
        implements ProtectedPathManager
    {
        private final FilterChainManager filterChainManager;

        @Inject
        private SimpleProtectedPathManager( FilterChainManager filterChainManager )
        {
            this.filterChainManager = filterChainManager;
        }

        public void addProtectedResource( String pathPattern, String filterExpression )
        {
            this.filterChainManager.createChain( pathPattern, filterExpression );
        }
    }
}
