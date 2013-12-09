package com.collabinate.server.engine;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.collabinate.server.StreamEntry;
import com.collabinate.server.engine.CollabinateReader;
import com.collabinate.server.engine.CollabinateWriter;

/**
 * Abstract test class to test any implementation of a CollabinateReader.
 * 
 * @author mafuba
 *
 */
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
		List<StreamEntry> stream = reader.getStream("c", "1", 0, 1);
		assertEquals(0, stream.size());
	}
	
	@Test
	public void adding_a_stream_entry_should_allow_retrieval_of_the_entry()
	{
		final DateTime instant = DateTime.now();
		writer.addStreamEntry("c", "entity",
				new StreamEntry("1", instant, null));
		final DateTime returnedTime =
				reader.getStream("c", "entity", 0, 1).get(0).getTime();
		assertEquals(instant.getMillis(), returnedTime.getMillis());
		
		//cleanup
		writer.deleteStreamEntry("c", "entity", "1");
	}
	
	@Test
	public void retrieving_should_not_return_more_elements_than_exist()
	{
		writer.addStreamEntry("c", "entity",
				new StreamEntry("1", DateTime.now(), null));
		writer.addStreamEntry("c", "entity",
				new StreamEntry("2", DateTime.now(), null));
		List<StreamEntry> stream = reader.getStream("c", "entity", 0, 3);
		assertEquals(2, stream.size());
		
		//cleanup
		writer.deleteStreamEntry("c", "entity", "1");
		writer.deleteStreamEntry("c", "entity", "2");
	}
	
	@Test
	public void adding_multiple_stream_entries_should_allow_retrieval_of_all()
	{
		writer.addStreamEntry("c", "entity",
				new StreamEntry("1", DateTime.now(), null));
		writer.addStreamEntry("c", "entity",
				new StreamEntry("2", DateTime.now(), null));
		
		List<StreamEntry> entries = reader.getStream("c", "entity", 0, 2);
		assertEquals("All entries not retrieved", 2, entries.size());
		
		//cleanup
		writer.deleteStreamEntry("c", "entity", "1");
		writer.deleteStreamEntry("c", "entity", "2");
	}
	
	@Test
	public void newest_stream_entry_should_come_first_in_stream()
	{
		final DateTime time0 = DateTime.now();
		final DateTime time1 = time0.plus(1000);
		final DateTime time2 = time0.minus(1000);
		final DateTime time3 = time0.plus(2000); // newest
		final DateTime time4 = time0.minus(2000);
		
		writer.addStreamEntry("c", "entity", new StreamEntry("1", time0, null));
		writer.addStreamEntry("c", "entity", new StreamEntry("2", time1, null));
		writer.addStreamEntry("c", "entity", new StreamEntry("3", time2, null));
		writer.addStreamEntry("c", "entity", new StreamEntry("4", time3, null));
		writer.addStreamEntry("c", "entity", new StreamEntry("5", time4, null));
		
		List<StreamEntry> entries = reader.getStream("c", "entity", 0, 1);
		assertEquals("Newest entry not first in stream", 
			time3.getMillis(), entries.get(0).getTime().getMillis());
		
		//cleanup
		writer.deleteStreamEntry("c", "entity", "1");
		writer.deleteStreamEntry("c", "entity", "2");
		writer.deleteStreamEntry("c", "entity", "3");
		writer.deleteStreamEntry("c", "entity", "4");
		writer.deleteStreamEntry("c", "entity", "5");
	}
	
	@Test
	public void oldest_stream_entry_should_come_last_in_stream()
	{
		final DateTime time0 = DateTime.now();
		final DateTime time1 = time0.minus(1000);
		final DateTime time2 = time0.plus(1000);
		final DateTime time3 = time0.minus(2000); // oldest	
		final DateTime time4 = time0.plus(2000);
		
		writer.addStreamEntry("c", "entity", new StreamEntry("1", time0, null));
		writer.addStreamEntry("c", "entity", new StreamEntry("2", time1, null));
		writer.addStreamEntry("c", "entity", new StreamEntry("3", time2, null));
		writer.addStreamEntry("c", "entity", new StreamEntry("4", time3, null));
		writer.addStreamEntry("c", "entity", new StreamEntry("5", time4, null));
		
		List<StreamEntry> entries = reader.getStream("c", "entity", 0, 5);
		assertEquals("Oldest entry not last in stream", 
			time3.getMillis(), entries.get(4).getTime().getMillis());
		
		//cleanup
		writer.deleteStreamEntry("c", "entity", "1");
		writer.deleteStreamEntry("c", "entity", "2");
		writer.deleteStreamEntry("c", "entity", "3");
		writer.deleteStreamEntry("c", "entity", "4");
		writer.deleteStreamEntry("c", "entity", "5");
	}
	
	@Test
	public void stream_with_all_entries_removed_should_be_empty()
	{
		writer.addStreamEntry("c", "entity", new StreamEntry("1", null, null));
		writer.deleteStreamEntry("c", "entity", "1");
		List<StreamEntry> stream = reader.getStream("c", "entity", 0, 1);
		assertEquals(0, stream.size());
	}
	
	@Test
	public void removed_stream_entry_should_not_appear_in_stream()
	{
		writer.addStreamEntry("c", "entity", new StreamEntry("1", null, null));
		writer.addStreamEntry("c", "entity", new StreamEntry("2", null, null));
		writer.deleteStreamEntry("c", "entity", "1");
		List<StreamEntry> stream = reader.getStream("c", "entity", 0, 1);
		assertNotEquals("Removed entry appeared in stream", 
				stream.get(0).getId(), "1");
		
		//cleanup
		writer.deleteStreamEntry("c", "entity", "2");
	}
	
	@Test
	public void removing_stream_entry_should_not_change_remaining_order()
	{
		final DateTime time1 = DateTime.now();
		final DateTime time2 = time1.minus(1000);
		final DateTime time3 = time1.plus(1000);
		
		// add entries, order will be 3, 1, 2
		writer.addStreamEntry("c", "entity", new StreamEntry("1", time1, null));
		writer.addStreamEntry("c", "entity", new StreamEntry("2", time2, null));
		writer.addStreamEntry("c", "entity", new StreamEntry("3", time3, null));
		
		// remove entry 1
		writer.deleteStreamEntry("c", "entity", "1");
		
		// order should be 3, 2		
		List<StreamEntry> entries = reader.getStream("c", "entity", 0, 2);
		assertEquals("Newest not first.", "3", entries.get(0).getId());
		assertEquals("Oldest not last.", "2", entries.get(1).getId());
		
		//cleanup
		writer.deleteStreamEntry("c", "entity", "2");
		writer.deleteStreamEntry("c", "entity", "3");
	}
	
	@Test
	public void removing_old_entry_within_stream_should_not_affect_feed()
	{
		// order is 3, 1, 2, 4
		final DateTime time1 = new DateTime(3000);
		final DateTime time2 = time1.minus(1000);
		final DateTime time3 = time1.plus(1000);
		final DateTime time4 = time1.minus(2000);
		
		// add entries to entities, order is A=3,2 B=1,4
		writer.addStreamEntry("c", "entityB",
				new StreamEntry("1", time1, null));
		writer.addStreamEntry("c", "entityA",
				new StreamEntry("2", time2, null));
		writer.addStreamEntry("c", "entityA",
				new StreamEntry("3", time3, null));
		writer.addStreamEntry("c", "entityB",
				new StreamEntry("4", time4, null));
		
		// follow the entities, feed order is 3, 1, 2, 4
		writer.followEntity("c", "user", "entityA");
		writer.followEntity("c", "user", "entityB");
		
		// remove entry 2
		writer.deleteStreamEntry("c", "entityA", "2");
		
		// order should be 3, 1, 4	
		List<StreamEntry> entries = reader.getFeed("c", "user", 0, 3);
		assertEquals("Newest not first.", "3", entries.get(0).getId());
		assertEquals("Middle not correct.", "1", entries.get(1).getId());
		assertEquals("Oldest not last.", "4", entries.get(2).getId());
		
		//cleanup
		writer.unfollowEntity("c", "user", "entityA");
		writer.unfollowEntity("c", "user", "entityB");
		writer.deleteStreamEntry("c", "entityB", "1");
		writer.deleteStreamEntry("c", "entityA", "3");
		writer.deleteStreamEntry("c", "entityB", "4");
	}
	
	@Test
	public void removing_newest_entry_in_stream_should_not_affect_feed()
	{
		// order is 3, 1, 2, 4
		final DateTime time1 = new DateTime(3000);
		final DateTime time2 = time1.minus(1000);
		final DateTime time3 = time1.plus(1000);
		final DateTime time4 = time1.minus(2000);
		
		// add entries to entities, order is A=3,2 B=1,4
		writer.addStreamEntry("c", "entityB",
				new StreamEntry("1", time1, null));
		writer.addStreamEntry("c", "entityA",
				new StreamEntry("2", time2, null));
		writer.addStreamEntry("c", "entityA",
				new StreamEntry("3", time3, null));
		writer.addStreamEntry("c", "entityB",
				new StreamEntry("4", time4, null));
		
		// follow the entities, feed order is 3, 1, 2, 4
		writer.followEntity("c", "user", "entityA");
		writer.followEntity("c", "user", "entityB");
		
		// remove entry 3
		writer.deleteStreamEntry("c", "entityA", "3");
		
		// order should be 1, 2, 4	
		List<StreamEntry> entries = reader.getFeed("c", "user", 0, 3);
		assertEquals("Newest not first.", "1", entries.get(0).getId());
		assertEquals("Middle not correct.", "2", entries.get(1).getId());
		assertEquals("Oldest not last.", "4", entries.get(2).getId());
		
		//cleanup
		writer.unfollowEntity("c", "user", "entityA");
		writer.unfollowEntity("c", "user", "entityB");
		writer.deleteStreamEntry("c", "entityB", "1");
		writer.deleteStreamEntry("c", "entityA", "2");
		writer.deleteStreamEntry("c", "entityB", "4");
	}
		
	@Test
	public void feed_for_user_who_follows_nothing_should_be_empty()
	{
		assertEquals(0, reader.getFeed("c", "user", 0, 1).size());
	}
	
	@Test
	public void feed_for_user_who_follows_entities_with_no_stream_entries_should_be_empty()
	{
		writer.followEntity("c", "user", "entityA");
		writer.followEntity("c", "user", "entityB");
		assertEquals(0, reader.getFeed("c", "user", 0, 1).size());
		
		//cleanup
		writer.unfollowEntity("c", "user", "entityA");
		writer.unfollowEntity("c", "user", "entityB");
	}
	
	@Test
	public void feed_should_contain_entry_from_followed_entity()
	{
		final DateTime time = DateTime.now();
		writer.addStreamEntry("c", "entity", new StreamEntry("1", time, null));
		writer.followEntity("c", "user", "entity");
		final DateTime returned = reader.getFeed("c", "user", 0, 1)
				.get(0).getTime();
		assertEquals(time.getMillis(), returned.getMillis());
		
		//cleanup
		writer.unfollowEntity("c", "user", "entity");
		writer.deleteStreamEntry("c", "entity", "1");
	}
	
	@Test
	public void feed_should_contain_entries_from_all_followed_entities()
	{
		final DateTime time1 = DateTime.now();
		final DateTime time2 = DateTime.now().plus(1000);
		writer.addStreamEntry("c", "entityA",
				new StreamEntry("1", time1, null));
		writer.addStreamEntry("c", "entityB",
				new StreamEntry("2", time2, null));
		writer.followEntity("c", "user", "entityA");
		writer.followEntity("c", "user", "entityB");
		ArrayList<Long> timeMillis = new ArrayList<Long>();
		for (StreamEntry entry : reader.getFeed("c", "user", 0, 2))
		{
			timeMillis.add(entry.getTime().getMillis());
		}
		assertThat(timeMillis, hasItems(
				time1.getMillis(), time2.getMillis()));
		
		//cleanup
		writer.unfollowEntity("c", "user", "entityA");
		writer.unfollowEntity("c", "user", "entityB");
		writer.deleteStreamEntry("c", "entityA", "1");
		writer.deleteStreamEntry("c", "entityB", "2");
	}
	
	@Test
	public void user_with_newest_entry_should_be_first_in_feed_when_added_last()
	{
		final DateTime time1 = new DateTime(1000);
		final DateTime time2 = time1.plus(1000);
		final DateTime time3 = time1.plus(2000);
		writer.addStreamEntry("c", "entityA",
				new StreamEntry("1", time1, null));
		writer.addStreamEntry("c", "entityB",
				new StreamEntry("2", time2, null));
		writer.addStreamEntry("c", "entityC",
				new StreamEntry("3", time3, null));
		writer.followEntity("c", "user", "entityA");
		writer.followEntity("c", "user", "entityB");
		writer.followEntity("c", "user", "entityC");
		List<StreamEntry> entries = reader.getFeed("c", "user", 0, 3);
		assertEquals("newest entry not first", time3.getMillis(),
				entries.get(0).getTime().getMillis());
		
		//cleanup
		writer.unfollowEntity("c", "user", "entityA");
		writer.unfollowEntity("c", "user", "entityB");
		writer.unfollowEntity("c", "user", "entityC");
		writer.deleteStreamEntry("c", "entityA", "1");
		writer.deleteStreamEntry("c", "entityB", "2");
		writer.deleteStreamEntry("c", "entityC", "3");
	}
	
	@Test
	public void user_with_oldest_entry_should_be_last_in_feed_when_added_last()
	{
		final DateTime time1 = new DateTime(3000);
		final DateTime time2 = time1.minus(1000);
		final DateTime time3 = time1.minus(2000);
		writer.addStreamEntry("c", "entityA",
				new StreamEntry("1", time1, null));
		writer.addStreamEntry("c", "entityB",
				new StreamEntry("2", time2, null));
		writer.addStreamEntry("c", "entityC",
				new StreamEntry("3", time3, null));
		writer.followEntity("c", "user", "entityA");
		writer.followEntity("c", "user", "entityB");
		writer.followEntity("c", "user", "entityC");
		List<StreamEntry> entries = reader.getFeed("c", "user", 0, 3);
		assertEquals("oldest entry not last", time3.getMillis(),
				entries.get(2).getTime().getMillis());
		
		//cleanup
		writer.unfollowEntity("c", "user", "entityA");
		writer.unfollowEntity("c", "user", "entityB");
		writer.unfollowEntity("c", "user", "entityC");
		writer.deleteStreamEntry("c", "entityA", "1");
		writer.deleteStreamEntry("c", "entityB", "2");
		writer.deleteStreamEntry("c", "entityC", "3");
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
		writer.addStreamEntry("c", "entityA",
				new StreamEntry("1", time1, null));
		writer.addStreamEntry("c", "entityB",
				new StreamEntry("2", time2, null));
		writer.followEntity("c", "user", "entityA");
		writer.followEntity("c", "user", "entityB");
		// The descending time order right now is B1, A1
		feed = reader.getFeed("c", "user", 0, 2);
		feedEntry = feed.get(0).getTime();
		assertEquals(time2.getMillis(), feedEntry.getMillis());
		feedEntry = feed.get(1).getTime();
		assertEquals(time1.getMillis(), feedEntry.getMillis());

		// Now add the entry with time A2 to A, making
		// the time order A2, B1, A1
		writer.addStreamEntry("c", "entityA",
				new StreamEntry("3", time3, null));
		feed = reader.getFeed("c", "user", 0, 3);
		feedEntry = feed.get(0).getTime();
		assertEquals(time3.getMillis(), feedEntry.getMillis());
		feedEntry = feed.get(1).getTime();
		assertEquals(time2.getMillis(), feedEntry.getMillis());
		feedEntry = feed.get(2).getTime();
		assertEquals(time1.getMillis(), feedEntry.getMillis());
		
		// Now we'll add B2, but it actually comes EARLIER than
		// all the rest, and thus the descending order should become
		// A2 (time3), B1 (time2), A1 (time1), B2 (time4)
		writer.addStreamEntry("c", "entityB",
				new StreamEntry("4", time4, null));
		feed = reader.getFeed("c", "user", 0, 4);
		feedEntry = feed.get(0).getTime();
		assertEquals(time3.getMillis(), feedEntry.getMillis());
		feedEntry = feed.get(1).getTime();
		assertEquals(time2.getMillis(), feedEntry.getMillis());
		feedEntry = feed.get(2).getTime();
		assertEquals(time1.getMillis(), feedEntry.getMillis());
		feedEntry = feed.get(3).getTime();
		assertEquals(time4.getMillis(), feedEntry.getMillis());
		
		//cleanup
		writer.unfollowEntity("c", "user", "entityA");
		writer.unfollowEntity("c", "user", "entityB");
		writer.deleteStreamEntry("c", "entityA", "1");
		writer.deleteStreamEntry("c", "entityB", "2");
		writer.deleteStreamEntry("c", "entityA", "3");
		writer.deleteStreamEntry("c", "entityB", "4");
	}
	
	@Test
	public void is_following_should_return_false_if_user_does_not_follow()
	{
		writer.addStreamEntry("c", "entity", new StreamEntry("1", null, null));
		writer.addStreamEntry("c", "user", new StreamEntry("2", null, null));
		
		assertFalse(reader.isUserFollowingEntity("c", "user", "entity"));
		
		//cleanup
		writer.deleteStreamEntry("c", "entity", "1");
		writer.deleteStreamEntry("c", "user", "2");
	}
	
	@Test
	public void is_following_should_return_true_if_user_follows_entity()
	{
		writer.addStreamEntry("c", "entity", new StreamEntry("1", null, null));
		writer.addStreamEntry("c", "user", new StreamEntry("2", null, null));
		writer.followEntity("c", "user", "entity");
		
		assertTrue(reader.isUserFollowingEntity("c", "user", "entity"));

		//cleanup
		writer.unfollowEntity("c", "user", "entity");
		writer.deleteStreamEntry("c", "entity", "1");
		writer.deleteStreamEntry("c", "user", "2");
	}
	
	@Test
	public void is_following_should_return_false_after_unfollow()
	{
		writer.addStreamEntry("c", "entity", new StreamEntry("1", null, null));
		writer.addStreamEntry("c", "user", new StreamEntry("2", null, null));
		writer.followEntity("c", "user", "entity");
		writer.unfollowEntity("c", "user", "entity");
		
		assertFalse(reader.isUserFollowingEntity("c", "user", "entity"));
		
		//cleanup
		writer.deleteStreamEntry("c", "entity", "1");
		writer.deleteStreamEntry("c", "user", "2");
	}
	
	@Test
	public void follow_same_entity_twice_should_not_cause_problem_reading_feed()
	{
		writer.followEntity("c", "user", "entity");
		writer.followEntity("c", "user", "entity");
		
		reader.getFeed("c", "user", 0, 20);
	}
	
	@Test
	public void streams_for_the_same_entityId_for_different_tenants_should_be_different()
	{
		writer.addStreamEntry("c1", "entity", new StreamEntry("1", null, null));
		writer.addStreamEntry("c2", "entity", new StreamEntry("1", null, null));
		
		List<StreamEntry> entries = reader.getStream("c1", "entity", 0, 2);
		assertEquals("Stream not separate - invalid entry count.",
				1, entries.size());
		
		//cleanup
		writer.deleteStreamEntry("c1", "entity", "1");
		writer.deleteStreamEntry("c2", "entity", "1");
	}
}