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
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter;
import com.tinkerpop.blueprints.util.wrappers.id.IdGraph;
import com.tinkerpop.blueprints.util.wrappers.partition.PartitionIndexableGraph;

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
	private PartitionIndexableGraph<IndexableGraph> graph;
	
	/**
	 * Whether the server should automatically commit transactions.
	 */
	private boolean autoCommit = true;
		
	/**
	 * Ensures that the graph can have IDs assigned.
	 * 
	 * @param graph A Tinkerpop BluePrints graph to act as the store for the
	 * server.
	 */
	public GraphServer(final KeyIndexableGraph graph)
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
		this.graph = new PartitionIndexableGraph<IndexableGraph>(
				indexableGraph, "_tenant", "c");
	}
	
	/**
	 * Sets whether the server should automatically commit transactions.
	 * 
	 * @param autoCommit The setting for whether the server automatically
	 * commits transactions. True by default.
	 */
	public void setAutoCommit(boolean autoCommit)
	{
		this.autoCommit = autoCommit;
	}
	
	/**
	 * Causes the graph database to commit the current transaction.
	 */
	private void commit()
	{
		if (autoCommit && graph.getFeatures().supportsTransactions)
		{
			((TransactionalGraph)graph).commit();
		}
	}
	
	@Override
	public void addStreamEntry(String tenantId, String entityId,
			StreamEntry streamEntry)
	{
		if (null == tenantId)
		{
			throw new IllegalArgumentException("tenantId must not be null");
		}
		
		if (null == entityId)
		{
			throw new IllegalArgumentException("entityId must not be null");
		}
		
		if (null == streamEntry)
		{
			throw new IllegalArgumentException("streamEntry must not be null");
		}
		
		Vertex entityVertex = getOrCreateEntityVertex(entityId);
		
		Vertex streamEntryVertex = serializeStreamEntry(streamEntry);
		
		if (insertStreamEntry(entityVertex, streamEntryVertex))
			updateFeedPaths(tenantId, entityVertex);
		
		commit();
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
	 * Creates a new vertex representation of a given stream entry.
	 * 
	 * @param streamEntry The stream entry to be represented.
	 * @return A vertex that represents the given stream entry.
	 */
	private Vertex serializeStreamEntry(final StreamEntry streamEntry)
	{
		Vertex streamEntryVertex = graph.addVertex(null);
		streamEntryVertex.setProperty(STRING_ID, streamEntry.getId());
		streamEntryVertex.setProperty(STRING_TIME, 
				streamEntry.getTime().toString());
		streamEntryVertex.setProperty(STRING_CONTENT, streamEntry.getContent());
		return streamEntryVertex;
	}
	
	/**
	 * Adds a stream entry vertex at the correct chronological location among
	 * the stream vertices of an entity.
	 * 
	 * @param entity The vertex representing the entity.
	 * @param addedStreamEntry The stream entry to add to the stream.
	 * @return true if the added stream entry is the newest (first) in the
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
		previousStreamEntry.addEdge(STRING_STREAM_ENTRY, addedStreamEntry);
		
		// if there are one or more entries that are older than the added one,
		// add an edge between the added one and the next older one, and delete
		// the edge between the that one and the previous (next newer) one.
		if (null != currentStreamEdge)
		{
			addedStreamEntry.addEdge(STRING_STREAM_ENTRY, currentStreamEntry);
			currentStreamEdge.remove();
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
	
	/**
	 * Puts an entity into the correct chronological order in the feed paths
	 * of all the users that follow it.  This is used for changes to the first
	 * stream entry of an entity, which potentially changes its feed order.
	 * 
	 * @param entity The entity for which followers are updated.
	 */
	private void updateFeedPaths(String tenantId, Vertex entity)
	{
		// get all the users that follow the entity
		Iterable<Vertex> usersInGraph =
				entity.getVertices(Direction.IN, STRING_FOLLOWS);
		
		// copy the users to a separate list to prevent the collection
		// underlying the iterable getting modified during processing
		ArrayList<Vertex> users = new ArrayList<Vertex>();
		for (Vertex user : usersInGraph)
		{
			users.add(user);
		}
		
		// loop over each user and move the entity to the correct
		// feed position by un-following and re-following
		// TODO: is this the best way to do this?
		String entityId = entity.getId().toString();
		String userId;
		for (Vertex user : users)
		{
			userId = getIdString(user);
			unfollowEntity(tenantId, userId, entityId);
			followEntity(tenantId, userId, entityId);
		}
	}
	
	/**
	 * Gets the ID of a vertex in string form.
	 * 
	 * @param vertex The vertex for which the ID will be returned.
	 * @return The ID of the vertex formatted as a string.
	 */
	private String getIdString(Vertex vertex)
	{
		return vertex.getId().toString();
	}
	
	@Override
	public void deleteStreamEntry(String tenantId, String entityId,
			String entryId)
	{
		if (null == tenantId)
			throw new IllegalArgumentException("tenantId must not be null");
		
		if (null == entityId)
			throw new IllegalArgumentException("entityId must not be null");
		
		if (null == entryId)
			throw new IllegalArgumentException("entryId must not be null");
		
		Vertex entityVertex = getOrCreateEntityVertex(entityId);
				
		if (removeStreamEntry(entityVertex, entryId))
			updateFeedPaths(tenantId, entityVertex);
		
		commit();
	}
	
	/**
	 * Deletes the stream entry vertex that matches the given entryId within
	 * the stream of the given entity.  The continuity of the stream is
	 * maintained.
	 * 
	 * @param entityVertex The vertex representing the entity.
	 * @param entryId The ID of the stream entry to delete from the stream.
	 * @return true if the deleted stream entry was the newest (first) in the
	 * stream, otherwise false.
	 */
	private boolean removeStreamEntry(Vertex entityVertex, String entryId)
	{
		if (null == entityVertex)
			throw new IllegalArgumentException("entityVertex must not be null");
		
		if (null == entryId)
			throw new IllegalArgumentException("entryId must not be null");
		
		Vertex currentStreamEntry = getNextStreamEntry(entityVertex);
		Vertex previousStreamEntry = entityVertex;
		int position = 0;		
		
		// advance along the stream path, checking each stream entry for a match
		while (currentStreamEntry != null)
		{
			// if a match is found, remove it and make a new edge from the
			// previous entry to the following entry (if one exists)
			if (entryId.equals(currentStreamEntry.getProperty(STRING_ID)))
			{
				Vertex followingEntry = getNextStreamEntry(currentStreamEntry);
				currentStreamEntry.remove();
				if (null != followingEntry)
					previousStreamEntry.addEdge(STRING_STREAM_ENTRY,
							followingEntry);
				currentStreamEntry = null;
			}
			// if no match, proceed along the stream updating the pointers
			else
			{
				previousStreamEntry = currentStreamEntry;
				currentStreamEntry = getNextStreamEntry(currentStreamEntry);
				position++;
			}
		}
		
		return position == 0;
	}

	@Override
	public List<StreamEntry> getStream(String tenantId, String entityId,
			long startIndex, int entriesToReturn)
	{
		Vertex entity = graph.getVertex(entityId);
		if (null == entity)
		{
			return new ArrayList<StreamEntry>();
		}
		
		// since we need to advance from the beginning of the stream,
		// this lets us keep track of where we are
		int streamPosition = 0;
		// once we reach the number of entries to return, we can stop
		int foundEntryCount = 0;
		
		List<Vertex> streamVertices = new ArrayList<Vertex>();
		
		Vertex currentStreamEntry = getNextStreamEntry(entity);
		
		// advance along the stream, collecting vertices after we get to the
		// start index, and stopping when we have enough to return or run out
		// of stream
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
		
		commit();
		
		// we only have the vertices, the actual entries need to be created
		return deserializeStreamEntries(streamVertices);
	}
	
	/**
	 * Retrieves the stream entry after the given node by following the outgoing
	 * stream edge. The node can be an entity (including users) or a stream
	 * entry.
	 * 
	 * @param node The entity or stream entry for which to find the next stream
	 * entry.
	 * @return The next stream entry after the given node, or null if one does
	 * not exist.
	 */
	private Vertex getNextStreamEntry(Vertex node)
	{
		Edge streamEntryEdge = getStreamEntryEdge(node);
		return null == streamEntryEdge ? null :
			streamEntryEdge.getVertex(Direction.IN);
	}

	/**
	 * Turns a collection of stream entry vertices into a collection of stream
	 * entries.
	 * 
	 * @param streamEntryVertices The vertices to deserialize.
	 * @return A collection of stream entries that were represented by the
	 * given vertices.
	 */
	private List<StreamEntry> deserializeStreamEntries(
			Collection<Vertex> streamEntryVertices)
	{
		ArrayList<StreamEntry> entries =
				new ArrayList<StreamEntry>();
		
		for (final Vertex vertex : streamEntryVertices)
		{
			if (null != vertex)
			{
				entries.add(deserializeStreamEntry(vertex));
			}
		}
		
		return entries;
	}
	
	/**
	 * Deserializes a vertex representing a stream entry.
	 * 
	 * @param streamEntryVertex The vertex to deserialize.
	 * @return A stream entry that was represented by the given vertex.
	 */
	private StreamEntry deserializeStreamEntry(final Vertex streamEntryVertex)
	{
		String id = (String)streamEntryVertex.getProperty(STRING_ID);
		DateTime time = DateTime.parse((String) streamEntryVertex
				.getProperty(STRING_TIME));
		String content = (String)streamEntryVertex.getProperty(STRING_CONTENT);
		
		return new StreamEntry(id, time, content);
	}

	@Override
	public void followEntity(String tenantId, String userId, String entityId)
	{
		if (null == tenantId)
		{
			throw new IllegalArgumentException("tenantId must not be null");
		}
		
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
		
		user.addEdge(STRING_FOLLOWS, entity);
		
		insertFeedEntity(user, entity);
		
		commit();
	}
	
	@Override
	public void unfollowEntity(String tenantId, String userId, String entityId)
	{
		if (null == tenantId)
		{
			throw new IllegalArgumentException("tenantId must not be null");
		}

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
				edge.remove();
		}
		
		// remove the entity from the user feed by removing the feed edges
		// into and out of it
		Vertex previousEntity = getNextFeedEntity(feedLabel, entity,
				Direction.IN);
		Vertex nextEntity = getNextFeedEntity(feedLabel, entity,
				Direction.OUT);
		for (Edge edge: entity.getEdges(Direction.BOTH, feedLabel))
		{
			edge.remove();
		}
		
		// replace the missing edge for the feed if necessary
		if (null != nextEntity)
		{
			previousEntity.addEdge(feedLabel, nextEntity);
		}
		
		commit();
	}
	
	@Override
	public Boolean isUserFollowingEntity(String tenantId, String userId,
			String entityId)
	{
		Vertex user = getOrCreateEntityVertex(userId);
		Vertex entity = getOrCreateEntityVertex(entityId);
		
		for (Edge edge : user.getEdges(Direction.OUT, STRING_FOLLOWS))
		{
			if (edge.getVertex(Direction.IN).getId().equals(entity.getId()))
			{
				commit();
				return true;
			}
		}

		commit();
		
		return false;
	}

	/**
	 * Inserts an entity into the feed for a user.
	 * 
	 * @param user The user whose feed will be updated.
	 * @param newEntity the entity to add to the user's feed.
	 * @return true if the entity is first in the feed, otherwise false.
	 */
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
		
		// we order entities based on the date of their first stream entry
		EntityFirstStreamEntryDateComparator comparator = 
				new EntityFirstStreamEntryDateComparator();
		
		// start with the user and the first feed entity
		String feedLabel = getFeedLabel((String)user.getId());
		Edge currentFeedEdge = getSingleOutgoingEdge(user, feedLabel);
		Vertex currentFeedEntity = getNextFeedEntity(feedLabel, user,
				Direction.OUT);
		Vertex previousFeedEntity = user;
		int position = 0;		
		
		// advance along the feed until we find where the new entity belongs
		while (currentFeedEntity != null &&
		       comparator.compare(newEntity, currentFeedEntity) > 0)
		{
			previousFeedEntity = currentFeedEntity;
			currentFeedEdge = getSingleOutgoingEdge(currentFeedEntity,
					feedLabel);
			currentFeedEntity = getNextFeedEntity(feedLabel, currentFeedEntity,
					Direction.OUT);
			position++;
		}
		
		// add an edge from the previous entity to the new one
		previousFeedEntity.addEdge(feedLabel, newEntity);
		
		// if there are following entities, add an edge from the new one to the
		// following one
		if (null != currentFeedEdge)
		{
			newEntity.addEdge(feedLabel, currentFeedEntity);
			currentFeedEdge.remove();
		}
		
		// if we didn't advance, the entity is the first in the feed
		return position == 0;
	}

	@Override
	public List<StreamEntry> getFeed(String tenantId, String userId,
			long startIndex, int entriesToReturn)
	{
		// this method represents the core of the Graphity algorithm
		// http://www.rene-pickhardt.de/graphity-an-efficient-graph-model-for-retrieving-the-top-k-news-feeds-for-users-in-social-networks/
		
		// we order stream entries by their date
		StreamEntryDateComparator comparator = new StreamEntryDateComparator();
		
		// a priority queue is used to order the entries that are "next" for
		// each entity we've already reached in the feed
		PriorityQueue<Vertex> queue =
				new PriorityQueue<Vertex>(11, comparator);
		ArrayList<Vertex> streamEntries = new ArrayList<Vertex>();
		
		// this is used to track the newest entry in the farthest entity reached
		Vertex topOfEntity = null;
		
		// this is used to track the newest entry within all of the streams for
		// entities that have already been reached
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

		commit();
		
		return deserializeStreamEntries(streamEntries);
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
	
	/**
	 * Retrieves an adjacent entity in a path of entities for a feed, where the
	 * entities are ordered by their newest stream entry.
	 * 
	 * @param feedLabel The unique-per-user label for the edges in the feed.
	 * @param currentEntity The start entity.
	 * @param direction Whether to get the next entity (out) or the previous
	 * entity (in).
	 * @return The entity adjacent to the given entity for the given feed in the
	 * given direction, or null if one does not exist.
	 */
	private Vertex getNextFeedEntity(String feedLabel,
			Vertex currentEntity, Direction direction)
	{
		if (null == currentEntity)
			return null;
		
		// get a list of vertices along edges that match the label and direction
		// of the given values
		Iterator<Vertex> vertices = 
				currentEntity
				.getVertices(direction, feedLabel)
				.iterator();
		
		// get the first vertex if it exists
		Vertex vertex = vertices.hasNext() ? vertices.next() : null;
		
		// if there are more vertices, the feed is incorrectly structured
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
	
	/**
	 * Builds a feed edge label based on a common prefix prepended to the
	 * userId.
	 * 
	 * @param userId The ID of the user for which to return a feed label.
	 * @return A feed edge label constructed from the given user ID.
	 */
	private String getFeedLabel(String userId)
	{
		return STRING_FEED_LABEL_PREFIX + userId;
	}

	/**
	 * A comparator for stream entry vertices that orders by the entry time.
	 * 
	 * @author mafuba
	 *
	 */
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
	
	/**
	 * A comparator for entity vertices that orders by the time of the first
	 * stream entry for the entity.
	 * 
	 * @author mafuba
	 *
	 */
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
				t1 = DateTime.parse((String)streamEntry
						.getProperty(STRING_TIME)).getMillis();
			
			streamEntry = null;
			long t2 = 0;
			if (null != v2)
				streamEntry = getNextStreamEntry(v2);
			if (null != streamEntry)
				t2 = DateTime.parse((String)streamEntry
						.getProperty(STRING_TIME)).getMillis();
			
			return new Long(t2).compareTo(t1);
		}
	}
	
	private static final String STRING_ID = "ID";
	private static final String STRING_TIME = "Time";
	private static final String STRING_CONTENT = "Content";
	private static final String STRING_FOLLOWS = "Follows";
	private static final String STRING_STREAM_ENTRY = "StreamEntry";
	private static final String STRING_FEED_LABEL_PREFIX = "Feed+";
}
