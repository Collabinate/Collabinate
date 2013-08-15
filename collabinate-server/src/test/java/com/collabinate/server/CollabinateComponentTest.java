package com.collabinate.server;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;

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
		component = new CollabinateComponent(null, null);
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
	public void application_should_be_named_collabinate()
	{
		assertEquals("Collabinate", component.getName());
	}
	
	@Test
	public void getting_root_resource_should_return_200()
	{
		Request request = new Request(Method.GET, "riap://application/");
		Response response = component.handle(request);
		
		assertEquals(200, response.getStatus().getCode());
	}
	
	@Test
	public void getting_invalid_route_should_return_500()
	{
		Request request = new Request(Method.GET, 
				"riap://application/invalid/route");
		Response response = component.handle(request);
		
		assertEquals(404, response.getStatus().getCode());		
	}
}
