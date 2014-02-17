package com.collabinate.server.resources;

import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.collabinate.server.activitystreams.ActivityStreamsCollection;
import com.collabinate.server.engine.CollabinateReader;

/**
 * Restful resource representing a collection of likes for an activity.
 * 
 * @author mafuba
 *
 */
public class LikesResource extends ServerResource
{
	@Get("json")
	public String getLikes()
	{
		// extract necessary information from the context
		CollabinateReader reader = (CollabinateReader)getContext()
				.getAttributes().get("collabinateReader");
		String tenantId = getAttribute("tenantId");
		String entityId = getAttribute("entityId");
		String activityId = getAttribute("activityId");
		String skipString = getQueryValue("skip");
		String takeString = getQueryValue("take");
		int skip = null == skipString ? 0 : Integer.parseInt(skipString);
		int take = null == takeString ? DEFAULT_TAKE : 
			Integer.parseInt(takeString);

		ActivityStreamsCollection likes = reader.getLikes(
				tenantId, entityId, activityId, skip, take);
		
		if (null != likes)
		{
			setStatus(Status.SUCCESS_OK);
			return likes.toString();
		}
		else
		{
			// TODO: set error message
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return null;
		}
	}
	
	private static final int DEFAULT_TAKE = 20;
}
