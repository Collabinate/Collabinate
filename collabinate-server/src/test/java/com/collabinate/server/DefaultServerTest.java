package com.collabinate.server;

import java.util.Comparator;

import org.joda.time.DateTime;
import org.junit.Test;

import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;

public class DefaultServerTest extends CollabinateServerTest
{
	@Override
	CollabinateServer createServer()
	{
		KeyIndexableGraph graph = new TinkerGraph();
		Comparator<Vertex> comparator = new StreamItemDateComparator();
		return new DefaultServer(graph, comparator);
	}
	
	private class StreamItemDateComparator implements Comparator<Vertex>
	{
		@Override
		public int compare(Vertex v1, Vertex v2)
		{
			long t1 = DateTime
					.parse((String)v1.getProperty("Time")).getMillis();
			long t2 = DateTime
					.parse((String)v2.getProperty("Time")).getMillis();
			return new Long(t1).compareTo(t2);
		}
	}
	
	@Test
	public void should_not_allow_null_graph()
	{
		exception.expect(IllegalArgumentException.class);
		new DefaultServer(null, new StreamItemDateComparator());
	}
	
	@Test
	public void should_not_allow_null_comparator()
	{
		exception.expect(IllegalArgumentException.class);
		new DefaultServer(new TinkerGraph(), null);
	}
}
