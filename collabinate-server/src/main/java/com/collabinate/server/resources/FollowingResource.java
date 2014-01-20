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
		String startString = getQueryValue("start");
		String countString = getQueryValue("count");
		long start = null == startString ? 0 : Long.parseLong(startString);
		int count = null == countString ? DEFAULT_COUNT : 
			Integer.parseInt(countString);


		return "{\"items\":[" + Joiner.on(',')
				.join(reader.getFollowing(tenantId, userId, start, count))
				+ "]}";
	}
	
	private static final int DEFAULT_COUNT = 20;
}
