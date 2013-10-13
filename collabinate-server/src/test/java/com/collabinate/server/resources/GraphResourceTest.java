package com.collabinate.server.resources;

import java.util.logging.Level;

import org.junit.After;
import org.junit.Before;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.engine.Engine;
import org.restlet.security.Authenticator;

import com.collabinate.server.engine.GraphAdmin;
import com.collabinate.server.engine.GraphServer;
import com.collabinate.server.webserver.CollabinateComponent;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;

/**
 * Abstract base test class and helper methods for testing RESTful resources.
 * Uses the graph implementations of CollabinateServer and CollabinateAdmin.
 * 
 * @author mafuba
 *
 */
public abstract class GraphResourceTest
{
	/**
	 * The graph used to back the server and admin.
	 */
	protected TinkerGraph graph;
	/**
	 * The CollabinateServer used for the resources.
	 */
	protected GraphServer server;
	/**
	 * The CollabinateAdmin used for the resources.
	 */
	protected GraphAdmin admin;
	/**
	 * The restlet component used for testing.
	 */
	protected Component component;
	
	@Before
	public void graphResourceSetup()
	{
		graph = new TinkerGraph();
		server = new GraphServer(graph);
		admin = new GraphAdmin(graph);
		Engine.setRestletLogLevel(Level.WARNING);
		component = new CollabinateComponent(server, server, admin,
			new Authenticator(null) {
				@Override
				protected boolean authenticate(Request request, 
						Response response)
				{
					return true;
				}
			});
	}
	
	@After
	public void graphResourceTeardown() throws Exception
	{
		if (component.isStarted())
		{
			component.stop();
		}
		
		graph.clear();
	}
	
	/**
	 * Retrieves the path portion of the resource to test. This does not include
	 * the protocol and server.
	 * 
	 * @return The path of the resource under test.
	 */
	protected abstract String getResourcePath();
	
	/**
	 * Retrieves a Restlet request object to be used to send to the resource.
	 * 
	 * @param method The HTTP method for the request.
	 * @param params Any parameters used for the request in string format, e.g.
	 * "?param1=foo&param2=bar".
	 * @return A request ready to be sent to the test resource.
	 */
	protected Request getRequest(Method method, String params)
	{
		if (null == params)
			params = "";
		
		return new Request(method, 
				"riap://application" + getResourcePath() + params);
	}
	
	/**
	 * Sends the given request to the test resource and returns the response.
	 * 
	 * @param request The request to send to the test resource.
	 * @return The response to the given request from the test resource.
	 */
	protected Response getResponse(Request request)
	{
		return component.handle(request);
	}
	
	/**
	 * Sends a GET request to the test resource and returns the response.
	 * 
	 * @return The response from the test resource to the GET request.
	 */
	protected Response get()
	{
		return get(null);
	}
	
	/**
	 * Sends a GET request to the test resource with the given parameters and
	 * returns the response.
	 * 
	 * @param params Any request parameters to send to the resource in the
	 * format "?param1=foo&param2=bar". May be null.
	 * @return The response from the test resource to the GET request.
	 */
	protected Response get(String params)
	{
		return getResponse(getRequest(Method.GET, params));
	}
	
	/**
	 * Sends a PUT request to the test resource and returns the response.
	 * 
	 * @return The response from the test resource to the PUT request.
	 */
	protected Response put()
	{
		return put(null);
	}
	
	/**
	 * Sends a PUT request to the test resource with the given parameters and
	 * returns the response.
	 * 
	 * @param params Any request parameters to send to the resource in the
	 * format "?param1=foo&param2=bar". May be null.
	 * @return The response from the test resource to the PUT request.
	 */
	protected Response put(String params)
	{
		return getResponse(getRequest(Method.PUT, params));
	}
	
	/**
	 * Sends a PUT request to the test resource with the given entity body and
	 * returns the response.
	 * 
	 * @param value The string entity body to send to the resource.
	 * @param mediaType The media type of the entity to send to the resource.
	 * @return The response from the test resource to the PUT request.
	 */
	protected Response put(String value, MediaType mediaType)
	{
		Request request = getRequest(Method.PUT, null);
		request.setEntity(value, mediaType);
		return getResponse(request);
	}
	
	/**
	 * Sends a POST request to the test resource and returns the response.
	 * 
	 * @return The response from the test resource to the POST request.
	 */
	protected Response post()
	{
		return post(null);
	}
	
	/**
	 * Sends a POST request to the test resource with the given parameters and
	 * returns the response.
	 * 
	 * @param params Any request parameters to send to the resource in the
	 * format "?param1=foo&param2=bar". May be null.
	 * @return The response from the test resource to the POST request.
	 */
	protected Response post(String params)
	{
		return getResponse(getRequest(Method.POST, params));
	}
	
	/**
	 * Sends a POST request to the test resource with the given entity body and
	 * returns the response.
	 * 
	 * @param value The string entity body to send to the resource.
	 * @param mediaType The media type of the entity to send to the resource.
	 * @return The response from the test resource to the POST request.
	 */
	protected Response post(String value, MediaType mediaType)
	{
		Request request = getRequest(Method.POST, null);
		request.setEntity(value, mediaType);
		return getResponse(request);
	}
	
	/**
	 * Sends a DELETE request to the test resource and returns the response.
	 * 
	 * @return The response from the test resource to the DELETE request.
	 */
	protected Response delete()
	{
		return delete(null);
	}
	
	/**
	 * Sends a DELETE request to the test resource with the given parameters and
	 * returns the response.
	 * 
	 * @param params Any request parameters to send to the resource in the
	 * format "?param1=foo&param2=bar". May be null.
	 * @return The response from the test resource to the DELETE request.
	 */
	protected Response delete(String params)
	{
		return getResponse(getRequest(Method.DELETE, params));
	}
}
