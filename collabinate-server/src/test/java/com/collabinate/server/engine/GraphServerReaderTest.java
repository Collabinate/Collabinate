package com.collabinate.server.engine;

import org.junit.After;
import org.junit.Test;

import com.collabinate.server.engine.CollabinateReader;
import com.collabinate.server.engine.CollabinateWriter;
import com.collabinate.server.engine.GraphEngine;
import com.tinkerpop.blueprints.GraphFactory;
import com.tinkerpop.blueprints.KeyIndexableGraph;

/**
 * Test class for the graph implementation of CollabinateReader.
 * 
 * @author mafuba
 *
 */
public class GraphServerReaderTest extends CollabinateReaderTest
{
	private CollabinateGraph graph;
	private GraphEngine server;
	
	@Override
	CollabinateReader getReader()
	{
		return getServer();
	}
	
	@Override
	CollabinateWriter getWriter()
	{
		return getServer();
	}
	
	/**
	 * Ensures the same server is used as both the reader and writer.
	 * 
	 * @return The server to use.
	 */
	private GraphEngine getServer()
	{
		if (null == server)
		{
			graph = CollabinateGraph.getInstance(
					(KeyIndexableGraph)GraphFactory.open(
					"src/test/resources/graph.properties"));
			graph.setAllowCommits(false);
			server = new GraphEngine(graph);
		}
		return server;
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
