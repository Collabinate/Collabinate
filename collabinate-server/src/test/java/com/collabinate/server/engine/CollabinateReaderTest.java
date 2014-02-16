package com.collabinate.server.engine;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.collabinate.server.activitystreams.Activity;
import com.collabinate.server.activitystreams.ActivityStreamsCollection;
import com.collabinate.server.activitystreams.ActivityStreamsObject;
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
	
	private Activity getActivity(String id, DateTime published, String content)
	{
		Activity activity = new Activity(content);
		
		if (null == id || id.equals(""))
		{
			id = UUID.randomUUID().toString();
		}
		activity.setId(id);
		
		if (null == published)
		{
			published = DateTime.now();
		}
		activity.setPublished(published);
		
		return activity;
	}
	
	@Test
	public void stream_for_new_entity_should_be_empty()
	{
		ActivityStreamsCollection stream =
				reader.getStream("test-000", "1", 0, 1);
		assertEquals(0, stream.size());
	}
	
	@Test
	public void adding_an_activity_should_allow_retrieval_of_the_activity()
	{
		final DateTime instant = DateTime.now();
		writer.addActivity("test-001", "entity",
				getActivity("1", instant, null));
		final DateTime returnedTime =
				reader.getStream("test-001", "entity", 0, 1).get(0).getSortTime();
		assertEquals(instant.getMillis(), returnedTime.getMillis());
		
		//cleanup
		writer.deleteActivity("test-001", "entity", "1");
	}
	
	@Test
	public void retrieving_should_not_return_more_elements_than_exist()
	{
		writer.addActivity("test-002", "entity",
				getActivity("1", DateTime.now(), null));
		writer.addActivity("test-002", "entity",
				getActivity("2", DateTime.now(), null));
		ActivityStreamsCollection stream =
				reader.getStream("test-002", "entity", 0, 3);
		assertEquals(2, stream.size());
		
		//cleanup
		writer.deleteActivity("test-002", "entity", "1");
		writer.deleteActivity("test-002", "entity", "2");
	}
	
	@Test
	public void adding_multiple_activities_should_allow_retrieval_of_all()
	{
		writer.addActivity("test-003", "entity",
				getActivity("1", DateTime.now(), null));
		writer.addActivity("test-003", "entity",
				getActivity("2", DateTime.now(), null));
		
		ActivityStreamsCollection activities =
				reader.getStream("test-003", "entity", 0, 2);
		assertEquals("All activities not retrieved", 2, activities.size());
		
		//cleanup
		writer.deleteActivity("test-003", "entity", "1");
		writer.deleteActivity("test-003", "entity", "2");
	}
	
	@Test
	public void newest_activity_should_come_first_in_stream()
	{
		final DateTime time0 = DateTime.now();
		final DateTime time1 = time0.plus(1000);
		final DateTime time2 = time0.minus(1000);
		final DateTime time3 = time0.plus(2000); // newest
		final DateTime time4 = time0.minus(2000);
		
		writer.addActivity("test-004", "entity",
				getActivity("1", time0, null));
		writer.addActivity("test-004", "entity",
				getActivity("2", time1, null));
		writer.addActivity("test-004", "entity",
				getActivity("3", time2, null));
		writer.addActivity("test-004", "entity",
				getActivity("4", time3, null));
		writer.addActivity("test-004", "entity",
				getActivity("5", time4, null));
		
		List<ActivityStreamsObject> activities =
				reader.getStream("test-004", "entity", 0, 1).getItems();
		assertEquals("Newest activity not first in stream", 
			time3.getMillis(), activities.get(0).getSortTime().getMillis());
		
		//cleanup
		writer.deleteActivity("test-004", "entity", "1");
		writer.deleteActivity("test-004", "entity", "2");
		writer.deleteActivity("test-004", "entity", "3");
		writer.deleteActivity("test-004", "entity", "4");
		writer.deleteActivity("test-004", "entity", "5");
	}
	
	@Test
	public void oldest_activity_should_come_last_in_stream()
	{
		final DateTime time0 = DateTime.now();
		final DateTime time1 = time0.minus(1000);
		final DateTime time2 = time0.plus(1000);
		final DateTime time3 = time0.minus(2000); // oldest	
		final DateTime time4 = time0.plus(2000);
		
		writer.addActivity("test-005", "entity",
				getActivity("1", time0, null));
		writer.addActivity("test-005", "entity",
				getActivity("2", time1, null));
		writer.addActivity("test-005", "entity",
				getActivity("3", time2, null));
		writer.addActivity("test-005", "entity",
				getActivity("4", time3, null));
		writer.addActivity("test-005", "entity",
				getActivity("5", time4, null));
		
		List<ActivityStreamsObject> activities =
				reader.getStream("test-005", "entity", 0, 5).getItems();
		assertEquals("Oldest activity not last in stream", 
			time3.getMillis(), activities.get(4).getSortTime().getMillis());
		
		//cleanup
		writer.deleteActivity("test-005", "entity", "1");
		writer.deleteActivity("test-005", "entity", "2");
		writer.deleteActivity("test-005", "entity", "3");
		writer.deleteActivity("test-005", "entity", "4");
		writer.deleteActivity("test-005", "entity", "5");
	}
	
	@Test
	public void stream_with_all_activities_removed_should_be_empty()
	{
		writer.addActivity("test-006", "entity",
				getActivity("1", null, null));
		writer.deleteActivity("test-006", "entity", "1");
		ActivityStreamsCollection stream =
				reader.getStream("test-006", "entity", 0, 1);
		assertEquals(0, stream.size());
	}
	
	@Test
	public void removed_activity_should_not_appear_in_stream()
	{
		writer.addActivity("test-007", "entity",
				getActivity("1", null, null));
		writer.addActivity("test-007", "entity",
				getActivity("2", null, null));
		writer.deleteActivity("test-007", "entity", "1");
		List<ActivityStreamsObject> stream =
				reader.getStream("test-007", "entity", 0, 1).getItems();
		assertNotEquals("Removed activity appeared in stream", 
				stream.get(0).getId(), "1");
		
		//cleanup
		writer.deleteActivity("test-007", "entity", "2");
	}
	
	@Test
	public void removing_activity_should_not_change_remaining_order()
	{
		final DateTime time1 = DateTime.now();
		final DateTime time2 = time1.minus(1000);
		final DateTime time3 = time1.plus(1000);
		
		// add activities, order will be 3, 1, 2
		writer.addActivity("test-008", "entity",
				getActivity("1", time1, null));
		writer.addActivity("test-008", "entity",
				getActivity("2", time2, null));
		writer.addActivity("test-008", "entity",
				getActivity("3", time3, null));
		
		// remove activity 1
		writer.deleteActivity("test-008", "entity", "1");
		
		// order should be 3, 2		
		List<ActivityStreamsObject> activities =
				reader.getStream("test-008", "entity", 0, 2).getItems();
		assertEquals("Newest not first.", "3", activities.get(0).getId());
		assertEquals("Oldest not last.", "2", activities.get(1).getId());
		
		//cleanup
		writer.deleteActivity("test-008", "entity", "2");
		writer.deleteActivity("test-008", "entity", "3");
	}
	
	@Test
	public void removing_old_activity_within_stream_should_not_affect_feed()
	{
		// order is 3, 1, 2, 4
		final DateTime time1 = new DateTime(3000);
		final DateTime time2 = time1.minus(1000);
		final DateTime time3 = time1.plus(1000);
		final DateTime time4 = time1.minus(2000);
		
		// add activities to entities, order is A=3,2 B=1,4
		writer.addActivity("test-009", "entityB",
				getActivity("1", time1, null));
		writer.addActivity("test-009", "entityA",
				getActivity("2", time2, null));
		writer.addActivity("test-009", "entityA",
				getActivity("3", time3, null));
		writer.addActivity("test-009", "entityB",
				getActivity("4", time4, null));
		
		// follow the entities, feed order is 3, 1, 2, 4
		writer.followEntity("test-009", "user", "entityA", null);
		writer.followEntity("test-009", "user", "entityB", null);
		
		// remove activity 2
		writer.deleteActivity("test-009", "entityA", "2");
		
		// order should be 3, 1, 4	
		List<ActivityStreamsObject> activities =
				reader.getFeed("test-009", "user", 0, 3).getItems();
		assertEquals("Newest not first.", "3", activities.get(0).getId());
		assertEquals("Middle not correct.", "1", activities.get(1).getId());
		assertEquals("Oldest not last.", "4", activities.get(2).getId());
		
		//cleanup
		writer.unfollowEntity("test-009", "user", "entityA");
		writer.unfollowEntity("test-009", "user", "entityB");
		writer.deleteActivity("test-009", "entityB", "1");
		writer.deleteActivity("test-009", "entityA", "3");
		writer.deleteActivity("test-009", "entityB", "4");
	}
	
	@Test
	public void removing_newest_activity_in_stream_should_not_affect_feed()
	{
		// order is 3, 1, 2, 4
		final DateTime time1 = new DateTime(3000);
		final DateTime time2 = time1.minus(1000);
		final DateTime time3 = time1.plus(1000);
		final DateTime time4 = time1.minus(2000);
		
		// add activities to entities, order is A=3,2 B=1,4
		writer.addActivity("test-010", "entityB",
				getActivity("1", time1, null));
		writer.addActivity("test-010", "entityA",
				getActivity("2", time2, null));
		writer.addActivity("test-010", "entityA",
				getActivity("3", time3, null));
		writer.addActivity("test-010", "entityB",
				getActivity("4", time4, null));
		
		// follow the entities, feed order is 3, 1, 2, 4
		writer.followEntity("test-010", "user", "entityA", null);
		writer.followEntity("test-010", "user", "entityB", null);
		
		// remove activity 3
		writer.deleteActivity("test-010", "entityA", "3");
		
		// order should be 1, 2, 4	
		List<ActivityStreamsObject> activities =
				reader.getFeed("test-010", "user", 0, 3).getItems();
		assertEquals("Newest not first.", "1", activities.get(0).getId());
		assertEquals("Middle not correct.", "2", activities.get(1).getId());
		assertEquals("Oldest not last.", "4", activities.get(2).getId());
		
		//cleanup
		writer.unfollowEntity("test-010", "user", "entityA");
		writer.unfollowEntity("test-010", "user", "entityB");
		writer.deleteActivity("test-010", "entityB", "1");
		writer.deleteActivity("test-010", "entityA", "2");
		writer.deleteActivity("test-010", "entityB", "4");
	}
		
	@Test
	public void feed_for_user_who_follows_nothing_should_be_empty()
	{
		assertEquals(0, reader.getFeed("test-011", "user", 0, 1).size());
	}
	
	@Test
	public void feed_for_user_who_follows_entities_with_no_activities_should_be_empty()
	{
		writer.followEntity("test-012", "user", "entityA", null);
		writer.followEntity("test-012", "user", "entityB", null);
		assertEquals(0, reader.getFeed("test-012", "user", 0, 1).size());
		
		//cleanup
		writer.unfollowEntity("test-012", "user", "entityA");
		writer.unfollowEntity("test-012", "user", "entityB");
	}
	
	@Test
	public void feed_should_contain_activity_from_followed_entity()
	{
		final DateTime time = DateTime.now();
		writer.addActivity("test-013", "entity",
				getActivity("1", time, null));
		writer.followEntity("test-013", "user", "entity", null);
		final DateTime returned = reader.getFeed("test-013", "user", 0, 1)
				.get(0).getSortTime();
		assertEquals(time.getMillis(), returned.getMillis());
		
		//cleanup
		writer.unfollowEntity("test-013", "user", "entity");
		writer.deleteActivity("test-013", "entity", "1");
	}
	
	@Test
	public void feed_should_contain_activities_from_all_followed_entities()
	{
		final DateTime time1 = DateTime.now();
		final DateTime time2 = DateTime.now().plus(1000);
		writer.addActivity("test-014", "entityA",
				getActivity("1", time1, null));
		writer.addActivity("test-014", "entityB",
				getActivity("2", time2, null));
		writer.followEntity("test-014", "user", "entityA", null);
		writer.followEntity("test-014", "user", "entityB", null);
		ArrayList<Long> timeMillis = new ArrayList<Long>();
		for (ActivityStreamsObject activity :
			reader.getFeed("test-014", "user", 0, 2).getItems())
		{
			timeMillis.add(activity.getSortTime().getMillis());
		}
		assertThat(timeMillis, hasItems(
				time1.getMillis(), time2.getMillis()));
		
		//cleanup
		writer.unfollowEntity("test-014", "user", "entityA");
		writer.unfollowEntity("test-014", "user", "entityB");
		writer.deleteActivity("test-014", "entityA", "1");
		writer.deleteActivity("test-014", "entityB", "2");
	}
	
	@Test
	public void user_with_newest_activity_should_be_first_in_feed_when_added_last()
	{
		final DateTime time1 = new DateTime(1000);
		final DateTime time2 = time1.plus(1000);
		final DateTime time3 = time1.plus(2000);
		writer.addActivity("test-015", "entityA",
				getActivity("1", time1, null));
		writer.addActivity("test-015", "entityB",
				getActivity("2", time2, null));
		writer.addActivity("test-015", "entityC",
				getActivity("3", time3, null));
		writer.followEntity("test-015", "user", "entityA", null);
		writer.followEntity("test-015", "user", "entityB", null);
		writer.followEntity("test-015", "user", "entityC", null);
		List<ActivityStreamsObject> activities =
				reader.getFeed("test-015", "user", 0, 3).getItems();
		assertEquals("newest activity not first", time3.getMillis(),
				activities.get(0).getSortTime().getMillis());
		
		//cleanup
		writer.unfollowEntity("test-015", "user", "entityA");
		writer.unfollowEntity("test-015", "user", "entityB");
		writer.unfollowEntity("test-015", "user", "entityC");
		writer.deleteActivity("test-015", "entityA", "1");
		writer.deleteActivity("test-015", "entityB", "2");
		writer.deleteActivity("test-015", "entityC", "3");
	}
	
	@Test
	public void user_with_oldest_activity_should_be_last_in_feed_when_added_last()
	{
		final DateTime time1 = new DateTime(3000);
		final DateTime time2 = time1.minus(1000);
		final DateTime time3 = time1.minus(2000);
		writer.addActivity("test-016", "entityA",
				getActivity("1", time1, null));
		writer.addActivity("test-016", "entityB",
				getActivity("2", time2, null));
		writer.addActivity("test-016", "entityC",
				getActivity("3", time3, null));
		writer.followEntity("test-016", "user", "entityA", null);
		writer.followEntity("test-016", "user", "entityB", null);
		writer.followEntity("test-016", "user", "entityC", null);
		List<ActivityStreamsObject> activities =
				reader.getFeed("test-016", "user", 0, 3).getItems();
		assertEquals("oldest activity not last", time3.getMillis(),
				activities.get(2).getSortTime().getMillis());
		
		//cleanup
		writer.unfollowEntity("test-016", "user", "entityA");
		writer.unfollowEntity("test-016", "user", "entityB");
		writer.unfollowEntity("test-016", "user", "entityC");
		writer.deleteActivity("test-016", "entityA", "1");
		writer.deleteActivity("test-016", "entityB", "2");
		writer.deleteActivity("test-016", "entityC", "3");
	}
	
	@Test
	public void new_activity_added_to_followed_entity_should_put_entity_into_correct_order_in_feed()
	{
		final DateTime time1 = new DateTime(2000); // A1
		final DateTime time2 = new DateTime(3000); // B1
		final DateTime time3 = new DateTime(4000); // A2
		final DateTime time4 = new DateTime(1000); // B2
		DateTime activityTime;
		List<ActivityStreamsObject> feed;
		
		// create activities for two entities
		// and have a user follow them
		writer.addActivity("test-017", "entityA",
				getActivity("1", time1, null));
		writer.addActivity("test-017", "entityB",
				getActivity("2", time2, null));
		writer.followEntity("test-017", "user", "entityA", null);
		writer.followEntity("test-017", "user", "entityB", null);
		// The descending time order right now is B1, A1
		feed = reader.getFeed("test-017", "user", 0, 2).getItems();
		activityTime = feed.get(0).getSortTime();
		assertEquals(time2.getMillis(), activityTime.getMillis());
		activityTime = feed.get(1).getSortTime();
		assertEquals(time1.getMillis(), activityTime.getMillis());

		// Now add the activity with time A2 to A, making
		// the time order A2, B1, A1
		writer.addActivity("test-017", "entityA",
				getActivity("3", time3, null));
		feed = reader.getFeed("test-017", "user", 0, 3).getItems();
		activityTime = feed.get(0).getSortTime();
		assertEquals(time3.getMillis(), activityTime.getMillis());
		activityTime = feed.get(1).getSortTime();
		assertEquals(time2.getMillis(), activityTime.getMillis());
		activityTime = feed.get(2).getSortTime();
		assertEquals(time1.getMillis(), activityTime.getMillis());
		
		// Now we'll add B2, but it actually comes EARLIER than
		// all the rest, and thus the descending order should become
		// A2 (time3), B1 (time2), A1 (time1), B2 (time4)
		writer.addActivity("test-017", "entityB",
				getActivity("4", time4, null));
		feed = reader.getFeed("test-017", "user", 0, 4).getItems();
		activityTime = feed.get(0).getSortTime();
		assertEquals(time3.getMillis(), activityTime.getMillis());
		activityTime = feed.get(1).getSortTime();
		assertEquals(time2.getMillis(), activityTime.getMillis());
		activityTime = feed.get(2).getSortTime();
		assertEquals(time1.getMillis(), activityTime.getMillis());
		activityTime = feed.get(3).getSortTime();
		assertEquals(time4.getMillis(), activityTime.getMillis());
		
		//cleanup
		writer.unfollowEntity("test-017", "user", "entityA");
		writer.unfollowEntity("test-017", "user", "entityB");
		writer.deleteActivity("test-017", "entityA", "1");
		writer.deleteActivity("test-017", "entityB", "2");
		writer.deleteActivity("test-017", "entityA", "3");
		writer.deleteActivity("test-017", "entityB", "4");
	}
	
	@Test
	public void is_following_should_return_null_if_user_does_not_follow()
	{
		writer.addActivity("test-018", "entity",
				getActivity("1", null, null));
		writer.addActivity("test-018", "user",
				getActivity("2", null, null));
		
		assertNull(reader.getDateTimeUserFollowedEntity(
				"test-018", "user", "entity"));
		
		//cleanup
		writer.deleteActivity("test-018", "entity", "1");
		writer.deleteActivity("test-018", "user", "2");
	}
	
	@Test
	public void is_following_should_return_datetime_if_user_follows_entity()
	{
		writer.addActivity("test-019", "entity",
				getActivity("1", null, null));
		writer.addActivity("test-019", "user",
				getActivity("2", null, null));
		DateTime followed = new DateTime(1977, 5, 13, 12, 00);
		writer.followEntity("test-019", "user", "entity", followed);
		
		
		assertEquals(followed.getMillis(), 
				reader.getDateTimeUserFollowedEntity(
						"test-019", "user", "entity").getMillis());

		//cleanup
		writer.unfollowEntity("test-019", "user", "entity");
		writer.deleteActivity("test-019", "entity", "1");
		writer.deleteActivity("test-019", "user", "2");
	}
	
	@Test
	public void is_following_should_return_null_after_unfollow()
	{
		writer.addActivity("test-020", "entity",
				getActivity("1", null, null));
		writer.addActivity("test-020", "user",
				getActivity("2", null, null));
		writer.followEntity("test-020", "user", "entity", null);
		writer.unfollowEntity("test-020", "user", "entity");
		
		assertNull(reader.getDateTimeUserFollowedEntity(
				"test-020", "user", "entity"));
		
		//cleanup
		writer.deleteActivity("test-020", "entity", "1");
		writer.deleteActivity("test-020", "user", "2");
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
		writer.addActivity("test-022-tenant1", "entity",
				getActivity("1", null, null));
		writer.addActivity("test-022-tenant2", "entity",
				getActivity("1", null, null));
		
		ActivityStreamsCollection activities =
				reader.getStream("test-022-tenant1", "entity", 0, 2);
		assertEquals("Stream not separate - invalid activity count.",
				1, activities.size());
		
		//cleanup
		writer.deleteActivity("test-022-tenant1", "entity", "1");
		writer.deleteActivity("test-022-tenant2", "entity", "1");
	}
	
	@Test
	public void start_parameter_should_start_stream_in_correct_place()
	{
		final DateTime time1 = DateTime.now();
		final DateTime time2 = DateTime.now().plus(1000);
		writer.addActivity("test-023", "entityA",
				getActivity("1", time1, null));
		writer.addActivity("test-023", "entityA",
				getActivity("2", time2, null));
		
		DateTime secondActivityTime =
				reader.getStream("test-023", "entityA", 1, 1).get(0).getSortTime();
		
		assertEquals(secondActivityTime.getMillis(), time1.getMillis());
		
		//cleanup
		writer.deleteActivity("test-023", "entityA", "1");
		writer.deleteActivity("test-023", "entityA", "2");
	}
	
	@Test
	public void start_parameter_should_start_feed_in_correct_place()
	{
		final DateTime time1 = DateTime.now();
		final DateTime time2 = DateTime.now().plus(1000);
		writer.addActivity("test-024", "entityA",
				getActivity("1", time1, null));
		writer.addActivity("test-024", "entityB",
				getActivity("2", time2, null));
		writer.followEntity("test-024", "user", "entityA", null);
		writer.followEntity("test-024", "user", "entityB", null);
		
		DateTime secondActivityTime =
				reader.getFeed("test-024", "user", 1, 1).get(0).getSortTime();
		
		assertEquals(secondActivityTime.getMillis(), time1.getMillis());

		//cleanup
		writer.deleteActivity("test-024", "entityA", "1");
		writer.deleteActivity("test-024", "entityB", "2");
		writer.unfollowEntity("test-024", "user", "entityA");
		writer.unfollowEntity("test-024", "user", "entityB");
	}
	
	@Test
	public void count_parameter_should_return_correct_number_of_stream_items()
	{
		final DateTime time1 = DateTime.now();
		final DateTime time2 = DateTime.now().plus(1000);
		writer.addActivity("test-025", "entityA",
				getActivity("1", time1, null));
		writer.addActivity("test-025", "entityA",
				getActivity("2", time2, null));
		
		assertEquals(1, reader.getStream("test-025", "entityA", 0, 1).size());
		
		//cleanup
		writer.deleteActivity("test-025", "entityA", "1");
		writer.deleteActivity("test-025", "entityA", "2");
	}

	@Test
	public void count_parameter_should_return_correct_number_of_feed_items()
	{
		final DateTime time1 = DateTime.now();
		final DateTime time2 = DateTime.now().plus(1000);
		writer.addActivity("test-026", "entityA",
				getActivity("1", time1, null));
		writer.addActivity("test-026", "entityB",
				getActivity("2", time2, null));
		writer.followEntity("test-026", "user", "entityA", null);
		writer.followEntity("test-026", "user", "entityB", null);
		
		assertEquals(1, reader.getFeed("test-026", "user", 0, 1).size());

		//cleanup
		writer.deleteActivity("test-026", "entityA", "1");
		writer.deleteActivity("test-026", "entityB", "2");
		writer.unfollowEntity("test-026", "user", "entityA");
		writer.unfollowEntity("test-026", "user", "entityB");
	}
	
	@Test
	public void updated_should_supercede_published_in_stream_order()
	{
		final DateTime current = DateTime.now();
		final DateTime futurePublish = current.plus(1000);
		final DateTime pastUpdate = current.minus(1000);
		
		writer.addActivity("test-027", "entity",
				getActivity("current", current, null));
		Activity withUpdate = getActivity("withUpdate", futurePublish, null);
		withUpdate.setUpdated(pastUpdate);
		writer.addActivity("test-027", "entity", withUpdate);
		
		List<ActivityStreamsObject> activities =
				reader.getStream("test-027", "entity", 0, 2).getItems();
		
		assertEquals(current.getMillis(),
				activities.get(0).getSortTime().getMillis());

		//cleanup
		writer.deleteActivity("test-027", "entity", "current");
		writer.deleteActivity("test-027", "entity", "withUpdate");
	}
	
	@Test
	public void updated_should_supercede_published_in_feed_order()
	{
		final DateTime current = DateTime.now();
		final DateTime futurePublish = current.plus(1000);
		final DateTime pastUpdate = current.minus(1000);
		
		writer.addActivity("test-028", "entity1",
				getActivity("current", current, null));
		Activity withUpdate = getActivity("withUpdate", futurePublish, null);
		withUpdate.setUpdated(pastUpdate);
		writer.addActivity("test-028", "entity2", withUpdate);
		
		writer.followEntity("test-028", "user", "entity1", current);
		writer.followEntity("test-028", "user", "entity2", current);		
		
		List<ActivityStreamsObject> activities =
				reader.getFeed("test-028", "user", 0, 2).getItems();
		
		assertEquals(current.getMillis(),
				activities.get(0).getSortTime().getMillis());
		
		//cleanup
		writer.deleteActivity("test-028", "entity1", "current");
		writer.deleteActivity("test-028", "entity2", "withUpdate");
		writer.unfollowEntity("test-028", "user", "entity1");
		writer.unfollowEntity("test-028", "user", "entity2");
}
	
	@Test
	public void zero_published_date_should_return_in_feed()
	{
		writer.addActivity("test-029", "entity1",
				new Activity("{\"published\":0,\"id\":\"test\"}"));
		writer.followEntity("test-029", "user", "entity1", DateTime.now());
		
		ActivityStreamsCollection activities =
				reader.getFeed("test-029", "user", 0, 1);
		
		assertEquals(1, activities.size());
		
		//cleanup
		writer.deleteActivity("test-029", "entity1", "test");
		writer.unfollowEntity("test-029", "user", "entity1");
	}
	
	@Test
	public void adding_a_comment_should_allow_retrieval_of_the_comment()
	{
		writer.addActivity("test-030", "entity",
				getActivity("activity", null, null));
		ActivityStreamsObject comment = new ActivityStreamsObject("comment");
		comment.setId("1");
		comment.setPublished(DateTime.now());
		writer.addComment("test-030", "entity", "activity", null, comment);
		
		List<ActivityStreamsObject> comments =
				reader.getComments("test-030", "entity", "activity", 0, 1)
					.getItems();
		
		assertThat(comments.get(0).toString(), containsString("comment"));
		
		//cleanup
		writer.deleteActivity("test-030", "entity", "activity");
	}
	
	@Test
	public void adding_an_activity_should_allow_retrieval_of_the_single_activity()
	{
		final DateTime instant = DateTime.now();
		writer.addActivity("test-031", "entity",
				getActivity("1", instant, null));
		final DateTime returnedTime =
				reader.getActivity("test-031", "entity", "1").getSortTime();
		
		assertEquals(instant.getMillis(), returnedTime.getMillis());
		
		//cleanup
		writer.deleteActivity("test-031", "entity", "1");
	}
	
	@Test
	public void retrieving_non_existent_activity_should_return_null()
	{
		assertNull(reader.getActivity("test-032", "entity", "1"));
	}
	
	@Test
	public void comments_should_be_returned_in_date_order()
	{
		final DateTime time1 = DateTime.now();
		final DateTime time2 = DateTime.now().plus(1000);
		writer.addActivity("test-033", "entity",
				getActivity("activity", null, null));
		ActivityStreamsObject comment1 = new ActivityStreamsObject("comment1");
		comment1.setId("1");
		comment1.setPublished(time1);
		ActivityStreamsObject comment2 = new ActivityStreamsObject("comment2");
		comment2.setId("2");
		comment2.setPublished(time2);
		writer.addComment("test-033", "entity", "activity", null, comment1);
		writer.addComment("test-033", "entity", "activity", null, comment2);
		
		List<ActivityStreamsObject> comments =
				reader.getComments("test-033", "entity", "activity", 0, 2)
					.getItems();
		
		assertEquals(time2.getMillis(),
				comments.get(0).getPublished().getMillis());
		
		//cleanup
		writer.deleteActivity("test-033", "entity", "activity");
	}
	
	@Test
	public void deleting_activity_should_remove_all_comments()
	{
		writer.addActivity("test-034", "entity",
				getActivity("activity", null, null));
		ActivityStreamsObject comment = new ActivityStreamsObject("comment");
		comment.setId("1");
		comment.setPublished(DateTime.now());
		writer.addComment("test-034", "entity", "activity", null, comment);
		
		writer.deleteActivity("test-034", "entity", "activity");
		
		writer.addActivity("test-034", "entity",
				getActivity("activity", null, null));

		writer.addComment("test-034", "entity", "activity", null, comment);
		
		//cleanup
		writer.deleteActivity("test-034", "entity", "activity");
	}
	
	@Test
	public void deleting_comment_should_maintain_comment_order()
	{
		final DateTime time1 = DateTime.now();
		final DateTime time2 = DateTime.now().plus(1000);
		final DateTime time3 = DateTime.now().plus(2000);
		writer.addActivity("test-035", "entity",
				getActivity("activity", null, null));
		ActivityStreamsObject comment1 = new ActivityStreamsObject("comment1");
		comment1.setId("1");
		comment1.setPublished(time1);
		ActivityStreamsObject comment2 = new ActivityStreamsObject("comment2");
		comment2.setId("2");
		comment2.setPublished(time2);
		ActivityStreamsObject comment3 = new ActivityStreamsObject("comment3");
		comment3.setId("3");
		comment3.setPublished(time3);
		writer.addComment("test-035", "entity", "activity", null, comment1);
		writer.addComment("test-035", "entity", "activity", null, comment2);
		writer.addComment("test-035", "entity", "activity", null, comment3);
		
		writer.deleteComment("test-035", "entity", "activity", "2");
		
		List<ActivityStreamsObject> comments =
				reader.getComments("test-035", "entity", "activity", 0, 2)
					.getItems();
		
		assertEquals(time3.getMillis(),
				comments.get(0).getPublished().getMillis());
		assertEquals(time1.getMillis(),
				comments.get(1).getPublished().getMillis());
		
		//cleanup
		writer.deleteActivity("test-035", "entity", "activity");		
	}
}