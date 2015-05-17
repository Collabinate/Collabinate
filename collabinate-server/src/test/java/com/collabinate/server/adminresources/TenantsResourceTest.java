package com.collabinate.server.adminresources;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.restlet.Request;
import org.restlet.data.Method;
import org.restlet.data.Status;

import com.collabinate.server.resources.GraphResourceTest;

/**
 * Tests for the Tenants Resource.
 * 
 * @author mafuba
 *
 */
public class TenantsResourceTest extends GraphResourceTest
{
	@Test
	public void get_tenants_should_return_200()
	{
		assertEquals(Status.SUCCESS_OK, get().getStatus());
	}
	
	@Test
	public void get_tenants_should_contain_tenant_data()
	{
		// add the tenants
		Request request = new Request(Method.PUT,
				"riap://application/1/admin/tenants/tenant?name=FirstTenant");
		component.handle(request);
		request = new Request(Method.PUT,
				"riap://application/1/admin/tenants/tenant2?name=NextTenant");
		component.handle(request);
		
		assertThat(get().getEntityAsText(), containsString("FirstTenant"));
		assertThat(get().getEntityAsText(), containsString("NextTenant"));
	}
	
	@Override
	protected String getResourcePath()
	{
		return "/1/admin/tenants";
	}
}
