package com.collabinate.server.activitystreams;

/**
 * Represents an Activity Streams collection serialization.
 * http://activitystrea.ms/specs/json/1.0/#collection
 * 
 * @author mafuba
 *
 */
public class Collection
{
	private Integer totalItems;
	private Object[] items;
	private String url;
	
	/**
	 * No-arg constructor for serialization.
	 */
	Collection() { }
	
	/**
	 * Non-negative integer specifying the total number of activities within the
	 * stream. The Stream serialization MAY contain a count property.
	 * 
	 * @return Non-negative integer specifying the total number of activities
	 * within the stream.
	 */
	public Integer getTotalItems()
	{
		return totalItems;
	}
	
	/**
	 * An array containing a listing of Objects of any object type. If used in
	 * combination with the url property, the items array can be used to provide
	 * a subset of the objects that may be found in the resource identified by
	 * the url.
	 * 
	 * @return An array containing a listing of Objects of any object type.
	 */
	public Object[] getItems()
	{
		return items;
	}
	
	/**
	 * An IRI [RFC3987] referencing a JSON document containing the full listing
	 * of objects in the collection.
	 * 
	 * @return An IRI [RFC3987].
	 */
	public String getUrl()
	{
		return url;
	}
}
