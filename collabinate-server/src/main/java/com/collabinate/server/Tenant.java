package com.collabinate.server;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
	private List<String> keys;
	
	/**
	 * Initializes a new tenant.
	 * 
	 * @param tenantId the identifier for the tenant, also used in the URL slug.
	 * @param tenantName the display name for the tenant.
	 */
	public Tenant(String tenantId, String tenantName)
	{
		if (null == tenantId)
			throw new IllegalArgumentException("tenantId must not be null");
		if (null == tenantName)
			throw new IllegalArgumentException("tenantName must not be nul");
		
		this.tenantId = tenantId;
		this.tenantName = tenantName;
		keys = new ArrayList<String>();
	}
	
	/**
	 * No-arg constructor for serialization.
	 */
	Tenant() { }
	
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
	 * Generates a new API key for the tenant.
	 * 
	 * @return A newly generated API key for the tenant.
	 */
	public String generateKey()
	{
		SecureRandom random = new SecureRandom();
		String key = new BigInteger(130, random).toString(32);
		keys.add(key);
		return key;
	}
	
	/**
	 * Verifies that the tenant has the provided API key.
	 * 
	 * @param key the key to verify.
	 * @return true if the key is valid for the tenant, otherwise false.
	 */
	public boolean verifyKey(String key)
	{
		return keys.contains(key);
	}
	
	/**
	 * Removes the given API key from the tenant.
	 * 
	 * @param key the key to remove.
	 */
	public void removeKey(String key)
	{
		keys.remove(key);
	}
	
	/**
	 * Provides a read-only view of the keys for the tenant.
	 *  
	 * @return A read-only List of the keys for the tenant.
	 */
	public List<String> getKeys()
	{
		return Collections.unmodifiableList(keys);
	}
}
