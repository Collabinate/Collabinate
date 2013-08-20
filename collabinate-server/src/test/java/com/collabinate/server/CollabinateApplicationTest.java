package com.collabinate.server;

import static org.junit.Assert.*;

import org.junit.Test;
import org.restlet.Application;

import com.tinkerpop.blueprints.GraphFactory;
import com.tinkerpop.blueprints.KeyIndexableGraph;

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
		GraphServer server = new GraphServer(
				(KeyIndexableGraph)GraphFactory.open("src/test/resources/graph.properties"));
		Application app = new CollabinateApplication(server, server);
		assertEquals("Collabinate", app.getName());
		app.stop();
	}
}
