package com.collabinate.server.resources;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;

import com.google.gson.JsonParser;

/**
 * Tests for the Stream Resource
 * 
 * @author mafuba
 * 
 */
public class StreamResourceTest extends GraphResourceTest
{
	@Test
	public void get_empty_stream_should_get_empty_atom_feed()
	{
		assertEquals(Status.SUCCESS_OK, get().getStatus());
	}
	
	@Test
	public void item_added_to_stream_should_return_201()
	{
		assertEquals(Status.SUCCESS_CREATED, post().getStatus());
	}
	
	@Test
	public void item_added_to_stream_should_create_and_return_child_location()
	{		
		assertEquals(
				getRequest(Method.POST, null).getResourceRef().getPath() + "/",
				post().getLocationRef().getParentRef().getPath());
	}
	
	@Test
	public void item_added_to_stream_should_have_entity_in_post_response_body()
	{
		String entityBody = "TEST";
		Response response = post(entityBody, MediaType.TEXT_PLAIN);
		
		assertEquals(entityBody, response.getEntityAsText());
	}
	
	@Test
	public void stream_should_be_json_object()
	{
		post("TEST", MediaType.TEXT_PLAIN);
		// parser will throw if result is not json
		new JsonParser().parse(get().getEntityAsText());
	}
	
	@Test
	public void item_added_to_stream_should_appear_in_stream()
	{
		String entityBody = "TEST";
		post(entityBody, MediaType.TEXT_PLAIN);
		
		assertThat(get().getEntityAsText(), containsString(entityBody));		
	}
	
	@Override
	protected String getResourcePath()
	{
		return "/1/tenant/entities/entity/stream";
	}
}
