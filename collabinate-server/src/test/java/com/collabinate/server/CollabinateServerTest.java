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
	public void add_stream_item_should_not_allow_null_entity_ID()
	{
		CollabinateServer server =
				createServer();
		exception.expect(IllegalArgumentException.class);
		server.addStreamItem(null, null);
	}
	
	@Test
	public void follow_entity_should_not_allow_null_user_ID()
	{
		CollabinateServer server =
				createServer();
		exception.expect(IllegalArgumentException.class);
		server.followEntity(null, null);
	}
}
