package com.collabinate.server.resources;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import com.collabinate.server.activitystreams.ActivityStreamsCollection;
import com.collabinate.server.activitystreams.ActivityStreamsObject;
import com.collabinate.server.engine.CollabinateReader;
import com.collabinate.server.engine.CollabinateWriter;;

/**
 * Restful resource representing the entities followed by a user.
 * 
 * @author mafuba
 *
 */
public class FollowingResource extends ServerResource
{
	@Get("json")
	public String getFollowing()
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
		
		return reader.getFollowing(tenantId, userId, skip, take).toString();
	}
	
	@Post
	public void addFollowing(String collectionString)
	{
		// extract necessary information from the context
		CollabinateWriter writer = (CollabinateWriter)getContext()
				.getAttributes().get("collabinateWriter");
		String tenantId = getAttribute("tenantId");
		String userId = getAttribute("userId");

		ActivityStreamsCollection followingCollection =
				new ActivityStreamsCollection(collectionString);
		
		for (ActivityStreamsObject following : followingCollection.getItems())
		{
			String id = following.getId();
			if (null != id && !id.equals(""))
			{
				writer.followEntity(tenantId, userId, id,
						DateTime.now(DateTimeZone.UTC));
			}
		}
	}
	
	private static final int DEFAULT_TAKE = 20;
}
