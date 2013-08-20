package com.collabinate.server;

import static org.junit.Assert.*;

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

import com.tinkerpop.blueprints.GraphFactory;
import com.tinkerpop.blueprints.KeyIndexableGraph;

/**
 * Test class for the server component.
 * 
 * @author mafuba
 *
 */
public class CollabinateComponentTest
{
	private Component component;
	
	@Before
	public void Setup()
	{
		GraphServer server = new GraphServer(
				(KeyIndexableGraph)GraphFactory.open("src/test/resources/graph.properties"));
		Engine.setRestletLogLevel(Level.WARNING);
		component = new CollabinateComponent(server, server, 8182);
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
	public void component_should_be_named_collabinate()
	{
		assertEquals("Collabinate", component.getName());
	}
	
	@Test
	public void getting_root_resource_should_return_200()
	{
		Request request = new Request(Method.GET, "riap://application/");
		Response response = component.handle(request);
		
		assertEquals(Status.SUCCESS_OK, response.getStatus());
	}
	
	@Test
	public void getting_invalid_route_should_return_404()
	{
		Request request = new Request(Method.GET, 
				"riap://application/invalid/route");
		Response response = component.handle(request);
		
		assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
	}
}
