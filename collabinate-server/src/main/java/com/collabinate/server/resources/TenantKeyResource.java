package com.collabinate.server.resources;

import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

import com.collabinate.server.Tenant;
import com.collabinate.server.engine.CollabinateAdmin;

/**
 * Restful resource representing an API key for a tenant.
 * 
 * @author mafuba
 *
 */
public class TenantKeyResource extends ServerResource
{
	@Put
	public String addTenantKey()
	{
		// extract necessary information from the context
		CollabinateAdmin admin = (CollabinateAdmin)getContext()
				.getAttributes().get("collabinateAdmin");
		String tenantId = getAttribute("tenantId");
		String key = getAttribute("key");
		
		// get the tenant, add the key, and save
		Tenant tenant = admin.getTenant(tenantId);
		if (null == tenant)
		{
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return null;
		}
		tenant.addKey(key);
		admin.putTenant(tenant);
		
		// return the representation
		return key;
	}
	
	@Delete
	public String removeTenantKey()
	{
		// extract necessary information from the context
		CollabinateAdmin admin = (CollabinateAdmin)getContext()
				.getAttributes().get("collabinateAdmin");
		String tenantId = getAttribute("tenantId");
		String key = getAttribute("key");
		
		// get the tenant, delete the key, and save
		Tenant tenant = admin.getTenant(tenantId);
		if (null == tenant)
		{
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return null;
		}
		tenant.removeKey(key);
		admin.putTenant(tenant);
		
		return key;
	}
}
