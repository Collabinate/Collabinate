package com.collabinate.server.activitystreams;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.gson.JsonElement;

/**
 * Represents an Activity Streams Activity serialization.
 * http://activitystrea.ms/specs/json/1.0/#activity
 * 
 * @author mafuba
 *
 */
public class Activity extends ActivityStreamsObject
{
	/**
	 * Constructor for a default activity.
	 */
	public Activity()
	{
		super();
	}
	
	/**
	 * Constructs a new Activity from the given string. If the string contains a
	 * JSON object, it will be used as the base of the object. If the string is
	 * not a valid JSON object, it will instead be added to the content property
	 * of a new ActivityStreams JSON object representation.
	 * 
	 * @param content
	 */
	public Activity(String content)
	{
		super(content);
	}
	
	@Override
	protected void ensureDefaultFields()
	{
		// test for published
		if (null == getPublished())
		{
			setPublished(DateTime.now(DateTimeZone.UTC));
		}
		
		// test for actor
		if (null == getActor())
		{
			setActor(new ActivityStreamsObject());
		}
	}
	
	/**
	 * Gets the entity that performed the activity. An activity MUST contain one
	 * actor property whose value is a single Object.
	 * 
	 * @return The entity that performed the activity.
	 */
	public ActivityStreamsObject getActor()
	{
		JsonElement element = jsonObject.get(ACTOR);
		if (null != element && element.isJsonObject())
		{
			return new ActivityStreamsObject(element.toString());
		}
		
		return null;
	}
	
	/**
	 * Sets the entity that performed the activity. An activity MUST contain one
	 * actor property whose value is a single Object.
	 * 
	 * @return The entity that performed the activity.
	 */
	public void setActor(ActivityStreamsObject actor)
	{
		jsonObject.add(ACTOR, actor.jsonObject);
	}

	protected static final String ACTOR = "actor";
}
