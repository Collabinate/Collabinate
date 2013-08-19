package com.collabinate.server;

import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;

public class GraphFactory
{
	static KeyIndexableGraph getGraph()
	{
		String graphType = Collabinate.getConfiguration()
				.getString("collabinate.graphType", "TinkerGraph");

		return getGraph(graphType);
	}
	
	static KeyIndexableGraph getGraph(String graphType)
	{
		switch (graphType)
		{
			case "Neo4jEmbedded":

			case "TinkerGraph":
				return new TinkerGraph();
				
			default:
				throw new UnsupportedOperationException(
						"Unsupported graph type configured: " + graphType);
		}
	}
}
