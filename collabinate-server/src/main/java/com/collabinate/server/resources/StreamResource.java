package com.collabinate.server.resources;

import java.util.UUID;

import org.joda.time.DateTime;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import com.collabinate.server.StreamEntry;
import com.collabinate.server.activitystreams.Activity;
import com.collabinate.server.engine.CollabinateReader;
import com.collabinate.server.engine.CollabinateWriter;
import com.google.common.base.Joiner;
import com.google.common.hash.Hashing;

/**
 * Restful resource representing a series of stream entries for an entity.
 * 
 * @author mafuba
 *
 */
public class StreamResource extends ServerResource
{
	@Get("json")
	public Representation getStream()
	{
		// extract necessary information from the context
		CollabinateReader reader = (CollabinateReader)getContext()
				.getAttributes().get("collabinateReader");
		String tenantId = getAttribute("tenantId");
		String entityId = getAttribute("entityId");
		String startString = getQueryValue("start");
		String countString = getQueryValue("count");
		long start = null == startString ? 0 : Long.parseLong(startString);
		int count = null == countString ? DEFAULT_COUNT : 
			Integer.parseInt(countString);
		
		String result = "{\"items\":[" + Joiner.on(',')
				.join(reader.getStream(tenantId, entityId, start, count))
				+ "]}";
		
		Representation representation = new StringRepresentation(
				result, MediaType.APPLICATION_JSON);
		representation.setTag(new Tag(Hashing.murmur3_128().hashUnencodedChars(
				result+tenantId+entityId+startString+countString)
				.toString(), false));
		
		return representation;
	}
	
	@Post
	public void addEntry(String entryContent)
	{
		// extract necessary information from the context
		String tenantId = getAttribute("tenantId");
		String entityId = getAttribute("entityId");
		CollabinateWriter writer = (CollabinateWriter)getContext()
				.getAttributes().get("collabinateWriter");
		
		if (null == writer)
			throw new IllegalStateException(
					"Context does not contain a CollabinateWriter");
		
		// create an activity from the given content
		Activity activity = new Activity(entryContent);
		
		// ensure the activity has an id - generate if not
		String id = activity.getId();
		if (null == id || id.equals(""))
		{
			id = generateId();
			activity.setId(id);
		}
		
		// pull the existing or created date from the activity
		DateTime published = activity.getPublished();
		
		// create and add new entry
		StreamEntry entry =
				new StreamEntry(id, published, activity.toString());
		writer.addStreamEntry(tenantId, entityId, entry);
		
		// return the entry in the response body
		getResponse().setEntity(entry.getContent(), MediaType.APPLICATION_JSON);
		
		//TODO: return relative reference location
		setLocationRef(new Reference(getReference()).addSegment(entry.getId()));
		setStatus(Status.SUCCESS_CREATED);
	}
	
	/**
	 * Generates an ID for an activity.
	 * 
	 * @return A globally unique URI acceptable for use in an activity ID.
	 */
	private String generateId()
	{
		// TODO: allow this to be configured
		return "tag:collabinate.com:" + UUID.randomUUID().toString();
	}
	
	private static final int DEFAULT_COUNT = 20;
}


