package com.collabinate.server.resources;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.restlet.Request;
import org.restlet.data.Method;
import org.restlet.data.Status;

import com.collabinate.server.activitystreams.ActivityStreamsCollection;

/**
 * Tests for the Likes Resource
 * 
 * @author mafuba
 * 
 */
public class LikesResourceTest extends GraphResourceTest
{
	@Test
	public void get_likes_for_nonexistent_activity_should_return_404()
	{
		assertEquals(Status.CLIENT_ERROR_NOT_FOUND, get().getStatus());
	}
	
	@Test
	public void get_likes_for_existing_activity_should_return_200()
	{
		addActivity();
		assertEquals(Status.SUCCESS_OK, get().getStatus());
	}
	
	@Test
	public void likes_for_never_liked_activity_should_be_empty()
	{
		addActivity();
		
		ActivityStreamsCollection likes =
				new ActivityStreamsCollection(get().getEntityAsText());
		
		assertEquals(0, likes.size());
	}
	
	@Test
	public void likes_for_liked_activity_should_have_item()
	{
		addActivity();

		// make user user like activity
		Request request = new Request(Method.PUT,
			"riap://application/1/tenant/users/user/likes/entity/activity");
		component.handle(request);

		ActivityStreamsCollection likes =
				new ActivityStreamsCollection(get().getEntityAsText());
		
		assertEquals(1, likes.size());
	}
	
	@Test
	public void likes_for_activity_liked_by_user_should_contain_user_id()
	{
		addActivity();
		// make user user like activity
		Request request = new Request(Method.PUT,
			"riap://application/1/tenant/users/user/likes/entity/activity");
		component.handle(request);
		
		assertThat(get().getEntityAsText(), containsString("user"));
	}
	
	@Test
	public void likes_should_not_contain_user_that_unliked_entity()
	{
		addActivity();
		// make user user1 like activity
		Request request = new Request(Method.PUT,
			"riap://application/1/tenant/users/user1/likes/entity/activity");
		component.handle(request);
		// make user user2 like activity
		request = new Request(Method.PUT,
			"riap://application/1/tenant/users/user2/likes/entity/activity");
		component.handle(request);
		// make user user1 unlike activity
		request = new Request(Method.DELETE,
			"riap://application/1/tenant/users/user1/likes/entity/activity");
		component.handle(request);
		
		assertThat(get().getEntityAsText(), not(containsString("user1")));
	}
	
	@Test
	public void collection_should_include_total_items_property()
	{
		addActivity();
		assertThat(get().getEntityAsText(), containsString("totalItems"));
	}
	
	@Test
	public void collection_should_include_zero_count_for_no_likes()
	{
		addActivity();
		ActivityStreamsCollection likes =
				new ActivityStreamsCollection(get().getEntityAsText());
		
		assertEquals(0, likes.getTotalItems());
	}
	
	@Test
	public void collection_should_include_count_that_matches_likes()
	{
		addActivity();
		// make user user1 like activity
		Request request = new Request(Method.PUT,
			"riap://application/1/tenant/users/user1/likes/entity/activity");
		component.handle(request);
		// make user user2 like activity
		request = new Request(Method.PUT,
			"riap://application/1/tenant/users/user2/likes/entity/activity");
		component.handle(request);

		ActivityStreamsCollection likes =
				new ActivityStreamsCollection(get().getEntityAsText());
		
		assertEquals(2, likes.getTotalItems());
	}
	
	@Test
	public void collection_count_should_account_for_unlikes()
	{
		addActivity();
		// make user user1 like activity
		Request request = new Request(Method.PUT,
			"riap://application/1/tenant/users/user1/likes/entity/activity");
		component.handle(request);
		// make user user2 like activity
		request = new Request(Method.PUT,
			"riap://application/1/tenant/users/user2/likes/entity/activity");
		component.handle(request);
		// make user user1 unlike activity
		request = new Request(Method.DELETE,
			"riap://application/1/tenant/users/user1/likes/entity/activity");
		component.handle(request);

		ActivityStreamsCollection likes =
				new ActivityStreamsCollection(get().getEntityAsText());
		
		assertEquals(1, likes.getTotalItems());
	}
	
	/**
	 * Prepares for like work by creating an activity.
	 */
	private void addActivity()
	{
		component.handle(new Request(Method.PUT,
			"riap://application/1/tenant/entities/entity/stream/activity"));
	}
	
	@Override
	protected String getResourcePath()
	{
		return "/1/tenant/entities/entity/stream/activity/likes";
	}

}
