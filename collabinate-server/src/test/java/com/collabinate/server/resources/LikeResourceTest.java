package com.collabinate.server.resources;

import static org.junit.Assert.*;

import org.junit.Test;
import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;

import com.collabinate.server.activitystreams.Activity;
import com.google.gson.JsonParser;

/**
 * Tests for the Like Resource
 * 
 * @author mafuba
 * 
 */
public class LikeResourceTest extends GraphResourceTest
{
	@Test
	public void get_like_for_non_existent_activity_should_return_404()
	{
		assertEquals(Status.CLIENT_ERROR_NOT_FOUND, get().getStatus());
	}

	@Test
	public void get_like_for_non_liked_activity_should_return_404()
	{
		addActivity();
		assertEquals(Status.CLIENT_ERROR_NOT_FOUND, get().getStatus());
	}
	
	@Test
	public void put_like_for_non_existent_activity_should_return_404()
	{
		assertEquals(Status.CLIENT_ERROR_NOT_FOUND, put().getStatus());
	}
	
	@Test
	public void put_like_for_existing_activity_should_return_204()
	{
		addActivity();
		assertEquals(Status.SUCCESS_NO_CONTENT, put().getStatus());
	}

	@Test
	public void get_like_for_liked_activity_should_return_200()
	{
		addActivity();
		put();
		assertEquals(Status.SUCCESS_OK, get().getStatus());
	}
	
	@Test
	public void get_should_return_json_content_type()
	{
		addActivity();
		put();
		
		assertEquals(MediaType.APPLICATION_JSON,
				get().getEntity().getMediaType());
	}
	
	@Test
	public void like_should_be_json_object()
	{
		addActivity();
		put();
		// parser will throw if result is not json
		new JsonParser().parse(get().getEntityAsText());
	}
	
	@Test
	public void like_should_contain_user_ID()
	{
		addActivity();
		put();
		
		Activity like = new Activity(get().getEntityAsText());
		
		assertEquals("user", like.getActor().getId());
	}
	
	@Test
	public void like_should_contain_activity_ID()
	{
		addActivity();
		put();
		
		Activity like = new Activity(get().getEntityAsText());
		
		assertEquals("activity", like.getObject().getId());
	}
	
	@Test
	public void delete_like_for_non_existent_activity_should_return_404()
	{
		assertEquals(Status.CLIENT_ERROR_NOT_FOUND, delete().getStatus());
	}
	
	@Test
	public void delete_like_for_existing_activity_should_return_204()
	{
		addActivity();
		assertEquals(Status.SUCCESS_NO_CONTENT, delete().getStatus());
	}
	
	@Test
	public void get_for_deleted_like_should_return_404()
	{
		addActivity();
		put();
		delete();
		
		assertEquals(Status.CLIENT_ERROR_NOT_FOUND, get().getStatus());
	}

	/**
	 * Prepares for like work by creating an activity.
	 */
	private void addActivity()
	{
		component.handle(new Request(Method.PUT,
			"riap://application/1/tenant/entities/entity/stream/activity"));
	}
	
	@Override
	protected String getResourcePath()
	{
		return "/1/tenant/users/user/likes/entity/activity";
	}
}
