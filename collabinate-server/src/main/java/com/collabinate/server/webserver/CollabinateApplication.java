package com.collabinate.server.webserver;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.restlet.security.Authenticator;

import com.collabinate.server.engine.CollabinateAdmin;
import com.collabinate.server.engine.CollabinateReader;
import com.collabinate.server.engine.CollabinateWriter;
import com.collabinate.server.resources.FeedResource;
import com.collabinate.server.resources.FollowingEntityResource;
import com.collabinate.server.resources.StreamEntryResource;
import com.collabinate.server.resources.StreamResource;
import com.collabinate.server.resources.TraceResource;

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
		
		// primary router is the in-bound root - the first router
		Router primaryRouter = new Router(getContext());

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
		resourceRouter.attach("/users/{userId}/following/{entityId}",
				FollowingEntityResource.class);
		resourceRouter.attach("/users/{userId}/feed", FeedResource.class);
		
		authenticator.setNext(resourceRouter);
		
		return primaryRouter;
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
