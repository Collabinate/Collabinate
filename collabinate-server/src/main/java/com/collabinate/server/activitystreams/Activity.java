package com.collabinate.server.activitystreams;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Represents an Activity Streams Activity serialization.
 * http://activitystrea.ms/specs/json/1.0/#activity
 * 
 * @author mafuba
 *
 */
public class Activity extends Object
{
	protected Object actor;
	
	public Activity(String id, DateTime published, String actor)
	{
		this.id = id;
		this.published = published.toString(
				ISODateTimeFormat.basicDateTime().withZoneUTC());
		Object actorObject = new Object();
		actorObject.setDisplayName(actor);
		this.actor = actorObject;
	}
	
	/**
	 * No-arg constructor for serialization.
	 */
	Activity() { }
	
	/**
	 * Describes the entity that performed the activity. An activity MUST
	 * contain one actor property whose value is a single Object.
	 * 
	 * @return The entity that performed the activity.
	 */
	public Object getActor()
	{
		return actor;
	}
}
