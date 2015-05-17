package com.collabinate.server.adminresources;

import java.util.List;

import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.collabinate.server.Tenant;
import com.collabinate.server.engine.CollabinateAdmin;

/**
 * Restful resource representing a set of tenants.
 * 
 * @author mafuba
 *
 */
public class TenantsResource extends ServerResource
{
	@Get
	public Representation getTenants()
	{
		// extract necessary information from the context
		CollabinateAdmin admin = (CollabinateAdmin)getContext()
				.getAttributes().get("collabinateAdmin");
		
		// get the tenants
		List<Tenant> tenants = admin.getAllTenants();
		
		// return the representation
		return new JacksonRepresentation<List<Tenant>>(tenants);
	}
}
