package com.collabinate.server;

import static org.junit.Assert.*;

import org.junit.Test;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;

public class GraphDbTest
{
	@Test
	public void shouldAttachToExistingDatabase()
	{
		GraphDb db = new GraphDb();
		Graph g = new TinkerGraph();
		Vertex v = g.addVertex(null);
		db.attach(g);
		Graph graph = db.getGraph();
		assertTrue(graph.getVertices().iterator().hasNext());
		assertEquals(v, graph.getVertices().iterator().next());
	}

}