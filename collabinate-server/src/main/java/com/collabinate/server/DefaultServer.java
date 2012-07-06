package com.collabinate.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.id.IdGraph;

public class DefaultServer implements CollabinateServer
{
	private KeyIndexableGraph graph;
	
	public DefaultServer(final KeyIndexableGraph graph)
	{
		if (null == graph)
		{
			throw new IllegalArgumentException("graph must not be null");
		}
		this.graph = new IdGraph<KeyIndexableGraph>(graph);
	}
	
	@Override
	public void addStreamItem(String entityId, StreamItemData streamItemData)
	{
		if (null == entityId)
		{
			throw new IllegalArgumentException("entityId must not be null");
		}
		
		if (null == streamItemData)
		{
			throw new IllegalArgumentException("streamItem must not be null");
		}
		
		Vertex entity = getOrCreateEntity(entityId);
		
		Vertex streamItem = createStreamItem(streamItemData);
		
		insertStreamItem(entity, streamItem);
	}

	private Vertex getOrCreateEntity(String entityId)
	{
		Vertex entity = graph.getVertex(entityId);		
		if (null == entity)
		{
			entity = graph.addVertex(entityId);
		}		
		return entity;
	}
	
	private Vertex createStreamItem(StreamItemData streamItemData)
	{
		Vertex streamItem = graph.addVertex(null);
		streamItem.setProperty("Time", streamItemData.getTime().toString());
		return streamItem;
	}
	
	private void insertStreamItem(Vertex entity, Vertex streamItem)
	{
		// get the edge to the first stream item, if any
		Edge originalEdge = getStreamItemEdge(entity);
		
		// get the first stream item, if any, and remove the first edge
		Vertex previousStreamItem = null;		
		if (null != originalEdge)
		{
			previousStreamItem = originalEdge.getVertex(Direction.OUT);
			graph.removeEdge(originalEdge);		
		}
		
		// connect the new stream item to the entity
		graph.addEdge(null, entity, streamItem, "StreamItem");
		
		// if there was a previous stream item,
		// connect the new stream item to it
		if (null != previousStreamItem)
		{
			graph.addEdge(null, streamItem, previousStreamItem, "StreamItem");
		}
	}
	
	private Edge getStreamItemEdge(Vertex entity)
	{
		Iterator<Edge> iterator = 
				entity.getEdges(Direction.OUT, "StreamItem").iterator();
		
		Edge edge = iterator.hasNext() ? iterator.next() : null;
		
		if (null != edge)
		{
			if (iterator.hasNext())
			{
				throw new IllegalStateException(
					"Multiple stream item edges for entity: " + 
					entity.getId());
			}
		}
		
		return edge;
	}

	@Override
	public StreamItemData[] getStream(String entityId, long startIndex, int itemsToReturn)
	{
		Vertex entity = graph.getVertex(entityId);
		
		int index = 0;
		int count = 0;
		List<Vertex> vertices = new ArrayList<Vertex>();
		
		for (Vertex vertex : entity.getVertices(Direction.OUT, "StreamItem"))
		{
			if (count >= itemsToReturn)
			{
				break;
			}
			
			if (index >= startIndex)
			{
				vertices.add(vertex);
				count++;
			}			
		}
		
		return createStreamItems(vertices);
	}
	
	private StreamItemData[] createStreamItems(Collection<Vertex> streamItems)
	{
		List<StreamItemData> itemData = new ArrayList<StreamItemData>();
		for (final Vertex vertex : streamItems)
		{
			itemData.add(new StreamItemData() {
				
				@Override
				public DateTime getTime()
				{
					return DateTime.parse((String) vertex.getProperty("Time"));
				}
			});
		}
		return itemData.toArray(new StreamItemData[0]);
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
