package com.collabinate.server;

import org.junit.Test;

import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;

public class DefaultServerReaderTest extends CollabinateReaderTest
{
	private GraphServer server;
	
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
	
	private GraphServer getServer()
	{
		if (null == server)
		{
			KeyIndexableGraph graph = new TinkerGraph();
			server = new GraphServer(graph);
		}
		return server;
	}
	
	@Test
	public void should_not_allow_null_graph()
	{
		exception.expect(IllegalArgumentException.class);
		new GraphServer(null);
	}
}
