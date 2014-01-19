package com.collabinate.server.resources;

import org.joda.time.DateTime;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

import com.collabinate.server.StreamEntry;
import com.collabinate.server.activitystreams.Activity;
import com.collabinate.server.engine.CollabinateReader;
import com.collabinate.server.engine.CollabinateWriter;
import com.google.common.hash.Hashing;

/**
 * Restful resource representing a single stream entry for an entity.
 * 
 * @author mafuba
 *
 */
public class StreamEntryResource extends ServerResource
{
	@Get("json")
	public Representation getStreamEntry()
	{
		// extract necessary information from the context
		String tenantId = getAttribute("tenantId");
		String entityId = getAttribute("entityId");
		String entryId = getAttribute("entryId");

		StreamEntry matchingEntry = 
				findMatchingEntry(tenantId, entityId, entryId);
		
		if (null != matchingEntry)
		{
			Representation representation = new StringRepresentation(
					matchingEntry.getContent(), MediaType.APPLICATION_JSON);
			representation.setTag(
				new Tag(Hashing.murmur3_128().hashUnencodedChars(
				matchingEntry.getContent()+tenantId+entityId+entryId)
				.toString(), false));
			
			return representation;
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
	private StreamEntry findMatchingEntry(
			String tenantId, String entityId, String entryId)
	{
		// extract necessary information from the context
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
		
		// create an activity from the given content
		Activity activity = new Activity(entryContent);
		
		// ensure the activity has an id - set to given id if not
		String id = activity.getId();
		if (null == id || id.equals(""))
		{
			id = entryId;
			activity.setId(id);
		}
		
		// deal with id differences
		if (!entryId.equals(id))
		{
			// this is tricky - we will use the URL entry ID
			// for access, with the activity ID still in the
			// object,  so make access easier the URL ID will
			// be stored as a "collabinateObjectId"
			activity.setCollabinateObjectId(entryId);
		}
		
		// pull the existing or created date from the activity
		DateTime published = activity.getPublished();
		
		// create and add new entry
		StreamEntry entry =
				new StreamEntry(entryId, published, activity.toString());
		writer.addStreamEntry(tenantId, entityId, entry);
		
		// return the entry in the response body
		getResponse().setEntity(entry.getContent(), MediaType.APPLICATION_JSON);
		
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
