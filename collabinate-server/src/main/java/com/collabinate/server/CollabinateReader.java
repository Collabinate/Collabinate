package com.collabinate.server;

import java.util.List;

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
	 * Retrieves a collection of entries for an entity, with paging ability. 
	 * 
	 * @param entityId The ID of the entity for which to retrieve a stream.
	 * @param startIndex The zero-based index of the first element to retrieve.
	 * @param itemsToReturn The maximum number of stream entries to retrieve.
	 * @return A collection of stream entries for the given entity.
	 */
	public List<StreamItemData> getStream(String entityId, long startIndex, int itemsToReturn);
	
	/**
	 * Retrieves a collection of stream entries for the entities that a user
	 * follows, in chronological order.
	 * 
	 * @param userId The ID of the user for which to retrieve a feed.
	 * @param startIndex The zero-based index of the first element to retrieve.
	 * @param itemsToReturn The maximum number of feed entries to retrieve.
	 * @return A collection of feed entries for the given user.
	 */
	public List<StreamItemData> getFeed(String userId, long startIndex, int itemsToReturn);
}
