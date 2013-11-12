package com.collabinate.server.activitystreams;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * Represents an Activity Streams collection serialization.
 * http://activitystrea.ms/specs/json/1.0/#collection
 * 
 * @author mafuba
 *
 */
public class ActivityStreamsCollection
{
	/**
	 * The internal representation of the object as JSON.
	 */
	protected JsonObject jsonObject;
	
	/**
	 * Default constructor. Creates the collection with an empty items array.
	 */
	public ActivityStreamsCollection()
	{
		jsonObject = new JsonObject();
		
		ensureDefaultFields();
	}
	
	/**
	 * Creates a collection from the given json. If the json is not valid, the
	 * string will be added to a "content" property in the collection. If the
	 * json does not contain an "items" property or it is not an array, an items
	 * array will be added.
	 * 
	 * @param json The json representation of the collection to create.
	 */
	public ActivityStreamsCollection(String json)
	{
		if (null != json)
		{
			JsonParser parser = new JsonParser();
			try
			{
				JsonElement element = parser.parse(json);
				if (element.isJsonObject())
					jsonObject = element.getAsJsonObject();
			}
			catch (JsonParseException e) { }
		}
		
		if (null == jsonObject)
		{
			jsonObject = new JsonObject();
			jsonObject.addProperty(CONTENT, json);
		}
		
		ensureDefaultFields();
	}
	
	/**
	 * Provides a means of ensuring that all required fields for the collection
	 * are in place. Called in the constructor. By default only the items
	 * property is required in the base ActivityStreamsCollection.
	 */
	protected void ensureDefaultFields()
	{
		if (!jsonObject.has(ITEMS) || !jsonObject.get(ITEMS).isJsonArray())
		{
			JsonArray items = new JsonArray();
			jsonObject.add(ITEMS, items);
		}
	}

	/**
	 * Returns the activity contained at the given index in the items property
	 * of the collection.
	 * 
	 * @return The activity at the given index.
	 */
	public Activity get(int index)
	{
		return new Activity(jsonObject.getAsJsonArray(ITEMS)
				.get(index).toString());
	}
	
	/**
	 * Adds the given activity to the collection.
	 * 
	 * @param activity The activity to add.
	 */
	public void add(Activity activity)
	{
		JsonParser parser = new JsonParser();
		jsonObject.getAsJsonArray(ITEMS)
			.add(parser.parse(activity.toString()));
	}
	
	@Override
	public String toString()
	{
		return jsonObject.toString();
	}
	
	protected static final String ITEMS = "items";
	protected static final String CONTENT = "content";
}
