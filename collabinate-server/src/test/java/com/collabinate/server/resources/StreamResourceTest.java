package com.collabinate.server.resources;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

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
import com.google.gson.JsonParser;

/**
 * Tests for the Stream Resource
 * 
 * @author mafuba
 * 
 */
public class StreamResourceTest extends GraphResourceTest
{
	@Test
	public void get_empty_stream_should_return_200()
	{
		assertEquals(Status.SUCCESS_OK, get().getStatus());
	}
	
	@Test
	public void get_should_return_json_content_type()
	{
		assertEquals(MediaType.APPLICATION_JSON,
				get().getEntity().getMediaType());
	}
	
	@Test
	public void get_response_should_contain_etag_header()
	{
		assertTrue(null != get().getEntity().getTag());
	}
	
	@Test
	public void etag_should_change_when_stream_changes()
	{
		Tag tag1 = get().getEntity().getTag();
		post("TEST", MediaType.TEXT_PLAIN);
		Tag tag2 = get().getEntity().getTag();
		
		assertNotEquals(tag1, tag2);
	}
	
	@Test
	public void matching_etag_should_return_304()
	{
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
		Request request = getRequest(Method.GET, null);
		Conditions conditions = new Conditions();
		conditions.getNoneMatch().add(new Tag("abc"));
		request.setConditions(conditions);
		
		assertEquals(Status.SUCCESS_OK, getResponse(request).getStatus());
	}
	
	@Test
	public void item_added_to_stream_should_return_201()
	{
		assertEquals(Status.SUCCESS_CREATED, post().getStatus());
	}
	
	@Test
	public void item_added_to_stream_should_create_and_return_child_location()
	{
		assertEquals(
				getRequest(Method.POST, null).getResourceRef().getPath() + "/",
				post().getLocationRef().getParentRef().getPath());
	}
	
	@Test
	public void item_added_to_stream_should_have_entity_in_post_response_body()
	{
		String entityBody = "TEST";
		Response response = post(entityBody, MediaType.TEXT_PLAIN);
		
		assertThat(response.getEntityAsText(), containsString(entityBody));
	}
	
	@Test
	public void post_response_body_should_be_json()
	{
		String entityBody = "TEST,";
		Response response = post(entityBody, MediaType.TEXT_PLAIN);
		
		new JsonParser().parse(response.getEntityAsText());
	}
	
	@Test
	public void post_response_body_should_contain_added_fields()
	{
		String entityBody = "TEST";
		Response response = post(entityBody, MediaType.TEXT_PLAIN);
		
		assertThat(response.getEntityAsText(), containsString("\"id\""));
	}
	
	@Test
	public void stream_should_be_json_object()
	{
		post("TEST", MediaType.TEXT_PLAIN);
		// parser will throw if result is not json
		new JsonParser().parse(get().getEntityAsText());
	}
	
	@Test
	public void item_added_to_stream_should_appear_in_stream()
	{
		String entityBody = "TEST";
		post(entityBody, MediaType.TEXT_PLAIN);
		
		assertThat(get().getEntityAsText(), containsString(entityBody));		
	}
	
	@Test
	public void activity_should_not_use_given_id()
	{
		String entityBody = "{\"id\":\"TEST\"}";
		post(entityBody, MediaType.TEXT_PLAIN);
		
		ActivityStreamsCollection stream =
				new ActivityStreamsCollection(get().getEntityAsText());
		
		assertNotEquals("TEST", stream.get(0).getId());
	}
	
	@Test
	public void individual_activity_should_be_retrievable_via_generated_id()
	{
		String entityBody = "{\"id\":\"test\",\"actor\":{\"id\":\"foo\"}}";
		Activity posted = new Activity(
				post(entityBody, MediaType.TEXT_PLAIN).getEntityAsText());
		
		Request request = new Request(Method.GET,
				"riap://application/1/tenant/entities/entity/stream/"
				+ posted.getId());
		Activity activity = new Activity(
				component.handle(request).getEntityAsText());

		assertEquals("foo", activity.getActor().getId());
	}
	
	@Test
	public void original_id_should_be_preserved()
	{
		String entityBody = "{\"id\":\"original\",\"actor\":{\"id\":\"foo\"}}";
		Activity posted = new Activity(
				post(entityBody, MediaType.TEXT_PLAIN).getEntityAsText());
		
		assertEquals("original", posted.getCollabinateValue("originalId"));
	}
	
	@Test
	public void posted_raw_text_should_have_id_in_activity_when_retrieved()
	{
		post("test", MediaType.TEXT_PLAIN);
		
		ActivityStreamsCollection stream =
				new ActivityStreamsCollection(get().getEntityAsText());
		
		assertNotNull(stream.get(0).getId());
	}
	
	@Test
	public void activity_stream_date_in_post_should_be_used_in_stream()
	{
		DateTime dateTime = new DateTime(1977, 5, 13, 5, 13, DateTimeZone.UTC);
		Activity activity = new Activity();
		activity.setPublished(dateTime);
		String entityBody = activity.toString();
		
		post(entityBody, MediaType.APPLICATION_JSON);
		
		ActivityStreamsCollection stream = 
				new ActivityStreamsCollection(get().getEntityAsText());
		
		assertEquals(dateTime, stream.get(0).getPublished());
	}
	
	@Test
	public void activities_should_appear_in_correct_date_order()
	{
		DateTime dateTime1 = new DateTime(1977, 5, 13, 5, 13, DateTimeZone.UTC);
		Activity activity1 = new Activity();
		activity1.setPublished(dateTime1);

		DateTime dateTime2 = new DateTime(1973, 6, 28, 6, 28, DateTimeZone.UTC);
		Activity activity2 = new Activity();
		activity2.setPublished(dateTime2);
		
		post(activity1.toString(), MediaType.APPLICATION_JSON);
		post(activity2.toString(), MediaType.APPLICATION_JSON);
		
		ActivityStreamsCollection stream = 
				new ActivityStreamsCollection(get().getEntityAsText());
		
		assertEquals(dateTime1, stream.get(0).getPublished());
	}
	
	@Test
	public void collection_should_include_total_items_property()
	{
		assertThat(get().getEntityAsText(), containsString("totalItems"));
	}
	
	@Test
	public void collection_should_include_zero_count_for_empty_stream()
	{
		ActivityStreamsCollection stream =
				new ActivityStreamsCollection(get().getEntityAsText());
		
		assertEquals(0, stream.getTotalItems());
	}
	
	@Test
	public void collection_should_include_count_that_matches_stream()
	{
		post();
		post();

		ActivityStreamsCollection stream =
				new ActivityStreamsCollection(get().getEntityAsText());
		
		assertEquals(2, stream.getTotalItems());
	}
	
	@Test
	public void collection_count_should_account_for_deleted_activities()
	{
		post();
		post();
		
		Activity deleted = new Activity(post().getEntityAsText());
		Request request = new Request(Method.DELETE,
				"riap://application/1/tenant/entities/entity/stream/"
				+ deleted.getId());
		component.handle(request);

		ActivityStreamsCollection stream =
				new ActivityStreamsCollection(get().getEntityAsText());
		
		assertEquals(2, stream.getTotalItems());
	}
	
	@Override
	protected String getResourcePath()
	{
		return "/1/tenant/entities/entity/stream";
	}
}
