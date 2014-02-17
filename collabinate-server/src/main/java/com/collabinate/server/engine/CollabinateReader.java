package com.collabinate.server.engine;

import org.joda.time.DateTime;

import com.collabinate.server.activitystreams.Activity;
import com.collabinate.server.activitystreams.ActivityStreamsCollection;
import com.collabinate.server.activitystreams.ActivityStreamsObject;

/**
 * The interface for a Collabinate server that performs read (retrieval)
 * operations.
 * 
 * @author mafuba
 *
 */
public interface CollabinateReader
{
	/**
	 * Retrieves a single activity, or null if no matching activity exists.
	 * 
	 * @param tenantId the tenant for which the request is processed.
	 * @param entityId the ID of the entity for which to retrieve an activity.
	 * @param activityId the ID of the activity to retrieve.
	 * @return An activity that matches the given parameters, or null if no
	 * matching activity exists.
	 */
	public Activity getActivity(String tenantId, String entityId,
			String activityId);
	/**
	 * Retrieves a collection of activities for an entity, with paging ability. 
	 * 
	 * @param tenantId the tenant for which the request is processed.
	 * @param entityId The ID of the entity for which to retrieve a stream.
	 * @param startIndex The zero-based index of the first element to retrieve.
	 * @param activitiesToReturn The maximum number of activities to retrieve.
	 * @return A collection of activities for the given entity.
	 */
	public ActivityStreamsCollection getStream(String tenantId, String entityId,
			int startIndex, int activitiesToReturn);
	
	/**
	 * Retrieves a collection of activities for the entities that a user
	 * follows, in chronological order.
	 * 
	 * @param tenantId the tenant for which the request is processed.
	 * @param userId The ID of the user for which to retrieve a feed.
	 * @param startIndex The zero-based index of the first element to retrieve.
	 * @param activitiesToReturn The maximum number of activities to retrieve.
	 * @return A collection of activities for the given user.
	 */
	public ActivityStreamsCollection getFeed(String tenantId, String userId,
			int startIndex, int activitiesToReturn);
	
	/**
	 * Retrieves a DateTime value for when a user followed an entity, or null if
	 * the user does not follow the entity.
	 * 
	 * @param tenantId the tenant for which the request is processed.
	 * @param userId The ID of the user for which to determine when an entity
	 * was followed.
	 * @param entityId The ID of the entity to check when the user followed.
	 * @return The DateTime of the start of the follow relationship if the given
	 * user follows the given entity, otherwise null.
	 */
	public DateTime getDateTimeUserFollowedEntity(String tenantId, String userId,
			String entityId);
	
	/**
	 * Retrieves the collection of entities followed by a user.
	 * 
	 * @param tenantId the tenant for which the request is processed.
	 * @param userId the ID of the user for which to retrieve the followed
	 * entities.
	 * @param startIndex The zero-based index of the first entity to retrieve.
	 * @param entitiesToReturn The maximum number of entities to retrieve.
	 * @return A collection of entities followed by the given user.
	 */
	public ActivityStreamsCollection getFollowing(String tenantId,
			String userId, int startIndex, int entitiesToReturn);
	
	/**
	 * Retrieves the collection of users that follow an entity.
	 * 
	 * @param tenantId the tenant for which the request is processed.
	 * @param entityId the ID of the entity for which the followers will be
	 * retrieved.
	 * @param startIndex The zero-based index of the first follower to retrieve.
	 * @param entitiesToReturn The maximum number of followers to retrieve.
	 * @return A collection of users following the given entity.
	 */
	public ActivityStreamsCollection getFollowers(String tenantId,
			String entityId, int startIndex, int followersToReturn);
	
	/**
	 * Retrieves a single comment, or null if no matching comment exists.
	 * 
	 * @param tenantId the tenant for which the request is processed.
	 * @param entityId The ID of the entity to which the activity belongs.
	 * @param activityId The ID of the activity from which to retrieve the
	 * comment.
	 * @param commentId The ID of the comment to retrieve.
	 * @return The ActivityStreamsObject representation of the comment matching
	 * the parameters, or null if none exists.
	 */
	public ActivityStreamsObject getComment(String tenantId, String entityId,
			String activityId, String commentId);
	
	/**
	 * Retrieves the collection of comments on an activity.
	 * 
	 * @param tenantId the tenant for which the request is processed.
	 * @param entityId The ID of the entity to which the activity belongs.
	 * @param activityId The ID of the activity from which to retrieve the
	 * comments.
	 * @param startIndex The zero-based index of the first comment to retrieve.
	 * @param commentsToReturn The maximum number of comments to retrieve.
	 * @return A collection of comments on the given activity.
	 */
	public ActivityStreamsCollection getComments(String tenantId,
			String entityId, String activityId, int startIndex,
			int commentsToReturn);
	
	/**
	 * Retrieves the status of a like relationship between a user and an
	 * activity.
	 * 
	 * @param tenantId the tenant for which the request is processed.
	 * @param userId The ID of the user to check for the like relationship.
	 * @param entityId The ID of the entity to which the activity belongs.
	 * @param activityId The ID of the activity to check for the like
	 * relationship.
	 * @return The date the user liked the activity, or null if no like
	 * relationship exists.
	 */
	public DateTime userLikesActivity(String tenantId, String userId,
			String entityId, String activityId);
	
	/**
	 * Retrieves the collection of users that like a given activity.
	 * 
	 * @param tenantId the tenant for which the request is processed.
	 * @param entityId The ID of the entity to which the activity belongs.
	 * @param activityId The ID of the activity for which to retrieve the liking
	 * users.
	 * @param startIndex The zero-based index of the first user to retrieve.
	 * @param usersToReturn The maximum number of users to retrieve.
	 * @return An ActivityStreamsCollection populated with
	 * ActivityStreamsObjects representing the liking users, or null if the
	 * activity does not exist.
	 */
	public ActivityStreamsCollection getLikingUsers(String tenantId,
			String entityId, String activityId, int startIndex,
			int usersToReturn);
}
