package com.collabinate.server;

/**
 * Contract for capabilities of serializing and deserializing stream entries.
 * 
 * @author mafuba
 *
 * @param <T> The type of object to which StreamEntries are serialized.
 */
public interface StreamEntrySerDes<T>
{
	/**
	 * Converts a StreamEntry to another object.
	 * 
	 * @param streamEntry The stream entry to serialize
	 * @return The serialized version of the given StreamEntry
	 */
	public T Serialize(StreamEntry streamEntry);
	
	/**
	 * Recreates a StreamEntry from its serialized version.
	 * 
	 * @param serializedEntry The object to deserialize into a StreamEntry
	 * @return The deserialized StreamEntry
	 */
	public StreamEntry Deserialize(T serializedEntry);
}
