package com.collabinate.server;

import org.junit.Test;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;

public class DefaultServerTest extends CollabinateServerTest
{
	@Override
	CollabinateServer createServer()
	{
		Graph graph = new TinkerGraph();
		return new DefaultServer(graph);
	}
	
	@Test
	public void shouldNotAllowNullGraph()
	{
		exception.expect(IllegalArgumentException.class);
		CollabinateServer server = new DefaultServer(null);
	}
}
