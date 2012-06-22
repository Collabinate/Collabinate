package com.collabinate.server;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CollabinateServerTest
{
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Test
	public void should_not_be_null()
	{
		CollabinateServer server =
				new CollabinateServer();
		assertNotNull(server);
	}

	@Test
	public void should_not_allow_null_ID()
	{
		CollabinateServer server =
				new CollabinateServer();
		exception.expect(IllegalArgumentException.class);
		server.CreateUser(null);
	}
}
