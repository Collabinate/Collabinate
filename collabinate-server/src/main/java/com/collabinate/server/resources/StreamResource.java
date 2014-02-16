package com.collabinate.server.resources;

import java.util.UUID;

import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import com.collabinate.server.activitystreams.Activity;
import com.collabinate.server.engine.CollabinateReader;
import com.collabinate.server.engine.CollabinateWriter;
import com.google.common.hash.Hashing;

/**
 * Restful resource representing a series of activities for an entity.
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
		int start = null == startString ? 0 : Integer.parseInt(startString);
		int count = null == countString ? DEFAULT_COUNT : 
			Integer.parseInt(countString);
		
		String result = reader.getStream(tenantId, entityId, start, count)
				.toString();
		
		Representation representation = new StringRepresentation(
				result, MediaType.APPLICATION_JSON);
		representation.setTag(new Tag(Hashing.murmur3_128().hashUnencodedChars(
				result+tenantId+entityId+startString+countString)
				.toString(), false));
		
		return representation;
	}
	
	@Post
	public void addActivity(String content)
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
		Activity activity = new Activity(content);
		
		// generate an id and relocate the original if necessary
		String originalId = activity.getId();
		String id = generateId();
		activity.setId(id);
		
		if (null != originalId && !originalId.equals(""))
		{
			activity.setCollabinateValue(ORIGINAL_ID, originalId);
		}
		
		writer.addActivity(tenantId, entityId, activity);
		
		// return the activity in the response body
		getResponse().setEntity(activity.toString(),
				MediaType.APPLICATION_JSON);
		
		//TODO: return relative reference location
		setLocationRef(new Reference(getReference())
			.addSegment(activity.getId()));
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
	private static final String ORIGINAL_ID = "originalId";
}


