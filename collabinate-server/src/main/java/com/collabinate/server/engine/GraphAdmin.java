package com.collabinate.server.engine;

import com.collabinate.server.Tenant;
import com.tinkerpop.blueprints.KeyIndexableGraph;

/**
 * An implementation of Administration against a graph database.
 * 
 * @author mafuba
 *
 */
public class GraphAdmin implements CollabinateAdmin
{
	private KeyIndexableGraph graph;
	
	public GraphAdmin(KeyIndexableGraph graph)
	{
		if (null == graph)
		{
			throw new IllegalArgumentException("graph must not be null");
		}
		
		this.graph = graph;
	}
	
	@Override
	public Tenant createTenant(String tenantId)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
