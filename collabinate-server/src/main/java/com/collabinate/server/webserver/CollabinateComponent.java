package com.collabinate.server.webserver;

import java.io.File;

import org.restlet.Component;
import org.restlet.data.MediaType;
import org.restlet.engine.component.ComponentXmlParser;
import org.restlet.representation.FileRepresentation;
import org.restlet.security.Authenticator;

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
	public CollabinateComponent(CollabinateReader reader,
			CollabinateWriter writer, Authenticator authenticator)
	{
		File file = new File("componentConfig.xml");
		FileRepresentation fileRepresentation = 
				new FileRepresentation(file, MediaType.TEXT_XML);
		ComponentXmlParser parser = new ComponentXmlParser(this, fileRepresentation);
		parser.parse();
		
		if (null == reader)
			throw new IllegalArgumentException("reader must not be null");
		
		if (null == writer)
			throw new IllegalArgumentException("writer must not be null");
		
		if (null == authenticator)
			throw new IllegalArgumentException(
					"authenticator must not be null");
		
		setName("Collabinate");

		// use a child context with the authenticator to avoid warnings
		authenticator.setContext(getContext().createChildContext());
		
		getDefaultHost().attachDefault(
				new CollabinateApplication(reader, writer, authenticator));
	}
}
