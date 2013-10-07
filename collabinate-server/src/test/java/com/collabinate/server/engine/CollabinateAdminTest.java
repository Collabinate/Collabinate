package com.collabinate.server.engine;

import org.junit.Before;
import org.junit.Test;

public abstract class CollabinateAdminTest
{
	private CollabinateAdmin admin;
	
	abstract CollabinateAdmin getAdmin();
	
	@Before
	public void setup()
	{
		admin = getAdmin();
	}
	
	@Test
	public void creating_new_tenant_should_succeed()
	{
		
	}

}
