package com.collabinate.server.resources;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Conditions;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.data.Tag;

import com.collabinate.server.activitystreams.Activity;
import com.collabinate.server.activitystreams.ActivityStreamsCollection;
import com.collabinate.server.activitystreams.ActivityStreamsObject;
import com.google.gson.JsonParser;

/**
 * Tests for the Comments Resource
 * 
 * @author mafuba
 * 
 */
public class CommentsResourceTest extends GraphResourceTest
{
	@Test
	public void get_comments_for_nonexistent_activity_should_return_404()
	{
		assertEquals(Status.CLIENT_ERROR_NOT_FOUND, get().getStatus());
	}
	
	@Test
	public void get_comments_for_existing_activity_should_return_200()
	{
		addActivity();
		assertEquals(Status.SUCCESS_OK, get().getStatus());
	}
	
	@Test
	public void get_should_return_json_content_type()
	{
		addActivity();
		assertEquals(MediaType.APPLICATION_JSON,
				get().getEntity().getMediaType());
	}
	
	@Test
	public void get_response_should_contain_etag_header()
	{
		addActivity();
		assertTrue(null != get().getEntity().getTag());
	}
	
	@Test
	public void etag_should_change_when_comments_change()
	{
		addActivity();
		Tag tag1 = get().getEntity().getTag();
		post("TEST", MediaType.TEXT_PLAIN);
		Tag tag2 = get().getEntity().getTag();
		
		assertNotEquals(tag1, tag2);
	}
	
	@Test
	public void matching_etag_should_return_304()
	{
		addActivity();
		Tag etag = get().getEntity().getTag();
		Request request = getRequest(Method.GET, null);
		Conditions conditions = new Conditions();
		conditions.getNoneMatch().add(etag);
		request.setConditions(conditions);
		
		assertEquals(Status.REDIRECTION_NOT_MODIFIED,
				getResponse(request).getStatus());
	}
	
	@Test
	public void non_matching_etag_should_return_200()
	{
		addActivity();
		Request request = getRequest(Method.GET, null);
		Conditions conditions = new Conditions();
		conditions.getNoneMatch().add(new Tag("abc"));
		request.setConditions(conditions);
		
		assertEquals(Status.SUCCESS_OK, getResponse(request).getStatus());
	}
	
	@Test
	public void post_to_missing_activity_should_return_404()
	{
		assertEquals(Status.CLIENT_ERROR_NOT_FOUND, post().getStatus());
	}
	
	@Test
	public void item_added_to_comments_should_return_201()
	{
		addActivity();
		assertEquals(Status.SUCCESS_CREATED, post().getStatus());
	}
	
	@Test
	public void item_added_to_comments_should_create_and_return_child_location()
	{
		addActivity();
		assertEquals(
				getRequest(Method.POST, null).getResourceRef().getPath() + "/",
				post().getLocationRef().getParentRef().getPath());
	}

	@Test
	public void item_added_to_comments_should_have_text_in_post_response_body()
	{
		addActivity();
		String entityBody = "TEST";
		Response response = post(entityBody, MediaType.TEXT_PLAIN);
		
		assertThat(response.getEntityAsText(), containsString(entityBody));
	}
	
	@Test
	public void post_response_body_should_be_json()
	{
		addActivity();
		String entityBody = "TEST,";
		Response response = post(entityBody, MediaType.TEXT_PLAIN);
		
		new JsonParser().parse(response.getEntityAsText());
	}
	
	@Test
	public void post_response_body_should_contain_added_fields()
	{
		addActivity();
		String entityBody = "TEST";
		Response response = post(entityBody, MediaType.TEXT_PLAIN);
		
		assertThat(response.getEntityAsText(), containsString("\"id\""));
	}
	
	@Test
	public void comment_should_be_json_object()
	{
		addActivity();
		post("TEST", MediaType.TEXT_PLAIN);
		// parser will throw if result is not json
		new JsonParser().parse(get().getEntityAsText());
	}

	@Test
	public void item_added_to_comments_should_appear_in_comments()
	{
		addActivity();
		String entityBody = "TEST";
		post(entityBody, MediaType.TEXT_PLAIN);
		
		assertThat(get().getEntityAsText(), containsString(entityBody));		
	}
	
	@Test
	public void comment_should_not_use_given_id()
	{
		addActivity();
		String entityBody = "{\"id\":\"TEST\"}";
		post(entityBody, MediaType.TEXT_PLAIN);
		
		ActivityStreamsCollection comments =
				new ActivityStreamsCollection(get().getEntityAsText());
		
		assertNotEquals("TEST", comments.get(0).getId());
	}
	
	@Test
	public void individual_comment_should_be_retrievable_via_generated_id()
	{
		addActivity();
		String entityBody = "{\"id\":\"test\",\"content\":\"TEST\"}";
		Activity posted = new Activity(
				post(entityBody, MediaType.TEXT_PLAIN).getEntityAsText());
		
		Request request = new Request(Method.GET,
			 "riap://application/1/tenant/entities/entity"
				+ "/stream/activity/comments/"
				+ posted.getId());
		ActivityStreamsObject comment = new ActivityStreamsObject(
				component.handle(request).getEntityAsText());

		assertEquals("TEST", comment.getContent());
	}
	
