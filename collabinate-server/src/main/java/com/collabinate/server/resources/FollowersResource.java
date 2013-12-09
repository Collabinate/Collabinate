package com.collabinate.server.resources;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.collabinate.server.engine.CollabinateReader;
import com.google.common.base.Joiner;

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

		return "{\"items\":[" + Joiner.on(',')
				.join(reader.getFollowers(tenantId, entityId))
				+ "]}";
	}
}
