package com.collabinate.server;

import java.util.HashMap;
import java.util.Map;

/**
 * Data that represents a tenant within the application.
 * 
 * @author mafuba
 *
 */
public class Tenant
{
	private String tenantId;
	private String tenantName;
	private Map<String, byte[]> keys;
	
	/**
	 * Initializes a new tenant.
	 * 
	 * @param tenantId the identifier for the tenant, also used in the URL slug.
	 * @param tenantName the display name for the tenant.
	 */
	public Tenant(String tenantId, String tenantName)
	{
		this.tenantId = tenantId;
		this.tenantName = tenantName;
		keys = new HashMap<String, byte[]>();
	}
	
	/**
	 * Gets the identifier for the tenant. This is unique for all tenants.
	 * 
	 * @return The unique identifier for the tenant.
	 */
	public String getId()
	{
		return tenantId;
	}
	
	/**
	 * Gets the display name for the tenant.
	 * 
	 * @return The display name for the tenant.
	 */
	public String getName()
	{
		return tenantName;
	}
	
	/**
	 * Gets the set of API keys and password hashes for the tenant.
	 * 
	 * @return A Map of the API keys to their hashed passwords.
	 */
	public Map<String, byte[]> getKeys()
	{
		return keys;
	}
}
