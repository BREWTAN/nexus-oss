package de.is24.nexus.yum.rest;

import static org.restlet.data.MediaType.TEXT_PLAIN;
import static org.restlet.data.Method.POST;
import java.io.File;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.Path;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.FileRepresentation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
import org.sonatype.plexus.rest.resource.AbstractPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import de.is24.nexus.yum.service.AliasMapper;
import de.is24.nexus.yum.service.AliasNotFoundException;
import de.is24.nexus.yum.service.RepositoryAliasService;


/**
 * Resource providing an aliases view on the repositories provided by Nexus.
 * That means, that you can configure serveral aliases for artifact versions.
 * E.g. you can introduce "trunk", "testing" and "production" aliases for the
 * versions "91.0.0", "90.0.0" and "89.0.0" and can access the RPMs via
 * http://localhost:8080/nexus/service/local/yum-alias/<repo-id>/alias.rpm
 *
 * @author sherold
 *
 */
@Path(RepositoryVersionAliasResource.RESOURCE_URI)
@Singleton
public class RepositoryVersionAliasResource extends AbstractPlexusResource implements PlexusResource {
  private static final String ALLOW_ANONYMOUS = "anon";
  private static final String RPM_EXTENSION = ".rpm";
  public static final String URL_PREFIX = "yum/alias";
  private static final String PATH_PATTERN_TO_PROTECT = "/" + URL_PREFIX + "/*";
  public static final String REPOSITORY_ID_PARAM = "repositoryId";
  public static final String ALIAS_PARAM = "alias";
  public static final String RESOURCE_URI = "/" + URL_PREFIX + "/{" + REPOSITORY_ID_PARAM + "}/{" + ALIAS_PARAM + "}";

  @Inject
  @Named(RepositoryAliasService.DEFAULT_BEAN_NAME)
  private RepositoryAliasService repositoryAliasService;

  @Inject
  @Named(AliasMapper.DEFAULT_BEAN_NAME)
  private AliasMapper aliasMapper;

  public RepositoryVersionAliasResource() {
    setModifiable(true);
  }

  @Override
  public Object get(Context context, Request request, Response response, Variant variant) throws ResourceException {
    final String repositoryId = getAttributeAsString(request, REPOSITORY_ID_PARAM);
    String alias = getAttributeAsString(request, ALIAS_PARAM);
    if (alias == null) {
      throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Could not find empty alias");
    }

    try {
      if (alias.endsWith(RPM_EXTENSION)) {
        alias = alias.substring(0, alias.length() - RPM_EXTENSION.length());

        File rpmFile = repositoryAliasService.getFile(repositoryId, alias);
        return new FileRepresentation(rpmFile, new MediaType("application/x-rpm"));
      }

      return new StringRepresentation(aliasMapper.getVersion(repositoryId, alias));
    } catch (AliasNotFoundException e) {
      throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND,
        "Could not find alias " + alias + " for repository " + repositoryId, e);
    }
  }

  @Override
  public Object post(Context context, Request request, Response response, Object payload) throws ResourceException {
    final String repositoryId = getAttributeAsString(request, REPOSITORY_ID_PARAM);
    final String alias = getAttributeAsString(request, ALIAS_PARAM);

    if ((payload == null) || !String.class.isAssignableFrom(payload.getClass())) {
      throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please provide a valid artifact version.");
    }

    aliasMapper.setAlias(repositoryId, alias, payload.toString());

    return new StringRepresentation(payload.toString(), TEXT_PLAIN);
  }

  private String getAttributeAsString(final Request request, final String attrName) {
    final Object attrValue = request.getAttributes().get(attrName);
    return (attrValue != null) ? attrValue.toString() : null;
  }

  @Override
  public String getResourceUri() {
    return RESOURCE_URI;
  }

  @Override
  public PathProtectionDescriptor getResourceProtection() {
    return new PathProtectionDescriptor(PATH_PATTERN_TO_PROTECT, ALLOW_ANONYMOUS);
  }

  @Override
  public Object getPayloadInstance(Method method) {
    if (POST.equals(method)) {
      return new String();
    }
    return null;
  }

  @Override
  public Object getPayloadInstance() {
    return null;
  }

}
