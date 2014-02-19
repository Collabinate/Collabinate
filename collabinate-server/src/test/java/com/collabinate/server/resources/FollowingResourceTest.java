package com.collabinate.server.resources;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;

import com.collabinate.server.activitystreams.ActivityStreamsCollection;
import com.collabinate.server.activitystreams.ActivityStreamsObject;

/**
 * Tests for the Following Resource
 * 
 * @author mafuba
 * 
 */
public class FollowingResourceTest extends GraphResourceTest
{
	@Test
	public void following_for_new_user_should_return_empty_array()
	{
		assertThat(get().getEntityAsText(), containsString("\"items\":[]"));		
	}
	
	@Test
	public void following_for_user_following_entity_should_contain_entity_id()
	{
		// make user user follow entity entity
		Request request = new Request(Method.PUT,
				"riap://application/1/tenant/users/user/following/entity");
		component.handle(request);
		
		assertThat(get().getEntityAsText(), containsString("entity"));
	}
	
	@Test
	public void following_entity_id_should_not_contain_tenant()
	{
		// make user user follow entity entity
		Request request = new Request(Method.PUT,
				"riap://application/1/tenant/users/user/following/entity");
		component.handle(request);
		
		assertThat(get().getEntityAsText(), not(containsString("tenant")));
	}
	
	@Test
	public void following_should_not_contain_unfollowed_entity()
	{
		// make user user follow entity entity1
		Request request = new Request(Method.PUT,
				"riap://application/1/tenant/users/user/following/entity1");
		component.handle(request);
		// make user user follow entity entity2
		request = new Request(Method.PUT,
				"riap://application/1/tenant/users/user/following/entity2");
		component.handle(request);
		// make user user unfollow entity entity1
		request = new Request(Method.DELETE,
				"riap://application/1/tenant/users/user/following/entity1");
		component.handle(request);
		
		assertThat(get().getEntityAsText(), not(containsString("entity1")));
	}
	
	@Test
	public void collection_should_include_total_items_property()
	{
		assertThat(get().getEntityAsText(), containsString("totalItems"));
	}
	
	@Test
	public void collection_should_include_zero_count_for_no_following()
	{
		ActivityStreamsCollection following =
				new ActivityStreamsCollection(get().getEntityAsText());
		
		assertEquals(0, following.getTotalItems());
	}
	
	@Test
	public void collection_should_include_count_that_matches_following()
	{
		// make user user follow entity entity1
		Request request = new Request(Method.PUT,
				"riap://application/1/tenant/users/user/following/entity1");
		component.handle(request);
		// make user user follow entity entity2
		request = new Request(Method.PUT,
				"riap://application/1/tenant/users/user/following/entity2");
		component.handle(request);

		ActivityStreamsCollection following =
				new ActivityStreamsCollection(get().getEntityAsText());
		
		assertEquals(2, following.getTotalItems());
	}
	
	@Test
	public void collection_count_should_account_for_unfollows()
	{
		// make user user follow entity entity1
		Request request = new Request(Method.PUT,
				"riap://application/1/tenant/users/user/following/entity1");
		component.handle(request);
		// make user user follow entity entity2
		request = new Request(Method.PUT,
				"riap://application/1/tenant/users/user/following/entity2");
		component.handle(request);
		// make user user unfollow entity entity1
		request = new Request(Method.DELETE,
				"riap://application/1/tenant/users/user/following/entity1");
		component.handle(request);
		
		ActivityStreamsCollection following =
				new ActivityStreamsCollection(get().getEntityAsText());
		
		assertEquals(1, following.getTotalItems());
	}
	
	@Test
	public void empty_post_should_return_200()
	{
		assertEquals(Status.SUCCESS_OK, post().getStatus());
	}
	
	@Test
	public void post_of_empty_collection_should_return_200()
	{
		ActivityStreamsCollection collection = new ActivityStreamsCollection();
		assertEquals(Status.SUCCESS_OK,
				post(collection.toString(), MediaType.APPLICATION_JSON)
				.getStatus());
	}
	
	@Test
	public void post_without_collection_should_not_affect_following_count()
	{
		ActivityStreamsCollection originalFollowing =
				new ActivityStreamsCollection(get().getEntityAsText());
		
		post((new ActivityStreamsCollection("foo")).toString(),
				MediaType.APPLICATION_JSON);
		
		ActivityStreamsCollection newFollowing =
				new ActivityStreamsCollection(get().getEntityAsText());		
		
		assertEquals(originalFollowing.size(), newFollowing.size());
	}
	
	@Test
	public void post_with_new_following_should_increase_following_count()
	{
		ActivityStreamsCollection originalFollowing =
				new ActivityStreamsCollection(get().getEntityAsText());
		
		ActivityStreamsCollection addFollowing =
				new ActivityStreamsCollection();
		
		ActivityStreamsObject object = new ActivityStreamsObject();
		object.setId("foo");
		
		addFollowing.add(object);
		
		post(addFollowing.toString(), MediaType.APPLICATION_JSON);
		
		ActivityStreamsCollection newFollowing =
				new ActivityStreamsCollection(get().getEntityAsText());		
		
		assertEquals(originalFollowing.size() + addFollowing.size(),
				newFollowing.size());
	}
	
	@Test
	public void post_with_existing_following_should_not_affect_following_count()
	{		
		ActivityStreamsCollection addFollowing =
				new ActivityStreamsCollection();
		
		ActivityStreamsObject object = new ActivityStreamsObject();
		object.setId("foo");
		
		addFollowing.add(object);
		
		post(addFollowing.toString(), MediaType.APPLICATION_JSON);
		
		ActivityStreamsCollection originalFollowing =
				new ActivityStreamsCollection(get().getEntityAsText());

		post(addFollowing.toString(), MediaType.APPLICATION_JSON);

		ActivityStreamsCollection newFollowing =
				new ActivityStreamsCollection(get().getEntityAsText());		
		
		assertEquals(originalFollowing.size(), newFollowing.size());
	}
	
	@Override
	protected String getResourcePath()
	{
		return "/1/tenant/users/user/following";
	}

}
