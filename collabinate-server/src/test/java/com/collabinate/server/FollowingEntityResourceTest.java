package com.collabinate.server;

import static org.junit.Assert.assertEquals;

import java.util.logging.Level;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.engine.Engine;

import com.tinkerpop.blueprints.impls.tg.TinkerGraph;

/**
 * Tests for the following entity resource.
 * 
 * @author mafuba
 *
 */
public class FollowingEntityResourceTest
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
	public void getting_not_followed_entity_should_return_404()
	{
		Request request = new Request(Method.GET, RESOURCE_PATH);
		Response response = component.handle(request);
		
		assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
	}
	
	@Test
	public void following_entity_should_return_201()
	{
		Request request = new Request(Method.PUT, RESOURCE_PATH);
		Response response = component.handle(request);
		
		assertEquals(Status.SUCCESS_CREATED, response.getStatus());
	}
	
	@Test
	public void getting_followed_entity_should_return_200()
	{
		Request request = new Request(Method.PUT, RESOURCE_PATH);
		Response response = component.handle(request);
		request = new Request(Method.GET, RESOURCE_PATH);
		response = component.handle(request);
		
		assertEquals(Status.SUCCESS_OK, response.getStatus());
	}
	
	@Test
	public void unfollowing_entity_should_return_200()
	{
		Request request = new Request(Method.PUT, RESOURCE_PATH);
		Response response = component.handle(request);
		request = new Request(Method.DELETE, RESOURCE_PATH);
		response = component.handle(request);
		
		assertEquals(Status.SUCCESS_OK, response.getStatus());
	}
	
	@Test
	public void unfollowing_never_followed_entity_should_return_200()
	{
		Request request = new Request(Method.DELETE, RESOURCE_PATH);
		Response response = component.handle(request);
		
		assertEquals(Status.SUCCESS_OK, response.getStatus());
	}
	
	@Test
	public void getting_unfollowed_entity_should_return_404()
	{
		Request request = new Request(Method.PUT, RESOURCE_PATH);
		Response response = component.handle(request);
		request = new Request(Method.DELETE, RESOURCE_PATH);
		response = component.handle(request);
		request = new Request(Method.GET, RESOURCE_PATH);
		response = component.handle(request);
		
		assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
	}

	private static final String RESOURCE_PATH = 
			"riap://application/1/tenant/users/user/following/entity";
}
