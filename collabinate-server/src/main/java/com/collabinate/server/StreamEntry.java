package com.collabinate.server;

import org.joda.time.DateTime;

/**
 * Contract for a stream entry data transfer object.
 * 
 * @author mafuba
 *
 */
public interface StreamEntry
{
	/**
	 * Get the date and time of the entry.  This is the time that will be used
	 * to compare against other entries to determine chronological ordering.
	 * @return
	 */
	public DateTime getTime();
}
