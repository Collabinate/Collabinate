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
		List<StreamEntry> stream = reader.getStream("test-000", "1", 0, 1);
		assertEquals(0, stream.size());
	}
	
	@Test
	public void adding_a_stream_entry_should_allow_retrieval_of_the_entry()
	{
		final DateTime instant = DateTime.now();
		writer.addStreamEntry("test-001", "entity",
				new StreamEntry("1", instant, null));
		final DateTime returnedTime =
				reader.getStream("test-001", "entity", 0, 1).get(0).getTime();
		assertEquals(instant.getMillis(), returnedTime.getMillis());
		
		//cleanup
		writer.deleteStreamEntry("test-001", "entity", "1");
	}
	
	@Test
	public void retrieving_should_not_return_more_elements_than_exist()
	{
		writer.addStreamEntry("test-002", "entity",
				new StreamEntry("1", DateTime.now(), null));
		writer.addStreamEntry("test-002", "entity",
				new StreamEntry("2", DateTime.now(), null));
		List<StreamEntry> stream = reader.getStream("test-002", "entity", 0, 3);
		assertEquals(2, stream.size());
		
		//cleanup
		writer.deleteStreamEntry("test-002", "entity", "1");
		writer.deleteStreamEntry("test-002", "entity", "2");
	}
	
	@Test
	public void adding_multiple_stream_entries_should_allow_retrieval_of_all()
	{
		writer.addStreamEntry("test-003", "entity",
				new StreamEntry("1", DateTime.now(), null));
		writer.addStreamEntry("test-003", "entity",
				new StreamEntry("2", DateTime.now(), null));
		
		List<StreamEntry> entries =
				reader.getStream("test-003", "entity", 0, 2);
		assertEquals("All entries not retrieved", 2, entries.size());
		
		//cleanup
		writer.deleteStreamEntry("test-003", "entity", "1");
		writer.deleteStreamEntry("test-003", "entity", "2");
	}
	
	@Test
	public void newest_stream_entry_should_come_first_in_stream()
	{
		final DateTime time0 = DateTime.now();
		final DateTime time1 = time0.plus(1000);
		final DateTime time2 = time0.minus(1000);
		final DateTime time3 = time0.plus(2000); // newest
		final DateTime time4 = time0.minus(2000);
		
		writer.addStreamEntry("test-004", "entity",
				new StreamEntry("1", time0, null));
		writer.addStreamEntry("test-004", "entity",
				new StreamEntry("2", time1, null));
		writer.addStreamEntry("test-004", "entity",
				new StreamEntry("3", time2, null));
		writer.addStreamEntry("test-004", "entity",
				new StreamEntry("4", time3, null));
		writer.addStreamEntry("test-004", "entity",
				new StreamEntry("5", time4, null));
		
		List<StreamEntry> entries =
				reader.getStream("test-004", "entity", 0, 1);
		assertEquals("Newest entry not first in stream", 
			time3.getMillis(), entries.get(0).getTime().getMillis());
		
		//cleanup
		writer.deleteStreamEntry("test-004", "entity", "1");
		writer.deleteStreamEntry("test-004", "entity", "2");
		writer.deleteStreamEntry("test-004", "entity", "3");
		writer.deleteStreamEntry("test-004", "entity", "4");
		writer.deleteStreamEntry("test-004", "entity", "5");
	}
	
	@Test
	public void oldest_stream_entry_should_come_last_in_stream()
	{
		final DateTime time0 = DateTime.now();
		final DateTime time1 = time0.minus(1000);
		final DateTime time2 = time0.plus(1000);
		final DateTime time3 = time0.minus(2000); // oldest	
		final DateTime time4 = time0.plus(2000);
		
		writer.addStreamEntry("test-005", "entity",
				new StreamEntry("1", time0, null));
		writer.addStreamEntry("test-005", "entity",
				new StreamEntry("2", time1, null));
		writer.addStreamEntry("test-005", "entity",
				new StreamEntry("3", time2, null));
		writer.addStreamEntry("test-005", "entity",
				new StreamEntry("4", time3, null));
		writer.addStreamEntry("test-005", "entity",
				new StreamEntry("5", time4, null));
		
		List<StreamEntry> entries =
				reader.getStream("test-005", "entity", 0, 5);
		assertEquals("Oldest entry not last in stream", 
			time3.getMillis(), entries.get(4).getTime().getMillis());
		
		//cleanup
		writer.deleteStreamEntry("test-005", "entity", "1");
		writer.deleteStreamEntry("test-005", "entity", "2");
		writer.deleteStreamEntry("test-005", "entity", "3");
		writer.deleteStreamEntry("test-005", "entity", "4");
		writer.deleteStreamEntry("test-005", "entity", "5");
	}
	
	@Test
	public void stream_with_all_entries_removed_should_be_empty()
	{
		writer.addStreamEntry("test-006", "entity",
				new StreamEntry("1", null, null));
		writer.deleteStreamEntry("test-006", "entity", "1");
		List<StreamEntry> stream = reader.getStream("test-006", "entity", 0, 1);
		assertEquals(0, stream.size());
	}
	
	@Test
	public void removed_stream_entry_should_not_appear_in_stream()
	{
		writer.addStreamEntry("test-007", "entity",
				new StreamEntry("1", null, null));
		writer.addStreamEntry("test-007", "entity",
				new StreamEntry("2", null, null));
		writer.deleteStreamEntry("test-007", "entity", "1");
		List<StreamEntry> stream = reader.getStream("test-007", "entity", 0, 1);
		assertNotEquals("Removed entry appeared in stream", 
				stream.get(0).getId(), "1");
		
		//cleanup
		writer.deleteStreamEntry("test-007", "entity", "2");
	}
	
	@Test
	public void removing_stream_entry_should_not_change_remaining_order()
	{
		final DateTime time1 = DateTime.now();
		final DateTime time2 = time1.minus(1000);
		final DateTime time3 = time1.plus(1000);
		
		// add entries, order will be 3, 1, 2
		writer.addStreamEntry("test-008", "entity",
				new StreamEntry("1", time1, null));
		writer.addStreamEntry("test-008", "entity",
				new StreamEntry("2", time2, null));
		writer.addStreamEntry("test-008", "entity",
				new StreamEntry("3", time3, null));
		
		// remove entry 1
		writer.deleteStreamEntry("test-008", "entity", "1");
		
		// order should be 3, 2		
		List<StreamEntry> entries =
				reader.getStream("test-008", "entity", 0, 2);
		assertEquals("Newest not first.", "3", entries.get(0).getId());
		assertEquals("Oldest not last.", "2", entries.get(1).getId());
		
		//cleanup
		writer.deleteStreamEntry("test-008", "entity", "2");
		writer.deleteStreamEntry("test-008", "entity", "3");
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
		writer.addStreamEntry("test-009", "entityB",
				new StreamEntry("1", time1, null));
		writer.addStreamEntry("test-009", "entityA",
				new StreamEntry("2", time2, null));
		writer.addStreamEntry("test-009", "entityA",
				new StreamEntry("3", time3, null));
		writer.addStreamEntry("test-009", "entityB",
				new StreamEntry("4", time4, null));
		
		// follow the entities, feed order is 3, 1, 2, 4
		writer.followEntity("test-009", "user", "entityA", null);
		writer.followEntity("test-009", "user", "entityB", null);
		
		// remove entry 2
		writer.deleteStreamEntry("test-009", "entityA", "2");
		
		// order should be 3, 1, 4	
		List<StreamEntry> entries = reader.getFeed("test-009", "user", 0, 3);
		assertEquals("Newest not first.", "3", entries.get(0).getId());
		assertEquals("Middle not correct.", "1", entries.get(1).getId());
		assertEquals("Oldest not last.", "4", entries.get(2).getId());
		
		//cleanup
		writer.unfollowEntity("test-009", "user", "entityA");
		writer.unfollowEntity("test-009", "user", "entityB");
		writer.deleteStreamEntry("test-009", "entityB", "1");
		writer.deleteStreamEntry("test-009", "entityA", "3");
		writer.deleteStreamEntry("test-009", "entityB", "4");
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
		writer.addStreamEntry("test-010", "entityB",
				new StreamEntry("1", time1, null));
		writer.addStreamEntry("test-010", "entityA",
				new StreamEntry("2", time2, null));
		writer.addStreamEntry("test-010", "entityA",
				new StreamEntry("3", time3, null));
		writer.addStreamEntry("test-010", "entityB",
				new StreamEntry("4", time4, null));
		
		// follow the entities, feed order is 3, 1, 2, 4
		writer.followEntity("test-010", "user", "entityA", null);
		writer.followEntity("test-010", "user", "entityB", null);
		
		// remove entry 3
		writer.deleteStreamEntry("test-010", "entityA", "3");
		
		// order should be 1, 2, 4	
		List<StreamEntry> entries = reader.getFeed("test-010", "user", 0, 3);
		assertEquals("Newest not first.", "1", entries.get(0).getId());
		assertEquals("Middle not correct.", "2", entries.get(1).getId());
		assertEquals("Oldest not last.", "4", entries.get(2).getId());
		
		//cleanup
		writer.unfollowEntity("test-010", "user", "entityA");
		writer.unfollowEntity("test-010", "user", "entityB");
		writer.deleteStreamEntry("test-010", "entityB", "1");
		writer.deleteStreamEntry("test-010", "entityA", "2");
		writer.deleteStreamEntry("test-010", "entityB", "4");
	}
		
	@Test
	public void feed_for_user_who_follows_nothing_should_be_empty()
	{
		assertEquals(0, reader.getFeed("test-011", "user", 0, 1).size());
	}
	
	@Test
	public void feed_for_user_who_follows_entities_with_no_stream_entries_should_be_empty()
	{
		writer.followEntity("test-012", "user", "entityA", null);
		writer.followEntity("test-012", "user", "entityB", null);
		assertEquals(0, reader.getFeed("test-012", "user", 0, 1).size());
		
		//cleanup
		writer.unfollowEntity("test-012", "user", "entityA");
		writer.unfollowEntity("test-012", "user", "entityB");
	}
	
	@Test
	public void feed_should_contain_entry_from_followed_entity()
	{
		final DateTime time = DateTime.now();
		writer.addStreamEntry("test-013", "entity",
				new StreamEntry("1", time, null));
		writer.followEntity("test-013", "user", "entity", null);
		final DateTime returned = reader.getFeed("test-013", "user", 0, 1)
				.get(0).getTime();
		assertEquals(time.getMillis(), returned.getMillis());
		
		//cleanup
		writer.unfollowEntity("test-013", "user", "entity");
		writer.deleteStreamEntry("test-013", "entity", "1");
	}
	
	@Test
	public void feed_should_contain_entries_from_all_followed_entities()
	{
		final DateTime time1 = DateTime.now();
		final DateTime time2 = DateTime.now().plus(1000);
		writer.addStreamEntry("test-014", "entityA",
				new StreamEntry("1", time1, null));
		writer.addStreamEntry("test-014", "entityB",
				new StreamEntry("2", time2, null));
		writer.followEntity("test-014", "user", "entityA", null);
		writer.followEntity("test-014", "user", "entityB", null);
		ArrayList<Long> timeMillis = new ArrayList<Long>();
		for (StreamEntry entry : reader.getFeed("test-014", "user", 0, 2))
		{
			timeMillis.add(entry.getTime().getMillis());
		}
		assertThat(timeMillis, hasItems(
				time1.getMillis(), time2.getMillis()));
		
		//cleanup
		writer.unfollowEntity("test-014", "user", "entityA");
		writer.unfollowEntity("test-014", "user", "entityB");
		writer.deleteStreamEntry("test-014", "entityA", "1");
		writer.deleteStreamEntry("test-014", "entityB", "2");
	}
	
	@Test
	public void user_with_newest_entry_should_be_first_in_feed_when_added_last()
	{
		final DateTime time1 = new DateTime(1000);
		final DateTime time2 = time1.plus(1000);
		final DateTime time3 = time1.plus(2000);
		writer.addStreamEntry("test-015", "entityA",
				new StreamEntry("1", time1, null));
		writer.addStreamEntry("test-015", "entityB",
				new StreamEntry("2", time2, null));
		writer.addStreamEntry("test-015", "entityC",
				new StreamEntry("3", time3, null));
		writer.followEntity("test-015", "user", "entityA", null);
		writer.followEntity("test-015", "user", "entityB", null);
		writer.followEntity("test-015", "user", "entityC", null);
		List<StreamEntry> entries = reader.getFeed("test-015", "user", 0, 3);
		assertEquals("newest entry not first", time3.getMillis(),
				entries.get(0).getTime().getMillis());
		
		//cleanup
		writer.unfollowEntity("test-015", "user", "entityA");
		writer.unfollowEntity("test-015", "user", "entityB");
		writer.unfollowEntity("test-015", "user", "entityC");
		writer.deleteStreamEntry("test-015", "entityA", "1");
		writer.deleteStreamEntry("test-015", "entityB", "2");
		writer.deleteStreamEntry("test-015", "entityC", "3");
	}
	
	@Test
	public void user_with_oldest_entry_should_be_last_in_feed_when_added_last()
	{
		final DateTime time1 = new DateTime(3000);
		final DateTime time2 = time1.minus(1000);
		final DateTime time3 = time1.minus(2000);
		writer.addStreamEntry("test-016", "entityA",
				new StreamEntry("1", time1, null));
		writer.addStreamEntry("test-016", "entityB",
				new StreamEntry("2", time2, null));
		writer.addStreamEntry("test-016", "entityC",
				new StreamEntry("3", time3, null));
		writer.followEntity("test-016", "user", "entityA", null);
		writer.followEntity("test-016", "user", "entityB", null);
		writer.followEntity("test-016", "user", "entityC", null);
		List<StreamEntry> entries = reader.getFeed("test-016", "user", 0, 3);
		assertEquals("oldest entry not last", time3.getMillis(),
				entries.get(2).getTime().getMillis());
		
		//cleanup
		writer.unfollowEntity("test-016", "user", "entityA");
		writer.unfollowEntity("test-016", "user", "entityB");
		writer.unfollowEntity("test-016", "user", "entityC");
		writer.deleteStreamEntry("test-016", "entityA", "1");
		writer.deleteStreamEntry("test-016", "entityB", "2");
		writer.deleteStreamEntry("test-016", "entityC", "3");
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
		writer.addStreamEntry("test-017", "entityA",
				new StreamEntry("1", time1, null));
		writer.addStreamEntry("test-017", "entityB",
				new StreamEntry("2", time2, null));
		writer.followEntity("test-017", "user", "entityA", null);
		writer.followEntity("test-017", "user", "entityB", null);
		// The descending time order right now is B1, A1
		feed = reader.getFeed("test-017", "user", 0, 2);
		feedEntry = feed.get(0).getTime();
		assertEquals(time2.getMillis(), feedEntry.getMillis());
		feedEntry = feed.get(1).getTime();
		assertEquals(time1.getMillis(), feedEntry.getMillis());

		// Now add the entry with time A2 to A, making
		// the time order A2, B1, A1
		writer.addStreamEntry("test-017", "entityA",
				new StreamEntry("3", time3, null));
		feed = reader.getFeed("test-017", "user", 0, 3);
		feedEntry = feed.get(0).getTime();
		assertEquals(time3.getMillis(), feedEntry.getMillis());
		feedEntry = feed.get(1).getTime();
		assertEquals(time2.getMillis(), feedEntry.getMillis());
		feedEntry = feed.get(2).getTime();
		assertEquals(time1.getMillis(), feedEntry.getMillis());
		
		// Now we'll add B2, but it actually comes EARLIER than
		// all the rest, and thus the descending order should become
		// A2 (time3), B1 (time2), A1 (time1), B2 (time4)
		writer.addStreamEntry("test-017", "entityB",
				new StreamEntry("4", time4, null));
		feed = reader.getFeed("test-017", "user", 0, 4);
		feedEntry = feed.get(0).getTime();
		assertEquals(time3.getMillis(), feedEntry.getMillis());
		feedEntry = feed.get(1).getTime();
		assertEquals(time2.getMillis(), feedEntry.getMillis());
		feedEntry = feed.get(2).getTime();
		assertEquals(time1.getMillis(), feedEntry.getMillis());
		feedEntry = feed.get(3).getTime();
		assertEquals(time4.getMillis(), feedEntry.getMillis());
		
		//cleanup
		writer.unfollowEntity("test-017", "user", "entityA");
		writer.unfollowEntity("test-017", "user", "entityB");
		writer.deleteStreamEntry("test-017", "entityA", "1");
		writer.deleteStreamEntry("test-017", "entityB", "2");
		writer.deleteStreamEntry("test-017", "entityA", "3");
		writer.deleteStreamEntry("test-017", "entityB", "4");
	}
	
	@Test
	public void is_following_should_return_null_if_user_does_not_follow()
	{
		writer.addStreamEntry("test-018", "entity",
				new StreamEntry("1", null, null));
		writer.addStreamEntry("test-018", "user",
				new StreamEntry("2", null, null));
		
		assertNull(reader.getDateTimeUserFollowedEntity(
				"test-018", "user", "entity"));
		
		//cleanup
		writer.deleteStreamEntry("test-018", "entity", "1");
		writer.deleteStreamEntry("test-018", "user", "2");
	}
	
	@Test
	public void is_following_should_return_datetime_if_user_follows_entity()
	{
		writer.addStreamEntry("test-019", "entity",
				new StreamEntry("1", null, null));
		writer.addStreamEntry("test-019", "user",
				new StreamEntry("2", null, null));
		DateTime followed = new DateTime(1977, 5, 13, 12, 00);
		writer.followEntity("test-019", "user", "entity", followed);
		
		
		assertEquals(followed.getMillis(), 
				reader.getDateTimeUserFollowedEntity(
						"test-019", "user", "entity").getMillis());

		//cleanup
		writer.unfollowEntity("test-019", "user", "entity");
		writer.deleteStreamEntry("test-019", "entity", "1");
		writer.deleteStreamEntry("test-019", "user", "2");
	}
	
	@Test
	public void is_following_should_return_null_after_unfollow()
	{
		writer.addStreamEntry("test-020", "entity",
				new StreamEntry("1", null, null));
		writer.addStreamEntry("test-020", "user",
				new StreamEntry("2", null, null));
		writer.followEntity("test-020", "user", "entity", null);
		writer.unfollowEntity("test-020", "user", "entity");
		
		assertNull(reader.getDateTimeUserFollowedEntity(
				"test-020", "user", "entity"));
		
		//cleanup
		writer.deleteStreamEntry("test-020", "entity", "1");
		writer.deleteStreamEntry("test-020", "user", "2");
	}
	
	@Test
	public void follow_same_entity_twice_should_not_cause_problem_reading_feed()
	{
		writer.followEntity("test-021", "user", "entity", null);
		writer.followEntity("test-021", "user", "entity", null);
		
		reader.getFeed("test-021", "user", 0, 20);
		
		//cleanup
		writer.unfollowEntity("test-021", "user", "entity");
	}
	
	@Test
	public void streams_for_the_same_entityId_for_different_tenants_should_be_different()
	{
		writer.addStreamEntry("test-022-tenant1", "entity",
				new StreamEntry("1", null, null));
		writer.addStreamEntry("test-022-tenant2", "entity",
				new StreamEntry("1", null, null));
		
		List<StreamEntry> entries =
				reader.getStream("test-022-tenant1", "entity", 0, 2);
		assertEquals("Stream not separate - invalid entry count.",
				1, entries.size());
		
		//cleanup
		writer.deleteStreamEntry("test-022-tenant1", "entity", "1");
		writer.deleteStreamEntry("test-022-tenant2", "entity", "1");
	}
	
	@Test
	public void start_parameter_should_start_stream_in_correct_place()
	{
		final DateTime time1 = DateTime.now();
		final DateTime time2 = DateTime.now().plus(1000);
		writer.addStreamEntry("test-023", "entityA",
				new StreamEntry("1", time1, null));
		writer.addStreamEntry("test-023", "entityA",
				new StreamEntry("2", time2, null));
		
		DateTime secondEntryTime =
				reader.getStream("test-023", "entityA", 1, 1).get(0).getTime();
		
		assertEquals(secondEntryTime.getMillis(), time1.getMillis());
	}
	
	@Test
	public void start_parameter_should_start_feed_in_correct_place()
	{
		final DateTime time1 = DateTime.now();
		final DateTime time2 = DateTime.now().plus(1000);
		writer.addStreamEntry("test-024", "entityA",
				new StreamEntry("1", time1, null));
		writer.addStreamEntry("test-024", "entityB",
				new StreamEntry("2", time2, null));
		writer.followEntity("test-024", "user", "entityA", null);
		writer.followEntity("test-024", "user", "entityB", null);
		
		DateTime secondEntryTime =
				reader.getFeed("test-024", "user", 1, 1).get(0).getTime();
		
		assertEquals(secondEntryTime.getMillis(), time1.getMillis());
	}
	
	@Test
	public void count_parameter_should_return_correct_number_of_stream_items()
	{
		final DateTime time1 = DateTime.now();
		final DateTime time2 = DateTime.now().plus(1000);
		writer.addStreamEntry("test-025", "entityA",
				new StreamEntry("1", time1, null));
		writer.addStreamEntry("test-025", "entityA",
				new StreamEntry("2", time2, null));
		
		assertEquals(1, reader.getStream("test-025", "entityA", 0, 1).size());
	}

	@Test
	public void count_parameter_should_return_correct_number_of_feed_items()
	{
		final DateTime time1 = DateTime.now();
		final DateTime time2 = DateTime.now().plus(1000);
		writer.addStreamEntry("test-026", "entityA",
				new StreamEntry("1", time1, null));
		writer.addStreamEntry("test-026", "entityB",
				new StreamEntry("2", time2, null));
		writer.followEntity("test-026", "user", "entityA", null);
		writer.followEntity("test-026", "user", "entityB", null);
		
		assertEquals(1, reader.getFeed("test-026", "user", 0, 1).size());
	}
}