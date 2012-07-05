package com.collabinate.server;

public class DefaultServer implements CollabinateServer
{
	@Override
	public void addStreamItem(String entityId, StreamItemData streamItem)
	{
		if (null == entityId)
		{
			throw new IllegalArgumentException("entityId must not be null");
		}
	}

	@Override
	public void followEntity(String userId, String entityId)
	{
		if (null == entityId)
		{
			throw new IllegalArgumentException("userId must not be null");
		}
	}
}
