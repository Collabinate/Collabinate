package com.collabinate.server.resources;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;

/**
 * Tests for the Feed Resource
 * 
 * @author mafuba
 * 
 */
public class FeedResourceTest extends GraphResourceTest
{
	@Test
	public void get_empty_feed_should_get_empty_atom_feed()
	{
		assertEquals(Status.SUCCESS_OK, get().getStatus());
	}
	
	@Test
	public void items_added_to_followed_entity_streams_should_appear_in_feed()
	{
		// add entry TEST-A to the stream of entity 1
		Request request = new Request(Method.POST,
				"riap://application/1/tenant/entities/entity1/stream");
		String entityBody1 = "TEST-A";
		request.setEntity(entityBody1, MediaType.TEXT_PLAIN);
		component.handle(request);
		
		// add entry TEST-B to the stream of entity 2
		request = new Request(Method.POST,
				"riap://application/1/tenant/entities/entity2/stream");
		String entityBody2 = "TEST-B";
		request.setEntity(entityBody2, MediaType.TEXT_PLAIN);
		component.handle(request);
		
		// follow the entities
		request = new Request(Method.PUT,
				"riap://application/1/tenant/users/user/following/entity1");
		component.handle(request);
		request = new Request(Method.PUT,
				"riap://application/1/tenant/users/user/following/entity2");
		component.handle(request);
		
		// get the feed
		String responseText = get().getEntityAsText();
		
		assertThat(responseText, containsString(entityBody1));		
		assertThat(responseText, containsString(entityBody2));		
	}
	
	@Override
	protected String getResourcePath()
	{
		return "/1/tenant/users/user/feed";
	}
}
