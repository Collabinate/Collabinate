package com.collabinate.server.webserver;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.restlet.security.Authenticator;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.SecretVerifier;

import com.collabinate.server.Collabinate;
import com.collabinate.server.engine.CollabinateAdmin;
import com.collabinate.server.engine.CollabinateReader;
import com.collabinate.server.engine.CollabinateWriter;
import com.collabinate.server.resources.*;

/**
 * Main Restlet application
 * 
 * @author mafuba
 *
 */
public class CollabinateApplication extends Application
{
	private CollabinateReader reader;
	private CollabinateWriter writer;
	private CollabinateAdmin admin;
	private Authenticator authenticator;
	
	/**
	 * Sets the application properties.
	 */
	public CollabinateApplication(
			CollabinateReader reader, 
			CollabinateWriter writer,
			CollabinateAdmin admin,
			Authenticator authenticator)
	{
		if (null == reader)
			throw new IllegalArgumentException("reader must not be null");
		
		if (null == writer)
			throw new IllegalArgumentException("writer must not be null");
		
		setName("Collabinate");
		this.reader = reader;
		this.writer = writer;
		this.admin = admin;
		this.authenticator = authenticator;
	}
	
	@Override
	public Restlet createInboundRoot()
	{
		if (null == reader || null == writer || null == authenticator)
		{
			throw new IllegalStateException(
					"reader, writer, and authenticator must not be null");
		}
		getContext().getAttributes().put("collabinateReader", reader);
		getContext().getAttributes().put("collabinateWriter", writer);
		if (null != admin)
			getContext().getAttributes().put("collabinateAdmin", admin);
		
		// primary router is the in-bound root - the first router
		Router primaryRouter = new Router(getContext());
		
		// admin resources are handled specially
		Authenticator adminAuthenticator = getAdminAuthenticator();
		primaryRouter.attach("/{apiVersion}/admin", adminAuthenticator)
			.setMatchingMode(Template.MODE_STARTS_WITH);
		
		Router adminRouter = new Router(getContext());
		adminRouter.attach("/database/export", DatabaseExportResource.class);
		adminRouter.attach("/tenants/{tenantId}", TenantResource.class);
		adminRouter.attach("/tenants/{tenantId}/keys",
				TenantKeysResource.class);
		adminAuthenticator.setNext(adminRouter);

		// normal resource paths go through the authenticator
		primaryRouter.attach("/{apiVersion}/{tenantId}", authenticator)
			.setMatchingMode(Template.MODE_STARTS_WITH);
		
		// trace resource for client debugging
		primaryRouter.attach("/trace", TraceResource.class);
		
		// directory resource for static content
		primaryRouter.attach("/", getStaticDirectoryResource());
		
		// resource router handles the routing for post-authentication resources
		Router resourceRouter = new Router(getContext());
		resourceRouter.attach("/entities/{entityId}/stream/{entryId}",
				StreamEntryResource.class);
		resourceRouter.attach("/entities/{entityId}/stream",
				StreamResource.class);
		resourceRouter.attach("/entities/{entityId}/followers",
				FollowersResource.class);
		resourceRouter.attach("/users/{userId}/following",
				FollowingResource.class);
		resourceRouter.attach("/users/{userId}/following/{entityId}",
				FollowingEntityResource.class);
		resourceRouter.attach("/users/{userId}/feed", FeedResource.class);
		
		authenticator.setNext(resourceRouter);
		
		return primaryRouter;
	}
	
	/**
	 * Provides an authenticator for administration resources.
	 * 
	 * @return An authenticator for securing administration resources.
	 */
	private Authenticator getAdminAuthenticator()
	{
		final String adminUsername = Collabinate.getConfiguration()
				.getString("adminUsername", "");
		final String adminPassword = Collabinate.getConfiguration()
				.getString("adminPassword", "");
		
		// only authenticate if the admin credentials are configured
		if (adminUsername.equals(""))
		{
			return new Authenticator(null) {
				@Override
				protected boolean authenticate(Request request, Response response)
				{
					return true;
				}
			};
		}
		
		return new ChallengeAuthenticator(
				getContext(),
				false,
				ChallengeScheme.HTTP_BASIC,
				"Collabinate",
				new SecretVerifier() {
					
					@Override
					public int verify(String identifier, char[] secret)
					{
						return ((adminUsername.equals(identifier)) &&
								compare(adminPassword.toCharArray(), secret)) ?
									RESULT_VALID : RESULT_INVALID;
					}
				});
	}

	/**
	 * Creates a directory resource used for static content.
	 * 
	 * @return A Directory used for static content.
	 */
	private Directory getStaticDirectoryResource()
	{
		URL staticFolderUrl;
		try
		{
			staticFolderUrl = (new File("static")).toURI().toURL();
		}
		catch (MalformedURLException e)
		{
			throw new IllegalStateException(
					"Could not find static content folder 'static'", e);
		}
		return new Directory(getContext(), staticFolderUrl.toString());		
	}
}
