package com.collabinate.server;

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
    	System.out.println("Collabinate Server version 1.0.0 Build 1");
    	Server server = new Server(Protocol.HTTP, 8182, App.class);
    	server.start();
    	long totalStartTime = System.currentTimeMillis() - startTime;
    	System.out.println(String.format("Server started in %1$d milliseconds", totalStartTime));
    	System.out.println("Press Enter to quit");
    	System.console().readLine();
    	server.stop();
    }
    
    @Get
    public String toString() {
    	return "hello, let's collabinate!";
    }
}
