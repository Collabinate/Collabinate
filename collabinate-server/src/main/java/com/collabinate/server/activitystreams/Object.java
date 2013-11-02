package com.collabinate.server.activitystreams;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Represents an Activity Streams object serialization.
 * http://activitystrea.ms/specs/json/1.0/#object
 * 
 * @author mafuba
 *
 */
public class Object
{
	protected String content;
	protected String displayName;
	protected String id;
	protected String published;
	
	/**
	 * No-arg constructor for serialization.
	 */
	Object() { }

	/**
	 * Sets a natural-language description of the object encoded as a single
	 * JSON String containing HTML markup. Visual elements such as thumbnail
	 * images MAY be included. An object MAY contain a content property.
	 * 
	 * @param content a JSON [RFC4627] String containing the content.
	 */
	public void setContent(String content)
	{
		this.content = content;
	}
	
	/**
	 * Sets a natural-language, human-readable and plain-text name for the
	 * object. HTML markup MUST NOT be included. An object MAY contain a
	 * displayName property. If the object does not specify an objectType
	 * property, the object SHOULD specify a displayName.
	 * 
	 * @param displayName a JSON [RFC4627] String containing the display name. 
	 */
	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}
	
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
	 * Gets the date and time at which the object was published. An object MAY
	 * contain a published property.
	 * 
	 * @return The [RFC3339] date-time at which the object was published.
	 */
	public DateTime getPublished()
	{
		return DateTime.parse(published,
				ISODateTimeFormat.basicDateTime().withZoneUTC());
	}
}
