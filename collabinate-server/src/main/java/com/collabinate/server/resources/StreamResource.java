package com.collabinate.server.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import com.collabinate.server.activitystreams.Activity;
import com.collabinate.server.activitystreams.ActivityStreamsCollection;
import com.collabinate.server.activitystreams.ActivityStreamsObject;
import com.collabinate.server.engine.CollabinateReader;
import com.collabinate.server.engine.CollabinateWriter;
import com.google.common.hash.Hashing;

/**
 * Restful resource representing a series of activities for an entity.
 * 
 * @author mafuba
 *
 */
public class StreamResource extends ServerResource
{
	@Get("json")
	public Representation getStream()
	{
		// extract necessary information from the context
		CollabinateReader reader = (CollabinateReader)getContext()
				.getAttributes().get("collabinateReader");
		String tenantId = getAttribute("tenantId");
		String entityId = getAttribute("entityId");
		String skipString = getQueryValue("skip");
		String takeString = getQueryValue("take");
		int skip = null == skipString ? 0 : Integer.parseInt(skipString);
		int take = null == takeString ? DEFAULT_COUNT : 
			Integer.parseInt(takeString);
		
		ActivityStreamsCollection activitiesCollection =
				reader.getStream(tenantId, entityId, skip, take);
		
		appendCollections(activitiesCollection, reader, tenantId, entityId);
		
		String result = activitiesCollection.toString();
		
		Representation representation = new StringRepresentation(
				result, MediaType.APPLICATION_JSON);
		representation.setTag(new Tag(Hashing.murmur3_128().hashUnencodedChars(
				result+tenantId+entityId+skipString+takeString)
				.toString(), false));
		
		return representation;
	}

	/**
	 * Appends the comments and likes collection values to each of the items
	 * in the given collection of activities, according to the value of the
	 * comments and likes query values. Note that even zero values will cause
	 * empty collections (with the correct counts) to be appended.
	 * 
	 * @param activitiesCollection The collection of activities that will have
	 * comments and likes added.
	 * @param reader The CollabinateReader to use for getting the collections.
	 * @param tenantId The tenant ID.
	 * @param entityId The entity ID.
	 */
	private void appendCollections(
			ActivityStreamsCollection activitiesCollection,
			CollabinateReader reader, String tenantId, String entityId)
	{
		String commentsString = getQueryValue("comments");
		String likesString = getQueryValue("likes");
		String userLikedString = getQueryValue("userLiked");
		
		if (null != commentsString || 
			null != likesString ||
			null != userLikedString)
		{
			boolean processComments = null != commentsString;
			boolean processLikes = null != likesString;
			boolean processUserLiked = null != userLikedString;
			int comments = processComments ? 
					Integer.parseInt(commentsString) : 0;
			int likes = processLikes ?
					Integer.parseInt(likesString) : 0;
			String likingUser = processUserLiked ?
					userLikedString : null;
			List<ActivityStreamsObject> activities =
					activitiesCollection.getItems();
			List<ActivityStreamsObject> updatedActivities =
					new ArrayList<ActivityStreamsObject>();
			
			for (ActivityStreamsObject activity : activities)
			{
				if (processComments)
				{
					activity.setReplies(reader.getComments(tenantId, entityId,
						activity.getId(), 0, comments));
				}
				if (processLikes)
				{
					activity.setLikes(reader.getLikes(tenantId, entityId,
						activity.getId(), 0, likes));
				}
				if (processUserLiked)
				{
					DateTime likedDate = reader.userLikesActivity(
							tenantId, likingUser, entityId, activity.getId());
					
					if (null != likedDate)
					{
						activity.setCollabinateValue("likedByUser",
								likedDate.toString());
					}
				}
				
				updatedActivities.add(activity);
			}
			
			activitiesCollection.setItems(updatedActivities);
		}
	}
	
	@Post
	public void addActivity(String content)
	{
		// extract necessary information from the context
		CollabinateWriter writer = (CollabinateWriter)getContext()
				.getAttributes().get("collabinateWriter");
		String tenantId = getAttribute("tenantId");
		String entityId = getAttribute("entityId");
		String ignoreCommentsString = getQueryValue("ignoreComments");
		String ignoreLikesString = getQueryValue("ignoreLikes");
		
		if (null == writer)
			throw new IllegalStateException(
					"Context does not contain a CollabinateWriter");
		
		// create an activity from the given content
		Activity activity = new Activity(content);
		
		// generate an id and relocate the original if necessary
		String originalId = activity.getId();
		String activityId = generateId();
		activity.setId(activityId);
		
		if (null != originalId && !originalId.equals(""))
		{
			activity.setCollabinateValue(ORIGINAL_ID, originalId);
		}
		
		// keep track of the entityID in the activity
		activity.setCollabinateValue("entityId", entityId);
		
		writer.addActivity(tenantId, entityId, activity);
		
		// if the activity has comments and we're not ignoring them,
		// add them to the database properly
		boolean ignoreComments = null != ignoreCommentsString &&
				!ignoreCommentsString.equalsIgnoreCase("false");
		
		ActivityStreamsCollection replies = activity.getReplies();
		
		if (!ignoreComments && null != replies && replies.size() > 0)
		{
			for (ActivityStreamsObject comment : replies.getItems())
			{
				// ensure the comment has an id - set to generated id if not
				String commentId = comment.getId();
				if (null == commentId || commentId.equals(""))
				{
					commentId = generateId();
					comment.setId(commentId);
				}
				writer.addComment(tenantId, entityId, activityId, null,
						comment);
			}
		}
		
		// if the activity has likes and we're not ignoring them,
		// add them to the database properly
		boolean ignoreLikes = null != ignoreLikesString &&
				!ignoreLikesString.equalsIgnoreCase("false");
		
		ActivityStreamsCollection likes = activity.getLikes();
		
		if (!ignoreLikes && null != likes && likes.size() > 0)
		{
			for (ActivityStreamsObject likeObject : likes.getItems())
			{
				Activity like = new Activity(likeObject.toString());
				ActivityStreamsObject actor = like.getActor();
				if (null != actor)
				{
					String userId = actor.getId();
					if (null != userId && !userId.equals(""))
					{
						writer.likeActivity(tenantId, userId, entityId,
								activityId);
					}
				}
			}
		}
		
		// return the activity in the response body
		getResponse().setEntity(activity.toString(),
				MediaType.APPLICATION_JSON);
		
		//TODO: return relative reference location
		setLocationRef(new Reference(getReference())
			.addSegment(activity.getId()));
		setStatus(Status.SUCCESS_CREATED);
	}
	
	/**
	 * Generates an ID for an activity streams object.
	 * 
	 * @return A globally unique URI acceptable for use in an activity streams
	 * object ID.
	 */
	private String generateId()
	{
		// TODO: allow this to be configured
		return "tag:collabinate.com:" + UUID.randomUUID().toString();
	}
	
	private static final int DEFAULT_COUNT = 20;
	private static final String ORIGINAL_ID = "originalId";
}


