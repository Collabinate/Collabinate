package com.collabinate.server.resources;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import com.collabinate.server.StreamEntry;
import com.collabinate.server.activitystreams.Activity;
import com.collabinate.server.engine.CollabinateReader;
import com.collabinate.server.engine.CollabinateWriter;
import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Restful resource representing a series of stream entries for an entity.
 * 
 * @author mafuba
 *
 */
public class StreamResource extends ServerResource
{
	// TODO: some much heavier content processing is going to need to happen
	// here to handle different XML and JSON representations of different
	// stream types (e.g. activity streams, OData, raw)
	@Get("json")
	public String getStream()
	{
		// extract necessary information from the context
		CollabinateReader reader = (CollabinateReader)getContext()
				.getAttributes().get("collabinateReader");
		String tenantId = getAttribute("tenantId");
		String entityId = getAttribute("entityId");
		String startString = getQueryValue("start");
		String countString = getQueryValue("count");
		long start = null == startString ? 0 : Long.parseLong(startString);
		int count = null == countString ? DEFAULT_COUNT : 
			Integer.parseInt(countString);
		
		return "{\"items\":[" + Joiner.on(',')
				.join(reader.getStream(tenantId, entityId, start, count))
				+ "]}";
	}
	
	@Post
	public void addEntry(String originalContent)
	{
		CollabinateWriter writer = (CollabinateWriter)getContext()
				.getAttributes().get("collabinateWriter");
		
		if (null == writer)
			throw new IllegalStateException(
					"Context does not contain a CollabinateWriter");
		
		String entity = getAttribute("entityId");
		
		StreamEntry entry = getEntry(originalContent, entity);
		
		writer.addStreamEntry(getAttribute("tenantId"), entity, entry);
		
		// if there is no request entity return empty string with text type
		if (null != originalContent)
			getResponse().setEntity(originalContent, 
					getRequest().getEntity().getMediaType());
		else
			getResponse().setEntity("", MediaType.TEXT_PLAIN);
		
		setLocationRef(new Reference(getReference()).addSegment(entry.getId()));
		setStatus(Status.SUCCESS_CREATED);
	}
	
	private StreamEntry getEntry(String originalContent, String entity)
	{
		Gson gson = new Gson();
		boolean useOriginalContent = false;
		Activity activity = null;
		String id = null;
		DateTime published = DateTime.now(DateTimeZone.UTC);
		
		try
		{
			activity = gson.fromJson(originalContent, Activity.class);
			
			if (null != activity &&
					null != activity.getPublished() &&
					null != activity.getActor())
			{
				useOriginalContent = true;
				id = activity.getId();
				published = activity.getPublished();
			}
		}
		catch (JsonSyntaxException e)
		{
		}
		
		if (null == activity)
		{
			activity = new Activity(id, published, entity);
			
			activity.setContent(originalContent);
		}
		
		String entryContent = useOriginalContent ? 
				originalContent : gson.toJson(activity);
		
		return new StreamEntry(id, published, entryContent);		
	}
	
	private static final int DEFAULT_COUNT = 20;
}
