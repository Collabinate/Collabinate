package com.collabinate.server;

import java.io.Console;
import java.io.File;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.DefaultConfigurationBuilder;
import org.restlet.*;
import org.restlet.data.ChallengeScheme;
import org.restlet.security.ChallengeAuthenticator;

import com.collabinate.server.engine.CollabinateAdmin;
import com.collabinate.server.engine.CollabinateGraph;
import com.collabinate.server.engine.CollabinateReader;
import com.collabinate.server.engine.CollabinateWriter;
import com.collabinate.server.engine.GraphAdmin;
import com.collabinate.server.engine.GraphEngine;
import com.collabinate.server.webserver.CollabinateComponent;
import com.collabinate.server.webserver.CollabinateVerifier;
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
	private static CollabinateAdmin admin;
	
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
		CollabinateGraph graph = new CollabinateGraph(
				(KeyIndexableGraph)configuredGraph);
		registerShutdownHook(graph);
		
		// create the engine
		GraphEngine engine = new GraphEngine(graph);
		reader = engine;
		writer = engine;
		admin = new GraphAdmin(graph);
		
		// create the authenticator
		ChallengeAuthenticator authenticator = new ChallengeAuthenticator(
				null,  // context gets added in the component
				false, // authentication is not optional
				ChallengeScheme.HTTP_BASIC,
				"Collabinate",
				new CollabinateVerifier(admin));
		
		// create the Restlet component and start it
		CollabinateComponent server = new CollabinateComponent(reader, writer,
				admin, authenticator);
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
			System.out.println("Server Stopped");
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
	
	private static void registerShutdownHook(final Graph graph)
	{
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			@Override
			public void run()
			{
				graph.shutdown();
			}
		});
	}
}
