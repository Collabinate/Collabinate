package com.collabinate.server.engine;

import com.collabinate.server.Tenant;
import com.google.gson.Gson;
import com.tinkerpop.blueprints.Vertex;

/**
 * An implementation of Administration against a graph database.
 * 
 * @author mafuba
 *
 */
public class GraphAdmin implements CollabinateAdmin
{
	private CollabinateGraph graph;
	
	public GraphAdmin(CollabinateGraph graph)
	{
		if (null == graph)
		{
			throw new IllegalArgumentException("graph must not be null");
		}
		
		this.graph = graph;
	}
	
	@Override
	public void putTenant(Tenant tenant)
	{
		Vertex tenantVertex = graph.getVertex(
				getTenantVertexId(tenant.getId()));
		
		if (null == tenantVertex)
		{
			tenantVertex = graph.addVertex(getTenantVertexId(tenant.getId()));
		}
		
		tenantVertex.setProperty(STRING_TENANTID, tenant.getId());
		tenantVertex.setProperty(STRING_TENANTNAME, tenant.getName());
		
		Gson gson = new Gson();
		String tenantJson = gson.toJson(tenant);
		tenantVertex.setProperty(STRING_TENANT, tenantJson);
		
		graph.commit();
	}

	@Override
	public Tenant getTenant(String tenantId)
	{
		Vertex tenantVertex = graph.getVertex(getTenantVertexId(tenantId));

		Tenant tenant = null;
		
		if (null != tenantVertex)
		{
			String tenantJson = (String)tenantVertex.getProperty(STRING_TENANT);
			Gson gson = new Gson();
			tenant = gson.fromJson(tenantJson, Tenant.class);
		}
		
		graph.commit();
		
		return tenant;
	}
	
	@Override
	public String exportDatabase()
	{
		return graph.exportGraph();
	}
	
	private String getTenantVertexId(String tenantId)
	{
		return STRING_TENANT_PREFIX + tenantId;
	}
	
	private static final String STRING_TENANT_PREFIX = "_TENANT-";
	private static final String STRING_TENANT = "tenant";
	private static final String STRING_TENANTID = "tenantId";
	private static final String STRING_TENANTNAME = "tenantName";
}
