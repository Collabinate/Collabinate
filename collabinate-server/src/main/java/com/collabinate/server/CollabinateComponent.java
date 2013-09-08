package com.collabinate.server;

import org.restlet.Component;
import org.restlet.Context;
import org.restlet.data.Protocol;

/**
 * Main Restlet component.
 * 
 * @author mafuba
 *
 */
public class CollabinateComponent extends Component
{
	/**
	 * Set up the component
	 */
	public CollabinateComponent(CollabinateReader reader,
			CollabinateWriter writer)
	{
		this(reader, writer, Collabinate.getConfiguration()
				.getInt("collabinate.port"));
	}
	
	public CollabinateComponent(CollabinateReader reader,
			CollabinateWriter writer, int port)
	{
		if (null == reader)
			throw new IllegalArgumentException("reader must not be null");
		
		if (null == writer)
			throw new IllegalArgumentException("writer must not be null");
		
		setName("Collabinate");
		//getServers().clear();
		getServers().add(Protocol.HTTP, port);
		//setContext(new Context());
		getDefaultHost().attachDefault(
				new CollabinateApplication(reader, writer));		
	}
}
