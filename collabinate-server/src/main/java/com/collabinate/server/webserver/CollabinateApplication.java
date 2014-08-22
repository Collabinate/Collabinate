package com.collabinate.server.webserver;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.resource.Directory;
import org.restlet.routing.Filter;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.restlet.security.Authenticator;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.SecretVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.collabinate.server.Collabinate;
import com.collabinate.server.engine.CollabinateAdmin;
import com.collabinate.server.engine.CollabinateReader;
import com.collabinate.server.engine.CollabinateWriter;
import com.collabinate.server.resources.*;
import com.google.common.base.Splitter;

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
	 * Static logger.
	 */
	private static final Logger logger =
			LoggerFactory.getLogger(CollabinateApplication.class);
	
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
		adminRouter.attach("/tenants/{tenantId}/keys/{key}",
				TenantKeyResource.class);
		adminRouter.attach("/tenants/{tenantId}/keys",
				TenantKeysResource.class);
		adminRouter.attach("/service/resetrequest", ResetRequestResource.class);
		adminAuthenticator.setNext(adminRouter);
		
		// resource router handles the routing for post-authentication resources
		Router resourceRouter = new Router(getContext());
		resourceRouter.attach("/entities/{entityId}/stream/{activityId}",
				ActivityResource.class);
		resourceRouter.attach("/entities/{entityId}/stream",
				StreamResource.class);
		resourceRouter.attach("/entities/{entityId}/followers",
				FollowersResource.class);
		resourceRouter.attach(
				"/entities/{entityId}/stream/{activityId}/comments",
				CommentsResource.class);
		resourceRouter.attach(
				"/entities/{entityId}/stream/{activityId}/comments/{commentId}",
				CommentResource.class);
		resourceRouter.attach(
				"/entities/{entityId}/stream/{activityId}/likes",
				LikesResource.class);
		resourceRouter.attach("/users/{userId}/following",
				FollowingResource.class);
		resourceRouter.attach("/users/{userId}/following/{entityId}",
				FollowingEntityResource.class);
		resourceRouter.attach("/users/{userId}/feed", FeedResource.class);
		resourceRouter.attach("/users/{userId}/likes/{entityId}/{activityId}",
				LikeResource.class);
		
		// plugin resource paths skip the authenticator
		addPlugins(primaryRouter, resourceRouter);

		// normal resource paths go through the authenticator
		primaryRouter.attach("/{apiVersion}/{tenantId}", authenticator)
			.setMatchingMode(Template.MODE_STARTS_WITH);
		
		// trace resource for client debugging
		primaryRouter.attach("/trace", TraceResource.class);
		
		// directory resource for static content
		primaryRouter.attach("/", getStaticDirectoryResource());
		
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
				.getString(ADMIN_USERNAME, "");
		final String adminPassword = Collabinate.getConfiguration()
				.getString(ADMIN_PASSWORD, "");
		
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
	 * Loads filter plugins as specified in configuration.
	 * 
	 * @param primaryRouter The router which will pass control to the filter.
	 * @param resourceRouter The router to which the filter will attach.
	 */
	private void addPlugins(Router primaryRouter, Router resourceRouter)
	{
		// get the configuration strings that define the plugins
		String[] pluginsList = Collabinate.getConfiguration()
				.getStringArray(FILTER_PLUGINS);
		
		logger.debug("Found {} filter plugins", pluginsList.length);
		
		// loop over each plugin
		for (String pluginConfig : pluginsList)
		{
			// separate the class name from the URL pattern
			List<String> pluginValues = Splitter.on(";").omitEmptyStrings()
					.trimResults().splitToList(pluginConfig);
			
			if (2 != pluginValues.size())
			{
				logger.error("Invalid plugin configuration: {}", pluginConfig);
				continue;
			}
			
			String pluginClassName = pluginValues.get(0);
			String pluginUrlPattern = pluginValues.get(1);
			
			try
			{
				// instantiate the plugin
				Class<?> pluginClass = getClass().getClassLoader()
						.loadClass(pluginClassName);
				Constructor<?> constructor = pluginClass
						.getConstructor(Context.class, Configuration.class);
				Filter plugin = 
						(Filter)constructor.newInstance(
								getContext(), Collabinate.getConfiguration());
				
				// set up the routing path with the plugin
				plugin.setNext(resourceRouter);
				primaryRouter.attach(pluginUrlPattern, plugin);
				logger.info("Loaded plugin: {} for path: {}", pluginClassName,
						pluginUrlPattern);
			}
			catch (ClassNotFoundException e)
			{
				logger.error("Plugin class not found: " + pluginClassName, e);
			}
			catch (NoSuchMethodException e)
			{
				logger.error("Invalid constructor (requires Context, " +
					"Configuration) for plugin class: " + pluginClassName, e);
			}
			catch (InvocationTargetException e)
			{
				logger.error("Problem instantiating plugin: " +
					pluginClassName, e);
			}
			catch (IllegalAccessException e)
			{
				logger.error("Problem instantiating plugin: " +
					pluginClassName, e);
			}
			catch (InstantiationException e)
			{
				logger.error("Problem instantiating plugin: " +
					pluginClassName, e);
			}			
		}
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
	
	private static final String ADMIN_USERNAME =
			"collabinate.server.webserver.admin.username";
	private static final String ADMIN_PASSWORD =
			"collabinate.server.webserver.admin.password";
	private static final String FILTER_PLUGINS =
			"collabinate.server.webserver.filterplugins";
}
