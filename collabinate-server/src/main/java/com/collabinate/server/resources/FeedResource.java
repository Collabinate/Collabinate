package com.collabinate.server.resources;

import org.restlet.ext.atom.Content;
import org.restlet.ext.atom.Entry;
import org.restlet.ext.atom.Feed;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.collabinate.server.CollabinateReader;
import com.collabinate.server.StreamEntry;

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
	@Get
	public Feed getFeed()
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
		
		// build a new Atom feed
		Feed feed = new Feed();
		feed.setTitle(userId);
		
		// loop over the stream entries for the feed and add them
		for (StreamEntry entry : reader.getFeed(tenantId, userId, start, count))
		{
			Entry atomEntry = new Entry();
			atomEntry.setId(entry.getId());
			atomEntry.setPublished(entry.getTime().toDate());
			
			Content content = new Content();
			StringRepresentation representation = 
					new StringRepresentation(entry.getContent().toCharArray());
			content.setInlineContent(representation);
			
			atomEntry.setContent(content);
			
			feed.getEntries().add(atomEntry);
		}
		
		return feed;
	}
	
	private static final int DEFAULT_COUNT = 20;
}
