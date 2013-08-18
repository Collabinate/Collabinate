package com.collabinate.server;

import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.atom.Entry;
import org.restlet.ext.atom.Feed;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

/**
 * Restful resource representing a series of stream entries for an entity.
 * 
 * @author mafuba
 *
 */
public class StreamResource extends ServerResource
{
	@Get
	public Feed getStream()
	{
		CollabinateReader reader = (CollabinateReader)getContext()
				.getAttributes().get("collabinateReader");
		String entityId = getAttribute("entityId");
		String startString = getQueryValue("start");
		String countString = getQueryValue("count");
		long start = null == startString ? 0 : Long.parseLong(startString);
		int count = null == countString ? DEFAULT_COUNT : 
			Integer.parseInt(countString);
		
		Feed feed = new Feed();
		feed.setTitle(entityId);
		
		for (StreamEntry entry : reader.getStream(entityId, start, count))
		{
			Entry atomEntry = new Entry();
			atomEntry.setSummary(entry.getContent());
			feed.getEntries().add(atomEntry);
		}
		
		return feed;
	}
	
	@Post
	public void addEntry(String entryContent)
	{
		CollabinateWriter writer = (CollabinateWriter)getContext()
				.getAttributes().get("collabinateWriter");
		
		if (null == writer)
			throw new IllegalStateException(
					"Context does not contain a CollabinateWriter");
		
		StreamEntry entry = new StreamEntry(null, null, entryContent);
		
		writer.addStreamEntry(getAttribute("entityId"), entry);
		
		if (null != entryContent)
			getResponse().setEntity(entry.getContent(), 
					getRequest().getEntity().getMediaType());
		else
			getResponse().setEntity(entry.getContent(), MediaType.TEXT_PLAIN);
		
		setLocationRef(new Reference(getReference()).addSegment(entry.getId()));
		setStatus(Status.SUCCESS_CREATED);
	}
	
	private static final int DEFAULT_COUNT = 20;
}
