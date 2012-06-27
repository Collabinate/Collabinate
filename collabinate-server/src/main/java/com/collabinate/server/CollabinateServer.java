package com.collabinate.server;

public interface CollabinateServer
{
	public void addStreamItem(String entityId, StreamItemData streamItem);
	
	public void followEntity(String userId, String entityId);
}
