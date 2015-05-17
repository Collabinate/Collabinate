package com.collabinate.server.adminresources;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.restlet.Request;
import org.restlet.data.Method;
import org.restlet.data.Status;

import com.collabinate.server.resources.GraphResourceTest;

/**
 * Tests for the Tenant Resource.
 * 
 * @author mafuba
 *
 */
public class TenantResourceTest extends GraphResourceTest
{
	@Test
	public void create_tenant_should_return_201()
	{
		assertEquals(Status.SUCCESS_CREATED, put().getStatus());
	}
	
	@Test
	public void created_tenant_should_have_specified_key()
	{
		put("?name=Tenant&key=abcd");
		
		Request request = new Request(Method.GET,
				"riap://application/1/admin/tenants/tenant/keys");
		
		assertThat(component.handle(request).getEntityAsText(),
				containsString("abcd"));
	}
	
	@Override
	protected String getResourcePath()
	{
		return "/1/admin/tenants/tenant";
	}
}
