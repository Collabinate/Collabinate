package com.collabinate.server.resources;

import org.restlet.data.MediaType;
import org.restlet.data.Tag;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.collabinate.server.engine.CollabinateReader;
import com.google.common.base.Joiner;
import com.google.common.hash.Hashing;

/**
 * Restful resource representing the collection of activities for all entities
 * followed by a user.
 * 
 * @author mafuba
 *
 */
public class FeedResource extends ServerResource
{
	@Get("json")
	public Representation getFeed()
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
		
		String result = "{\"items\":[" + Joiner.on(',')
				.join(reader.getFeed(tenantId, userId, skip, take))
				+ "]}";
		
		Representation representation = new StringRepresentation(
				result, MediaType.APPLICATION_JSON);
		representation.setTag(new Tag(Hashing.murmur3_128().hashUnencodedChars(
				result+tenantId+userId+skipString+takeString)
				.toString(), false));
		
		return representation;
	}
	
	private static final int DEFAULT_TAKE = 20;
}
