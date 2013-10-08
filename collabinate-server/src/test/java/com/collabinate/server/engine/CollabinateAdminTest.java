package com.collabinate.server.engine;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.collabinate.server.Tenant;

/**
 * Abstract test class to test any implementation of a CollabinateAdmin.
 * 
 * @author mafuba
 *
 */
public abstract class CollabinateAdminTest
{
	private CollabinateAdmin admin;
	
	abstract CollabinateAdmin getAdmin();
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Before
	public void setup()
	{
		admin = getAdmin();
	}
	
	@Test
	public void add_tenant_should_succeed()
	{
		Tenant tenant = admin.addTenant("tenant", "tenant");
		assertNotNull(tenant);
	}
	
	@Test
	public void get_tenant_should_return_correct_tenant()
	{
		admin.addTenant("tenant", "tenant");		
		Tenant tenant = admin.getTenant("tenant");
		assertEquals("tenant", tenant.getId());
		assertEquals("tenant", tenant.getName());
	}
	
	@Test
	public void adding_existing_tenant_should_return_existing_tenant()
	{
		Tenant tenant1 = admin.addTenant("tenant", "tenant1");
		Tenant tenant2 = admin.addTenant("tenant", "tenant2");
		assertEquals(tenant1.getName(), tenant2.getName());
	}
}
