package com.collabinate.server.resources;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

import org.junit.Test;
import org.restlet.Request;
import org.restlet.data.Conditions;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.data.Tag;

import com.google.gson.JsonParser;

/**
 * Tests for the Feed Resource
 * 
 * @author mafuba
 * 
 */
public class FeedResourceTest extends GraphResourceTest
{
	@Test
	public void get_empty_feed_should_return_200()
	{
		assertEquals(Status.SUCCESS_OK, get().getStatus());
	}
	
	@Test
	public void get_should_return_json_content_type()
	{
		assertEquals(MediaType.APPLICATION_JSON,
				get().getEntity().getMediaType());
	}
	
	@Test
	public void get_response_should_contain_etag_header()
	{
		assertTrue(null != get().getEntity().getTag());
	}
	
	@Test
	public void etag_should_change_when_feed_changes()
	{
		Tag tag1 = get().getEntity().getTag();
		
		// add activity TEST to the stream of entity 1
		Request request = new Request(Method.POST,
				"riap://application/1/tenant/entities/entity1/stream");
		String entityBody1 = "TEST";
		request.setEntity(entityBody1, MediaType.TEXT_PLAIN);
		component.handle(request);
		// follow the entity
		request = new Request(Method.PUT,
				"riap://application/1/tenant/users/user/following/entity1");
		component.handle(request);

		Tag tag2 = get().getEntity().getTag();
		
		assertNotEquals(tag1, tag2);
	}
	
	@Test
	public void matching_etag_should_return_304()
	{
		Tag etag = get().getEntity().getTag();
		Request request = getRequest(Method.GET, null);
		Conditions conditions = new Conditions();
		conditions.getNoneMatch().add(etag);
		request.setConditions(conditions);
		
		assertEquals(Status.REDIRECTION_NOT_MODIFIED,
				getResponse(request).getStatus());
	}
	
	@Test
	public void non_matching_etag_should_return_200()
	{
		Request request = getRequest(Method.GET, null);
		Conditions conditions = new Conditions();
		conditions.getNoneMatch().add(new Tag("abc"));
		request.setConditions(conditions);
		
		assertEquals(Status.SUCCESS_OK, getResponse(request).getStatus());
	}
	
	@Test
	public void feed_should_be_json_object()
	{
		// add activity TEST to the stream of entity 1
		Request request = new Request(Method.POST,
				"riap://application/1/tenant/entities/entity1/stream");
		String entityBody1 = "TEST";
		request.setEntity(entityBody1, MediaType.TEXT_PLAIN);
		component.handle(request);
		// follow the entity
		request = new Request(Method.PUT,
				"riap://application/1/tenant/users/user/following/entity1");
		component.handle(request);

		// parser will throw if result is not json
		new JsonParser().parse(get().getEntityAsText());
	}
	
	@Test
	public void items_added_to_followed_entity_streams_should_appear_in_feed()
	{
		// add activity TEST-A to the stream of entity 1
		Request request = new Request(Method.POST,
				"riap://application/1/tenant/entities/entity1/stream");
		String entityBody1 = "TEST-A";
		request.setEntity(entityBody1, MediaType.TEXT_PLAIN);
		component.handle(request);
		
		// add activity TEST-B to the stream of entity 2
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
