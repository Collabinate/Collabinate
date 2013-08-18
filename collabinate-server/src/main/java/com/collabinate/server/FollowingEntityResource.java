package com.collabinate.server;

import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

/**
 * Restful resource representing a follow relationship between a user and an
 * entity.
 * 
 * @author mafuba
 *
 */
public class FollowingEntityResource extends ServerResource
{
	@Get
	public String getFollowRelationship()
	{
		// extract necessary information from the context
		CollabinateReader reader = (CollabinateReader)getContext()
				.getAttributes().get("collabinateReader");
		String userId = getAttribute("userId");
		String entityId = getAttribute("entityId");
		
		// test the follow relationship
		if (reader.isUserFollowingEntity(userId, entityId))
			setStatus(Status.SUCCESS_OK);
		else
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		
		// TODO: this has to be something other than empty string or else 204
		// results.  What body should this return?
		return entityId;
	}
	
	@Put
	public void createFollowRelationship()
	{
		// extract necessary information from the context
		CollabinateWriter writer = (CollabinateWriter)getContext()
				.getAttributes().get("collabinateWriter");
		String userId = getAttribute("userId");
		String entityId = getAttribute("entityId");
		
		// add the follow relationship
		writer.followEntity(userId, entityId);
		
		setStatus(Status.SUCCESS_CREATED);
	}
	
	@Delete
	public String removeFollowRelationship()
	{
		// extract necessary information from the context
		CollabinateWriter writer = (CollabinateWriter)getContext()
				.getAttributes().get("collabinateWriter");
		String userId = getAttribute("userId");
		String entityId = getAttribute("entityId");
		
		// remove the follow relationship
		writer.unfollowEntity(userId, entityId);
		
		setStatus(Status.SUCCESS_OK);
		
		// TODO: this has to be something other than empty string or else 204
		// results.  What body should this return?
		return userId;
	}
}
