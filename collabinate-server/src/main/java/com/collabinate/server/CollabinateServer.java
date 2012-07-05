package com.collabinate.server;

public interface CollabinateServer
{
	public void addStreamItem(String entityId, StreamItemData streamItem);
	
	public StreamItemData[] getStream(String entityId, long startIndex, int itemsToReturn);
	
	public void followEntity(String userId, String entityId);
}
