package com.collabinate.server;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;

import com.tinkerpop.blueprints.impls.tg.TinkerGraph;

/**
 * Tests for the Stream Resource
 * 
 * @author mafuba
 * 
 */
public class StreamResourceTest
{
	GraphServer server;
	Component component;
	
	@Before
	public void Setup()
	{
		server = new GraphServer(new TinkerGraph());
		component = new CollabinateComponent(server, server);
	}
	
	@After
	public void Teardown() throws Exception
	{
		if (component.isStarted())
		{
			component.stop();
		}
	}
	
	
	@Test
	public void empty_stream_should_return_empty_response()
	{
		Request request = new Request(Method.GET, 
				"riap://application/1/tenant/entity/stream");
		Response response = component.handle(request);
		
		assertEquals(0, response.getEntity().getSize());
	}
	
	@Test
	public void item_added_to_stream_should_return_201()
	{
		Request request = new Request(Method.POST,
				"riap://application/1/tenant/entity/stream");
		Response response = component.handle(request);
		
		assertEquals(Status.SUCCESS_CREATED, response.getStatus());
	}
	
	@Test
	public void item_added_to_stream_should_return_child_location()
	{
		Request request = new Request(Method.POST,
				"riap://application/1/tenant/entity/stream");
		Response response = component.handle(request);
		
		assertEquals(request.getResourceRef().getPath() + "/",
				response.getLocationRef().getParentRef().getPath());
	}
	
	@Test
	public void item_added_to_stream_should_have_entity_in_body()
	{
		Request request = new Request(Method.POST,
				"riap://application/1/tenant/entity/stream");
		String entityBody = "TEST";
		request.setEntity(entityBody, MediaType.TEXT_PLAIN);
		Response response = component.handle(request);
		
		assertEquals(entityBody, response.getEntityAsText());
	}
}
