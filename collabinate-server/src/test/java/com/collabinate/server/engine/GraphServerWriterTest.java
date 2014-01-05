package com.collabinate.server.engine;

import org.junit.After;
import org.junit.Test;

import com.collabinate.server.engine.CollabinateWriter;
import com.collabinate.server.engine.GraphEngine;
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
	private CollabinateGraph graph;
	
	@Override
	CollabinateWriter getWriter()
	{
		graph = new CollabinateGraph(
				(KeyIndexableGraph)GraphFactory.open(
				"src/test/resources/graph.properties"));
		graph.setAllowCommits(false);
		return new GraphEngine(graph);
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
		new GraphEngine(null);
	}
}
