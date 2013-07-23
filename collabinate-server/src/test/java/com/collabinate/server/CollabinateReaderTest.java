package com.collabinate.server;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public abstract class CollabinateReaderTest
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
	public void stream_for_new_entity_should_be_empty()
	{
		List<StreamEntry> stream = reader.getStream("1", 0, 1);
		assertEquals(0, stream.size());
	}
	
	@Test
	public void adding_a_stream_entry_should_allow_retrieval_of_the_entry()
	{
		final DateTime instant = DateTime.now();
		writer.addStreamEntry("1", new StreamEntryImpl(instant));
		final DateTime returnedTime =
				reader.getStream("1", 0, 1).get(0).getTime();
		assertEquals(instant.getMillis(), returnedTime.getMillis());
	}
	
	@Test
	public void retrieving_should_not_return_more_elements_than_exist()
	{
		writer.addStreamEntry("1", new StreamEntryImpl(DateTime.now()));
		writer.addStreamEntry("1", new StreamEntryImpl(DateTime.now()));
		List<StreamEntry> stream = reader.getStream("1", 0, 3);
		assertEquals(2, stream.size());
	}
	
	@Test
	public void adding_multiple_stream_entries_should_allow_retrieval_of_all()
	{
		writer.addStreamEntry("1", new StreamEntryImpl(DateTime.now()));
		writer.addStreamEntry("1", new StreamEntryImpl(DateTime.now()));
		
		List<StreamEntry> entries = reader.getStream("1", 0, 2);
		assertEquals("All entries not retrieved", 2, entries.size());
	}
	
	@Test
	public void newest_stream_entry_should_come_first_in_stream()
	{
		final DateTime time0 = DateTime.now();
		final DateTime time1 = time0.plus(1000);
		final DateTime time2 = time0.minus(1000);
		final DateTime time3 = time0.plus(2000); // newest
		final DateTime time4 = time0.minus(2000);
		
		writer.addStreamEntry("1", new StreamEntryImpl(time0));
		writer.addStreamEntry("1", new StreamEntryImpl(time1));
		writer.addStreamEntry("1", new StreamEntryImpl(time2));
		writer.addStreamEntry("1", new StreamEntryImpl(time3));
		writer.addStreamEntry("1", new StreamEntryImpl(time4));
		
		List<StreamEntry> entries = reader.getStream("1", 0, 1);
		assertEquals("Newest entry not first in stream", 
			time3.getMillis(), entries.get(0).getTime().getMillis());
	}
	
	@Test
	public void oldest_stream_entry_should_come_last_in_stream()
	{
		final DateTime time0 = DateTime.now();
		final DateTime time1 = time0.minus(1000);
		final DateTime time2 = time0.plus(1000);
		final DateTime time3 = time0.minus(2000); // oldest	
		final DateTime time4 = time0.plus(2000);
		
		writer.addStreamEntry("1", new StreamEntryImpl(time0));
		writer.addStreamEntry("1", new StreamEntryImpl(time1));
		writer.addStreamEntry("1", new StreamEntryImpl(time2));
		writer.addStreamEntry("1", new StreamEntryImpl(time3));
		writer.addStreamEntry("1", new StreamEntryImpl(time4));
		
		List<StreamEntry> entries = reader.getStream("1", 0, 5);
		assertEquals("Oldest entry not last in stream", 
			time3.getMillis(), entries.get(4).getTime().getMillis());
	}
		
	@Test
	public void feed_for_user_who_follows_nothing_should_be_empty()
	{
		assertEquals(0, reader.getFeed("user", 0, 1).size());
	}
	
	@Test
	public void feed_for_user_who_follows_entities_with_no_stream_entries_should_be_empty()
	{
		writer.followEntity("user", "1");
		writer.followEntity("user", "2");
		assertEquals(0, reader.getFeed("user", 0, 1).size());
	}
	
	@Test
	public void feed_should_contain_entry_from_followed_entity()
	{
		final DateTime time = DateTime.now();
		writer.addStreamEntry("1", new StreamEntryImpl(time));
		writer.followEntity("user", "1");
		final DateTime returned = reader.getFeed("user", 0, 1).get(0).getTime();
		assertEquals(time.getMillis(), returned.getMillis());
	}
	
	@Test
	public void feed_should_contain_entries_from_all_followed_entities()
	{
		final DateTime time1 = DateTime.now();
		final DateTime time2 = DateTime.now().plus(1000);
		writer.addStreamEntry("1", new StreamEntryImpl(time1));
		writer.addStreamEntry("2", new StreamEntryImpl(time2));
		writer.followEntity("user", "1");
		writer.followEntity("user", "2");
		ArrayList<Long> timeMillis = new ArrayList<Long>();
		for (StreamEntry entry : reader.getFeed("user", 0, 2))
		{
			timeMillis.add(entry.getTime().getMillis());
		}
		assertThat(timeMillis, hasItems(
				time1.getMillis(), time2.getMillis()));
	}
	
	@Test
	public void new_stream_entry_added_to_followed_entity_should_put_entity_into_correct_order_in_feed()
	{
		final DateTime time1 = new DateTime(2000); // A1
		final DateTime time2 = new DateTime(3000); // B1
		final DateTime time3 = new DateTime(4000); // A2
		final DateTime time4 = new DateTime(1000); // B2
		DateTime feedEntry;
		List<StreamEntry> feed;
		
		// create stream entries for two entities
		// and have a user follow them
		writer.addStreamEntry("A", new StreamEntryImpl(time1));
		writer.addStreamEntry("B", new StreamEntryImpl(time2));
		writer.followEntity("user", "A");
		writer.followEntity("user", "B");
		// The descending time order right now is B1, A1
		feed = reader.getFeed("user", 0, 2);
		feedEntry = feed.get(0).getTime();
		assertEquals(time2.getMillis(), feedEntry.getMillis());
		feedEntry = feed.get(1).getTime();
		assertEquals(time1.getMillis(), feedEntry.getMillis());

		// Now add the entry with time A2 to A, making
		// the time order A2, B1, A1
		writer.addStreamEntry("A", new StreamEntryImpl(time3));
		feed = reader.getFeed("user", 0, 3);
		feedEntry = feed.get(0).getTime();
		assertEquals(time3.getMillis(), feedEntry.getMillis());
		feedEntry = feed.get(1).getTime();
		assertEquals(time2.getMillis(), feedEntry.getMillis());
		feedEntry = feed.get(2).getTime();
		assertEquals(time1.getMillis(), feedEntry.getMillis());
		
		// Now we'll add B2, but it actually comes EARLIER than
		// all the rest, and thus the descending order should become
		// A2 (time3), B1 (time2), A1 (time1), B2 (time4)
		writer.addStreamEntry("B", new StreamEntryImpl(time4));
		feed = reader.getFeed("user", 0, 4);
		feedEntry = feed.get(0).getTime();
		assertEquals(time3.getMillis(), feedEntry.getMillis());
		feedEntry = feed.get(1).getTime();
		assertEquals(time2.getMillis(), feedEntry.getMillis());
		feedEntry = feed.get(2).getTime();
		assertEquals(time1.getMillis(), feedEntry.getMillis());
		feedEntry = feed.get(3).getTime();
		assertEquals(time4.getMillis(), feedEntry.getMillis());
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