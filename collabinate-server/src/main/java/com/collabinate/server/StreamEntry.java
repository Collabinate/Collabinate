package com.collabinate.server;

import java.util.UUID;

import org.joda.time.DateTime;

/**
 * Data that represents a single entry in a chronological activity stream.
 * 
 * @author mafuba
 *
 */
public class StreamEntry
{
	private String id;
	private DateTime time;
	private String content;
	
	/**
	 * Initializes a new Stream Entry.
	 * 
	 * @param id A stream-unique identifier for the entry. Will be set to a
	 * random UUID if null.
	 * @param time The date and time of the entry, used for sorting the stream.
	 * Will be set to the current date and time if null.
	 * @param content The content of the entry, typically in Activity Streams
	 * format.
	 */
	public StreamEntry(String id, DateTime time, String content)
	{
		if (null == id || "" == id)
		{
			id = UUID.randomUUID().toString();
		}
		this.id = id;
		
		if (null == time)
		{
			time = DateTime.now();
		}
		this.time = time;
		
		if (null == content)
		{
			content = "";
		}
		this.content = content;
	}
	
	/**
	 * Get the identifier for the entry. This is unique among entries within the
	 * stream to which this entry belongs.
	 * 
	 * @return The stream-unique identifier for the entry.
	 */
	public String getId()
	{
		return id;
	}
	
	/**
	 * Get the date and time of the entry.  This is the time that will be used
	 * to compare against other entries to determine chronological ordering.
	 * 
	 * @return The time of the entry.
	 */
	public DateTime getTime()
	{
		return time;
	}
	
	/**
	 * Get the content of the entry.  This can be anything, but will typically
	 * be information in the Activity Streams format.
	 * 
	 * @return
	 */
	public String getContent()
	{
		return content;
	}
}
