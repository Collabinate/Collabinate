package com.collabinate.server;

import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;

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
		return new Restlet(){
			@Override
			public void handle(Request request, Response response) {
				String entity = "Method       : " + request.getMethod()
						+ "\nResource URI : "
						+ request.getResourceRef()
						+ "\nIP address   : "
						+ request.getClientInfo().getAddress()
						+ "\nAgent name   : "
						+ request.getClientInfo().getAgentName()
						+ "\nAgent version: "
						+ request.getClientInfo().getAgentVersion();
				response.setEntity(entity, MediaType.TEXT_PLAIN);
            }
		};
	}
}
