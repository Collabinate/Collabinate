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

/**
 * An implementation of both reader and writer backed by a graph database.
 * 
 * @author mafuba
 * 
 */
public class GraphServer implements CollabinateReader, CollabinateWriter
{
	/**
	 * The graph database backing this instance.
	 */
	private KeyIndexableGraph graph;
	
	/**
	 * Ensures that the graph can have IDs assigned.
	 * 
	 * @param graph A Tinkerpop BluePrints graph to act as the store for the server.
	 */
	public GraphServer(final KeyIndexableGraph graph)
	{
		if (null == graph)
		{
			throw new IllegalArgumentException("graph must not be null");
		}
		
		// ensure we can provide IDs to the graph
		if (graph.getFeatures().ignoresSuppliedIds)
			this.graph = new IdGraph<KeyIndexableGraph>(graph);
		else
			this.graph = graph;
	}
	
	@Override
	public void addStreamEntry(String entityId, StreamEntry streamEntry)
	{
		if (null == entityId)
		{
			throw new IllegalArgumentException("entityId must not be null");
		}
		
		if (null == streamEntry)
		{
			throw new IllegalArgumentException("streamEntry must not be null");
		}
		
		Vertex entityVertex = getOrCreateEntityVertex(entityId);
		
		Vertex streamEntryVertex = createStreamEntryVertex(streamEntry);
		
		if (insertStreamEntry(entityVertex, streamEntryVertex))
			updateFeedPaths(entityVertex);
	}

	/**
	 * Attempts to retrieve the vertex for the entity with the given ID. If a
	 * matching entity cannot be found, the vertex is created.
	 * 
	 * @param entityId The ID of the entity for which to retrieve a vertex.
	 * @return The vertex for the given entity.
	 */
	private Vertex getOrCreateEntityVertex(final String entityId)
	{
		Vertex entityVertex = graph.getVertex(entityId);
		if (null == entityVertex)
		{
			entityVertex = graph.addVertex(entityId);
		}
		return entityVertex;
	}
	
	/**
	 * Creates a new vertex to represent a given stream entry.
	 * 
	 * @param streamEntry The stream entry to be represented.
	 * @return A vertex that represents the given stream entry.
	 */
	private Vertex createStreamEntryVertex(final StreamEntry streamEntry)
	{
		Vertex streamEntryVertex = graph.addVertex(null);
		streamEntryVertex.setProperty(STRING_TIME, 
				streamEntry.getTime().toString());
		return streamEntryVertex;
	}
	
	/**
	 * Adds a stream entry vertex at the correct chronological location among
	 * the stream vertices of an entity.
	 * 
	 * @param entity The vertex representing the entity.
	 * @param addedStreamEntry The stream entry to add to the stream.
	 * @return true if the added stream entry is the newest (first)in the
	 * stream, otherwise false.
	 */
	private boolean insertStreamEntry(final Vertex entity,
			final Vertex addedStreamEntry)
	{
		if (null == entity)
		{
			throw new IllegalArgumentException("entity must not be null");
		}
		
		if (null == addedStreamEntry)
		{
			throw new IllegalArgumentException(
					"addedStreamEntry must not be null");
		}
		
		StreamEntryDateComparator comparator = new StreamEntryDateComparator();
		
		Edge currentStreamEdge = getStreamEntryEdge(entity);
		Vertex currentStreamEntry = getNextStreamEntry(entity);
		Vertex previousStreamEntry = entity;
		int position = 0;		
		
		// advance along the stream path, comparing each stream entry to the
		// new entry
		while (currentStreamEntry != null &&
		       comparator.compare(addedStreamEntry, currentStreamEntry) > 0)
		{
			previousStreamEntry = currentStreamEntry;
			currentStreamEdge = getStreamEntryEdge(currentStreamEntry);
			currentStreamEntry = getNextStreamEntry(currentStreamEntry);
			position++;
		}
		
		// add a stream edge between the previous entry (the one that is newer
		// than the added one, or the entity if there are none) and the new one.
		graph.addEdge(null, previousStreamEntry, addedStreamEntry,
				STRING_STREAM_ENTRY);
		
		// if there are one or more entries that are older than the added one,
		// add an edge between the added one and the next older one, and delete
		// the edge between the that one and the previous (next newer) one.
		if (null != currentStreamEdge)
		{
			graph.addEdge(null, addedStreamEntry, currentStreamEntry,
					STRING_STREAM_ENTRY);
			graph.removeEdge(currentStreamEdge);
		}
		
		return position == 0;
	}
	
