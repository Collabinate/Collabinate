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
	public void add_stream_entry_should_not_allow_null_tenant_ID()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("tenantId");
		writer.addStreamEntry(null, "entity", new StreamEntry(null, null, null));
	}
	
	@Test
	public void add_stream_entry_should_not_allow_null_entity_ID()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("entityId");
		writer.addStreamEntry("c", null, new StreamEntry(null, null, null));
	}
	
	@Test
	public void add_stream_entry_should_not_allow_null_stream_entry()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("streamEntry");
		writer.addStreamEntry("c", "", null);
	}

	@Test
	public void adding_duplicate_stream_entries_should_succeed()
	{
		StreamEntry entry = new StreamEntry("1", DateTime.now(), "content");
		writer.addStreamEntry("c", "entity", entry);
		writer.addStreamEntry("c", "entity", entry);
		
		//cleanup
		writer.deleteStreamEntry("c", "entity", "1");
		writer.deleteStreamEntry("c", "entity", "1");
	}
	
	@Test
	public void delete_stream_entry_should_not_allow_null_tenant_ID()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("tenantId");
		writer.deleteStreamEntry(null, "entity", "");
	}
		
	@Test
	public void delete_stream_entry_should_not_allow_null_entity_ID()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("entityId");
		writer.deleteStreamEntry("c", null, "");
	}
		
	@Test
	public void delete_stream_entry_should_not_allow_null_entry_ID()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("entryId");
		writer.deleteStreamEntry("c", "", null);
	}
	
	@Test
	public void deleting_nonexistent_entry_should_succeed()
	{
		writer.addStreamEntry("c", "entity", new StreamEntry("1", null, null));
		writer.deleteStreamEntry("c", "entity", "2");
		
		//cleanup
		writer.deleteStreamEntry("c", "entity", "1");
	}
	
	@Test
	public void follow_entity_should_not_allow_null_tenant_ID()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("tenantId");
		writer.followEntity(null, "user", "1");
	}
	
	@Test
	public void follow_entity_should_not_allow_null_user_ID()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("userId");
		writer.followEntity("c", null, "1");
	}
	
	@Test
	public void follow_entity_should_not_allow_null_entity_ID()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("entityId");
		writer.followEntity("c", "user", null);
	}
	
	@Test
	public void unfollow_entity_should_not_allow_null_tenant_ID()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("tenantId");
		writer.unfollowEntity(null, "user", "1");
	}
	
	@Test
	public void unfollow_entity_should_not_allow_null_user_ID()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("userId");
		writer.unfollowEntity("c", null, "1");
	}
	
	@Test
	public void unfollow_entity_should_not_allow_null_entity_ID()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("entityId");
		writer.unfollowEntity("c", "user", null);
	}	
}