package com.collabinate.server.resources;

import java.util.List;

import org.restlet.data.Status;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.collabinate.server.Tenant;
import com.collabinate.server.engine.CollabinateAdmin;

/**
 * Restful resource representing the API keys for a tenant.
 * 
 * @author mafuba
 *
 */
public class TenantKeysResource extends ServerResource
{
	@Get
	public Representation getTenantKeys()
	{
		// extract necessary information from the context
		CollabinateAdmin admin = (CollabinateAdmin)getContext()
				.getAttributes().get("collabinateAdmin");
		String tenantId = getAttribute("tenantId");
		
		// get the keys
		Tenant tenant = admin.getTenant(tenantId);
		if (null == tenant)
		{
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return null;
		}
		List<String> keys = tenant.getKeys();
		
		// return the representation
		return new JacksonRepresentation<List<String>>(keys);
	}
}
