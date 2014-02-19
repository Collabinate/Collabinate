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
		super.ensureDefaultFields();
		
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
	
	/**
	 * Gets the primary object of the activity. For instance, in the activity,
	 * "John saved a movie to his wishlist", the object of the activity is
	 * "movie". An activity SHOULD contain an object property whose value is a
	 * single Object. If the object property is not contained, the primary
	 * object of the activity MAY be implied by context.
	 * 
	 * @return The primary object of the activity.
	 */
	public ActivityStreamsObject getObject()
	{
		JsonElement element = jsonObject.get(OBJECT);
		if (null != element && element.isJsonObject())
		{
			return new ActivityStreamsObject(element.toString());
		}
		
		return null;
	}
	
	/**
	 * Sets the primary object of the activity. For instance, in the activity,
	 * "John saved a movie to his wishlist", the object of the activity is
	 * "movie". An activity SHOULD contain an object property whose value is a
	 * single Object. If the object property is not contained, the primary
	 * object of the activity MAY be implied by context.
	 * 
	 * @param object The primary object of the activity.
	 */
	public void setObject(ActivityStreamsObject object)
	{
		jsonObject.add(OBJECT, object.jsonObject);
	}
	
	/**
	 * Gets the action that the activity describes. An activity SHOULD contain a
	 * verb property whose value is a JSON String that is non-empty and matches
	 * either the "isegment-nz-nc" or the "IRI" production in [RFC3339]. Note
	 * that the use of a relative reference other than a simple name is not
	 * allowed. If the verb is not specified, or if the value is null, the verb
	 * is assumed to be "post".
	 * 
	 * @return The action that the activity describes.
	 */
	public String getVerb()
	{
		return getStringValue(VERB);
	}
	
	/**
	 * Sets the action that the activity describes. An activity SHOULD contain a
	 * verb property whose value is a JSON String that is non-empty and matches
	 * either the "isegment-nz-nc" or the "IRI" production in [RFC3339]. Note
	 * that the use of a relative reference other than a simple name is not
	 * allowed. If the verb is not specified, or if the value is null, the verb
	 * is assumed to be "post".
	 * 
	 * @param verb The action that the activity describes.
	 */
	public void setVerb(String verb)
	{
		jsonObject.addProperty(VERB, verb);
	}

	protected static final String ACTOR = "actor";
	protected static final String OBJECT = "object";
	protected static final String VERB = "verb";
}
