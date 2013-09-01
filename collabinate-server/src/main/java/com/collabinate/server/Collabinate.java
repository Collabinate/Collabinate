package com.collabinate.server;

import java.io.Console;
import java.io.File;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.DefaultConfigurationBuilder;
import org.restlet.*;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphFactory;
import com.tinkerpop.blueprints.KeyIndexableGraph;

/**
 * Main Collabinate entry point.
 * 
 */
public class Collabinate
{
	private static Configuration configuration;
	private static CollabinateReader reader;
	private static CollabinateWriter writer;
	
	public static void main(String[] args) throws Exception
	{
		// track server startup time
		long startTime = System.currentTimeMillis();
		
		//load configuration
		String version = getConfiguration().getString(
				"collabinate.version", "Unknown");
		String build = getConfiguration().getString("collabinate.build", "");
		System.out.println("Collabinate Server Version " + 
				version + (build.equals("") ? "" : ("+" + build)));
		
		// connect to the data store
		Graph configuredGraph = GraphFactory.open("graph.properties");
		if (!(configuredGraph instanceof KeyIndexableGraph))
			throw new IllegalStateException(
					"Configured graph is not a KeyIndexableGraph");
		KeyIndexableGraph graph = (KeyIndexableGraph)configuredGraph;
		
		// create the engine
		GraphServer engine = new GraphServer(graph);
		reader = engine;
		writer = engine;
		
		// create the Restlet component and start it
		Component server = new CollabinateComponent(reader, writer);
		server.start();		
		
		// output server startup time
		long totalStartTime = System.currentTimeMillis() - startTime;
		System.out.println(String.format("Server started in %1$d milliseconds",
				totalStartTime));
		quit(server);
	}

	private static void quit(Restlet server) throws Exception
	{
		Console console = System.console();
		if (null != console)
		{
			System.out.println("Press Enter to quit");
			System.console().readLine();
			server.stop();
		}
		else
		{
			System.out.println("No interactive console available;" + 
				" terminate process to quit");
		}
	}
	
	/**
	 * Retrieves the loaded configuration for the server.
	 * 
	 * @return The server configuration.
	 * @throws ConfigurationException 
	 */
	public static Configuration getConfiguration()
	{
		if (null != configuration)
			return configuration;
		
		DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
		builder.setFile(new File("configDefinition.xml"));
		try
		{
			configuration = builder.getConfiguration();
		}
		catch (ConfigurationException exc)
		{
			throw new IllegalStateException("Problem loading config", exc);
		}
		
		if (null == configuration)
			throw new IllegalStateException("Configuration not loaded");

		return configuration;
	}
}
