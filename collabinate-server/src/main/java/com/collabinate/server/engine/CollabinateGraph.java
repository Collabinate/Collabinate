package com.collabinate.server.engine;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter;
import com.tinkerpop.blueprints.util.wrappers.id.IdGraph;

/**
 * Wrapper for the graph storage used by the engine classes. Ensures that the
 * graph is an ID graph and is key indexable. Also enables auto committing
 * transactions when calling commit.
 * 
 * @author mafuba
 *
 */
public class CollabinateGraph implements KeyIndexableGraph
{
	/**
	 * The underlying graph implementation.
	 */
	private KeyIndexableGraph baseGraph;
	
	/**
	 * The graph that allows IDs.
	 */
	private KeyIndexableGraph graph;
	
	/**
	 * Whether the graph should commit transactions.
	 */
	private boolean allowCommits = true;
	
	/**
	 * Whether the graph supports transactions.
	 */
	private boolean supportsTransactions = false;
	
	/**
	 * Creates the internally used graph while maintaining a reference to the
	 * base graph.
	 * 
	 * @param baseGraph The base graph to maintain a reference to.
	 */
	public CollabinateGraph(KeyIndexableGraph baseGraph)
	{
		if (null == baseGraph)
		{
			throw new IllegalArgumentException("baseGraph must not be null");
		}
		
		this.baseGraph = baseGraph;
		
		// ensure we can provide IDs to the graph
		KeyIndexableGraph idGraph;
		if (baseGraph.getFeatures().ignoresSuppliedIds)
			idGraph = new IdGraph<KeyIndexableGraph>(baseGraph);
		else
			idGraph = baseGraph;
		
		this.graph = idGraph;
		
		supportsTransactions = graph.getFeatures().supportsTransactions;
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
		if (allowCommits && supportsTransactions)
		{
			((TransactionalGraph)graph).commit();
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
			GraphMLWriter.outputGraph(baseGraph, stream);
			return stream.toString(StandardCharsets.UTF_8.name());
		}
		catch (Exception e)
		{
			e.printStackTrace();
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
			GraphMLWriter writer = new GraphMLWriter(baseGraph);
			writer.setNormalize(true);
			FileOutputStream file = new FileOutputStream(fileName);
			writer.outputGraph(file);
			file.flush();
			file.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public Features getFeatures()
	{
		return graph.getFeatures();
	}

	@Override
	public Vertex addVertex(Object id)
	{
		return graph.addVertex(id);
	}

	@Override
	public Vertex getVertex(Object id)
	{
		return graph.getVertex(id);
	}

	@Override
	public void removeVertex(Vertex vertex)
	{
		graph.removeVertex(vertex);
	}

	@Override
	public Iterable<Vertex> getVertices()
	{
		return graph.getVertices();
	}

	@Override
	public Iterable<Vertex> getVertices(String key, Object value)
	{
		return graph.getVertices(key, value);
	}

	@Override
	public Edge addEdge(Object id, Vertex outVertex, Vertex inVertex,
			String label)
	{
		return graph.addEdge(id, outVertex, inVertex, label);
	}

	@Override
	public Edge getEdge(Object id)
	{
		return graph.getEdge(id);
	}

	@Override
	public void removeEdge(Edge edge)
	{
		graph.removeEdge(edge);
	}

	@Override
	public Iterable<Edge> getEdges()
	{
		return graph.getEdges();
	}

	@Override
	public Iterable<Edge> getEdges(String key, Object value)
	{
		return graph.getEdges(key, value);
	}

	@Override
	public GraphQuery query()
	{
		return graph.query();
	}

	@Override
	public void shutdown()
	{
		graph.shutdown();
	}

	@Override
	public <T extends Element> void dropKeyIndex(String key,
			Class<T> elementClass)
	{
		graph.dropKeyIndex(key, elementClass);
	}

	@Override
	public <T extends Element> void createKeyIndex(String key,
			Class<T> elementClass,
			@SuppressWarnings("rawtypes") Parameter... indexParameters)
	{
		graph.createKeyIndex(key, elementClass, indexParameters);		
	}

	@Override
	public <T extends Element> Set<String> getIndexedKeys(Class<T> elementClass)
	{
		return graph.getIndexedKeys(elementClass);
	}
}
