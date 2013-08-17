package com.collabinate.server;

import org.restlet.Application;
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
	/**
	 * Sets the application properties.
	 */
	public CollabinateApplication()
	{
		setName("Collabinate");
	}
	
	@Override
	public Restlet createInboundRoot()
	{
		Router router = new Router(getContext());
		router.attach("/", TraceResource.class);
		router.attach("/{apiVersion}/{tenantId}/{entityId}/stream", StreamResource.class);
		
		return router;
	}
}
