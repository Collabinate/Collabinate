package com.collabinate.server.resources;

import java.util.ArrayList;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Tag;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.collabinate.server.activitystreams.ActivityStreamsCollection;
import com.collabinate.server.activitystreams.ActivityStreamsObject;
import com.collabinate.server.engine.CollabinateReader;
import com.google.common.hash.Hashing;

/**
 * Restful resource representing the collection of activities for all entities
 * followed by a user.
 * 
 * @author mafuba
 *
 */
public class FeedResource extends ServerResource
{
	@Get("json")
	public Representation getFeed()
	{
		// extract necessary information from the context
		CollabinateReader reader = (CollabinateReader)getContext()
				.getAttributes().get("collabinateReader");
		String tenantId = getAttribute("tenantId");
		String userId = getAttribute("userId");
		String skipString = getQueryValue("skip");
		String takeString = getQueryValue("take");
		int skip = null == skipString ? 0 : Integer.parseInt(skipString);
		int take = null == takeString ? DEFAULT_TAKE : 
			Integer.parseInt(takeString);
		
		ActivityStreamsCollection activitiesCollection =
				reader.getFeed(tenantId, userId, skip, take);
		
		appendCollections(activitiesCollection, reader, tenantId);
		
		String result = activitiesCollection.toString();
		
		Representation representation = new StringRepresentation(
				result, MediaType.APPLICATION_JSON);
		representation.setTag(new Tag(Hashing.murmur3_128().hashUnencodedChars(
				result+tenantId+userId+skipString+takeString)
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
	 */
	private void appendCollections(
			ActivityStreamsCollection activitiesCollection,
			CollabinateReader reader, String tenantId)
	{
		String commentsString = getQueryValue("comments");
		String likesString = getQueryValue("likes");
		
		if (null != commentsString || null != likesString)
		{
			boolean processComments = null != commentsString;
			boolean processLikes = null != likesString;
			int comments = processComments ? 
					Integer.parseInt(commentsString) : 0;
			int likes = processLikes ?
					Integer.parseInt(likesString) : 0;
			List<ActivityStreamsObject> activities =
					activitiesCollection.getItems();
			List<ActivityStreamsObject> updatedActivities =
					new ArrayList<ActivityStreamsObject>();
			
			for (ActivityStreamsObject activity : activities)
			{
				String entityId = activity.getCollabinateValue("entityId");
				
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
				
				updatedActivities.add(activity);
			}
			
			activitiesCollection.setItems(updatedActivities);
		}
	}
		
	private static final int DEFAULT_TAKE = 20;
}
