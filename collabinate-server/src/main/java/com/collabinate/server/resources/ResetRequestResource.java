package com.collabinate.server.resources;

import org.restlet.data.Status;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.collabinate.server.Collabinate;

/**
 * Restful resource representing a request to restart the service.
 * 
 * @author mafuba
 *
 */
public class ResetRequestResource extends ServerResource
{
	@Post
	public void createResetRequest()
	{
		setStatus(Status.SUCCESS_ACCEPTED);
		
		new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					Collabinate.resetService();
				}
				catch (Exception e)
				{
					throw new RuntimeException("Error resetting service", e);
				}
			};
		}.start();
	}
}
