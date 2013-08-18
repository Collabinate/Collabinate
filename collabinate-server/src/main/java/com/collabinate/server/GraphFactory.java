package com.collabinate.server;

import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;

public class GraphFactory
{
	static KeyIndexableGraph getGraph()
	{
		return new TinkerGraph();
	}
}
