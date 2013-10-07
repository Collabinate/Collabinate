package com.collabinate.server.engine;

import com.collabinate.server.Tenant;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.Vertex;

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
	public Tenant addTenant(String tenantId, String tenantName)
	{
		Tenant tenant = new Tenant(tenantId, tenantName);
		
		Vertex tenantVertex = graph.addVertex("_TENANT-" + tenantId);
		
		tenantVertex.setProperty("tenantId", tenantId);
		tenantVertex.setProperty("tenantName", tenantName);
		tenantVertex.setProperty("tenant", tenant);
		
		return tenant;
	}

	@Override
	public Tenant getTenant(String tenantId)
	{
		Vertex tenantVertex = graph.getVertex("_TENANT-" + tenantId);
		
		Tenant tenant = null;
		
		if (null != tenantVertex)
		{
			tenant = (Tenant)tenantVertex.getProperty("tenant");
		}
		
		return tenant;
	}
}
