package com.collabinate.server;

import java.io.Console;

import org.restlet.*;

/**
 * Main Collabinate entry point.
 * 
 */
public class App
{
	private static CollabinateReader reader;
	private static CollabinateWriter writer;
	
	public static void main(String[] args) throws Exception
	{
		// track server startup time
		long startTime = System.currentTimeMillis();
		// TODO: output version number automatically
		System.out.println("Collabinate Server Version 1.0.0 Build 1");
		
		// connect to the data store
		// TODO: use configuration 
		com.tinkerpop.blueprints.KeyIndexableGraph graph = 
				new com.tinkerpop.blueprints.impls.tg.TinkerGraph();
		
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
}
