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
 * Restful resource representing the collection of stream entries for all
 * entities followed by a user.
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
		String startString = getQueryValue("start");
		String countString = getQueryValue("count");
		long start = null == startString ? 0 : Long.parseLong(startString);
		int count = null == countString ? DEFAULT_COUNT : 
			Integer.parseInt(countString);
		
		String result = "{\"items\":[" + Joiner.on(',')
				.join(reader.getFeed(tenantId, userId, start, count))
				+ "]}";
		
		Representation representation = new StringRepresentation(
				result, MediaType.APPLICATION_JSON);
		representation.setTag(new Tag(Hashing.murmur3_128().hashUnencodedChars(
				result+tenantId+userId+startString+countString)
				.toString(), false));
		
		return representation;
	}
	
	private static final int DEFAULT_COUNT = 20;
}
