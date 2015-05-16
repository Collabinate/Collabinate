package com.collabinate.server.adminresources;

import org.restlet.data.Status;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

import com.collabinate.server.Tenant;
import com.collabinate.server.engine.CollabinateAdmin;

/**
 * Restful resource representing a tenant.
 * 
 * @author mafuba
 *
 */
public class TenantResource extends ServerResource
{
	@Put
	public void putTenant()
	{
		// extract necessary information from the context
		CollabinateAdmin admin = (CollabinateAdmin)getContext()
				.getAttributes().get("collabinateAdmin");
		String tenantId = getAttribute("tenantId");
		String tenantName = getQueryValue("name");
		String tenantKey = getQueryValue("key");
		
		// set defaults for values not provided
		if (null == tenantName || tenantName.equals(""))
			tenantName = tenantId;
		
		// create the tenant
		Tenant tenant = new Tenant(tenantId, tenantName);
		
		// add or generate the initial key
		if (null == tenantKey || tenantKey.equals(""))
			tenant.generateKey();
		else
			tenant.addKey(tenantKey);
		
		// put the tenant
		admin.putTenant(tenant);
		
		setStatus(Status.SUCCESS_CREATED);
	}
}
