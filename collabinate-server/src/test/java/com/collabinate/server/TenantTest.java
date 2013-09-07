package com.collabinate.server;

/**
 * Abstract test class to test any implementation of a Tenant.
 * 
 * @author mafuba
 *
 */
public abstract class TenantTest
{
	private Tenant tenant;
	
	abstract Tenant getTenant();
}
