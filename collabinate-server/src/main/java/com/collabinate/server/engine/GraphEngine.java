package com.collabinate.server.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.collabinate.server.activitystreams.Activity;
import com.collabinate.server.activitystreams.ActivityStreamsObject;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

/**
 * An implementation of both reader and writer backed by a graph database.
 * 
 * @author mafuba
 * 
 */
public class GraphEngine implements CollabinateReader, CollabinateWriter
{
	/**
	 * Logger instance.
	 */
	private final Logger logger = LoggerFactory.getLogger(GraphEngine.class);
	
	/**
	 * The graph database backing this instance.
	 */
	private CollabinateGraph graph;
	
	/**
	 * Ensures that the graph can have IDs assigned.
	 * 
	 * @param graph A Tinkerpop BluePrints graph to act as the store for the
	 * server.
	 */
	public GraphEngine(final CollabinateGraph graph)
	{
		if (null == graph)
		{
			throw new IllegalArgumentException("graph must not be null");
		}
		
		this.graph = graph;
	}
	
	@Override
	public void addActivity(String tenantId, String entityId,
			Activity activity)
	{
		if (null == tenantId)
		{
			throw new IllegalArgumentException("tenantId must not be null");
		}
		
		if (null == entityId)
		{
			throw new IllegalArgumentException("entityId must not be null");
		}
		
		if (null == activity)
		{
			throw new IllegalArgumentException("activity must not be null");
		}
		
		entityId = tenantId + "/" + entityId;
		
		Vertex entityVertex = getOrCreateEntityVertex(entityId, tenantId);
		
		String activityVertexId = entityId + "/" + activity.getId();
		
		Vertex activityVertex = serializeActivity(activityVertexId, activity,
				tenantId);
		
		if (insertActivity(entityVertex, activityVertex, tenantId))
			// if the inserted activity is first in its stream, it may have
			// changed the entity order for feed paths
			updateFeedPaths(tenantId, entityVertex);
		
		graph.commit();		
	}

	/**
	 * Attempts to retrieve the vertex for the entity with the given ID. If a
	 * matching entity cannot be found, the vertex is created.
	 * 
	 * @param entityId The ID of the entity for which to retrieve a vertex.
	 * @param tenantId The ID of the tenant to add to the vertex upon creation.
	 * @return The vertex for the given entity.
	 */
	private synchronized Vertex getOrCreateEntityVertex(final String entityId,
			final String tenantId)
	{
		Vertex entityVertex = graph.getVertex(entityId);
		if (null == entityVertex)
		{
			entityVertex = graph.addVertex(entityId);
			entityVertex.setProperty(STRING_TENANT_ID, tenantId);
			graph.commit();
		}
		return entityVertex;
	}
	
	/**
	 * Creates a new vertex representation of a given activity.
	 * 
	 * @param activity The activity to be represented.
	 * @param tenantId The tenant for the activity.
	 * @return A vertex that represents the given activity.
	 */
	private Vertex serializeActivity(final String vertexId,
			final Activity activity, final String tenantId)
	{
		Vertex activityVertex = graph.addVertex(vertexId);
		activityVertex.setProperty(STRING_TENANT_ID, tenantId);
		activityVertex.setProperty(STRING_SORTTIME, 
				activity.getSortTime().toString());
		activityVertex.setProperty(STRING_CONTENT, activity.toString());
		return activityVertex;
	}
	
	/**
	 * Adds an activity vertex at the correct chronological location among the
	 * stream vertices of an entity.
	 * 
	 * @param entity The vertex representing the entity.
	 * @param addedActivity The activity to add to the stream.
	 * @param tenantId The tenant for the entity.
	 * @return true if the added activity is the newest (first) in the stream,
	 * otherwise false.
	 */
	private boolean insertActivity(final Vertex entity,
			final Vertex addedActivity, final String tenantId)
	{
		if (null == entity)
		{
			throw new IllegalArgumentException("entity must not be null");
		}
		
		if (null == addedActivity)
		{
			throw new IllegalArgumentException(
					"addedActivity must not be null");
		}
		
		ActivityDateComparator comparator = new ActivityDateComparator();
		
		Edge currentStreamEdge = getStreamEdge(entity);
		Vertex currentActivity = getNextActivity(entity);
		Vertex previousActivity = entity;
		int position = 0;
		
		// advance along the stream path, comparing each activity to the new one
		while (currentActivity != null &&
		       comparator.compare(addedActivity, currentActivity) > 0)
		{
			previousActivity = currentActivity;
			currentStreamEdge = getStreamEdge(currentActivity);
			currentActivity = getNextActivity(currentActivity);
			position++;
		}
		
		// add a stream edge between the previous activity (the one that is 
		// newer than the added one, or the entity if there are none) and the
		// new one.
		previousActivity.addEdge(STRING_STREAM, addedActivity)
			.setProperty(STRING_TENANT_ID, tenantId);
		
		// if there are one or more activities that are older than the added
		// one, add an edge between the added one and the next older one, and
		// delete the edge between the that one and the previous (next newer)
		// one.
		if (null != currentStreamEdge)
		{
			addedActivity.addEdge(STRING_STREAM, currentActivity)
				.setProperty(STRING_TENANT_ID, tenantId);
			currentStreamEdge.remove();
		}
		
		return position == 0;
	}
	
