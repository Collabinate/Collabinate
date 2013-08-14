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
		setName("Collabinate");
		getServers().add(Protocol.HTTP, 8182);
		getDefaultHost().attachDefault(new CollabinateApplication());
	}
}
