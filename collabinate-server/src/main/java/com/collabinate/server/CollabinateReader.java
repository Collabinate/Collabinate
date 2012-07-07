package com.collabinate.server;

public interface CollabinateReader
{
	public StreamItemData[] getStream(String entityId, long startIndex, int itemsToReturn);
}
