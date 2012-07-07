package com.collabinate.server;

import static org.junit.Assert.*;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public abstract class CollabinateServerTest
{
	private CollabinateReader reader;
	private CollabinateWriter writer;
	
	abstract CollabinateReader getReader();
	abstract CollabinateWriter getWriter();
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Before
	public void setup()
	{
		reader = getReader();
		writer = getWriter();
	}
	
	@Test
	public void should_not_be_null()
	{
		assertNotNull(reader);
		assertNotNull(writer);
	}

	@Test
	public void add_stream_item_should_not_allow_null_entity_ID()
	{
		exception.expect(IllegalArgumentException.class);
		writer.addStreamItem(null, new StreamItemDataImpl(null));
	}
	
	@Test
	public void add_stream_item_should_not_allow_null_stream_item()
	{
		exception.expect(IllegalArgumentException.class);
		writer.addStreamItem("", null);
	}
	
	@Test
	public void retrieving_no_stream_items_should_give_empty_array()
	{
		StreamItemData[] stream = reader.getStream("1", 0, 1);
		assertEquals(0, stream.length);
	}
	
	@Test
	public void adding_a_stream_item_should_allow_retrieval_of_the_item()
	{
		final DateTime instant = DateTime.now();
		writer.addStreamItem("1", new StreamItemDataImpl(instant));
		DateTime returnedTime =
				reader.getStream("1", 0, 1)[0].getTime();
		assertEquals(instant.getMillis(), returnedTime.getMillis());
	}
	
	@Test
	public void retrieving_should_not_return_more_elements_than_exist()
	{
		writer.addStreamItem("1", new StreamItemDataImpl(DateTime.now()));
		writer.addStreamItem("1", new StreamItemDataImpl(DateTime.now()));
		StreamItemData[] stream = reader.getStream("1", 0, 3);
		assertEquals(2, stream.length);
	}
	
	@Test
	public void adding_multiple_stream_items_should_allow_retrieval_of_all()
	{
		writer.addStreamItem("1", new StreamItemDataImpl(DateTime.now()));
		writer.addStreamItem("1", new StreamItemDataImpl(DateTime.now()));
		
		StreamItemData[] items = reader.getStream("1", 0, 2);
		assertEquals("All items not retrieved", 2, items.length, 0);
	}
		
	@Test
	public void stream_items_should_be_returned_in_date_order()
			throws InterruptedException
	{
		final DateTime time0 = DateTime.now();
		final DateTime time1 = time0.plus(1000);

		writer.addStreamItem("1", new StreamItemDataImpl(time1));		
		writer.addStreamItem("1", new StreamItemDataImpl(time0));
		
		StreamItemData[] items = reader.getStream("1", 0, 2);
		assertEquals("All items not retrieved", 2, items.length, 0);
		assertEquals("Items not in correct order", 
				time0.getMillis(), items[0].getTime().getMillis(), 0);
	}
	
	@Test
	public void follow_entity_should_not_allow_null_user_ID()
	{
		exception.expect(IllegalArgumentException.class);
		writer.followEntity(null, null);
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