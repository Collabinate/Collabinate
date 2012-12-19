package com.collabinate.server;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

import org.joda.time.DateTime;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter;
import com.tinkerpop.blueprints.util.wrappers.id.IdGraph;

public class DefaultServer implements CollabinateReader, CollabinateWriter
{
	private KeyIndexableGraph graph;
	
	public DefaultServer(final KeyIndexableGraph graph)
	{
		if (null == graph)
		{
			throw new IllegalArgumentException("graph must not be null");
		}
		if (graph.getFeatures().ignoresSuppliedIds)
			this.graph = new IdGraph<KeyIndexableGraph>(graph);
		else
			this.graph = graph;
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
		
		Vertex entity = getOrCreateEntityVertex(entityId);
		
		Vertex streamItem = createStreamItemVertex(streamItemData);
		
		if (insertStreamItem(entity, streamItem))
			updateFeedPaths(entity);
	}

	private Vertex getOrCreateEntityVertex(final String entityId)
	{
		Vertex entity = graph.getVertex(entityId);
		if (null == entity)
		{
			entity = graph.addVertex(entityId);
		}
		return entity;
	}
	
	private Vertex createStreamItemVertex(final StreamItemData streamItemData)
	{
		Vertex streamItem = graph.addVertex(null);
		streamItem.setProperty("Time", streamItemData.getTime().toString());
		return streamItem;
	}
	
	private boolean insertStreamItem(final Vertex entity,
			final Vertex newStreamItem)
	{
		if (null == entity)
		{
			throw new IllegalArgumentException("entity must not be null");
		}
		
		if (null == newStreamItem)
		{
			throw new IllegalArgumentException(
					"newStreamItem must not be null");
		}
		
		StreamItemDateComparator comparator = new StreamItemDateComparator();
		
		Edge currentStreamEdge = getStreamItemEdge(entity);
		Vertex currentStreamItem = getNextStreamItem(entity);
		Vertex previousStreamItem = entity;
		int position = 0;		
						
		while (currentStreamItem != null &&
		       comparator.compare(newStreamItem, currentStreamItem) > 0)
		{
			previousStreamItem = currentStreamItem;
			currentStreamEdge = getStreamItemEdge(currentStreamItem);
			currentStreamItem = getNextStreamItem(currentStreamItem);
			position++;
		}
		
		graph.addEdge(null, previousStreamItem, newStreamItem, "StreamItem");
		if (null != currentStreamEdge)
		{
			graph.addEdge(null, newStreamItem, currentStreamItem, "StreamItem");
			graph.removeEdge(currentStreamEdge);
		}
		
		return position == 0;
	}
	
	private Edge getStreamItemEdge(Vertex node)
	{
		if (null == node)
			return null;
		
		Iterator<Edge> edges = 
				node.getEdges(Direction.OUT, "StreamItem").iterator();
		
		Edge edge = edges.hasNext() ? edges.next() : null;
		
		if (null != edge)
		{
			if (edges.hasNext())
			{
				throw new IllegalStateException(
					"Multiple stream item edges for vertex: " + 
					node.getId());
			}
		}
		
		return edge;
	}
	
	private Edge getFeedEdge(Vertex node, String feedLabel)
	{
		if (null == node)
			return null;
		
		Iterator<Edge> edges =
				node.getEdges(Direction.OUT, feedLabel).iterator();
		
		Edge edge = edges.hasNext() ? edges.next() : null;
		
		if (null != edge)
		{
			if (edges.hasNext())
			{
				throw new IllegalStateException(
					"Multiple feed edges for vertex: " +
					node.getId());
			}
		}
		
		return edge;
	}
	
	private void updateFeedPaths(Vertex entity)
	{
		// get all the users that follow the entity
		Iterable<Vertex> users =
				entity.getVertices(Direction.IN, "Follows");
		
		// loop over each user and move the entity to the correct
		// feed position by un-following and re-following
		String entityId = entity.getId().toString();
		String userId;
		for (Vertex user : users)
		{
			userId = user.getId().toString();
			unfollowEntity(userId, entityId);
			followEntity(userId, entityId);
		}
	}
		
	private String getIdString(Vertex vertex)
	{
		return vertex.getId().toString();
	}

