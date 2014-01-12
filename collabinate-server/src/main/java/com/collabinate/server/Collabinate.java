package com.collabinate.server;

import java.io.Console;
import java.io.File;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.DefaultConfigurationBuilder;
import org.restlet.*;
import org.restlet.data.ChallengeScheme;
import org.restlet.security.ChallengeAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * Main Collabinate Server entry point.
 * 
 */
public class Collabinate
{
	private static Configuration configuration;
	private static CollabinateReader reader;
	private static CollabinateWriter writer;
	private static CollabinateAdmin admin;
	
	static
	{
		// Set up log4j2 logging before logger is created.
		System.setProperty("log4j.configurationFile", "log4j2.xml");
		System.setProperty("Log4jContextSelector",
			"org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
	}
	
	/**
	 * Static logger for main application.
	 */
	private static final Logger logger =
			LoggerFactory.getLogger(Collabinate.class);
	
	public static void main(String[] args) throws Exception
	{		
		// track server startup time
		long startTime = System.currentTimeMillis();
		
		// output non-logged startup message
		System.out.println("Collabinate Server starting...");
		
		// set up logging
		System.setProperty("org.restlet.engine.loggerFacadeClass",
				"org.restlet.ext.slf4j.Slf4jLoggerFacade");
		
		// load configuration
		String version = getConfiguration().getString(
				"collabinate.version", "Unknown");
		String build = getConfiguration().getString("collabinate.build", "");
		logger.info("Collabinate Server version {}{}",
				version, build.equals("") ? "" : ("+" + build));
		
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
		logger.info(String.format(
				"Collabinate Server started in %1$d milliseconds",
				totalStartTime));
		quit(server);
	}

	private static void quit(Restlet server) throws Exception
	{
		Console console = System.console();
		if (null != console)
		{
			// if the system can be stopped via the console, let the user know
			System.out.println("Press Enter to quit");
			System.console().readLine();
			logger.info("Collabinate Server shutting down...");
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
	
	/**
	 * Registers a shutdown hook for clean graph shutdown.
	 * 
	 * @param graph The graph that needs clean shutdown upon system shutdown.
	 */
	private static void registerShutdownHook(final Graph graph)
	{
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			@Override
			public void run()
			{
				graph.shutdown();
				System.out.println("Collabinate Server shutdown complete.");
			}
		});
	}
}
