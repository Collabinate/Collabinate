package com.collabinate.server.resources;

import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import com.collabinate.server.activitystreams.ActivityStreamsCollection;
import com.collabinate.server.activitystreams.ActivityStreamsObject;
import com.collabinate.server.engine.CollabinateReader;
import com.collabinate.server.engine.CollabinateWriter;
import com.google.common.hash.Hashing;

/**
 * Restful resource representing a collection of comments on an activity.
 * 
 * @author mafuba
 *
 */
public class CommentsResource extends ServerResource
{
	@Get("json")
	public Representation getComments()
	{
		// extract necessary information from the context
		CollabinateReader reader = (CollabinateReader)getContext()
				.getAttributes().get("collabinateReader");
		String tenantId = getAttribute("tenantId");
		String entityId = getAttribute("entityId");
		String activityId = getAttribute("activityId");
		String skipString = getQueryValue("skip");
		String takeString = getQueryValue("take");
		int skip = null == skipString ? 0 : Integer.parseInt(skipString);
		int take = null == takeString ? DEFAULT_COUNT : 
			Integer.parseInt(takeString);
		
		ActivityStreamsCollection commentsCollection =
			reader.getComments(tenantId, entityId, activityId, skip, take);
		
		if (null != commentsCollection)
		{
			String comments = commentsCollection.toString();
			Representation representation = new StringRepresentation(
					comments, MediaType.APPLICATION_JSON);
			representation.setTag(
				new Tag(Hashing.murmur3_128().hashUnencodedChars(
				comments+tenantId+entityId+activityId+skipString+takeString)
				.toString(), false));
			
			return representation;
		}
		else
		{
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return null;
		}
	}
	
	@Post
	public void addComment(String content)
	{
		// extract necessary information from the context
		CollabinateWriter writer = (CollabinateWriter)getContext()
				.getAttributes().get("collabinateWriter");
		CollabinateReader reader = (CollabinateReader)getContext()
				.getAttributes().get("collabinateReader");
		String tenantId = getAttribute("tenantId");
		String entityId = getAttribute("entityId");
		String activityId = getAttribute("activityId");
		String userId = getQueryValue("userId");
		
		// ensure the activity exists
		if (null == reader.getActivity(tenantId, entityId, activityId))
		{
			// TODO: set error message
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return;
		}
		
		// create a comment from the given content
		ActivityStreamsObject comment = new ActivityStreamsObject(content);
		
		// generate an id and relocate the original if necessary
		String originalId = comment.getId();
		String id = generateId();
		comment.setId(id);
		
		if (null != originalId && !originalId.equals(""))
		{
			comment.setCollabinateValue(ORIGINAL_ID, originalId);
		}
		
		// set the object type to comment and relocate the original if necessary
		String originalObjectType = comment.getObjectType();
		comment.setObjectType(COMMENT);
		
		if (null != originalObjectType && !originalObjectType.equals(""))
		{
			comment.setCollabinateValue(
					ORIGINAL_OBJECT_TYPE, originalObjectType);
		}
		
		// set the published date if it does not exist
		if (null == comment.getPublished())
		{
			comment.setPublished(DateTime.now(DateTimeZone.UTC));
		}
		
		// put the associated user into a special property
		if (null != userId && !userId.equals(""))
		{
			comment.setCollabinateValue(USER_ID, userId);
		}
		
		// keep track of the entityID and activityID in the comment
		comment.setCollabinateValue("entityId", entityId);
		comment.setCollabinateValue("activityId", activityId);
		
		writer.addComment(tenantId, entityId, activityId, userId, comment);
		
		// return the comment in the response body
		getResponse().setEntity(comment.toString(),
				MediaType.APPLICATION_JSON);
		
		//TODO: return relative reference location
		setLocationRef(new Reference(getReference())
			.addSegment(comment.getId()));
		setStatus(Status.SUCCESS_CREATED);
	}
	
	/**
	 * Generates an ID for an comment.
	 * 
	 * @return A globally unique URI acceptable for use in a comment ID.
	 */
	private String generateId()
	{
		// TODO: allow this to be configured
		return "tag:collabinate.com:" + UUID.randomUUID().toString();
	}
	
	private static final int DEFAULT_COUNT = 20;
	private static final String ORIGINAL_ID = "originalId";
	private static final String COMMENT = "comment";
	private static final String ORIGINAL_OBJECT_TYPE = "originalObjectType";
	private static final String USER_ID = "userId";
}
