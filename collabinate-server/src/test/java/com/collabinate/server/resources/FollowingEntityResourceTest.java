package com.collabinate.server.resources;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Status;

/**
 * Tests for the following entity resource.
 * 
 * @author mafuba
 *
 */
public class FollowingEntityResourceTest extends GraphResourceTest
{	
	@Test
	public void getting_not_followed_entity_should_return_404()
	{
		assertEquals(Status.CLIENT_ERROR_NOT_FOUND, get().getStatus());
	}
	
	@Test
	public void following_entity_for_the_first_time_should_return_201()
	{
		assertEquals(Status.SUCCESS_CREATED, put().getStatus());
	}
	
	@Test
	public void following_entity_subsequent_times_should_return_200()
	{
		put();
		assertEquals(Status.SUCCESS_OK, put().getStatus());		
	}
	
	@Test
	public void getting_followed_entity_should_return_200()
	{
		put();
		assertEquals(Status.SUCCESS_OK, get().getStatus());
	}
	
	@Test
	public void get_should_return_json_content_type()
	{
		put();
		assertEquals(MediaType.APPLICATION_JSON,
				get().getEntity().getMediaType());
	}
	
	@Test
	public void put_should_return_json_content_type()
	{
		assertEquals(MediaType.APPLICATION_JSON,
				put().getEntity().getMediaType());
	}
	
	@Test
	public void unfollowing_entity_should_return_200()
	{
		put();
		assertEquals(Status.SUCCESS_OK, delete().getStatus());
	}
	
	@Test
	public void unfollowing_not_followed_entity_should_return_404()
	{
		assertEquals(Status.CLIENT_ERROR_NOT_FOUND, delete().getStatus());
	}
	
	@Test
	public void getting_unfollowed_entity_should_return_404()
	{
		put();
		delete();
		assertEquals(Status.CLIENT_ERROR_NOT_FOUND, get().getStatus());
	}

	@Override
	protected String getResourcePath()
	{
		return "/1/tenant/users/user/following/entity";
	}
}
