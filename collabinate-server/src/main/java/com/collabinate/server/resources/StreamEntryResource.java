package com.collabinate.server.resources;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

import com.collabinate.server.StreamEntry;
import com.collabinate.server.engine.CollabinateReader;
import com.collabinate.server.engine.CollabinateWriter;

/**
 * Restful resource representing a single stream entry for an entity.
 * 
 * @author mafuba
 *
 */
public class StreamEntryResource extends ServerResource
{
	@Get("json")
	public String getStreamEntry()
	{
		StreamEntry matchingEntry = findMatchingEntry();
		
		if (null != matchingEntry)
		{
			return matchingEntry.getContent();
		}
		else
		{
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return null;
		}
	}
	
	/**
	 * Finds an entry that matches the tenantId, entityId, and entryId in the
	 * request.
	 * @return
	 */
	private StreamEntry findMatchingEntry()
	{
		// extract necessary information from the context
		String tenantId = getAttribute("tenantId");
		String entityId = getAttribute("entityId");
		String entryId = getAttribute("entryId");
		CollabinateReader reader = (CollabinateReader)getContext()
				.getAttributes().get("collabinateReader");
		
		StreamEntry matchingEntry = null;
		
		// loop over all stream entries to find the desired entity
		// TODO: this is highly inefficient for large feeds, it will be better
		// to have a method on the server to query by ID
		for (StreamEntry entry : 
			reader.getStream(tenantId, entityId, 0, Integer.MAX_VALUE))
		{
			if (entry.getId().equals(entryId))
				matchingEntry = entry;
		}
		
		return matchingEntry;
	}
	
	@Put
	public void putEntry(String entryContent)
	{
		// extract necessary information from the context
		String tenantId = getAttribute("tenantId");
		String entityId = getAttribute("entityId");
		String entryId = getAttribute("entryId");
		CollabinateWriter writer = (CollabinateWriter)getContext()
				.getAttributes().get("collabinateWriter");
		
		if (null == writer)
			throw new IllegalStateException(
					"Context does not contain a CollabinateWriter");
		
		// remove any existing entry
		writer.deleteStreamEntry(tenantId, entityId, entryId);
		
		// create and add new entry
		StreamEntry entry = new StreamEntry(entryId, null, entryContent);
		writer.addStreamEntry(tenantId, entityId, entry);
		
		// if there is no request entity return empty string with text type
		if (null != entryContent)
			getResponse().setEntity(entry.getContent(), 
					getRequest().getEntity().getMediaType());
		else
			getResponse().setEntity(entry.getContent(), MediaType.TEXT_PLAIN);
		
		setStatus(Status.SUCCESS_OK);
	}
	
	@Delete
	public void deleteEntry()
	{
		// extract necessary information from the context
		String tenantId = getAttribute("tenantId");
		String entityId = getAttribute("entityId");
		String entryId = getAttribute("entryId");
		CollabinateWriter writer = (CollabinateWriter)getContext()
				.getAttributes().get("collabinateWriter");
		
		if (null == writer)
			throw new IllegalStateException(
					"Context does not contain a CollabinateWriter");
		
		// remove any existing entry
		writer.deleteStreamEntry(tenantId, entityId, entryId);
	}
}
