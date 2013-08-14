package com.collabinate.server;

import java.io.Console;

import org.restlet.*;

/**
 * Hello world!
 * 
 */
public class App
{
	public static void main(String[] args) throws Exception
	{
		long startTime = System.currentTimeMillis();
		System.out.println("Collabinate Server Version 1.0.0 Build 1");
		Component server = new CollabinateComponent();
		server.start();
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
			System.out
					.println("No interactive console available; terminate process to quit");
		}
	}
}
