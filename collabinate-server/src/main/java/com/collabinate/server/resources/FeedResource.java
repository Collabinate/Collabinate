package com.collabinate.server.resources;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.collabinate.server.engine.CollabinateReader;
import com.google.common.base.Joiner;

/**
 * Restful resource representing the collection of stream entries for all
 * entities followed by a user.
 * 
 * @author mafuba
 *
 */
public class FeedResource extends ServerResource
{
	// TODO: some much heavier content processing is going to need to happen
	// here to handle different XML and JSON representations of different
	// feed types (e.g. activity streams, OData, raw)
	@Get("json")
	public String getFeed()
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
				.join(reader.getFeed(tenantId, userId, start, count))
				+ "]}";
	}
	
	private static final int DEFAULT_COUNT = 20;
}
