package com.collabinate.server.resources;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.restlet.Request;
import org.restlet.data.Method;
import org.restlet.data.Status;

/**
 * Tests for the tenant key resource.
 * 
 * @author mafuba
 *
 */
public class TenantKeyResourceTest extends GraphResourceTest
{
	@Test
	public void put_key_for_nonexistent_tenant_should_return_404()
	{
		assertEquals(Status.CLIENT_ERROR_NOT_FOUND, put().getStatus());
	}
	
	@Test
	public void put_key_for_existing_tenant_should_return_200()
	{
		// add the tenant
		Request request = new Request(Method.PUT,
				"riap://application/1/admin/tenants/tenant");
		component.handle(request);
		
		assertEquals(Status.SUCCESS_OK, put().getStatus());
	}
	
	@Test
	public void put_key_for_existing_tenant_should_add_key()
	{
		// add the tenant
		Request request = new Request(Method.PUT,
				"riap://application/1/admin/tenants/tenant");
		component.handle(request);

		put();

		// get the keys
		request = new Request(Method.GET,
				"riap://application/1/admin/tenants/tenant/keys");
		String keys = component.handle(request).getEntityAsText();
		
		assertThat(keys, containsString("\"key\""));
	}
	
	@Test
	public void put_key_should_return_key_in_body()
	{
		// add the tenant
		Request request = new Request(Method.PUT,
				"riap://application/1/admin/tenants/tenant");
		component.handle(request);

		assertEquals("key", put().getEntityAsText());
	}

	@Test
	public void delete_key_for_nonexistent_tenant_should_return_404()
	{
		assertEquals(Status.CLIENT_ERROR_NOT_FOUND, delete().getStatus());
	}
	
	@Test
	public void delete_key_for_existing_tenant_should_return_200()
	{
		// add the tenant
		Request request = new Request(Method.PUT,
				"riap://application/1/admin/tenants/tenant");
		component.handle(request);
		
		assertEquals(Status.SUCCESS_OK, put().getStatus());
	}
	
	@Test
	public void delete_key_for_existing_tenant_should_remove_key()
	{
		// add the tenant
		Request request = new Request(Method.PUT,
				"riap://application/1/admin/tenants/tenant");
		component.handle(request);

		put();
		delete();

		// get the keys
		request = new Request(Method.GET,
				"riap://application/1/admin/tenants/tenant/keys");
		String keys = component.handle(request).getEntityAsText();
		
		assertThat(keys, not(containsString("\"key\"")));
	}
	
	@Test
	public void delete_key_should_return_key_in_body()
	{
		// add the tenant
		Request request = new Request(Method.PUT,
				"riap://application/1/admin/tenants/tenant");
		component.handle(request);

		assertEquals("key", delete().getEntityAsText());
	}
	
	@Override
	protected String getResourcePath()
	{
		return "/1/admin/tenants/tenant/keys/key";
	}

}
