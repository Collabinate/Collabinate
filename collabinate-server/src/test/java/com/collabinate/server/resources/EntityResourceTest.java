package com.collabinate.server.resources;

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
 * Tests for the Activity Resource
 * 
 * @author mafuba
 * 
 */
public class EntityResourceTest extends GraphResourceTest
{	
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
	public void etag_should_change_when_entity_changes()
	{
		Tag tag1 = get().getEntity().getTag();
		// add an activity
		Request request = new Request(Method.POST,
				"riap://application/1/tenant/entities/entity/stream");
		component.handle(request);
		Tag tag2 = get().getEntity().getTag();
		
		assertNotEquals(tag1, tag2);
	}
	
	@Test
	public void matching_etag_should_return_304_for_get()
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
	public void non_matching_etag_should_return_200_for_get()
	{
		Request request = getRequest(Method.GET, null);
		Conditions conditions = new Conditions();
		conditions.getNoneMatch().add(new Tag("abc"));
		request.setConditions(conditions);
		
		assertEquals(Status.SUCCESS_OK, getResponse(request).getStatus());
	}
	
	@Test
	public void non_matching_etag_should_return_412_for_delete()
	{
		Request request = getRequest(Method.DELETE, null);
		Conditions conditions = new Conditions();
		conditions.getMatch().add(new Tag("abc"));
		request.setConditions(conditions);
		
		assertEquals(Status.CLIENT_ERROR_PRECONDITION_FAILED,
				getResponse(request).getStatus());
	}
	
	@Test
	public void matching_etag_should_return_204_for_delete()
	{
		Tag etag = get().getEntity().getTag();
		Request request = getRequest(Method.DELETE, null);
		Conditions conditions = new Conditions();
		conditions.getMatch().add(etag);
		request.setConditions(conditions);
		
		assertEquals(Status.SUCCESS_NO_CONTENT, getResponse(request).getStatus());
	}
	
	@Test
	public void get_should_return_entity()
	{
		assertNotNull(get().getEntityAsText());		
	}
	
	@Test
	public void entity_should_be_json_object()
	{
		// parser will throw if result is not json
		new JsonParser().parse(get().getEntityAsText());
	}
	
	@Test
	public void delete_entity_should_return_204()
	{
		assertEquals(Status.SUCCESS_NO_CONTENT, delete().getStatus());
	}
	
	@Override
	protected String getResourcePath()
	{
		return "/1/tenant/entities/entity";
	}
}
