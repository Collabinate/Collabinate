package com.collabinate.server.engine;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Set;

import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter;
import com.tinkerpop.blueprints.util.wrappers.id.IdGraph;
import com.tinkerpop.blueprints.util.wrappers.partition.PartitionIndexableGraph;

/**
 * Wrapper for the graph storage used by the engine classes. Ensures that the
 * graph is an ID graph, is key indexable, and is partition ready. Also contains
 * utilities to manage setting the tenant partitions, as well as auto committing
 * when calling commit.
 * 
 * @author mafuba
 *
 */
public class CollabinateGraph extends PartitionIndexableGraph<IndexableGraph>
{
	/**
	 * The underlying graph implementation.
	 */
	private KeyIndexableGraph baseGraph;
	
	/**
	 * Whether the graph should commit transactions.
	 */
	private boolean allowCommits = true;
	
	/**
	 * Creates a PartitionIndexableGraph{IndexableGraph} while maintaining
	 * a reference to the baseGraph.
	 * 
	 * @param baseGraph The base graph to maintain a reference to.
	 * @param baseIndexableGraph The indexable graph to wrap with partitions.
	 * @param writeGraphKey The write graph key for the partition graph.
	 * @param readWriteGraph The read write graph key for the partition graph.
	 */
	public CollabinateGraph(KeyIndexableGraph baseGraph,
			IndexableGraph baseIndexableGraph,
			String writeGraphKey, String readWriteGraph)
	{
		super(baseIndexableGraph, writeGraphKey, readWriteGraph);
		
		this.baseGraph = baseGraph;
	}

	/**
	 * Creates an instance of a CollabinateGraph from the given
	 * KeyIndexableGraph.
	 * 
	 * @param graph The graph to wrap with a CollabinateGraph.
	 * @return A CollabinateGraph that wraps the given graph.
	 */
	public static CollabinateGraph getInstance(KeyIndexableGraph graph)
	{
		if (null == graph)
		{
			throw new IllegalArgumentException("graph must not be null");
		}
		
		// we need to go through hoops in the following to ensure that we end up
		// with a graph that is both ID enabled and partitioned.
		if (!(graph instanceof IndexableGraph))
			throw new IllegalArgumentException(
					"graph must implement IndexableGraph.");
		
		// ensure we can provide IDs to the graph
		KeyIndexableGraph idGraph;
		if (graph.getFeatures().ignoresSuppliedIds)
			idGraph = new IdGraph<KeyIndexableGraph>(graph);
		else
			idGraph = graph;
		
		// make the graph multi-tenant
		IndexableGraph indexableGraph = (IndexableGraph)idGraph;
		
		return new CollabinateGraph(graph, indexableGraph, "_tenant", "");
	}
	
	/**
	 * Uses the PartitionGraph to keep tenant data separate by setting the
	 * current partition to the current tenant.
	 * 
	 * @param map A structure containing the data used to switch partitions.
	 * @return The previous partition values, so they can be replaced later.
	 */
	public TenantMap setCurrentTenant(TenantMap map)
	{
		// capture the existing values so they can be returned
		TenantMap savedValues = 
				new TenantMap(getWritePartition(),getReadPartitions());
		
		// switch out the partition to the values provided
		for (String readPartition : savedValues.getValue())
			removeReadPartition(readPartition);
		
		for (String readPartition : map.getValue())
			addReadPartition(readPartition);
		
		setWritePartition(map.getKey());
		
		// return the previous values for use in returning to previous state
		return savedValues;
	}

	/**
	 * Builds the structure used to switch the partition to a single tenant.
	 * 
	 * @param tenantId The ID of the tenant for which to build the structure.
	 * @return The structure used to switch to the given tenant.
	 */
	public TenantMap getTenantMap(String tenantId)
	{
		Set<String> set = new HashSet<String>();
		set.add(tenantId);
		return new TenantMap(tenantId, set);
	}
	
	/**
	 * Sets whether the graph should commit transactions.
	 * 
	 * @param allowCommits The setting for whether the graph allows
	 * transactions to be committed. True by default.
	 */
	public void setAllowCommits(boolean allowCommits)
	{
		this.allowCommits = allowCommits;
	}
	
	/**
	 * Causes the graph database to commit the current transaction, if allow
	 * commits is true.
	 */
	public void commit()
	{
		if (allowCommits && baseGraph.getFeatures().supportsTransactions)
		{
			((TransactionalGraph)baseGraph).commit();
		}
	}
	
	/**
	 * Outputs the graph to GraphML.
	 * 
	 * @return A String containing the GraphML for the database.
	 */
	public String exportGraph()
	{
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try
		{
			GraphMLWriter.outputGraph(this, stream);
			return stream.toString(StandardCharsets.UTF_8.name());
		}
		catch (Exception e)
		{
			//TODO: handle
			return null;
		}
	}
	
	/**
	 * Outputs the graph to a GraphML file.
	 * 
	 * @param fileName The file to which the data will be written.
	 */
	public void exportGraph(String fileName)
	{
		try
		{
			GraphMLWriter writer = new GraphMLWriter(this);
			writer.setNormalize(true);
			FileOutputStream file = new FileOutputStream(fileName);
			writer.outputGraph(file);
			file.flush();
			file.close();
		}
		catch (IOException exc)
		{
			// TODO: handle
		}
	}
	
	/**
	 * Structure for holding read and write partitions for a graph.
	 * 
	 * @author mafuba
	 *
	 */
	public class TenantMap extends
		AbstractMap.SimpleImmutableEntry<String, Set<String>>
	{
		private static final long serialVersionUID = 2898214691112356328L;

		/**
		 * Constructor.
		 * 
		 * @param writeValue The write partition.
		 * @param readValues The set of read partitions.
		 */
		public TenantMap(String writeValue, Set<String> readValues)
		{
			super(writeValue, readValues);
		}
		
	}
}
