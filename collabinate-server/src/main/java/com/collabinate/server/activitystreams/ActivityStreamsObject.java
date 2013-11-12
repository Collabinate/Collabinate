package com.collabinate.server.activitystreams;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * Represents an Activity Streams object serialization.
 * http://activitystrea.ms/specs/json/1.0/#object
 * 
 * @author mafuba
 *
 */
public class ActivityStreamsObject
{
	/**
	 * The internal representation of the object as JSON.
	 */
	protected JsonObject jsonObject;
	
	/**
	 * Default constructor for an empty object.
	 */
	public ActivityStreamsObject()
	{
		jsonObject = new JsonObject();
		ensureDefaultFields();
	}
	
	/**
	 * Constructs a new ActivityStreamsObject from the given string. If the
	 * string contains a JSON object, it will be used as the base of the object.
	 * If the string is not a valid JSON object, it will instead be added to the
	 * content property of a new ActivityStreams JSON object representation.
	 * 
	 * @param content
	 */
	public ActivityStreamsObject(String content)
	{
		if (null != content)
		{
			JsonParser parser = new JsonParser();
			try
			{
				JsonElement element = parser.parse(content);
				if (element.isJsonObject())
					jsonObject = element.getAsJsonObject();
			}
			catch (JsonParseException e) { }
		}
		
		if (null == jsonObject)
		{
			jsonObject = new JsonObject();
			setContent(content);
		}
		
		ensureDefaultFields();
	}
	
	/**
	 * Provides a means of ensuring that all required fields for the object
	 * are in place. Called in the constructor. By default nothing is required
	 * in the base ActivityStreamsObject.
	 */
	protected void ensureDefaultFields() { }
	
	/**
	 * Sets a natural-language description of the object encoded as a single
	 * JSON String containing HTML markup. Visual elements such as thumbnail
	 * images MAY be included. An object MAY contain a content property.
	 * 
	 * @param content a JSON [RFC4627] String containing the content.
	 */
	public void setContent(String content)
	{
		jsonObject.addProperty(CONTENT, content);
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
		jsonObject.addProperty(DISPLAY_NAME, displayName);
	}
	
	/**
	 * Gets the permanent, universally unique identifier for the object in the
	 * form of an absolute IRI [RFC3987]. An object SHOULD contain a single id
	 * property. If an object does not contain an id property, consumers MAY use
	 * the value of the url property as a less-reliable, non-unique identifier.
	 * 
	 * @return A permanent, universally unique identifier for the object in the
	 * form of an absolute IRI [RFC3987].
	 */
	public String getId()
	{
		String id = null;
		JsonElement element = jsonObject.get(ID);
		if (null != element)
		{
			try 
			{
				id = element.getAsString();
			}
			catch (ClassCastException | IllegalStateException e) { }
		}
		
		return id;
	}
	
	/**
	 * Sets the permanent, universally unique identifier for the object in the
	 * form of an absolute IRI [RFC3987]. An object SHOULD contain a single id
	 * property. If an object does not contain an id property, consumers MAY use
	 * the value of the url property as a less-reliable, non-unique identifier.
	 * 
	 * @param id A permanent, universally unique identifier for the object in
	 * the form of an absolute IRI [RFC3987].
	 */
	public void setId(String id)
	{
		jsonObject.addProperty(ID, id);
	}
	
	/**
	 * Gets the date and time at which the object was published. An object MAY
	 * contain a published property.
	 * 
	 * @return The [RFC3339] date-time at which the object was published.
	 */
	public DateTime getPublished()
	{
		JsonElement element = jsonObject.get(PUBLISHED);
		DateTime published = null;
		try
		{
			if (null != element)
			{
				String publishedString = element.getAsString();
				published = DateTime.parse(publishedString,
					ISODateTimeFormat.dateTimeParser().withZoneUTC());
			}
		}
		catch (ClassCastException | IllegalStateException e) { }

		return  published;
	}
	
	/**
	 * Sets the date and time at which the object was published. An object MAY
	 * contain a published property.
	 * 
	 * @param dateTime The [RFC3339] date-time at which the object was
	 * published.
	 */
	public void setPublished(DateTime dateTime)
	{
		jsonObject.addProperty(PUBLISHED, dateTime.toString(
				ISODateTimeFormat.dateTime().withZoneUTC()));
	}
	
	@Override
	public String toString()
	{
		return jsonObject.toString();
	}

	protected static final String ID = "id";
	protected static final String CONTENT = "content";
	protected static final String DISPLAY_NAME = "displayName";
	protected static final String PUBLISHED = "published";
}
