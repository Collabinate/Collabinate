package com.collabinate.server;

import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

/**
 * Restful resource representing a series of stream entries for an entity.
 * 
 * @author mafuba
 *
 */
public class StreamResource extends ServerResource
{
	@Get
	public String getStream()
	{
		return "";
	}
	
	@Post
	public void addEntry(String entry)
	{
		setLocationRef(new Reference(getReference()).addSegment("A"));
		setStatus(Status.SUCCESS_CREATED);
	}
}