	/**
	 * Retrieves the edge to the next stream entry from the given vertex,
	 * whether the given vertex is an entity or a stream entry.
	 * @param node A stream entry or entity for which to find the next stream
	 * entry edge.
	 * @return The next stream edge in the stream containing or starting at
	 * the given vertex.
	 */
	private Edge getStreamEntryEdge(Vertex node)
	{
		return getSingleOutgoingEdge(node, STRING_STREAM_ENTRY);
	}
	
	/**
	 * Retrieves the single edge emanating from the given vertex where the edge
	 * has the given label.
	 * 
	 * @param node The vertex for which to find the labeled edge.
	 * @param edgeLabel The label of the edge to find.
	 * @return The edge with the given label emanating from the given vertex, or
	 * null if the node is null or has no such edge.
	 */
	private Edge getSingleOutgoingEdge(Vertex node, String edgeLabel)
	{
		if (null == node)
			return null;
		
		Iterator<Edge> edges =
				node.getEdges(Direction.OUT, edgeLabel).iterator();
		
		Edge edge = edges.hasNext() ? edges.next() : null;
		
		if (null != edge)
		{
			if (edges.hasNext())
			{
				throw new IllegalStateException(
					"Multiple outgoing edges with label: \"" +
					edgeLabel +
					"\" for vertex: " +
					node.getId());
			}
		}
		
		return edge;
	}
	
