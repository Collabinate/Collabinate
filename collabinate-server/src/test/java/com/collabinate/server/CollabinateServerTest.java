package com.collabinate.server;

import static org.junit.Assert.*;

import org.joda.time.DateTime;
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
		server.addStreamItem(null, new StreamItemData() {

			@Override
			public DateTime getTime()
			{
				return null;
			} });
	}
	
	@Test
	public void add_stream_item_should_not_allow_null_stream_item()
	{
		CollabinateServer server =
				createServer();
		exception.expect(IllegalArgumentException.class);
		server.addStreamItem("", null);
	}
	
	@Test
	public void adding_a_stream_item_should_allow_retrieval_of_the_item()
	{
		CollabinateServer server =
				createServer();
		final DateTime instant = DateTime.now();
		server.addStreamItem("1", new StreamItemData() {
			
			@Override
			public DateTime getTime()
			{
				return instant;
			}
		});
		DateTime returnedTime =
				server.getStream("1", 0, 1)[0].getTime();
		assertEquals(instant.getMillis(), returnedTime.getMillis());
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
