package com.collabinate.server.adminresources;

import org.restlet.data.Status;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

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
					Thread.sleep(1000);
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
