package com.collabinate.server.engine;

import com.collabinate.server.Tenant;

/**
 * The interface for administrative functions against a Collabinate server.
 * 
 * @author mafuba
 *
 */
public interface CollabinateAdmin
{
	/**
	 * Adds a new tenant to the data store if it does not exist. If a tenant
	 * with a matching ID already exists, no change is made.
	 * 
	 * @param tenant the tenant to add.
	 */
	public void addTenant(Tenant tenant);
	
	/**
	 * Gets the tenant with the given ID.
	 * 
	 * @param tenantId the ID of the tenant to retrieve.
	 * @return The tenant with the given ID, or null if none exists.
	 */
	public Tenant getTenant(String tenantId);
}
