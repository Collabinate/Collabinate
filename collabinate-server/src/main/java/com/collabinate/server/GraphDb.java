package com.collabinate.server;

import com.tinkerpop.blueprints.Graph;

public class GraphDb
{
	private Graph _graph;

	public void attach(Graph graph)
	{
		_graph = graph;
	}

	public Graph getGraph()
	{
		return _graph;
	}

}
