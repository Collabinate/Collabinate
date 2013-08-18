package com.collabinate.server;

import static org.junit.Assert.*;

import org.junit.Test;
import org.restlet.Application;

import com.tinkerpop.blueprints.impls.tg.TinkerGraph;

/**
 * Test class for the server application.
 * 
 * @author mafuba
 *
 */
public class CollabinateApplicationTest
{	
	@Test
	public void application_should_be_named_collabinate() throws Exception
	{
		GraphServer server = new GraphServer(new TinkerGraph());
		Application app = new CollabinateApplication(server, server);
		assertEquals("Collabinate", app.getName());
		app.stop();
	}
}
