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
		exception.expectMessage("entityId");
		writer.addStreamEntry(null, new StreamEntry(null, null, null));
	}
	
	@Test
	public void add_stream_entry_should_not_allow_null_stream_entry()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("streamEntry");
		writer.addStreamEntry("", null);
	}

	@Test
	public void adding_duplicate_stream_entries_should_succeed()
	{
		StreamEntry entry = new StreamEntry("1", DateTime.now(), "content");
		writer.addStreamEntry("entity", entry);
		writer.addStreamEntry("entity", entry);
		
		//cleanup
		writer.deleteStreamEntry("entity", "1");
		writer.deleteStreamEntry("entity", "1");
	}
	
	@Test
	public void deleting_nonexistent_entry_should_succeed()
	{
		writer.addStreamEntry("entity", new StreamEntry("1", null, null));
		writer.deleteStreamEntry("entity", "2");
		
		//cleanup
		writer.deleteStreamEntry("entity", "1");
	}
	
	@Test
	public void delete_stream_entry_should_not_allow_null_entity_ID()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("entityId");
		writer.deleteStreamEntry(null, "");
	}
		
	@Test
	public void delete_stream_entry_should_not_allow_null_entry_ID()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("entryId");
		writer.deleteStreamEntry("", null);
	}
		
	@Test
	public void follow_entity_should_not_allow_null_user_ID()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("userId");
		writer.followEntity(null, "1");
	}
	
	@Test
	public void follow_entity_should_not_allow_null_entity_ID()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("entityId");
		writer.followEntity("user", null);
	}
	
	@Test
	public void unfollow_entity_should_not_allow_null_user_ID()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("userId");
		writer.unfollowEntity(null, "1");
	}
	
	@Test
	public void unfollow_entity_should_not_allow_null_entity_ID()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("entityId");
		writer.unfollowEntity("user", null);
	}	
}