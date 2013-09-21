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
import com.collabinate.server.engine.GraphServer;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;

/**
 * Tests for the Feed Resource
 * 
 * @author mafuba
 * 
 */
public class FeedResourceTest
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
	public void get_empty_feed_should_get_empty_atom_feed()
	{
		Request request = new Request(Method.GET, RESOURCE_PATH);
		Response response = component.handle(request);
		
		assertEquals(Status.SUCCESS_OK, response.getStatus());
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
		request = new Request(Method.GET, RESOURCE_PATH);
		Response response = component.handle(request);
		
		String responseText = response.getEntityAsText();
		
		assertThat(responseText, containsString(entityBody1));		
		assertThat(responseText, containsString(entityBody2));		
	}
	
	private static final String RESOURCE_PATH = 
			"riap://application/1/tenant/users/user/feed";
}
