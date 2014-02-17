package com.collabinate.server.resources;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

import com.collabinate.server.activitystreams.ActivityStreamsObject;
import com.collabinate.server.engine.CollabinateReader;
import com.collabinate.server.engine.CollabinateWriter;
import com.google.common.hash.Hashing;

/**
 * Restful resource representing a comment on an activity.
 * 
 * @author mafuba
 *
 */
public class CommentResource extends ServerResource
{
	@Get("json")
	public Representation getComment()
	{
		// extract necessary information from the context
		CollabinateReader reader = (CollabinateReader)getContext()
				.getAttributes().get("collabinateReader");
		String tenantId = getAttribute("tenantId");
		String entityId = getAttribute("entityId");
		String activityId = getAttribute("activityId");
		String commentId = getAttribute("commentId");

		ActivityStreamsObject matchingComment =
				reader.getComment(tenantId, entityId, activityId, commentId);
		
		if (null != matchingComment)
		{
			Representation representation = new StringRepresentation(
					matchingComment.toString(), MediaType.APPLICATION_JSON);
			representation.setTag(
				new Tag(Hashing.murmur3_128().hashUnencodedChars(
				matchingComment.toString()
				+tenantId+entityId+activityId+commentId)
				.toString(), false));
			
			return representation;
		}
		else
		{
			// TODO: set error message
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return null;
		}
	}
	
	@Put
	public void putComment(String content)
	{
		// extract necessary information from the context
		CollabinateReader reader = (CollabinateReader)getContext()
				.getAttributes().get("collabinateReader");
		CollabinateWriter writer = (CollabinateWriter)getContext()
				.getAttributes().get("collabinateWriter");
		String tenantId = getAttribute("tenantId");
		String entityId = getAttribute("entityId");
		String activityId = getAttribute("activityId");
		String commentId = getAttribute("commentId");
		String userId = getQueryValue("userId");
		
		// ensure the activity exists
		if (null == reader.getActivity(tenantId, entityId, activityId))
		{
			// TODO: set error message
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return;
		}
		
		// remove any existing comment
		writer.deleteComment(tenantId, entityId, activityId, commentId);
		
		// create a comment from the given content
		ActivityStreamsObject comment = new ActivityStreamsObject(content);
		
		// ensure the comment has an id - set to given id if not
		String id = comment.getId();
		if (null == id || id.equals(""))
		{
			id = commentId;
			comment.setId(id);
		}
		
		// if the URL ID differs from the comment ID, the comment cannot be
		// processed
		if (!commentId.equals(id))
		{
			// TODO: set error message
			setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
			return;
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
		
		writer.addComment(tenantId, entityId, activityId, userId, comment);
		
		// return the comment in the response body
		getResponse().setEntity(comment.toString(),
				MediaType.APPLICATION_JSON);
		
		setStatus(Status.SUCCESS_OK);
	}
	
	@Delete
	public void deleteComment()
	{
		// extract necessary information from the context
		CollabinateWriter writer = (CollabinateWriter)getContext()
				.getAttributes().get("collabinateWriter");
		String tenantId = getAttribute("tenantId");
		String entityId = getAttribute("entityId");
		String activityId = getAttribute("activityId");
		String commentId = getAttribute("commentId");
		
		// remove any existing comment
		writer.deleteComment(tenantId, entityId, activityId, commentId);
	}
	
	private static final String COMMENT = "comment";
	private static final String ORIGINAL_OBJECT_TYPE = "originalObjectType";
	private static final String USER_ID = "userId";
}
