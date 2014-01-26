package com.collabinate.server.engine;

import org.joda.time.DateTime;

import com.collabinate.server.activitystreams.Activity;

/**
 * The interface for a Collabinate server that performs write (create, update,
 * delete) operations.
 * 
 * @author mafuba
 *
 */
public interface CollabinateWriter
{
	/**
	 * Adds an activity to an entity's stream, at the correct chronological
	 * location. This method should be implemented to be as idempotent as
	 * possible, e.g. activities with the same exact time and content should
	 * not be duplicated.
	 * 
	 * @param tenantId The tenant for the operation.
	 * @param entityId The ID of the entity to which an activity will be added.
	 * This value must not be null.
	 * @param activity The activity to add. This value must not be null.
	 */
	public void addActivity(String tenantId, String entityId,
			Activity activity);
	
	/**
	 * Deletes an activity from an entity's stream.
	 * 
	 * @param tenantId The tenant for the operation.
	 * @param entityId The ID of the entity from which the activity will be
	 * removed. This value must not be null.
	 * @param activityId The ID of the activity to remove. The first matching
	 * activity with this ID value will be deleted. This value must not be null.
	 */
	public void deleteActivity(String tenantId, String entityId,
			String activityId);
	
	/**
	 * Adds an entity to the collection of entities a user follows.
	 * 
	 * @param tenantId The tenant for the operation.
	 * @param userId The ID of the user that follows the entity.
	 * @param entityId The ID of the entity that the user follows.
	 * @param dateFollowed The date that the follow relationship occurred. If
	 * null is passed, the current date will be used.
	 * @return The date of the follow relationship. If the relationship already
	 * existed before this call, the existing date will be returned.
	 */
	public DateTime followEntity(String tenantId, String userId,
			String entityId, DateTime dateFollowed);
	
	/**
	 * Removes an entity from the collection of entities a user follows.
	 * 
	 * @param tenantId The tenant for the operation.
	 * @param userId The ID of the user that follows the entity.
	 * @param entityId The ID of the entity that the user follows.
	 * @return The date that the deleted follow relationship occurred.
	 */
	public DateTime unfollowEntity(String tenantId, String userId,
			String entityId);
}
