package com.collabinate.server;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

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
	
	/**
	 * Sets the application properties.
	 */
	public CollabinateApplication(CollabinateReader reader, 
			CollabinateWriter writer)
	{
		if (null == reader)
			throw new IllegalArgumentException("reader must not be null");
		
		if (null == writer)
			throw new IllegalArgumentException("writer must not be null");
		
		setName("Collabinate");
		//setContext(new Context());
		this.reader = reader;
		this.writer = writer;
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
		router.attach("/", TraceResource.class);
		router.attach("/{apiVersion}/{tenantId}/entities/{entityId}/stream/{entryId}",
				StreamEntryResource.class);
		router.attach("/{apiVersion}/{tenantId}/entities/{entityId}/stream",
				StreamResource.class);
		router.attach(
				"/{apiVersion}/{tenantId}/users/{userId}/following/{entityId}",
				FollowingEntityResource.class);
		router.attach("/{apiVersion}/{tenantId}/users/{userId}/feed",
				FeedResource.class);
		
		return router;
	}
	
	@Override
	public synchronized void stop() throws Exception
	{
		reader = null;
		writer = null;
		//setContext(null);
		getContext().getAttributes().put("collabinateReader", null);
		getContext().getAttributes().put("collabinateWriter", null);
		super.stop();
	}
}
