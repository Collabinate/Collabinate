package com.collabinate.server.resources;

import org.joda.time.DateTime;
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
 * Restful resource representing a user like of an activity.
 * 
 * @author mafuba
 *
 */
public class LikeResource extends ServerResource
{
	@Get("json")
	public String getLike()
	{
		// extract necessary information from the context
		CollabinateReader reader = (CollabinateReader)getContext()
				.getAttributes().get("collabinateReader");
		String tenantId = getAttribute("tenantId");
		String userId = getAttribute("userId");
		String entityId = getAttribute("entityId");
		String activityId = getAttribute("activityId");
		
		DateTime likeDate =
			reader.userLikesActivity(tenantId, userId, entityId, activityId);
		
		if (null != likeDate)
		{
			setStatus(Status.SUCCESS_OK);
			return createLikeActivity(userId, LIKE, activityId, likeDate)
					.toString();
		}
		else
		{
			// TODO: set error message
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return null;
		}
	}
	
	@Put
	public void likeActivity()
	{
		// extract necessary information from the context
		CollabinateReader reader = (CollabinateReader)getContext()
				.getAttributes().get("collabinateReader");
		CollabinateWriter writer = (CollabinateWriter)getContext()
				.getAttributes().get("collabinateWriter");
		String tenantId = getAttribute("tenantId");
		String userId = getAttribute("userId");
		String entityId = getAttribute("entityId");
		String activityId = getAttribute("activityId");
		
		if (null == reader.getActivity(tenantId, entityId, activityId))
		{
			// TODO: set error message
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return;
		}
		
		writer.likeActivity(tenantId, userId, entityId, activityId);
		setStatus(Status.SUCCESS_OK);
	}
	
	@Delete
	public void unlikeActivity()
	{
		// extract necessary information from the context
		CollabinateReader reader = (CollabinateReader)getContext()
				.getAttributes().get("collabinateReader");
		CollabinateWriter writer = (CollabinateWriter)getContext()
				.getAttributes().get("collabinateWriter");
		String tenantId = getAttribute("tenantId");
		String userId = getAttribute("userId");
		String entityId = getAttribute("entityId");
		String activityId = getAttribute("activityId");
		
		if (null == reader.getActivity(tenantId, entityId, activityId))
		{
			// TODO: set error message
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return;
		}
		
		writer.unlikeActivity(tenantId, userId, entityId, activityId);
		setStatus(Status.SUCCESS_OK);
	}
	
	/**
	 * Creates an activity to represent a change to a like relationship.
	 * 
	 * @param userId The user who started or stopped liking.
	 * @param verb Like or unlike.
	 * @param activityId The object that was liked or unliked.
	 * @param published The time of the activity.
	 * 
	 * @return An appropriately structured activity that captures the like
	 * change.
	 */
	protected Activity createLikeActivity(
			String userId, String verb, String activityId, DateTime published)
	{
		Activity activity = new Activity();
		
		ActivityStreamsObject actor = new ActivityStreamsObject();
		actor.setId(userId);
		activity.setActor(actor);
		
		ActivityStreamsObject object = new ActivityStreamsObject();
		object.setId(activityId);
		activity.setObject(object);
		
		activity.setVerb(verb);
		
		activity.setPublished(published);
		
		return activity;
	}
	
	protected static final String LIKE = "like";
	protected static final String UNLIKE = "unlike";
}
