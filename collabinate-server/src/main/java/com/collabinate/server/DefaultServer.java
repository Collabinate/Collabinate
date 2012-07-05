package com.collabinate.server;

import com.tinkerpop.blueprints.Graph;

public class DefaultServer implements CollabinateServer
{
	private Graph _graph;
	
	public DefaultServer(final Graph graph)
	{
		if (null == graph)
		{
			throw new IllegalArgumentException("graph must not be null");
		}
		_graph = graph;
	}
	
	@Override
	public void addStreamItem(String entityId, StreamItemData streamItem)
	{
		if (null == entityId)
		{
			throw new IllegalArgumentException("entityId must not be null");
		}
	}

	@Override
	public void followEntity(String userId, String entityId)
	{
		if (null == entityId)
		{
			throw new IllegalArgumentException("userId must not be null");
		}
	}
}
