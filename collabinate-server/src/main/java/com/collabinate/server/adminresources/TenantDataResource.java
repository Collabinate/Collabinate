package com.collabinate.server.adminresources;

import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

import com.collabinate.server.engine.CollabinateAdmin;

/**
 * Restful resource representing the data for a tenant.
 * 
 * @author mafuba
 *
 */
public class TenantDataResource extends ServerResource
{
	@Get("xml")
	public String exportTenantData()
	{
		// extract necessary information from the context
		CollabinateAdmin admin = (CollabinateAdmin)getContext()
				.getAttributes().get("collabinateAdmin");
		String tenantId = getAttribute("tenantId");
		
		// ensure the tenant exists
		if (null == admin.getTenant(tenantId))
		{
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return null;
		}
		
		// return the tenant data
		return admin.exportTenantData(tenantId);
	}
	
	@Put("xml")
	public void importTenantData(String data)
	{
		// extract necessary information from the context
		CollabinateAdmin admin = (CollabinateAdmin)getContext()
				.getAttributes().get("collabinateAdmin");
		String tenantId = getAttribute("tenantId");
		
		// ensure the tenant does not exist
		if (null != admin.getTenant(tenantId))
		{
			setStatus(Status.CLIENT_ERROR_CONFLICT);
			return;
		}
		
		admin.importTenantData(tenantId, data);
	}
	
	@Delete
	public void deleteTenantData()
	{
		// extract necessary information from the context
		CollabinateAdmin admin = (CollabinateAdmin)getContext()
				.getAttributes().get("collabinateAdmin");
		String tenantId = getAttribute("tenantId");
		
		// ensure the tenant exists
		if (null == admin.getTenant(tenantId))
		{
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return;
		}
		
		admin.deleteTenant(tenantId);
	}
}
