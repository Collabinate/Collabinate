package com.collabinate.server.resources;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Status;

/**
 * Tests for the Stream Entry Resource
 * 
 * @author mafuba
 * 
 */
public class StreamEntryResourceTest extends GraphResourceTest
{
	@Test
	public void get_nonexistent_entry_should_return_404()
	{
		assertEquals(Status.CLIENT_ERROR_NOT_FOUND, get().getStatus());
	}
	
	@Test
	public void putting_entry_should_return_200()
	{
		assertEquals(Status.SUCCESS_OK, put().getStatus());
	}
	
	@Test
	public void get_existing_entry_should_return_entry()
	{
		put("test", MediaType.TEXT_PLAIN);
		assertThat(get().getEntityAsText(), containsString("test"));		
	}

	@Override
	protected String getResourcePath()
	{
		return "/1/tenant/entities/entity/stream/entry";
	}
}
