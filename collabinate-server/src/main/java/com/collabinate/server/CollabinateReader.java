package com.collabinate.server;

import java.util.List;

public interface CollabinateReader
{
	public List<StreamItemData> getStream(String entityId, long startIndex, int itemsToReturn);
	
	public List<StreamItemData> getFeed(String userId, long startIndex, int itemsToReturn);
}
