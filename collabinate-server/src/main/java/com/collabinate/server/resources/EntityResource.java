package com.collabinate.server.resources;

import org.restlet.data.MediaType;
import org.restlet.data.Tag;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.collabinate.server.engine.CollabinateReader;
import com.collabinate.server.engine.CollabinateWriter;
import com.google.common.hash.Hashing;

/**
 * Restful resource representing an entity.
 * 
 * @author mafuba
 *
 */
public class EntityResource extends ServerResource
{
	@Get("json")
	public Representation getEntity()
	{
		// extract necessary information from the context
		CollabinateReader reader = (CollabinateReader)getContext()
				.getAttributes().get("collabinateReader");
		String tenantId = getAttribute("tenantId");
		String entityId = getAttribute("entityId");

		String result = reader.getEntity(tenantId, entityId).toString();
		
		Representation representation = new StringRepresentation(
				result, MediaType.APPLICATION_JSON);
		representation.setTag(new Tag(Hashing.murmur3_128().hashUnencodedChars(
				result+tenantId+entityId)
				.toString(), false));
		
		return representation;
	}
	
	@Delete
	public void deleteEntity()
	{
		// extract necessary information from the context
		CollabinateWriter writer = (CollabinateWriter)getContext()
				.getAttributes().get("collabinateWriter");
		String tenantId = getAttribute("tenantId");
		String entityId = getAttribute("entityId");
		
		// remove all data for the entity
		writer.deleteEntity(tenantId, entityId);
	}
}
