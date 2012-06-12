package com.collabinate.server;

import static org.junit.Assert.*;

import org.junit.Test;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;

public class GraphDbTest
{
	@Test
	public void shouldAttachToExistingDatabase()
	{
		Graph g = new TinkerGraph();
		g.addVertex(null);
		GraphDb.attach(g);
		Graph graph = GraphDb.getGraph();
		assertTrue(graph.getVertices().iterator().hasNext());
	}

}