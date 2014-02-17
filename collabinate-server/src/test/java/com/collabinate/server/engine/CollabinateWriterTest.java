package com.collabinate.server.engine;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.collabinate.server.activitystreams.Activity;
import com.collabinate.server.activitystreams.ActivityStreamsObject;
import com.collabinate.server.engine.CollabinateWriter;

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
	public void add_activity_should_not_allow_null_tenant_ID()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("tenantId");
		writer.addActivity(null, "entity", new Activity());
	}
	
	@Test
	public void add_activity_should_not_allow_null_entity_ID()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("entityId");
		writer.addActivity("c", null, new Activity());
	}
	
	@Test
	public void add_activity_should_not_allow_null_activity()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("activity");
		writer.addActivity("c", "", null);
	}

	@Test
	public void adding_duplicate_activities_should_fail()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("exists");

		Activity activity = new Activity();
		activity.setId("1");
		writer.addActivity("c", "entity", activity);
		writer.addActivity("c", "entity", activity);
		
		//cleanup
		writer.deleteActivity("c", "entity", "1");
	}
	
	@Test
	public void delete_activity_should_not_allow_null_tenant_ID()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("tenantId");
		writer.deleteActivity(null, "entity", "");
	}
		
	@Test
	public void delete_activity_should_not_allow_null_entity_ID()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("entityId");
		writer.deleteActivity("c", null, "");
	}
		
	@Test
	public void delete_activity_should_not_allow_null_activity_ID()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("activityId");
		writer.deleteActivity("c", "", null);
	}
	
	@Test
	public void deleting_nonexistent_activity_should_succeed()
	{
		Activity activity = new Activity();
		activity.setId("1");
		writer.addActivity("c", "entity", activity);
		writer.deleteActivity("c", "entity", "2");
		
		//cleanup
		writer.deleteActivity("c", "entity", "1");
	}
	
	@Test
	public void follow_entity_should_not_allow_null_tenant_ID()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("tenantId");
		writer.followEntity(null, "user", "1", null);
	}
	
	@Test
	public void follow_entity_should_not_allow_null_user_ID()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("userId");
		writer.followEntity("c", null, "1", null);
	}
	
	@Test
	public void follow_entity_should_not_allow_null_entity_ID()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("entityId");
		writer.followEntity("c", "user", null, null);
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
	
	@Test
	public void add_comment_should_not_allow_null_tenant_ID()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("tenantId");
		writer.addComment(null, "entity", "activity", "user",
				new ActivityStreamsObject());
	}
	
	@Test
	public void add_comment_should_not_allow_null_entity_ID()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("entityId");
		writer.addComment("c", null, "activity", "user",
				new ActivityStreamsObject());
	}
	
	@Test
	public void add_comment_should_not_allow_null_activity_ID()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("activityId");
		writer.addComment("c", "entity", null, "user",
				new ActivityStreamsObject());
	}

	@Test
	public void add_comment_should_not_allow_null_comment()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("comment");
		writer.addComment("c", "entity", "activity", "user", null);
	}
	
	@Test
	public void add_comment_should_allow_null_user_ID()
	{
		writer.addComment("c", "entity", "activity", null,
				new ActivityStreamsObject());
	}
	
	@Test
	public void delete_comment_should_not_allow_null_tenant_ID()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("tenantId");
		writer.deleteComment(null, "entity", "activity", "1");
	}
	
	@Test
	public void delete_comment_should_not_allow_null_entity_ID()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("entityId");
		writer.deleteComment("c", null, "activity", "1");
	}
	
	@Test
	public void delete_comment_should_not_allow_null_activity_ID()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("activityId");
		writer.deleteComment("c", "entity", null, "1");
	}
	
	@Test
	public void delete_comment_should_not_allow_null_comment_ID()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("commentId");
		writer.deleteComment("c", "entity", "activity", null);
	}
	
	@Test
	public void like_activity_should_not_allow_null_tenant_ID()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("tenantId");
		writer.likeActivity(null, "user", "entity", "activity");
	}

	@Test
	public void like_activity_should_not_allow_null_user_ID()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("userId");
		writer.likeActivity("c", null, "entity", "activity");
	}

	@Test
	public void like_activity_should_not_allow_null_entity_ID()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("entityId");
		writer.likeActivity("c", "user", null, "activity");
	}

	@Test
	public void like_activity_should_not_allow_null_activity_ID()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("activityId");
		writer.likeActivity("c", "user", "entity", null);
	}
	
	@Test
	public void liking_same_activity_multiple_times_should_succeed()
	{
		writer.likeActivity("c", "user", "entity", "activity");
		writer.likeActivity("c", "user", "entity", "activity");
	}
	
	@Test
	public void unlike_activity_should_not_allow_null_tenant_ID()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("tenantId");
		writer.unlikeActivity(null, "user", "entity", "activity");
	}

	@Test
	public void unlike_activity_should_not_allow_null_user_ID()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("userId");
		writer.unlikeActivity("c", null, "entity", "activity");
	}

	@Test
	public void unlike_activity_should_not_allow_null_entity_ID()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("entityId");
		writer.unlikeActivity("c", "user", null, "activity");
	}

	@Test
	public void unlike_activity_should_not_allow_null_activity_ID()
	{
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("activityId");
		writer.unlikeActivity("c", "user", "entity", null);
	}
	
	@Test
	public void unliking_same_activity_multiple_times_should_succeed()
	{
		writer.unlikeActivity("c", "user", "entity", "activity");
		writer.unlikeActivity("c", "user", "entity", "activity");
	}
}