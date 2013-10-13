package com.collabinate.server.resources;

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
		
		// set defaults for values not provided
		if (null == tenantName || "" == tenantName)
			tenantName = tenantId;
		
		// put the tenant
		Tenant tenant = new Tenant(tenantId, tenantName);
		tenant.generateKey();
		admin.putTenant(tenant);
		
		setStatus(Status.SUCCESS_CREATED);
	}
}
