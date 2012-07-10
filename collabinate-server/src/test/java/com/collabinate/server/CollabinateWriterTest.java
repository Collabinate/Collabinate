package com.collabinate.server;

import static org.junit.Assert.*;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public abstract class CollabinateWriterTest
{
	private CollabinateWriter writer;
	
	abstract CollabinateWriter getWriter();
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Before
	public void setup()
	{
		writer = getWriter();
	}
	
	@Test
	public void should_not_be_null()
	{
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
	public void follow_entity_should_not_allow_null_user_ID()
	{
		exception.expect(IllegalArgumentException.class);
		writer.followEntity(null, "1");
	}
	
	@Test
	public void follow_entity_should_not_allow_null_entity_ID()
	{
		exception.expect(IllegalArgumentException.class);
		writer.followEntity("user", null);
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