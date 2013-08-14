package com.collabinate.server;

import static org.junit.Assert.*;

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
	@Test
	public void application_should_be_named_collabinate()
	{
		Component component = new CollabinateComponent();
		assertEquals("Collabinate", component.getName());
	}
	
	@Test
	public void getting_root_resource_should_return_200()
	{
		Component component = new CollabinateComponent();
		Request request = new Request(Method.GET, "riap://application/");
		Response response = component.handle(request);
		
		assertEquals(200, response.getStatus().getCode());
	}
}
