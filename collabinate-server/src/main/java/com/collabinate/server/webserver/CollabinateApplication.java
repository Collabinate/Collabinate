package com.collabinate.server.webserver;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;
import org.restlet.security.Authenticator;

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
	public CollabinateApplication(CollabinateReader reader, 
			CollabinateWriter writer, Authenticator authenticator)
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
		if (null == reader || null == writer)
		{
			throw new IllegalStateException(
					"reader and writer must not be null");
		}
		getContext().getAttributes().put("collabinateReader", reader);
		getContext().getAttributes().put("collabinateWriter", writer);
		
		Router router = new Router(getContext());
		router.attach("/{apiVersion}/{tenantId}/entities/{entityId}/stream/{entryId}",
				StreamEntryResource.class);
		router.attach("/{apiVersion}/{tenantId}/entities/{entityId}/stream",
				StreamResource.class);
		router.attach(
				"/{apiVersion}/{tenantId}/users/{userId}/following/{entityId}",
				FollowingEntityResource.class);
		router.attach("/{apiVersion}/{tenantId}/users/{userId}/feed",
				FeedResource.class);
		
		// trace resource for client debugging
		router.attach("/trace", TraceResource.class);
		
		// directory resource for static content
		URL staticFolderUrl;
		try
		{
			staticFolderUrl = (new File("static")).toURI().toURL();
		}
		catch (MalformedURLException e)
		{
			throw new IllegalStateException("Could not find static content folder 'static'", e);
		}
		Directory directory = new Directory(getContext(), staticFolderUrl.toString());
		router.attach("/", directory);
		
		authenticator.setNext(router);
		return authenticator;
	}
}