	@Override
	public List<StreamItemData> getStream(String entityId, long startIndex,
			int itemsToReturn)
	{
		Vertex entity = graph.getVertex(entityId);
		if (null == entity)
		{
			return new ArrayList<StreamItemData>();
		}
		
		int streamPosition = 0;
		int foundItemCount = 0;
		List<Vertex> streamVertices = new ArrayList<Vertex>();
		
		Vertex currentStreamItem = getNextStreamItem(entity);
		
		while (null != currentStreamItem && foundItemCount < itemsToReturn)
		{
			if (streamPosition >= startIndex)
			{
				streamVertices.add(currentStreamItem);
				foundItemCount++;
			}
			currentStreamItem = getNextStreamItem(currentStreamItem);
			streamPosition++;
		}
		
		return createStreamItems(streamVertices);
	}
	
	private Vertex getNextStreamItem(Vertex node)
	{
		Edge streamItemEdge = getStreamItemEdge(node);
		return null == streamItemEdge ? null :
			streamItemEdge.getVertex(Direction.IN);
	}

	private List<StreamItemData> createStreamItems(
			Collection<Vertex> streamItems)
	{
		ArrayList<StreamItemData> itemData =
				new ArrayList<StreamItemData>();
		
		for (final Vertex vertex : streamItems)
		{
			if (null != vertex)
			{
				itemData.add(createStreamItemData(vertex));
			}
		}
		
		return itemData;
	}
	
	private StreamItemData createStreamItemData(final Vertex streamItem)
	{
		return new StreamItemData() {
			
			@Override
			public DateTime getTime()
			{
				return DateTime.parse((String) streamItem.getProperty("Time"));
			}
		};
	}

	@Override
	public void followEntity(String userId, String entityId)
	{
		if (null == userId)
		{
			throw new IllegalArgumentException("userId must not be null");
		}
		
		if (null == entityId)
		{
			throw new IllegalArgumentException("entityId must not be null");
		}
		
		Vertex user = getOrCreateEntityVertex(userId);
		Vertex entity = getOrCreateEntityVertex(entityId);
		
		graph.addEdge(null, user, entity, "Follows");
		
		insertFeedEntity(user, entity);
	}
	
	public void unfollowEntity(String userId, String entityId)
	{
		if (null == userId)
		{
			throw new IllegalArgumentException("userId must not be null");
		}

		if (null == entityId)
		{
			throw new IllegalArgumentException("entityId must not be null");
		}
		
		Vertex user = getOrCreateEntityVertex(userId);
		Vertex entity = getOrCreateEntityVertex(entityId);
		String feedLabel = getFeedLabel(getIdString(user));
		
		// remove the follow relationship
		for (Edge edge: entity.getEdges(Direction.IN, "Follows"))
		{
			if (edge.getVertex(Direction.OUT).getId().equals(
					user.getId()))
				graph.removeEdge(edge);
		}
		
		// remove the entity from the user feed
		Vertex previousEntity = getNextFeedEntity(feedLabel, entity,
				Direction.IN);
		Vertex nextEntity = getNextFeedEntity(feedLabel, entity,
				Direction.OUT);
		for (Edge edge: entity.getEdges(Direction.BOTH, feedLabel))
		{
			graph.removeEdge(edge);
		}
		
		if (null != nextEntity)
		{
			graph.addEdge(null, previousEntity, nextEntity, feedLabel);
		}
		
	}
	
	private boolean insertFeedEntity(final Vertex user, final Vertex newEntity)
	{
		if (null == user)
		{
			throw new IllegalArgumentException("user must not be null");
		}
		
		if (null == newEntity)
		{
			throw new IllegalArgumentException(
					"newEntity must not be null");
		}
		
		EntityFirstStreamItemDateComparator comparator = 
				new EntityFirstStreamItemDateComparator();
		
		String feedLabel = getFeedLabel((String)user.getId());
		Edge currentFeedEdge = getFeedEdge(user, feedLabel);
		Vertex currentFeedEntity = getNextFeedEntity(feedLabel, user,
				Direction.OUT);
		Vertex previousFeedEntity = user;
		int position = 0;		
						
		while (currentFeedEntity != null &&
		       comparator.compare(newEntity, currentFeedEntity) > 0)
		{
			previousFeedEntity = currentFeedEntity;
			currentFeedEdge = getStreamItemEdge(currentFeedEntity);
			currentFeedEntity = getNextStreamItem(currentFeedEntity);
			position++;
		}
		
		graph.addEdge(null, previousFeedEntity, newEntity, feedLabel);
		if (null != currentFeedEdge)
		{
			graph.addEdge(null, newEntity, currentFeedEntity, feedLabel);
			graph.removeEdge(currentFeedEdge);
		}
		
		return position == 0;
	}