	@Test
	public void original_id_should_be_preserved()
	{
		addActivity();
		String entityBody = "{\"id\":\"original\",\"actor\":{\"id\":\"foo\"}}";
		ActivityStreamsObject posted = new ActivityStreamsObject(
				post(entityBody, MediaType.TEXT_PLAIN).getEntityAsText());
		
		assertEquals("original", posted.getCollabinateValue("originalId"));
	}
	
	@Test
	public void posted_raw_text_should_have_id_in_comment_when_retrieved()
	{
		addActivity();
		post("test", MediaType.TEXT_PLAIN);
		
		ActivityStreamsCollection comments =
				new ActivityStreamsCollection(get().getEntityAsText());
		
		assertNotNull(comments.get(0).getId());
	}
		
	@Test
	public void comment_should_always_use_comment_object_type()
	{
		addActivity();
		String entityBody = "{\"objectType\":\"TEST\"}";
		post(entityBody, MediaType.TEXT_PLAIN);
		
		ActivityStreamsCollection comments =
				new ActivityStreamsCollection(get().getEntityAsText());
		
		assertEquals("comment", comments.get(0).getObjectType());
	}
	
	@Test
	public void original_object_type_should_be_preserved()
	{
		addActivity();
		String entityBody = "{\"objectType\":\"original\"}";
		ActivityStreamsObject posted = new ActivityStreamsObject(
				post(entityBody, MediaType.TEXT_PLAIN).getEntityAsText());
		
		assertEquals("original",
				posted.getCollabinateValue("originalObjectType"));
	}
	
	@Test
	public void user_id_should_be_preserved()
	{
		addActivity();
		post("?userId=user");
		
		ActivityStreamsCollection comments = 
				new ActivityStreamsCollection(get().getEntityAsText());

		assertEquals("user", comments.get(0).getCollabinateValue("userId"));
	}
	
	@Test
	public void comment_date_in_post_should_be_used_in_comments()
	{
		addActivity();
		DateTime dateTime = new DateTime(1977, 5, 13, 5, 13, DateTimeZone.UTC);
		ActivityStreamsObject comment = new ActivityStreamsObject();
		comment.setPublished(dateTime);
		String entityBody = comment.toString();
		
		post(entityBody, MediaType.APPLICATION_JSON);
		
		ActivityStreamsCollection comments = 
				new ActivityStreamsCollection(get().getEntityAsText());
		
		assertEquals(dateTime, comments.get(0).getPublished());
	}
	
	@Test
	public void comments_should_appear_in_correct_date_order()
	{
		addActivity();
		DateTime dateTime1 = new DateTime(1977, 5, 13, 5, 13, DateTimeZone.UTC);
		ActivityStreamsObject comment1 = new ActivityStreamsObject();
		comment1.setPublished(dateTime1);

		DateTime dateTime2 = new DateTime(1973, 6, 28, 6, 28, DateTimeZone.UTC);
		ActivityStreamsObject comment2 = new ActivityStreamsObject();
		comment2.setPublished(dateTime2);
		
		post(comment1.toString(), MediaType.APPLICATION_JSON);
		post(comment2.toString(), MediaType.APPLICATION_JSON);
		
		ActivityStreamsCollection comments = 
				new ActivityStreamsCollection(get().getEntityAsText());
		
		assertEquals(dateTime1, comments.get(0).getPublished());
	}
	
	@Test
	public void collection_should_include_total_items_property()
	{
		addActivity();
		assertThat(get().getEntityAsText(), containsString("totalItems"));
	}
	
	@Test
	public void collection_should_include_zero_count_for_empty_comments()
	{
		addActivity();
		ActivityStreamsCollection comments =
				new ActivityStreamsCollection(get().getEntityAsText());
		
		assertEquals(0, comments.getTotalItems());
	}
	
	@Test
	public void collection_should_include_count_that_matches_comments()
	{
		addActivity();
		post();
		post();

		ActivityStreamsCollection comments =
				new ActivityStreamsCollection(get().getEntityAsText());
		
		assertEquals(2, comments.getTotalItems());
	}
	
	@Test
	public void collection_count_should_account_for_deleted_comments()
	{
		addActivity();
		post();
		post();
		
		ActivityStreamsObject deleted =
				new ActivityStreamsObject(post().getEntityAsText());
		Request request = new Request(Method.DELETE,
			"riap://application/1/tenant/entities/entity/"
			+ "stream/activity/comments/"
			+ deleted.getId());
		component.handle(request);

		ActivityStreamsCollection comments =
				new ActivityStreamsCollection(get().getEntityAsText());
		
		assertEquals(2, comments.getTotalItems());
	}
	
	/**
	 * Prepares for comment work by creating an activity.
	 */
	private void addActivity()
	{
		component.handle(new Request(Method.PUT,
			"riap://application/1/tenant/entities/entity/stream/activity"));
	}
	
	@Override
	protected String getResourcePath()
	{
		return "/1/tenant/entities/entity/stream/activity/comments";
	}

}
