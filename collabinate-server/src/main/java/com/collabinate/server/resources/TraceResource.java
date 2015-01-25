package com.collabinate.server.resources;

import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Options;
import org.restlet.resource.ServerResource;

import com.collabinate.server.Collabinate;

/**
 * Resource for tracing and displaying request information.
 * 
 * @author mafuba
 *
 */
public class TraceResource extends ServerResource
{
	@Get
	@Options
	public Representation Trace()
	{
		String entity = "Method       : " + getRequest().getMethod()
				+ "\nResource URI : "
				+ getRequest().getResourceRef()
				+ "\nIP address   : "
				+ getRequest().getClientInfo().getAddress()
				+ "\nAgent name   : "
				+ getRequest().getClientInfo().getAgentName()
				+ "\nAgent version: "
				+ getRequest().getClientInfo().getAgentVersion()
				+ "\nCollabinate version: "
				+ Collabinate.getConfiguration()
					.getString("collabinate.version", "Unknown");
		return new StringRepresentation(entity);
	}
}
