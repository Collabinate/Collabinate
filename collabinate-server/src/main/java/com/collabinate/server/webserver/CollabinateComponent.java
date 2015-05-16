package com.collabinate.server.webserver;

import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.security.Authenticator;

import com.collabinate.server.Collabinate;
import com.collabinate.server.engine.CollabinateAdmin;
import com.collabinate.server.engine.CollabinateReader;
import com.collabinate.server.engine.CollabinateWriter;

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
	public CollabinateComponent(
			CollabinateReader reader,
			CollabinateWriter writer,
			CollabinateAdmin admin,
			Authenticator authenticator)
	{
		this.getClients().add(Protocol.FILE);
		
		this.getServers().add(
				new Server(getSslContext(), getProtocol(), getPort()));
		
		if (null == reader)
			throw new IllegalArgumentException("reader must not be null");
		
		if (null == writer)
			throw new IllegalArgumentException("writer must not be null");
		
		if (null == admin)
			throw new IllegalArgumentException("admin must not be null");
		
		if (null == authenticator)
			throw new IllegalArgumentException(
					"authenticator must not be null");
		
		setName("Collabinate");

		// use a child context with the authenticator to avoid warnings
		authenticator.setContext(getContext().createChildContext());
		
		getDefaultHost().attachDefault(
				new CollabinateApplication(reader, writer, admin,
						authenticator));
	}
	
	/**
	 * Get the context populated with values for SSL (if the protocol is HTTPS).
	 * 
	 * @return the current context, with added parameters if necessary.
	 */
	private Context getSslContext()
	{
		Context context = getContext().createChildContext();
		
		if (Protocol.HTTPS.equals(getProtocol()))
		{
			context.getParameters().add("keystorePath", 
					Collabinate.getConfiguration().getString(
						"collabinate.server.webserver.keystorePath"));
			context.getParameters().add("keystorePassword", 
					Collabinate.getConfiguration().getString(
						"collabinate.server.webserver.keystorePassword"));
			context.getParameters().add("keystoreType", 
					Collabinate.getConfiguration().getString(
						"collabinate.server.webserver.keystoreType"));
			context.getParameters().add("keyPassword", 
					Collabinate.getConfiguration().getString(
						"collabinate.server.webserver.keyPassword"));
		}
		
		return context;
	}
	
	/**
	 * Get the server protocol from config, default to HTTP.
	 * 
	 * @return the configured server protocol.
	 */
	private Protocol getProtocol()
	{
		String protocol = Collabinate.getConfiguration()
				.getString("collabinate.server.webserver.protocol", "HTTP");
		
		if (protocol.equalsIgnoreCase("HTTP"))
		{
			return Protocol.HTTP;
		}
		else if (protocol.equalsIgnoreCase("HTTPS"))
		{
			return Protocol.HTTPS;
		}
		else
		{
			throw new UnsupportedOperationException(
					"Unsupported server protocol: " + protocol);
		}
	}
	
	/**
	 * Get the server port from config, default to 8182.
	 * 
	 * @return the configured server port.
	 */
	private int getPort()
	{
		return Collabinate.getConfiguration()
				.getInt("collabinate.server.webserver.port", 8182);
	}
}
