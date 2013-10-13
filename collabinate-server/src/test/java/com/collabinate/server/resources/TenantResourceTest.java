package com.collabinate.server.resources;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.restlet.data.Status;

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
	
	@Override
	protected String getResourcePath()
	{
		return "/1/admin/tenants/tenant?name=Tenant";
	}
}
