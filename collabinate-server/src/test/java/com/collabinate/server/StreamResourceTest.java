package com.collabinate.server;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.logging.Level;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.engine.Engine;

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
	TinkerGraph graph;
	
	@Before
	public void setup()
	{
		graph = (TinkerGraph)GraphFactory.getGraph("TinkerGraph");
		server = new GraphServer(graph);
		Engine.setRestletLogLevel(Level.WARNING);
		component = new CollabinateComponent(server, server, 8182);
	}
	
	@After
	public void teardown() throws Exception
	{
		if (component.isStarted())
		{
			component.stop();
		}
		
		graph.clear();
	}
	
	@Test
	public void get_empty_stream_should_get_empty_atom_feed()
	{
		Request request = new Request(Method.GET, RESOURCE_PATH);
		Response response = component.handle(request);
		
		assertEquals(Status.SUCCESS_OK, response.getStatus());
	}
	
	@Test
	public void item_added_to_stream_should_return_201()
	{
		Request request = new Request(Method.POST, RESOURCE_PATH);
		Response response = component.handle(request);
		
		assertEquals(Status.SUCCESS_CREATED, response.getStatus());
	}
	
	@Test
	public void item_added_to_stream_should_create_and_return_child_location()
	{
		Request request = new Request(Method.POST, RESOURCE_PATH);
		Response response = component.handle(request);
		
		assertEquals(request.getResourceRef().getPath() + "/",
				response.getLocationRef().getParentRef().getPath());
	}
	
	@Test
	public void item_added_to_stream_should_have_entity_in_response_body()
	{
		Request request = new Request(Method.POST, RESOURCE_PATH);
		String entityBody = "TEST";
		request.setEntity(entityBody, MediaType.TEXT_PLAIN);
		Response response = component.handle(request);
		
		assertEquals(entityBody, response.getEntityAsText());
	}
	
	@Test
	public void item_added_to_stream_should_appear_in_stream()
	{
		Request request = new Request(Method.POST, RESOURCE_PATH);
		String entityBody = "TEST";
		request.setEntity(entityBody, MediaType.TEXT_PLAIN);
		component.handle(request);
		
		request = new Request(Method.GET, RESOURCE_PATH);
		Response response = component.handle(request);
		
		assertThat(response.getEntityAsText(), containsString(entityBody));		
	}
	
	private static final String RESOURCE_PATH = 
			"riap://application/1/tenant/entities/entity/stream";
}
