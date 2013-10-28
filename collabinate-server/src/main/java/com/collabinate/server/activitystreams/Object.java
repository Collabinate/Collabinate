package com.collabinate.server.activitystreams;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * Represents an Activity Streams object serialization.
 * http://activitystrea.ms/specs/json/1.0/#object
 * 
 * @author mafuba
 *
 */
public class Object
{
	protected String id;
	protected String published;
	
	/**
	 * No-arg constructor for serialization.
	 */
	Object() { }
	
	/**
	 * Provides a permanent, universally unique identifier for the object in the
	 * form of an absolute IRI [RFC3987]. An object SHOULD contain a single id
	 * property. If an object does not contain an id property, consumers MAY use
	 * the value of the url property as a less-reliable, non-unique identifier.
	 * 
	 * @return A permanent, universally unique identifier for the object in the
	 * form of an absolute IRI [RFC3987].
	 */
	public String getId()
	{
		return id;
	}
	
	/**
	 * The date and time at which the object was published. An object MAY
	 * contain a published property.
	 * 
	 * @return The [RFC3339] date-time at which the object was published.
	 */
	public DateTime getPublished()
	{
		return new DateTime(published, DateTimeZone.UTC);
	}
}
