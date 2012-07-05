package com.collabinate.server;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import org.joda.time.DateTime;

import com.tinkerpop.blueprints.Direction;
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
		
		Vertex entity = graph.getVertex(entityId);
		
		if (null == entity)
		{
			entity = graph.addVertex(entityId);
		}
		
		Vertex streamItem = graph.addVertex(null);
		streamItem.setProperty("Time", streamItemData.getTime().toString());
		
		graph.addEdge(null, entity, streamItem, "StreamItem");
	}
	
	public StreamItemData[] getStream(String entityId, long startIndex, int itemsToReturn)
	{
		Vertex entity = graph.getVertex(entityId);
		
		int index = 0;
		int count = 0;
		SortedSet<Vertex> vertices = new TreeSet<Vertex>();
		
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
		SortedSet<StreamItemData> itemData = new TreeSet<StreamItemData>();
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
