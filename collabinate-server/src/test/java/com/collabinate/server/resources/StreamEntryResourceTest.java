package com.collabinate.server.resources;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

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
import org.restlet.security.Authenticator;

import com.collabinate.server.CollabinateComponent;
import com.collabinate.server.GraphServer;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;

/**
 * Tests for the Stream Entry Resource
 * 
 * @author mafuba
 * 
 */
public class StreamEntryResourceTest
{
	GraphServer server;
	Component component;
	TinkerGraph graph;
	
	@Before
	public void setup()
	{
		graph = new TinkerGraph();
		server = new GraphServer(graph);
		Engine.setRestletLogLevel(Level.WARNING);
		component = new CollabinateComponent(server, server,
			new Authenticator(null) {
				@Override
				protected boolean authenticate(Request request, Response response)
				{
					return true;
				}
			}, 8182);
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
	public void get_nonexistent_entry_should_return_404()
	{
		Request request = new Request(Method.GET, RESOURCE_PATH);
		Response response = component.handle(request);
		
		assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
	}
	
	@Test
	public void putting_entry_should_return_200()
	{
		Request request = new Request(Method.PUT, RESOURCE_PATH);
		Response response = component.handle(request);
		
		assertEquals(Status.SUCCESS_OK, response.getStatus());
	}
	
	@Test
	public void get_existing_entry_should_return_entry()
	{
		Request request = new Request(Method.PUT, RESOURCE_PATH);
		request.setEntity("test", MediaType.TEXT_PLAIN);
		Response response = component.handle(request);
		request = new Request(Method.GET, RESOURCE_PATH);
		response = component.handle(request);
		
		assertThat(response.getEntityAsText(), containsString("test"));		

	}
	
	private static final String RESOURCE_PATH = 
			"riap://application/1/tenant/entities/entity/stream/entry";
}
