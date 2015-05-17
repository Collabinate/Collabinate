package com.collabinate.server.engine;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.collabinate.server.Tenant;
import com.google.gson.Gson;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.partition.PartitionGraph;

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
		
		tenantVertex.setProperty(STRING_TENANT_ID, tenant.getId());
		tenantVertex.setProperty(STRING_NAME, tenant.getName());
		tenantVertex.setProperty(STRING_CREATED,
				DateTime.now(DateTimeZone.UTC).toString());
		
		Gson gson = new Gson();
		String tenantJson = gson.toJson(tenant);
		tenantVertex.setProperty(STRING_CONTENT, tenantJson);
		
		graph.commit();
	}

	@Override
	public Tenant getTenant(String tenantId)
	{
		Vertex tenantVertex = graph.getVertex(getTenantVertexId(tenantId));

		Tenant tenant = null;
		
		if (null != tenantVertex)
		{
			String tenantJson = (String)tenantVertex.getProperty(STRING_CONTENT);
			Gson gson = new Gson();
			tenant = gson.fromJson(tenantJson, Tenant.class);
		}
		
		graph.commit();
		
		return tenant;
	}
	
	@Override
	public void deleteTenant(String tenantId)
	{
		for (Vertex vertex : graph.getVertices(STRING_TENANT_ID, tenantId))
		{
			vertex.remove();
		}
	}

	@Override
	public String exportDatabase()
	{
		return CollabinateGraph.exportGraph(graph);
	}
	
	@Override
	public void importDatabase(String data)
	{
		graph.importGraph(data);
	}

	@Override
	public String exportTenantData(String tenantId)
	{
		PartitionGraph<KeyIndexableGraph> tenantGraph =
				new PartitionGraph<KeyIndexableGraph>(
						graph, STRING_TENANT_ID, tenantId);
		
		return CollabinateGraph.exportGraph(tenantGraph);
	}

	@Override
	public void importTenantData(String tenantId, String data)
	{
		if (null != getTenant(tenantId))
		{
			throw new IllegalStateException(
					"Cannot import data for tenant with ID: '" 
					+ tenantId
					+ "', tenant already exists.");
		}
		
		graph.importGraph(data);
	}

	private String getTenantVertexId(String tenantId)
	{
		return STRING_TENANT_PREFIX + tenantId;
	}
	
	private static final String STRING_TENANT_PREFIX = "collabinate.tenant.";
	private static final String STRING_CONTENT = "Content";
	private static final String STRING_TENANT_ID = "TenantID";
	private static final String STRING_NAME = "Name";
	private static final String STRING_CREATED = "Created";
}
