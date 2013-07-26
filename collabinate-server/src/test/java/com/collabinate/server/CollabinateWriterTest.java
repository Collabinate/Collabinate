package com.collabinate.server;

import static org.junit.Assert.*;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Abstract test class to test any implementation of a CollabinateWriter.
 * 
 * @author mafuba
 *
 */
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
	public void add_stream_entry_should_not_allow_null_entity_ID()
	{
		exception.expect(IllegalArgumentException.class);
		writer.addStreamEntry(null, new StreamEntryImpl(null));
	}
	
	@Test
	public void add_stream_entry_should_not_allow_null_stream_entry()
	{
		exception.expect(IllegalArgumentException.class);
		writer.addStreamEntry("", null);
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
	
	private class StreamEntryImpl implements StreamEntry
	{
		private DateTime time;
		
		public StreamEntryImpl(DateTime time)
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