	@Override
	public List<StreamItemData> getFeed(String userId, long startIndex,
			int itemsToReturn)
	{
		StreamItemDateComparator comparator = new StreamItemDateComparator();		
		PriorityQueue<Vertex> queue =
				new PriorityQueue<Vertex>(11, comparator);
		ArrayList<Vertex> streamItems = new ArrayList<Vertex>();
		Vertex topOfEntity = null;
		Vertex topOfQueue = null;
		
		Vertex user = getOrCreateEntityVertex(userId);
		String feedLabel = getFeedLabel(getIdString(user));
		Vertex entity = getNextFeedEntity(feedLabel, user, Direction.OUT);
		
		if (null != entity)
		{
			topOfEntity = getNextStreamItem(entity);
			if (null != topOfEntity)
			{
				queue.add(topOfEntity);
				topOfQueue = topOfEntity;
			}
			entity = getNextFeedEntity(feedLabel, entity, Direction.OUT);
			topOfEntity = getNextStreamItem(entity);
		}
		
		// while we have not yet hit our items to return,
		// and there are still items in the queue OR
		// there are more entities
		while (streamItems.size() < (itemsToReturn + startIndex)
				&& (queue.size() > 0 || entity != null))
		{
			// compare top of next entity to top of queue
			int result = comparator.compare(topOfEntity, topOfQueue);

			// if top of next entity is newer, take the top element,
			// push the next element to the queue, and move to
			// the next entity
			if (result < 0)
			{
				streamItems.add(topOfEntity);
				Vertex nextItem = getNextStreamItem(topOfEntity);
				if (null != nextItem)
					queue.add(nextItem);
				entity = getNextFeedEntity(feedLabel, entity, Direction.OUT);
				topOfEntity = getNextStreamItem(entity);
				topOfQueue = queue.peek();
			}
			
			else
			{
				// if there's no top of entity and the queue is empty,
				// we need to move to the next entity
				if (queue.isEmpty())
				{
					entity = getNextFeedEntity(feedLabel,
							entity, Direction.OUT);
					topOfEntity = getNextStreamItem(entity);
				}
				// if top of queue is newer, take the top element, and
				// push the next element to the queue
				else
				{
					Vertex removedFromQueue = queue.remove();
					Vertex nextItem = getNextStreamItem(removedFromQueue);
					if (null != nextItem)
						queue.add(nextItem);
					streamItems.add(removedFromQueue);
					topOfQueue = queue.peek();
				}
			}
		}

		return createStreamItems(streamItems);
	}

	private void exportGraph(String fileName)
	{
		try
		{
			GraphMLWriter writer = new GraphMLWriter(graph);
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
	
	private Vertex getNextFeedEntity(String feedLabel,
			Vertex currentEntity, Direction direction)
	{
		if (null == currentEntity)
			return null;
		
		Iterator<Vertex> vertices = 
				currentEntity
				.getVertices(direction, feedLabel)
				.iterator();
		
		Vertex vertex = vertices.hasNext() ? vertices.next() : null;
		
		if (null != vertex)
		{
			if (vertices.hasNext())
			{
				throw new IllegalStateException(
					"Multiple feed edges for vertex: " +
					vertex.getId() + " with feedLabel: " +
					feedLabel);
			}
		}
		
		return vertex;
	}
	
	private String getFeedLabel(String userId)
	{
		return "Feed+" + userId;
	}

	private class StreamItemDateComparator implements Comparator<Vertex>
	{
		@Override
		public int compare(Vertex v1, Vertex v2)
		{
			long t1 = 0;
			if (null != v1)
				t1 = DateTime.parse((String)v1.getProperty("Time"))
					.getMillis();
			long t2 = 0;
			if (null != v2)
				t2 = DateTime.parse((String)v2.getProperty("Time"))
					.getMillis();

			return new Long(t2).compareTo(t1);
		}
	}
	
	private class EntityFirstStreamItemDateComparator
		implements Comparator<Vertex>
	{
		@Override
		public int compare(Vertex v1, Vertex v2)
		{
			Vertex streamItem = null;
			
			long t1 = 0;
			if (null != v1)
				streamItem = getNextStreamItem(v1);
			if (null != streamItem)
				t1 = DateTime.parse((String)streamItem.getProperty("Time"))
					.getMillis();
			
			long t2 = 0;
			if (null != v2)
				streamItem = getNextStreamItem(v2);
			if (null != streamItem)
				t2 = DateTime.parse((String)streamItem.getProperty("Time"))
					.getMillis();
			
			return new Long(t2).compareTo(t1);
		}
	}
}
