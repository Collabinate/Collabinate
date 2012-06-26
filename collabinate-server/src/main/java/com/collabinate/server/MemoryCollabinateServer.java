package com.collabinate.server;

public class MemoryCollabinateServer implements CollabinateServer
{
	public void createUser(final String userId)
		throws IllegalArgumentException
	{
		if (null == userId)
		{
			throw new IllegalArgumentException("userId must not be null");
		}
	}
	
	public void createEntity(final String entityId)
		throws IllegalArgumentException
	{
		if (null == entityId)
		{
			throw new IllegalArgumentException("entityId must not be null");
		}
	}
}
