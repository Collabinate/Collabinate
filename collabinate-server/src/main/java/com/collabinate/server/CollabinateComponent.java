package com.collabinate.server;

import org.restlet.Component;
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
		if (null == reader)
			throw new IllegalArgumentException("reader must not be null");
		
		if (null == writer)
			throw new IllegalArgumentException("writer must not be null");
		
		setName("Collabinate");
		getServers().add(Protocol.HTTP, 8182);
		getDefaultHost().attachDefault(
				new CollabinateApplication(reader, writer));
	}
}
