package com.collabinate.server.resources;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

import com.collabinate.server.activitystreams.Activity;
import com.collabinate.server.activitystreams.ActivityStreamsObject;
import com.collabinate.server.engine.CollabinateReader;
import com.collabinate.server.engine.CollabinateWriter;

/**
 * Restful resource representing a follow relationship between a user and an
 * entity.
 * 
 * @author mafuba
 *
 */
public class FollowingEntityResource extends ServerResource
{
	@Get("json")
	public String getFollowRelationship()
	{
		// extract necessary information from the context
		CollabinateReader reader = (CollabinateReader)getContext()
				.getAttributes().get("collabinateReader");
		String tenantId = getAttribute("tenantId");
		String userId = getAttribute("userId");
		String entityId = getAttribute("entityId");
		
		// test the follow relationship
		DateTime followed = reader.getDateTimeUserFollowedEntity(
				tenantId, userId, entityId);
		
		if (null != followed)
		{
			setStatus(Status.SUCCESS_OK);
			return createFollowActivity(userId, FOLLOW, entityId, followed)
					.toString();
		}
		
		setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		return null;
	}
	
	@Put
	public String createFollowRelationship()
	{
		// extract necessary information from the context
		CollabinateWriter writer = (CollabinateWriter)getContext()
				.getAttributes().get("collabinateWriter");
		String tenantId = getAttribute("tenantId");
		String userId = getAttribute("userId");
		String entityId = getAttribute("entityId");
		
		// add the follow relationship
		DateTime followed = DateTime.now(DateTimeZone.UTC);
		DateTime returnDate = 
				writer.followEntity(tenantId, userId, entityId, followed);
		
		if (returnDate.getMillis() == followed.getMillis())
			setStatus(Status.SUCCESS_CREATED);
		else
			setStatus(Status.SUCCESS_OK);
		
		return createFollowActivity(userId, FOLLOW, entityId, returnDate)
				.toString();
	}
	
	@Delete
	public String removeFollowRelationship()
	{
		// extract necessary information from the context
		CollabinateWriter writer = (CollabinateWriter)getContext()
				.getAttributes().get("collabinateWriter");
		String tenantId = getAttribute("tenantId");
		String userId = getAttribute("userId");
		String entityId = getAttribute("entityId");
		
		// remove the follow relationship
		DateTime followed = writer.unfollowEntity(tenantId, userId, entityId);
		
		if (null == followed)
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		else
			setStatus(Status.SUCCESS_OK);
		
		return createFollowActivity(
				userId,
				STOP_FOLLOWING,
				entityId,
				DateTime.now(DateTimeZone.UTC)).toString();
	}
	
	/**
	 * Creates an activity to represent a change to a follow relationship.
	 * 
	 * @param userId The user who started or stopped following.
	 * @param verb Follow or stop-following.
	 * @param entityId The object that was followed or stopped being followed.
	 * @param published The time of the activity.
	 * 
	 * @return An appropriately structured activity that captures the follow
	 * change.
	 */
	protected Activity createFollowActivity(
			String userId, String verb, String entityId, DateTime published)
	{
		Activity activity = new Activity();
		
		ActivityStreamsObject actor = new ActivityStreamsObject();
		actor.setId(userId);
		activity.setActor(actor);
		
		ActivityStreamsObject object = new ActivityStreamsObject();
		object.setId(entityId);
		activity.setObject(object);
		
		activity.setVerb(verb);
		
		activity.setPublished(published);
		
		return activity;
	}
	
	protected static final String FOLLOW = "follow";
	protected static final String STOP_FOLLOWING = "stop-following";
}
