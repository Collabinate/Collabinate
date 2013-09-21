package com.collabinate.server;

import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.security.Authenticator;

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
			CollabinateWriter writer, Authenticator authenticator)
	{
		// the server is http on the configured port
		this(reader, writer, authenticator, Collabinate.getConfiguration()
				.getInt("collabinate.port"));
	}
	
	public CollabinateComponent(CollabinateReader reader,
			CollabinateWriter writer, Authenticator authenticator, int port)
	{
		if (null == reader)
			throw new IllegalArgumentException("reader must not be null");
		
		if (null == writer)
			throw new IllegalArgumentException("writer must not be null");
		
		setName("Collabinate");
		getServers().add(Protocol.HTTP, port);
		
		// use a child context with the authenticator to avoid warnings
		authenticator.setContext(getContext().createChildContext());
		
		getDefaultHost().attachDefault(
				new CollabinateApplication(reader, writer, authenticator));				
	}
}
