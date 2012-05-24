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
        //System.out.println( "Hello World!" );
    	new Server(Protocol.HTTP, 8182, App.class).start();
    }
    
    @Get
    public String toString() {
    	return "hello, let's collabinate!";
    }
}
