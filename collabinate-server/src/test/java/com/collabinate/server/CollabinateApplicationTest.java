package com.collabinate.server;

import static org.junit.Assert.*;

import org.junit.Test;
import org.restlet.Application;

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
}
