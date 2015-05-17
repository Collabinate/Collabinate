package com.collabinate.server.engine;

import java.util.List;

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
	 * Removes all data for the given tenant. Use with caution!
	 * 
	 * @param tenantId the ID of the tenant that will be eradicated.
	 */
	public void deleteTenant(String tenantId);
	
	/**
	 * Gets a list of all tenants in the system.
	 * 
	 * @return A list of all the tenants in the system.
	 */
	public List<Tenant> getAllTenants();
	
	/**
	 * Gets the underlying data store as a string.
	 * 
	 * CAUTION: this can be large, and should not be used for large production
	 * data.
	 * 
	 * @return The service data store as a string.
	 */
	public String exportDatabase();
	
	/**
	 * Restores the underlying data store from a string.
	 * 
	 * CAUTION: this must only be used with an empty database.
	 * 
	 * @param data the data to restore.
	 */
	public void importDatabase(String data);
		
	/**
	 * Gets the data for a single tenant as a string.
	 * 
	 * CAUTION: the result can be large, and may cause service interruption in a
	 * production system.
	 * 
	 * @param tenantId the ID of the tenant for which data will be retrieved.
	 * @return The tenant data as a string.
	 */
	public String exportTenantData(String tenantId);
	
	/**
	 * Imports data for a single tenant from a string.
	 * 
	 * CAUTION: the tenant must not exist in the system.
	 * 
	 * @param tenantId the ID of the tenant for which data will be restored.
	 * @param data the data for the tenant
	 */
	public void importTenantData(String tenantId, String data);
}
