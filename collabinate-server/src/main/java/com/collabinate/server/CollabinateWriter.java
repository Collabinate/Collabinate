package com.collabinate.server;

/**
 * The interface for a Collabinate server that performs write (update)
 * operations.
 * 
 * @author mafuba
 *
 */
public interface CollabinateWriter
{
	/**
	 * Adds an entry to an entity's stream, at the correct chronological
	 * location.
	 * 
	 * @param entityId The entity for which an entry will be added.
	 * @param streamEntry The entry to add.
	 */
	public void addStreamEntry(String entityId, StreamEntry streamEntry);
	
	/**
	 * Adds an entity to the collection of entities a user follows.
	 * 
	 * @param userId The ID of the user that follows the entity.
	 * @param entityId The ID of the entity that the user follows.
	 */
	public void followEntity(String userId, String entityId);
}