	private void updateFeedPaths(Vertex entity)
	{
		// get all the users that follow the entity
		Iterable<Vertex> users =
				entity.getVertices(Direction.IN, STRING_FOLLOWS);
		
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
	public List<StreamEntry> getStream(String entityId, long startIndex,
			int entriesToReturn)
	{
		Vertex entity = graph.getVertex(entityId);
		if (null == entity)
		{
			return new ArrayList<StreamEntry>();
		}
		
		int streamPosition = 0;
		int foundEntryCount = 0;
		List<Vertex> streamVertices = new ArrayList<Vertex>();
		
		Vertex currentStreamEntry = getNextStreamEntry(entity);
		
		while (null != currentStreamEntry && foundEntryCount < entriesToReturn)
		{
			if (streamPosition >= startIndex)
			{
				streamVertices.add(currentStreamEntry);
				foundEntryCount++;
			}
			currentStreamEntry = getNextStreamEntry(currentStreamEntry);
			streamPosition++;
		}
		
		return createStreamEntries(streamVertices);
	}
	
	private Vertex getNextStreamEntry(Vertex node)
	{
		Edge streamEntryEdge = getStreamEntryEdge(node);
		return null == streamEntryEdge ? null :
			streamEntryEdge.getVertex(Direction.IN);
	}

	private List<StreamEntry> createStreamEntries(
			Collection<Vertex> streamEntries)
	{
		ArrayList<StreamEntry> entries =
				new ArrayList<StreamEntry>();
		
		for (final Vertex vertex : streamEntries)
		{
			if (null != vertex)
			{
				entries.add(createStreamEntry(vertex));
			}
		}
		
		return entries;
	}
	
	private StreamEntry createStreamEntry(final Vertex streamEntry)
	{
		return new StreamEntry() {
			
			@Override
			public DateTime getTime()
			{
				return DateTime.parse((String) streamEntry
						.getProperty(STRING_TIME));
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
		
		graph.addEdge(null, user, entity, STRING_FOLLOWS);
		
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
		for (Edge edge: entity.getEdges(Direction.IN, STRING_FOLLOWS))
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
		
		EntityFirstStreamEntryDateComparator comparator = 
				new EntityFirstStreamEntryDateComparator();
		
		String feedLabel = getFeedLabel((String)user.getId());
		Edge currentFeedEdge = getSingleOutgoingEdge(user, feedLabel);
		Vertex currentFeedEntity = getNextFeedEntity(feedLabel, user,
				Direction.OUT);
		Vertex previousFeedEntity = user;
		int position = 0;		
						
		while (currentFeedEntity != null &&
		       comparator.compare(newEntity, currentFeedEntity) > 0)
		{
			previousFeedEntity = currentFeedEntity;
			currentFeedEdge = getStreamEntryEdge(currentFeedEntity);
			currentFeedEntity = getNextStreamEntry(currentFeedEntity);
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
	public List<StreamEntry> getFeed(String userId, long startIndex,
			int entriesToReturn)
	{
		StreamEntryDateComparator comparator = new StreamEntryDateComparator();		
		PriorityQueue<Vertex> queue =
				new PriorityQueue<Vertex>(11, comparator);
		ArrayList<Vertex> streamEntries = new ArrayList<Vertex>();
		Vertex topOfEntity = null;
		Vertex topOfQueue = null;
		
		Vertex user = getOrCreateEntityVertex(userId);
		String feedLabel = getFeedLabel(getIdString(user));
		Vertex entity = getNextFeedEntity(feedLabel, user, Direction.OUT);
		
		if (null != entity)
		{
			topOfEntity = getNextStreamEntry(entity);
			if (null != topOfEntity)
			{
				queue.add(topOfEntity);
				topOfQueue = topOfEntity;
			}
			entity = getNextFeedEntity(feedLabel, entity, Direction.OUT);
			topOfEntity = getNextStreamEntry(entity);
		}
		
		// while we have not yet hit our entries to return,
		// and there are still entries in the queue OR
		// there are more entities
		while (streamEntries.size() < (entriesToReturn + startIndex)
				&& (queue.size() > 0 || entity != null))
		{
			// compare top of next entity to top of queue
			int result = comparator.compare(topOfEntity, topOfQueue);

			// if top of next entity is newer, take the top element,
			// push the next element to the queue, and move to
			// the next entity
			if (result < 0)
			{
				streamEntries.add(topOfEntity);
				Vertex nextEntry = getNextStreamEntry(topOfEntity);
				if (null != nextEntry)
					queue.add(nextEntry);
				entity = getNextFeedEntity(feedLabel, entity, Direction.OUT);
				topOfEntity = getNextStreamEntry(entity);
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
					topOfEntity = getNextStreamEntry(entity);
				}
				// if top of queue is newer, take the top element, and
				// push the next element to the queue
				else
				{
					Vertex removedFromQueue = queue.remove();
					Vertex nextEntry = getNextStreamEntry(removedFromQueue);
					if (null != nextEntry)
						queue.add(nextEntry);
					streamEntries.add(removedFromQueue);
					topOfQueue = queue.peek();
				}
			}
		}

		return createStreamEntries(streamEntries);
	}

	@SuppressWarnings("unused")
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
		return STRING_FEED_LABEL_PREFIX + userId;
	}

	private class StreamEntryDateComparator implements Comparator<Vertex>
	{
		@Override
		public int compare(Vertex v1, Vertex v2)
		{
			long t1 = 0;
			if (null != v1)
				t1 = DateTime.parse((String)v1.getProperty(STRING_TIME))
					.getMillis();
			long t2 = 0;
			if (null != v2)
				t2 = DateTime.parse((String)v2.getProperty(STRING_TIME))
					.getMillis();

			return new Long(t2).compareTo(t1);
		}
	}
	
	private class EntityFirstStreamEntryDateComparator
		implements Comparator<Vertex>
	{
		@Override
		public int compare(Vertex v1, Vertex v2)
		{
			Vertex streamEntry = null;
			
			long t1 = 0;
			if (null != v1)
				streamEntry = getNextStreamEntry(v1);
			if (null != streamEntry)
				t1 = DateTime.parse((String)streamEntry.getProperty(STRING_TIME))
					.getMillis();
			
			long t2 = 0;
			if (null != v2)
				streamEntry = getNextStreamEntry(v2);
			if (null != streamEntry)
				t2 = DateTime.parse((String)streamEntry.getProperty(STRING_TIME))
					.getMillis();
			
			return new Long(t2).compareTo(t1);
		}
	}
	
	private static final String STRING_TIME = "Time";
	private static final String STRING_FOLLOWS = "Follows";
	private static final String STRING_STREAM_ENTRY = "StreamEntry";
	private static final String STRING_FEED_LABEL_PREFIX = "Feed+";
}
