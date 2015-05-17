package com.collabinate.server.adminresources;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.restlet.Request;
import org.restlet.data.Method;
import org.restlet.data.Status;

import com.collabinate.server.resources.GraphResourceTest;

/**
 * Tests for the tenant keys resource.
 * 
 * @author mafuba
 *
 */
public class TenantKeysResourceTest extends GraphResourceTest
{
	@Test
	public void get_keys_for_nonexistent_tenant_should_return_404()
	{
		assertEquals(Status.CLIENT_ERROR_NOT_FOUND, get().getStatus());
	}
	
	@Test
	public void get_keys_for_existing_tenant_should_return_200()
	{
		// add the tenant
		Request request = new Request(Method.PUT,
				"riap://application/1/admin/tenants/tenant");
		component.handle(request);
		
		assertEquals(Status.SUCCESS_OK, get().getStatus());
	}
	
	@Override
	protected String getResourcePath()
	{
		return "/1/admin/tenants/tenant/keys";
	}

}
