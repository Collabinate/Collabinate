package com.collabinate.server.resources;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.collabinate.server.engine.CollabinateReader;
import com.google.common.base.Joiner;

/**
 * Restful resource representing the entities followed by a user.
 * 
 * @author mafuba
 *
 */
public class FollowingResource extends ServerResource
{
	@Get("json")
	public String getFollowing()
	{
		// extract necessary information from the context
		CollabinateReader reader = (CollabinateReader)getContext()
				.getAttributes().get("collabinateReader");
		String tenantId = getAttribute("tenantId");
		String userId = getAttribute("userId");
		String skipString = getQueryValue("skip");
		String takeString = getQueryValue("take");
		long skip = null == skipString ? 0 : Long.parseLong(skipString);
		int take = null == takeString ? DEFAULT_TAKE : 
			Integer.parseInt(takeString);


		return "{\"items\":[" + Joiner.on(',')
				.join(reader.getFollowing(tenantId, userId, skip, take))
				+ "]}";
	}
	
	private static final int DEFAULT_TAKE = 20;
}
