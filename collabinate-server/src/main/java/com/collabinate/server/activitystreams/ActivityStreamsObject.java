package com.collabinate.server.activitystreams;

import java.util.UUID;

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
	 * Gets a natural-language description of the object encoded as a single
	 * JSON String containing HTML markup. Visual elements such as thumbnail
	 * images MAY be included. An object MAY contain a content property.
	 * 
	 * @return content a JSON [RFC4627] String containing the content.
	 */
	public String getContent()
	{
		return getStringValue(CONTENT);
	}
	
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
	 * Gets a natural-language, human-readable and plain-text name for the
	 * object. HTML markup MUST NOT be included. An object MAY contain a
	 * displayName property. If the object does not specify an objectType
	 * property, the object SHOULD specify a displayName.
	 * 
	 * @return displayName a JSON [RFC4627] String containing the display name. 
	 */
	public String getDisplayName(String displayName)
	{
		return getStringValue(DISPLAY_NAME);
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
		return getStringValue(ID);
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
		if (null == id)
		{
			throw new IllegalArgumentException("id must not be null");
		}
		
		jsonObject.addProperty(ID, id);
	}
	
	/**
	 * Gets the type of object. An object MAY contain an objectType property
	 * whose value is a JSON String that is non-empty and matches either the
	 * "isegment-nz-nc" or the "IRI" production in [RFC3987]. Note that the use
	 * of a relative reference other than a simple name is not allowed. If no
	 * objectType property is contained, the object has no specific type.
	 * 
	 * @return JSON [RFC4627] String.
	 */
	public String getObjectType()
	{
		return getStringValue(OBJECT_TYPE);
	}
	
	/**
	 * Gets the type of object. An object MAY contain an objectType property
	 * whose value is a JSON String that is non-empty and matches either the
	 * "isegment-nz-nc" or the "IRI" production in [RFC3987]. Note that the use
	 * of a relative reference other than a simple name is not allowed. If no
	 * objectType property is contained, the object has no specific type.

	 * @param objectType JSON [RFC4627] String.
	 */
	public void setObjectType(String objectType)
	{
		if (null == objectType)
		{
			throw new IllegalArgumentException("objectType must not be null");
		}
		
		jsonObject.addProperty(OBJECT_TYPE, objectType);
	}
	
	/**
	 * Gets the date by which the activity should be sorted. The updated date is
	 * preferred, but if it is not available the published date is used.
	 * 
	 * @return The date by which the activity should be sorted.
	 */
	public DateTime getSortTime()
	{
		DateTime updated = getUpdated();
		if (null != updated)
		{
			return updated;
		}
		
		return getPublished();
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
		if (null == dateTime)
		{
			throw new IllegalArgumentException("dateTime must not be null");
		}
		
		jsonObject.addProperty(PUBLISHED, dateTime.toString(
				ISODateTimeFormat.dateTime().withZoneUTC()));
	}
	
	/**
	 * Gets the date and time at which a previously published object has been
	 * modified. An Object MAY contain an updated property.
	 * 
	 * @return The [RFC3339] date-time at which the object was published.
	 */
	public DateTime getUpdated()
	{
		JsonElement element = jsonObject.get(UPDATED);
		DateTime updated = null;
		try
		{
			if (null != element)
			{
				String updatedString = element.getAsString();
				updated = DateTime.parse(updatedString,
					ISODateTimeFormat.dateTimeParser().withZoneUTC());
			}
		}
		catch (ClassCastException | IllegalStateException e) { }

		return  updated;
	}
	
	/**
	 * Sets the date and time at which a previously published object has been
	 * modified. An Object MAY contain an updated property.
	 * 
	 * @param dateTime The [RFC3339] date-time at which the object was
	 * updated.
	 */
	public void setUpdated(DateTime dateTime)
	{
		jsonObject.addProperty(UPDATED, dateTime.toString(
				ISODateTimeFormat.dateTime().withZoneUTC()));
	}
	
	/**
	 * Gets information about the set of objects that can be considered to be
	 * replies to the containing object.
	 * 
	 * @return An ActivityStreamsCollection representation of the replies
	 * property.
	 */
	public ActivityStreamsCollection getReplies()
	{
		JsonElement replies = jsonObject.get(REPLIES);
		
		if (null != replies)
		{
			return new ActivityStreamsCollection(replies.toString());
		}
		
		return null;
	}
	
	/**
	 * Sets information about the set of objects that can be considered to be
	 * replies to the containing object.
	 * 
	 * @param replies An ActivityStreamsCollection containing the replies.
	 */
	public void setReplies(ActivityStreamsCollection replies)
	{
		jsonObject.add(REPLIES, replies.jsonObject);
	}
	
	/**
	 * Gets information about the set of objects that can be considered to be
	 * likes of the containing object.
	 * 
	 * @return An ActivityStreamsCollection representation of the likes
	 * property.
	 */
	public ActivityStreamsCollection getLikes()
	{
		JsonElement likes = jsonObject.get(LIKES);
		
		if (null != likes)
		{
			return new ActivityStreamsCollection(likes.toString());
		}
		
		return null;
	}
	
	/**
	 * Sets information about the set of objects that can be considered to be
	 * likes of the containing object.
	 * 
	 * @param replies An ActivityStreamsCollection containing the likes.
	 */
	public void setLikes(ActivityStreamsCollection likes)
	{
		jsonObject.add(LIKES, likes.jsonObject);
	}
	
	@Override
	public String toString()
	{
		return jsonObject.toString();
	}
	
	/**
	 * Gets a string value from the activity streams object.
	 * 
	 * @param key The key of the value to retrieve.
	 * @return The value of the given string key, or null if it does not exist.
	 */
	protected String getStringValue(String key)
	{
		return getStringValue(key, jsonObject);
	}
	
	/**
	 * Gets a string value from the given json object.
	 * 
	 * @param key The key of the value to retrieve.
	 * @param container The json object from which to retrieve a string.
	 * @return The value of the given string key, or null if it does not exist.
	 */
	private String getStringValue(String key, JsonObject container)
	{
		if (null == key || null == container)
			return null;
		
		String value = null;
		
		JsonElement element = container.get(key);
		if (null != element)
		{
			try 
			{
				value = element.getAsString();
			}
			catch (ClassCastException | IllegalStateException e) { }
		}
		
		return value;
	}
	
	/**
	 * Gets a Collabinate metadata string value from the activity streams
	 * object.
	 * 
	 * @param key The key for the Collabinate metadata value.
	 * @return The value for the given key, or null if it does not exist.
	 */
	public String getCollabinateValue(String key)
	{
		return getStringValue(key, jsonObject.getAsJsonObject(COLLABINATE));
	}
	
	/**
	 * Sets a Collabinate metadata string value in the activity streams object.
	 * 
	 * @param key The key for the Collabinate metadata value.
	 * @param value The value for the given key.
	 */
	public void setCollabinateValue(String key, String value)
	{
		if (!jsonObject.has(COLLABINATE))
		{
			jsonObject.add(COLLABINATE, new JsonObject());
		}
		
		jsonObject.getAsJsonObject(COLLABINATE).addProperty(key, value);
	}
	
	/**
	 * Returns a new, randomly generated UUID URN.
	 * 
	 * @return a new randomly generated UUID URN.ß
	 */
	public static String generateUuidUrn()
	{
		return UUID_URN_PREFIX + UUID.randomUUID().toString();
	}
	

	protected static final String ID = "id";
	protected static final String CONTENT = "content";
	protected static final String DISPLAY_NAME = "displayName";
	protected static final String OBJECT_TYPE = "objectType";
	protected static final String PUBLISHED = "published";
	protected static final String UPDATED = "updated";
	protected static final String REPLIES = "replies";
	protected static final String LIKES = "likes";
	protected static final String COLLABINATE = "collabinate";
	protected static final String UUID_URN_PREFIX = "urn:uuid:";
}
