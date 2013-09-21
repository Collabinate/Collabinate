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
import org.restlet.security.Authenticator;

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
	private KeyIndexableGraph graph;
	private Component component;
	
	@Before
	public void Setup()
	{
		graph = (KeyIndexableGraph)GraphFactory.open(
				"src/test/resources/graph.properties");
		GraphServer server = new GraphServer(graph);
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
	public void Teardown() throws Exception
	{
		if (component.isStarted())
		{
			component.stop();
		}
		graph.shutdown();
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
