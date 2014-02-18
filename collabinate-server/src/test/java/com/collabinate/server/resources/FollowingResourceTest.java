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
		
		assertThat(get().getEntityAsText(),
				not(containsString("tenant/entity")));
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
	
	@Override
	protected String getResourcePath()
	{
		return "/1/tenant/users/user/following";
	}

}
