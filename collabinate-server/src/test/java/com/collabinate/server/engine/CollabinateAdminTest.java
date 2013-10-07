package com.collabinate.server.engine;

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
	public void creating_new_tenant_should_succeed()
	{
		Tenant tenant = admin.createTenant("tenant");
	}

}
