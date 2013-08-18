package com.collabinate.server;

import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
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
	public String getStream()
	{
		return "";
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
			getResponse().setEntity(entryContent, 
					getRequest().getEntity().getMediaType());
		else
			getResponse().setEntity(entry.getContent(), MediaType.TEXT_PLAIN);
		
		setLocationRef(new Reference(getReference()).addSegment(entry.getId()));
		setStatus(Status.SUCCESS_CREATED);
	}
}
