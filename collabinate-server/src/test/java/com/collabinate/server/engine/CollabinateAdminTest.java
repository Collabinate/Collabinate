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
		assertEquals(tenant.getId(), "tenant");
		assertEquals(tenant.getName(), "tenant");
	}
	
	@Test
	public void get_tenant_should_return_correct_tenant()
	{
		admin.addTenant("tenant", "tenant");		
		Tenant tenant = admin.getTenant("tenant");
		assertEquals(tenant.getName(), "tenant");
	}

}
