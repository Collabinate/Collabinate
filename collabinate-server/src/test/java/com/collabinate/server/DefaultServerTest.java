package com.collabinate.server;

import org.junit.Test;

import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;

public class DefaultServerTest extends CollabinateServerTest
{
	@Override
	CollabinateServer createServer()
	{
		KeyIndexableGraph graph = new TinkerGraph();
		return new DefaultServer(graph);
	}
	
	@Test
	public void shouldNotAllowNullGraph()
	{
		exception.expect(IllegalArgumentException.class);
		new DefaultServer(null);
	}
}
