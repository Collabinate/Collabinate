package com.collabinate.server.activitystreams;

/**
 * Represents an Activity Streams Activity serialization.
 * http://activitystrea.ms/specs/json/1.0/#activity
 * 
 * @author mafuba
 *
 */
public class Activity extends Object
{
	private String actor;
	
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
	public String getActor()
	{
		return actor;
	}
}
