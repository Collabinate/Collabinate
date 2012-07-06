package com.collabinate.server;

import static org.junit.Assert.*;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public abstract class CollabinateServerTest
{
	private CollabinateServer server;
	
	abstract CollabinateServer createServer();
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Before
	public void setup()
	{
		server = createServer();
	}
	
	@Test
	public void should_not_be_null()
	{
		assertNotNull(server);
	}

	@Test
	public void add_stream_item_should_not_allow_null_entity_ID()
	{
		exception.expect(IllegalArgumentException.class);
		server.addStreamItem(null, new StreamItemDataImpl(null));
	}
	
	@Test
	public void add_stream_item_should_not_allow_null_stream_item()
	{
		exception.expect(IllegalArgumentException.class);
		server.addStreamItem("", null);
	}
	
	@Test
	public void adding_a_stream_item_should_allow_retrieval_of_the_item()
	{
		final DateTime instant = DateTime.now();
		server.addStreamItem("1", new StreamItemDataImpl(instant));
		DateTime returnedTime =
				server.getStream("1", 0, 1)[0].getTime();
		assertEquals(instant.getMillis(), returnedTime.getMillis());
	}
	
	@Test
	public void adding_multiple_stream_items_should_allow_retrieval_of_all()
	{
		server.addStreamItem("1", new StreamItemDataImpl(DateTime.now()));
		server.addStreamItem("1", new StreamItemDataImpl(DateTime.now()));
		
		StreamItemData[] items = server.getStream("1", 0, 2);
		assertEquals("All items not retrieved", 2, items.length, 0);
	}
		
	@Test
	public void stream_items_should_be_returned_in_date_order()
			throws InterruptedException
	{
		final DateTime time0 = DateTime.now();
		final DateTime time1 = time0.plus(1000);

		server.addStreamItem("1", new StreamItemDataImpl(time1));		
		server.addStreamItem("1", new StreamItemDataImpl(time0));
		
		StreamItemData[] items = server.getStream("1", 0, 3);
		assertEquals("All items not retrieved", 2, items.length, 0);
		assertEquals("Items not in correct order", 
				time0.getMillis(), items[0].getTime().getMillis(), 0);
	}
	
	@Test
	public void follow_entity_should_not_allow_null_user_ID()
	{
		exception.expect(IllegalArgumentException.class);
		server.followEntity(null, null);
	}
	
	private class StreamItemDataImpl implements StreamItemData
	{
		private DateTime time;
		
		public StreamItemDataImpl(DateTime time)
		{
			this.time = time;
		}
		
		@Override
		public DateTime getTime()
		{
			return time;
		}
	}
}