package com.collabinate.server.adminresources;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.collabinate.server.engine.CollabinateAdmin;

/**
 * RESTful resource allowing retrieval of the system database. This should be
 * used with caution and only in testing as the database can get quite large.
 * 
 * @author mafuba
 *
 */
public class DatabaseResource extends ServerResource
{
	@Get("xml")
	public String exportDatabase()
	{
		// extract necessary information from the context
		CollabinateAdmin admin = (CollabinateAdmin)getContext()
				.getAttributes().get("collabinateAdmin");
		
		return admin.exportDatabase();
	}
}
