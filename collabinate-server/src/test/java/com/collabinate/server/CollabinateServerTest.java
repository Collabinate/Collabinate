package com.collabinate.server;

import static org.junit.Assert.*;

import org.junit.Test;

public class CollabinateServerTest
{
	@Test
	public void shouldInstantiate()
	{
		CollabinateServer server =
				new CollabinateServer();
		assertNotNull(server);
	}
	
	@Test
	public void shouldCreateUser()
	{
		CollabinateServer server =
				new CollabinateServer();
		User user = server.CreateUser(null);
		assertNotNull(user);
	}
	
	@Test
	public void shouldHaveProvidedId()
	{
		String id = "testId";
		CollabinateServer server =
				new CollabinateServer();
		User user = server.CreateUser(id);
		assertEquals(id, user.getId());
	}
}
