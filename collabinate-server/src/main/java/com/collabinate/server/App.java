package com.collabinate.server;

import java.io.Console;

import org.restlet.*;
import org.restlet.data.*;
import org.restlet.resource.*;


/**
 * Hello world!
 *
 */
public class App extends ServerResource
{
    public static void main( String[] args ) throws Exception
    {
    	long startTime = System.currentTimeMillis();
    	System.out.println("Collabinate Server Version 1.0.0 Build 1");
    	Server server = new Server(Protocol.HTTP, 8182, App.class);
    	server.start();
    	long totalStartTime = System.currentTimeMillis() - startTime;
    	System.out.println(String.format("Server started in %1$d milliseconds", totalStartTime));
    	
    	Console console = System.console();
    	if (null != console)
    	{
        	System.out.println("Press Enter to quit");
        	System.console().readLine();
        	server.stop();
    	}
    	else
    	{
    		System.out.println("No interactive console available; terminate process to quit");
    	}
    }
    
    @Get
    public String toString() {
    	return "hello, let's collabinate!";
    }
}
