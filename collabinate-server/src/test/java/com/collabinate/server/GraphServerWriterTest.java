package com.collabinate.server;

import org.junit.Test;

import com.tinkerpop.blueprints.GraphFactory;
import com.tinkerpop.blueprints.KeyIndexableGraph;

/**
 * Test class for the graph implementation of CollabinateWriter.
 * 
 * @author mafuba
 *
 */
public class GraphServerWriterTest extends CollabinateWriterTest
{
	@Override
	CollabinateWriter getWriter()
	{
		KeyIndexableGraph graph = 
				(KeyIndexableGraph)GraphFactory.open("src/test/resources/graph.properties");
		return new GraphServer(graph);
	}
	
	@Test
	public void should_not_allow_null_graph()
	{
		exception.expect(IllegalArgumentException.class);
		new GraphServer(null);
	}
}
