package com.collabinate.server.adminresources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;

import com.collabinate.server.resources.GraphResourceTest;

/**
 * Tests for the tenant data resource.
 * 
 * @author mafuba
 *
 */
public class TenantDataResourceTest extends GraphResourceTest
{
	@Test
	public void get_data_for_nonexistent_tenant_should_return_404()
	{
		assertEquals(Status.CLIENT_ERROR_NOT_FOUND, get().getStatus());
	}
	
	@Test
	public void get_data_for_existing_tenant_should_return_200()
	{
		// add the tenant
		Request request = new Request(Method.PUT,
				"riap://application/1/admin/tenants/tenant");
		component.handle(request);
		
		assertEquals(Status.SUCCESS_OK, get().getStatus());
	}
	
	@Test
	public void tenant_data_should_have_xml_content_type()
	{
		// add the tenant
		Request request = new Request(Method.PUT,
				"riap://application/1/admin/tenants/tenant");
		component.handle(request);
		
		assertEquals(MediaType.APPLICATION_XML,
				get().getEntity().getMediaType());
	}

	@Test
	public void tenant_data_should_contain_graphml()
	{
		// add the tenant
		Request request = new Request(Method.PUT,
				"riap://application/1/admin/tenants/tenant");
		component.handle(request);
		
//		assertTrue(DatabaseResourceTest.isValidGraphml(
//				get().getEntityAsText()));
	}
	
	@Test
	public void put_data_for_nonexistant_tenant_should_return_204()
	{
		// add the tenant
		Request request = new Request(Method.PUT,
				"riap://application/1/admin/tenants/tenant");
		component.handle(request);
		
		// get the data and remove the tenant
		String data = get().getEntityAsText();
		delete();
		
		assertEquals(Status.SUCCESS_NO_CONTENT,
				put(data, MediaType.APPLICATION_XML).getStatus());
		
	}
	
	@Test
	public void put_data_for_existing_tenant_should_return_409()
	{
		// add the tenant
		Request request = new Request(Method.PUT,
				"riap://application/1/admin/tenants/tenant");
		component.handle(request);
		
		assertEquals(Status.CLIENT_ERROR_CONFLICT,
				put(get().getEntityAsText(), MediaType.APPLICATION_XML)
					.getStatus());
	}
	
	@Test
	public void delete_data_for_nonexistent_tenant_should_return_404()
	{
		assertEquals(Status.CLIENT_ERROR_NOT_FOUND, delete().getStatus());
	}
	
	@Test
	public void delete_data_for_existing_tenant_should_return_204()
	{
		// add the tenant
		Request request = new Request(Method.PUT,
				"riap://application/1/admin/tenants/tenant");
		component.handle(request);
		
		assertEquals(Status.SUCCESS_NO_CONTENT, delete().getStatus());
	}
	
	@Override
	protected String getResourcePath()
	{
		return "/1/admin/tenants/tenant/data";
	}

}
