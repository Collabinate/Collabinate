package com.collabinate.server;

import static org.junit.Assert.*;

import org.junit.Test;
import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;

/**
 * Test class for the server application.
 * 
 * @author mafuba
 *
 */
public class CollabinateApplicationTest
{	
	@Test
	public void application_should_be_named_collabinate()
	{
		Application app = new CollabinateApplication();
		assertEquals("Collabinate", app.getName());
	}
	
	@Test
	public void getting_root_resource_should_return_200()
	{
		Application app = new CollabinateApplication();
		Request request = new Request(Method.GET, "riap://application/");
		Response response = app.handle(request);
		
		assertEquals(200, response.getStatus().getCode());
	}
}
