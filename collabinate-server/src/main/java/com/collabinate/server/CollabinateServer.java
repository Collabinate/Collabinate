package com.collabinate.server;

public class CollabinateServer
{
	public User CreateUser(final String userId)
	{
		return new User()
		{
			@Override
			public String getId()
			{
				return userId;
			}
		};
	}
}
