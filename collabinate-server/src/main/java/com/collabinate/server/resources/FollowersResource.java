package com.collabinate.server.resources;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.collabinate.server.engine.CollabinateReader;

/**
 * Restful resource representing the users following an entity.
 * 
 * @author mafuba
 *
 */
public class FollowersResource extends ServerResource
{
	@Get("json")
	public String getFollowers()
	{
		// extract necessary information from the context
		CollabinateReader reader = (CollabinateReader)getContext()
				.getAttributes().get("collabinateReader");
		String tenantId = getAttribute("tenantId");
		String entityId = getAttribute("entityId");
		String skipString = getQueryValue("skip");
		String takeString = getQueryValue("take");
		int skip = null == skipString ? 0 : Integer.parseInt(skipString);
		int take = null == takeString ? DEFAULT_TAKE : 
			Integer.parseInt(takeString);

		return reader.getFollowers(tenantId, entityId, skip, take).toString();
	}
	
	private static final int DEFAULT_TAKE = 20;
}
