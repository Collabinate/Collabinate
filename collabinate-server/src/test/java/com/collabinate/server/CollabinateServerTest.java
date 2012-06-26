package com.collabinate.server;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public abstract class CollabinateServerTest
{
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	abstract CollabinateServer createServer();
	
	@Test
	public void should_not_be_null()
	{
		CollabinateServer server =
				createServer();
		assertNotNull(server);
	}

	@Test
	public void create_user_should_not_allow_null_ID()
	{
		CollabinateServer server =
				createServer();
		exception.expect(IllegalArgumentException.class);
		server.createUser(null);
	}
	
	@Test
	public void create_entity_should_not_allow_null_ID()
	{
		CollabinateServer server =
				createServer();
		exception.expect(IllegalArgumentException.class);
		server.createEntity(null);
	}
}