	/**
	 * Retrieves the edge to the next activity from the given vertex, whether
	 * the given vertex is an entity or a activity.
	 * 
	 * @param node An activity or entity for which to find the next stream edge.
	 * @return The next stream edge in the stream containing or starting at
	 * the given vertex.
	 */
	private Edge getStreamEdge(Vertex node)
	{
		return getSingleOutgoingEdge(node, STRING_STREAM);
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
				logger.error("Multiple outgoing edges with label: \"{}\" " +
					" for vertex: {}", edgeLabel, node.getId());
			}
		}
		
		return edge;
	}
	
	/**
	 * Puts an entity into the correct chronological order in the feed paths
	 * of all the users that follow it.  This is used for changes to the first
	 * activity of an entity, which potentially changes its feed order.
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
			DateTime followed = unfollowEntity(tenantId, userId, entityId);
			followEntity(tenantId, userId, entityId, followed);
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
	public void deleteActivity(String tenantId, String entityId,
			String activityId)
	{
		if (null == tenantId)
			throw new IllegalArgumentException("tenantId must not be null");
		
		if (null == entityId)
			throw new IllegalArgumentException("entityId must not be null");
		
		if (null == activityId)
			throw new IllegalArgumentException("activityId must not be null");
		
		entityId = tenantId + "/" + entityId;
		
		Vertex entityVertex = getOrCreateEntityVertex(entityId, tenantId);
				
		if (removeActivity(entityVertex, activityId, entityId))
			// if the deleted activity was first in its stream, it may have
			// changed the entity order for feed paths
			updateFeedPaths(tenantId, entityVertex);
		
		graph.commit();
	}
	
	/**
	 * Deletes the activity vertex that matches the given activityId within the
	 * stream of the given entity.  The continuity of the stream is maintained.
	 * 
	 * @param entityVertex The vertex representing the entity.
	 * @param activityId The ID of the activity to delete from the stream.
	 * @param entityId The entity for the stream.
	 * @return true if the deleted activity was the newest (first) in the
	 * stream, otherwise false.
	 */
	private boolean removeActivity(Vertex entityVertex, String activityId,
			String entityId)
	{
		if (null == entityVertex)
			throw new IllegalArgumentException("entityVertex must not be null");
		
		if (null == activityId)
			throw new IllegalArgumentException("activityId must not be null");
		
		Vertex currentActivity = getNextActivity(entityVertex);
		Vertex previousActivity = entityVertex;
		String activityVertexId = entityId + "/" + activityId;
		int position = 0;		
		
		// advance along the stream path, checking each activity for a match
		while (currentActivity != null)
		{
			// if a match is found, remove it and make a new edge from the
			// previous activity to the following activity (if one exists)
			if (activityVertexId.equals(currentActivity.getId()))
			{
				Vertex followingActivity = getNextActivity(currentActivity);
				currentActivity.remove();
				if (null != followingActivity)
					previousActivity.addEdge(STRING_STREAM,
							followingActivity).setProperty(
									STRING_TENANT_ID, entityId);
				currentActivity = null;
			}
			// if no match, proceed along the stream updating the pointers
			else
			{
				previousActivity = currentActivity;
				currentActivity = getNextActivity(currentActivity);
				position++;
			}
		}
		
		return position == 0;
	}

	@Override
	public List<Activity> getStream(String tenantId, String entityId,
			long startIndex, int activitiesToReturn)
	{
		entityId = tenantId + "/" + entityId;
		
		Vertex entity = graph.getVertex(entityId);
		if (null == entity)
		{
			return new ArrayList<Activity>();
		}
		
		// since we need to advance from the beginning of the stream,
		// this lets us keep track of where we are
		int streamPosition = 0;
		// once we reach the number of activities to return, we can stop
		int foundActivityCount = 0;
		
		List<Vertex> activityVertices = new ArrayList<Vertex>();
		
		Vertex currentActivity = getNextActivity(entity);
		
		// advance along the stream, collecting vertices after we get to the
		// start index, and stopping when we have enough to return or run out
		// of stream
		while (null != currentActivity &&
				foundActivityCount < activitiesToReturn)
		{
			if (streamPosition >= startIndex)
			{
				activityVertices.add(currentActivity);
				foundActivityCount++;
			}
			currentActivity = getNextActivity(currentActivity);
			streamPosition++;
		}
		
		graph.commit();
		
		// we only have the vertices, the actual activities need to be created
		return deserializeActivities(activityVertices);
	}
	
	/**
	 * Retrieves the activity after the given node by following the outgoing
	 * stream edge. The node can be an entity (including users) or an activity.
	 * 
	 * @param node The entity or activity for which to find the next activity.
	 * @return The next activity after the given node, or null if one does not
	 * exist.
	 */
	private Vertex getNextActivity(Vertex node)
	{
		Edge streamEdge = getStreamEdge(node);
		return null == streamEdge ? null : streamEdge.getVertex(Direction.IN);
	}

	/**
	 * Turns a collection of activity vertices into a collection of activities.
	 * 
	 * @param activityVertices The vertices to deserialize.
	 * @return A collection of activities that were represented by the given
	 * vertices.
	 */
	private List<Activity> deserializeActivities(
			Collection<Vertex> activityVertices)
	{
		ArrayList<Activity> activities = new ArrayList<Activity>();
		
		for (final Vertex vertex : activityVertices)
		{
			if (null != vertex)
			{
				activities.add(deserializeActivity(vertex));
			}
		}
		
		return activities;
	}
	
	/**
	 * Deserializes a vertex representing an activity.
	 * 
	 * @param activityVertex The vertex to deserialize.
	 * @return An activity that was represented by the given vertex.
	 */
	private Activity deserializeActivity(final Vertex activityVertex)
	{
		String content = (String)activityVertex.getProperty(STRING_CONTENT);
		
		return new Activity(content);
	}

	@Override
	public DateTime followEntity(String tenantId, String userId,
			String entityId, DateTime followed)
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
		
		if (null == followed)
		{
			followed = DateTime.now(DateTimeZone.UTC);
		}
		
		entityId = tenantId + "/" + entityId;
		userId = tenantId + "/" + userId;
		
		Vertex user = getOrCreateEntityVertex(userId, tenantId);
		Vertex entity = getOrCreateEntityVertex(entityId, tenantId);
		
		for (Edge edge : user.getEdges(Direction.OUT, STRING_FOLLOWS))
		{
			if (edge.getVertex(Direction.IN).getId().equals(entity.getId()))
			{
				DateTime existingDateTime =
						DateTime.parse((String)edge.getProperty(STRING_SORTTIME));
				
				graph.commit();
				return existingDateTime;
			}
		}
		
		Edge followEdge = user.addEdge(STRING_FOLLOWS, entity);
		followEdge.setProperty(STRING_TENANT_ID, tenantId);
		followEdge.setProperty(STRING_SORTTIME, followed.toString());
		
		insertFeedEntity(user, entity, tenantId);
		
		graph.commit();
		
		return followed;
	}
	
	@Override
	public DateTime unfollowEntity(String tenantId, String userId,
			String entityId)
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
		
		entityId = tenantId + "/" + entityId;
		userId = tenantId + "/" + userId;
		
		Vertex user = getOrCreateEntityVertex(userId, tenantId);
		Vertex entity = getOrCreateEntityVertex(entityId, tenantId);
		String feedLabel = getFeedLabel(getIdString(user));
		DateTime followed = null;
		
		// remove the follow relationship
		for (Edge edge: entity.getEdges(Direction.IN, STRING_FOLLOWS))
		{
			if (edge.getVertex(Direction.OUT).getId().equals(
					user.getId()))
				followed = 
					DateTime.parse((String)edge.getProperty(STRING_SORTTIME));
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
			previousEntity.addEdge(feedLabel, nextEntity)
				.setProperty(STRING_TENANT_ID, tenantId);
		}
		
		graph.commit();
		return followed;
	}
	
	@Override
	public DateTime getDateTimeUserFollowedEntity(String tenantId,
			String userId, String entityId)
	{
		entityId = tenantId + "/" + entityId;
		userId = tenantId + "/" + userId;
		
		Vertex user = getOrCreateEntityVertex(userId, tenantId);
		Vertex entity = getOrCreateEntityVertex(entityId, tenantId);
		
		for (Edge edge : user.getEdges(Direction.OUT, STRING_FOLLOWS))
		{
			if (edge.getVertex(Direction.IN).getId().equals(entity.getId()))
			{
				graph.commit();
				return DateTime.parse((String)edge.getProperty(STRING_SORTTIME));
			}
		}
		
		graph.commit();
		
		return null;
	}

	@Override
	public List<ActivityStreamsObject> getFollowing(String tenantId,
			String userId, long startIndex, int entitiesToReturn)
	{
		userId = tenantId + "/" + userId;
		
		Vertex user = getOrCreateEntityVertex(userId, tenantId);
		List<ActivityStreamsObject> following =
				new ArrayList<ActivityStreamsObject>();
		
		long currentPosition = 0;
		
		// find all the followed entities and add them to the following list
		for (Edge edge : user.getEdges(Direction.OUT, STRING_FOLLOWS))
		{
			if (currentPosition >= startIndex)
			{
				ActivityStreamsObject entity = new ActivityStreamsObject();
				// remove the tenant id from the entity id before adding
				entity.setId(edge.getVertex(Direction.IN).getId().toString()
						.replaceFirst(tenantId + "/", ""));
				following.add(entity);
				if (following.size() >= entitiesToReturn)
					break;
			}
			currentPosition++;
		}

		graph.commit();
		
		return following;
	}
	
	@Override
	public List<ActivityStreamsObject> getFollowers(String tenantId,
			String entityId, long startIndex, int followersToReturn)
	{
		entityId = tenantId + "/" + entityId;
		
		Vertex entity = getOrCreateEntityVertex(entityId, tenantId);
		List<ActivityStreamsObject> followers =
				new ArrayList<ActivityStreamsObject>();
		
		long currentPosition = 0;
		
		// find all the following users and add them to the followers list
		for (Edge edge : entity.getEdges(Direction.IN, STRING_FOLLOWS))
		{
			if (currentPosition >= startIndex)
			{
				ActivityStreamsObject user = new ActivityStreamsObject();
				// remove the tenant id from the user id before adding
				user.setId(edge.getVertex(Direction.OUT).getId().toString()
						.replaceFirst(tenantId + "/", ""));
				followers.add(user);
				if (followers.size() >= followersToReturn)
					break;
			}
			currentPosition++;
		}

		graph.commit();
		
		return followers;
	}

	/**
	 * Inserts an entity into the feed for a user.
	 * 
	 * @param user The user whose feed will be updated.
	 * @param newEntity the entity to add to the user's feed.
	 * @param tenantId the tenant for the feed.
	 * @return true if the entity is first in the feed, otherwise false.
	 */
	private boolean insertFeedEntity(final Vertex user, final Vertex newEntity,
			final String tenantId)
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
		
		// we order entities based on the date of their first activity
		EntityFirstActivityDateComparator comparator = 
				new EntityFirstActivityDateComparator();
		
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
		previousFeedEntity.addEdge(feedLabel, newEntity)
			.setProperty(STRING_TENANT_ID, tenantId);
		
		// if there are following entities, add an edge from the new one to the
		// following one
		if (null != currentFeedEdge)
		{
			newEntity.addEdge(feedLabel, currentFeedEntity)
				.setProperty(STRING_TENANT_ID, tenantId);
			currentFeedEdge.remove();
		}
		
		// if we didn't advance, the entity is the first in the feed
		return position == 0;
	}

	@Override
	public List<Activity> getFeed(String tenantId, String userId,
			long startIndex, int activitiesToReturn)
	{
		// this method represents the core of the Graphity algorithm
		// http://www.rene-pickhardt.de/graphity-an-efficient-graph-model-for-retrieving-the-top-k-news-feeds-for-users-in-social-networks/
		
		userId = tenantId + "/" + userId;
		
		// we order activities by their date
		ActivityDateComparator comparator = new ActivityDateComparator();
		
		// a priority queue is used to order the activities that are "next" for
		// each entity we've already reached in the feed
		PriorityQueue<Vertex> queue =
				new PriorityQueue<Vertex>(11, comparator);
		ArrayList<Vertex> activities = new ArrayList<Vertex>();
		
		// since we need to advance from the beginning of the feed, this lets
		// us keep track of where we are
		long feedPosition = 0;
		
		// this is used to track the newest activity in the last entity reached
		Vertex topOfEntity = null;
		
		// this is used to track the newest activity within all of the streams
		// for entities that have already been reached
		Vertex topOfQueue = null;
		
		Vertex user = getOrCreateEntityVertex(userId, tenantId);
		String feedLabel = getFeedLabel(getIdString(user));
		Vertex entity = getNextFeedEntity(feedLabel, user, Direction.OUT);
		
		if (null != entity)
		{
			topOfEntity = getNextActivity(entity);
			if (null != topOfEntity)
			{
				queue.add(topOfEntity);
				topOfQueue = topOfEntity;
			}
			entity = getNextFeedEntity(feedLabel, entity, Direction.OUT);
			topOfEntity = getNextActivity(entity);
		}
		
		// while we have not yet hit our activities to return,
		// and there are still activities in the queue OR
		// there are more entities
		while (activities.size() < activitiesToReturn
				&& (queue.size() > 0 || entity != null))
		{
			// compare top of next entity to top of queue
			int result = comparator.compare(topOfEntity, topOfQueue);

			// if top of next entity is newer, take the top element,
			// push the next element to the queue, and move to
			// the next entity
			if (result < 0)
			{
				if (feedPosition >= startIndex)
					activities.add(topOfEntity);
				Vertex nextActivity = getNextActivity(topOfEntity);
				if (null != nextActivity)
					queue.add(nextActivity);
				entity = getNextFeedEntity(feedLabel, entity, Direction.OUT);
				topOfEntity = getNextActivity(entity);
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
					topOfEntity = getNextActivity(entity);
				}
				// if top of queue is newer, take the top element, and
				// push the next element to the queue
				else
				{
					Vertex removedFromQueue = queue.remove();
					Vertex nextActivity = getNextActivity(removedFromQueue);
					if (null != nextActivity)
						queue.add(nextActivity);
					if (feedPosition >= startIndex)
						activities.add(removedFromQueue);
					topOfQueue = queue.peek();
				}
			}
			
			feedPosition++;
		}
		
		graph.commit();
		
		return deserializeActivities(activities);
	}

	/**
	 * Retrieves an adjacent entity in a path of entities for a feed, where the
	 * entities are ordered by their newest activity.
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
				logger.error("Multiple feed edges for vertex: {} " +
						"with feedLabel: {}", vertex.getId(), feedLabel);
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
	 * A comparator for activity vertices that orders by the sort time.
	 * 
	 * @author mafuba
	 *
	 */
	private class ActivityDateComparator implements Comparator<Vertex>
	{
		@Override
		public int compare(Vertex v1, Vertex v2)
		{
			long t1 = 0;
			if (null != v1)
				t1 = DateTime.parse((String)v1.getProperty(STRING_SORTTIME))
					.getMillis();
			long t2 = 0;
			if (null != v2)
				t2 = DateTime.parse((String)v2.getProperty(STRING_SORTTIME))
					.getMillis();

			return new Long(t2).compareTo(t1);
		}
	}
	
	/**
	 * A comparator for entity vertices that orders by the time of the first
	 * activity for the entity.
	 * 
	 * @author mafuba
	 *
	 */
	private class EntityFirstActivityDateComparator
		implements Comparator<Vertex>
	{
		@Override
		public int compare(Vertex v1, Vertex v2)
		{
			Vertex activity = null;
			
			long t1 = 0;
			if (null != v1)
				activity = getNextActivity(v1);
			if (null != activity)
				t1 = DateTime.parse((String)activity
						.getProperty(STRING_SORTTIME)).getMillis();
			
			activity = null;
			long t2 = 0;
			if (null != v2)
				activity = getNextActivity(v2);
			if (null != activity)
				t2 = DateTime.parse((String)activity
						.getProperty(STRING_SORTTIME)).getMillis();
			
			return new Long(t2).compareTo(t1);
		}
	}
	
	private static final String STRING_TENANT_ID = "TenantID";
	private static final String STRING_SORTTIME = "SortTime";
	private static final String STRING_CONTENT = "Content";
	private static final String STRING_FOLLOWS = "Follows";
	private static final String STRING_STREAM = "Stream";
	private static final String STRING_FEED_LABEL_PREFIX = "Feed+";
}
