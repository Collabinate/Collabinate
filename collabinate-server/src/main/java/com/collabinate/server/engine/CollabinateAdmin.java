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
	 * Puts a new tenant in the data store if it does not exist. If a tenant
	 * with a matching ID already exists, it is updated.
	 * 
	 * @param tenant the tenant to put.
	 */
	public void putTenant(Tenant tenant);
	
	/**
	 * Gets the tenant with the given ID.
	 * 
	 * @param tenantId the ID of the tenant to retrieve.
	 * @return The tenant with the given ID, or null if none exists.
	 */
	public Tenant getTenant(String tenantId);
	
	/**
	 * Gets the underlying data store as a string.
	 * 
	 * CAUTION: this can be large, and should not be used for large production
	 * data.
	 * 
	 * @return The service data store as a string.
	 */
	public String exportDatabase();
}
