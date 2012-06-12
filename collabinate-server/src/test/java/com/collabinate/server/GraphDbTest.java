package com.collabinate.server;

import static org.junit.Assert.*;

import java.awt.Graphics2D;

import org.junit.Test;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;

public class GraphDbTest
{
	@Test
	public void shouldAttachToExistingDatabase()
	{
		Graph g = new TinkerGraph();
		Vertex v = g.addVertex(null);
		GraphDb.attach(g);
		Graph graph = GraphDb.getGraph();
		assertTrue(graph.getVertices().iterator().hasNext());
		assertEquals(v, graph.getVertices().iterator().next());
	}

}