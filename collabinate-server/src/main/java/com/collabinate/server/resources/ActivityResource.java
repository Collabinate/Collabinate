package com.collabinate.server.resources;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

import com.collabinate.server.activitystreams.Activity;
import com.collabinate.server.engine.CollabinateReader;
import com.collabinate.server.engine.CollabinateWriter;
import com.google.common.hash.Hashing;

/**
 * Restful resource representing a single activity for an entity.
 * 
 * @author mafuba
 *
 */
public class ActivityResource extends ServerResource
{
	@Get("json")
	public Representation getActivity()
	{
		// extract necessary information from the context
		CollabinateReader reader = (CollabinateReader)getContext()
				.getAttributes().get("collabinateReader");
		String tenantId = getAttribute("tenantId");
		String entityId = getAttribute("entityId");
		String activityId = getAttribute("activityId");

		Activity matchingActivity =
				reader.getActivity(tenantId, entityId, activityId);
		
		if (null != matchingActivity)
		{
			Representation representation = new StringRepresentation(
					matchingActivity.toString(), MediaType.APPLICATION_JSON);
			representation.setTag(
				new Tag(Hashing.murmur3_128().hashUnencodedChars(
				matchingActivity.toString()+tenantId+entityId+activityId)
				.toString(), false));
			
			return representation;
		}
		else
		{
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return null;
		}
	}
		
	@Put
	public void putActivity(String activityContent)
	{
		// extract necessary information from the context
		String tenantId = getAttribute("tenantId");
		String entityId = getAttribute("entityId");
		String activityId = getAttribute("activityId");
		CollabinateWriter writer = (CollabinateWriter)getContext()
				.getAttributes().get("collabinateWriter");
		
		if (null == writer)
			throw new IllegalStateException(
					"Context does not contain a CollabinateWriter");
		
		// remove any existing activity
		writer.deleteActivity(tenantId, entityId, activityId);
		
		// create an activity from the given content
		Activity activity = new Activity(activityContent);
		
		// ensure the activity has an id - set to given id if not
		String id = activity.getId();
		if (null == id || id.equals(""))
		{
			id = activityId;
			activity.setId(id);
		}
		
		// if the URL ID differs from the activity ID, the activity cannot be
		// processed
		if (!activityId.equals(id))
		{
			setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
			return;
		}
		
		writer.addActivity(tenantId, entityId, activity);
		
		// return the activity in the response body
		getResponse().setEntity(activity.toString(),
				MediaType.APPLICATION_JSON);
		
		setStatus(Status.SUCCESS_OK);
	}
	
	@Delete
	public void deleteActivity()
	{
		// extract necessary information from the context
		String tenantId = getAttribute("tenantId");
		String entityId = getAttribute("entityId");
		String activityId = getAttribute("activityId");
		CollabinateWriter writer = (CollabinateWriter)getContext()
				.getAttributes().get("collabinateWriter");
		
		if (null == writer)
			throw new IllegalStateException(
					"Context does not contain a CollabinateWriter");
		
		// remove any existing activity
		writer.deleteActivity(tenantId, entityId, activityId);
	}
}
