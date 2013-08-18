package com.collabinate.server;

import org.restlet.data.Status;
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
		setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		return "";
	}
	
	@Put
	public void createFollowRelationship()
	{
		setStatus(Status.SUCCESS_CREATED);
	}
}
