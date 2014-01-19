package com.collabinate.server.engine;

import org.joda.time.DateTime;

import com.collabinate.server.StreamEntry;

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
	 * Adds an entry to an entity's stream, at the correct chronological
	 * location. This method should be implemented to be as idempotent as
	 * possible, e.g. stream entries with the same exact time and content should
	 * not be duplicated.
	 * 
	 * @param tenantId The tenant for the operation.
	 * @param entityId The ID of the entity to which an entry will be added.
	 * This value must not be null.
	 * @param streamEntry The entry to add. This value must not be null.
	 */
	public void addStreamEntry(String tenantId, String entityId,
			StreamEntry streamEntry);
	
	/**
	 * Deletes an entry from an entity's stream.
	 * 
	 * @param tenantId The tenant for the operation.
	 * @param entityId The ID of the entity from which an entry will be removed.
	 * This value must not be null.
	 * @param entryId The ID of the entry to remove. The first matching entry
	 * with this ID value will be deleted. This value must not be null.
	 */
	public void deleteStreamEntry(String tenantId, String entityId,
			String entryId);
	
	/**
	 * Adds an entity to the collection of entities a user follows.
	 * 
	 * @param tenantId The tenant for the operation.
	 * @param userId The ID of the user that follows the entity.
	 * @param entityId The ID of the entity that the user follows.
	 * @param dateFollowed The date that the follow relationship occurred. If
	 * null is passed, the current date will be used.
	 */
	public void followEntity(String tenantId, String userId, String entityId,
			DateTime dateFollowed);
	
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
