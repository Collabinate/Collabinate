package com.collabinate.server.engine;

import org.junit.After;
import org.junit.Test;

import com.tinkerpop.blueprints.GraphFactory;
import com.tinkerpop.blueprints.KeyIndexableGraph;

/**
 * Test class for the graph implementation of CollabinateAdmin.
 * 
 * @author mafuba
 *
 */
public class GraphAdminTest extends CollabinateAdminTest
{
	private KeyIndexableGraph graph;
	
	@Override
	CollabinateAdmin getAdmin()
	{
		graph = (KeyIndexableGraph)GraphFactory.open(
				"src/test/resources/graph.properties");
		return new GraphAdmin(graph);
	}

	@After
	public void teardown()
	{
		if (null != graph)
			graph.shutdown();
	}
	
	@Test
	public void should_not_allow_null_graph()
	{
		exception.expect(IllegalArgumentException.class);
		new GraphAdmin(null);
	}
}
