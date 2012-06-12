package com.collabinate.server;

import com.tinkerpop.blueprints.Graph;

public class GraphDb
{
	private static Graph _graph;

	public static void attach(Graph graph)
	{
		_graph = graph;
	}

	public static Graph getGraph()
	{
		return _graph;
	}

}
