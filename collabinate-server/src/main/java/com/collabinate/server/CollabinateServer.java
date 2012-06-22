package com.collabinate.server;

public class CollabinateServer
{
	public void CreateUser(final String userId)
		throws IllegalArgumentException
	{
		if (null == userId) {
			throw new IllegalArgumentException("userId must not be null");
		}
	}
}
