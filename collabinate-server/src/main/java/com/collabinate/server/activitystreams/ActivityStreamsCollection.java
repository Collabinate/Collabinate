package com.collabinate.server.activitystreams;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Represents an Activity Streams collection serialization.
 * http://activitystrea.ms/specs/json/1.0/#collection
 * 
 * @author mafuba
 *
 */
public class ActivityStreamsCollection extends ActivityStreamsObject
{	
	/**
	 * Default constructor. Creates the collection with an empty items array.
	 */
	public ActivityStreamsCollection()
	{
		super();
	}
	
	/**
	 * Creates a collection from the given json. If the json is not valid, the
	 * string will be added to a "content" property in the collection. If the
	 * json does not contain an "items" property or it is not an array, an items
	 * array will be added.
	 * 
	 * @param json The json representation of the collection to create.
	 */
	public ActivityStreamsCollection(String content)
	{
		super(content);
	}
	
	/**
	 * Creates a collection populated with the given items.
	 * @param items
	 */
	public ActivityStreamsCollection(List<ActivityStreamsObject> items)
	{
		setItems(items);
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
	 * Returns an immutable copy of the current items collection.
	 * 
	 * @return An immutable list copy of the current items collection.
	 */
	public List<ActivityStreamsObject> getItems()
	{
		List<ActivityStreamsObject> items =
				new ArrayList<ActivityStreamsObject>();
		
		for (JsonElement element : jsonObject.getAsJsonArray(ITEMS))
		{
			items.add(new ActivityStreamsObject(element.toString()));
		}
		
		return ImmutableList.copyOf(items);
	}
	
	/**
	 * Creates or replaces the current items array with an array made up of the
	 * given objects.
	 * 
	 * @param items The collection of ActivityStreamObjects used to populate the
	 * items.
	 */
	protected void setItems(List<ActivityStreamsObject> items)
	{
		JsonArray array = new JsonArray();
		
		for (ActivityStreamsObject activityStreamsObject : items)
		{
			array.add(activityStreamsObject.jsonObject);
		}
		
		jsonObject.add(ITEMS, array);
	}

	/**
	 * Returns the activity contained at the given index in the items property
	 * of the collection.
	 * 
	 * @return The activity at the given index.
	 */
	public ActivityStreamsObject get(int index)
	{
		return new ActivityStreamsObject(jsonObject.getAsJsonArray(ITEMS)
				.get(index).toString());
	}
	
	/**
	 * Adds the given activity to the collection.
	 * 
	 * @param activity The activity to add.
	 */
	public void add(ActivityStreamsObject activityStreamsObject)
	{
		JsonParser parser = new JsonParser();
		jsonObject.getAsJsonArray(ITEMS)
			.add(parser.parse(activityStreamsObject.toString()));
	}
	
	protected static final String ITEMS = "items";
}
