package com.collabinate.server.resources;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.restlet.Request;
import org.restlet.data.Method;

import com.collabinate.server.activitystreams.ActivityStreamsCollection;

/**
 * Tests for the Followers Resource
 * 
 * @author mafuba
 * 
 */
public class FollowersResourceTest extends GraphResourceTest
{
	@Test
	public void followers_for_new_entity_should_return_empty_array()
	{
		assertThat(get().getEntityAsText(), containsString("\"items\":[]"));		
	}
	
	@Test
	public void followers_for_entity_followed_by_user_should_contain_user_id()
	{
		// make user user follow entity entity
		Request request = new Request(Method.PUT,
				"riap://application/1/tenant/users/user/following/entity");
		component.handle(request);
		
		assertThat(get().getEntityAsText(), containsString("user"));
	}
	
	@Test
	public void followers_user_id_should_not_contain_tenant()
	{
		// make user user follow entity entity
		Request request = new Request(Method.PUT,
				"riap://application/1/tenant/users/user/following/entity");
		component.handle(request);
		
		assertThat(get().getEntityAsText(),
				not(containsString("tenant/user")));
	}
	
	@Test
	public void followers_should_not_contain_user_that_unfollowed_entity()
	{
		// make user user1 follow entity entity
		Request request = new Request(Method.PUT,
				"riap://application/1/tenant/users/user1/following/entity");
		component.handle(request);
		// make user user2 follow entity entity
		request = new Request(Method.PUT,
				"riap://application/1/tenant/users/user2/following/entity");
		component.handle(request);
		// make user user1 unfollow entity entity
		request = new Request(Method.DELETE,
				"riap://application/1/tenant/users/user1/following/entity");
		component.handle(request);
		
		assertThat(get().getEntityAsText(), not(containsString("user1")));
	}
	
	@Test
	public void collection_should_include_total_items_property()
	{
		assertThat(get().getEntityAsText(), containsString("totalItems"));
	}
	
	@Test
	public void collection_should_include_zero_count_for_no_followers()
	{
		ActivityStreamsCollection followers =
				new ActivityStreamsCollection(get().getEntityAsText());
		
		assertEquals(0, followers.getTotalItems());
	}
	
	@Test
	public void collection_should_include_count_that_matches_followers()
	{
		// make user user1 follow entity entity
		Request request = new Request(Method.PUT,
				"riap://application/1/tenant/users/user1/following/entity");
		component.handle(request);
		// make user user2 follow entity entity
		request = new Request(Method.PUT,
				"riap://application/1/tenant/users/user2/following/entity");
		component.handle(request);

		ActivityStreamsCollection followers =
				new ActivityStreamsCollection(get().getEntityAsText());
		
		assertEquals(2, followers.getTotalItems());
	}
	
	@Test
	public void collection_count_should_account_for_unfollows()
	{
		// make user user1 follow entity entity
		Request request = new Request(Method.PUT,
				"riap://application/1/tenant/users/user1/following/entity");
		component.handle(request);
		// make user user2 follow entity entity
		request = new Request(Method.PUT,
				"riap://application/1/tenant/users/user2/following/entity");
		component.handle(request);
		// make user user1 unfollow entity entity
		request = new Request(Method.DELETE,
				"riap://application/1/tenant/users/user1/following/entity");
		component.handle(request);
		
		ActivityStreamsCollection followers =
				new ActivityStreamsCollection(get().getEntityAsText());
		
		assertEquals(1, followers.getTotalItems());
	}
	
	@Override
	protected String getResourcePath()
	{
		return "/1/tenant/entities/entity/followers";
	}

}
