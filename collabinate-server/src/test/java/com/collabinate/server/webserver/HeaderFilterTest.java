package com.collabinate.server.webserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.logging.Level;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.engine.header.Header;
import org.restlet.security.Authenticator;
import org.restlet.util.Series;

import com.collabinate.server.Collabinate;
import com.collabinate.server.engine.CollabinateGraph;
import com.collabinate.server.engine.GraphAdmin;
import com.collabinate.server.engine.GraphEngine;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;

public class HeaderFilterTest
{
	/**
	 * The graph used to back the server and admin.
	 */
	protected TinkerGraph graph;
	
	/**
	 * The collabinate graph wrapping the graph store.
	 */
	protected CollabinateGraph collabinateGraph;
	
	/**
	 * The CollabinateServer used for the resources.
	 */
	protected GraphEngine server;
	
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
		collabinateGraph = new CollabinateGraph(graph);
		server = new GraphEngine(collabinateGraph);
		admin = new GraphAdmin(collabinateGraph);
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
	
	@Test
	public synchronized void request_should_authenticate_if_secret_header_matches()
	{
//		setupHeader("specialauthorization", "specauth", "specauth");
//		
//		Request request = new Request(Method.GET,
//				"riap://application/1/tenant/entities/entity/stream");
//		
//		addRequestHeader(request, "specauth", "specauth");
//				
//		assertNotEquals(Status.CLIENT_ERROR_UNAUTHORIZED,
//				component.handle(request).getStatus());
//		
//		teardownHeader("specialauthorization");
	}
	
	@Test
	public synchronized void request_should_fail_if_secret_header_does_not_match()
	{
//		setupHeader("specialauthorization", "specauth", "specauth");
//		
//		Request request = new Request(Method.GET,
//				"riap://application/1/tenant/entities/entity/stream");
//		
//		addRequestHeader(request, "specauth", "bad");
//		
//		assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED,
//				component.handle(request).getStatus());
//		
//		teardownHeader("specialauthorization");
	}
	
	@Test
	public void write_meter_header_should_be_in_write_request()
	{
		
	}
	
	protected void setupHeader(String headerConfigKey, String name, String value)
	{
		Collabinate.getConfiguration().setProperty(
				"collabinate.headers." + headerConfigKey + ".name", name);
		Collabinate.getConfiguration().setProperty(
				"collabinate.headers." + headerConfigKey + ".value", value);
	}
	
	protected void teardownHeader(String headerConfigKey)
	{
		Collabinate.getConfiguration().clearProperty(
				"collabinate.headers." + headerConfigKey + ".name");
		Collabinate.getConfiguration().clearProperty(
				"collabinate.headers." + headerConfigKey + ".value");
	}
	
	protected void addRequestHeader(Request request, String name, String value)
	{
		@SuppressWarnings("unchecked")
		Series<Header> requestHeaders = (Series<Header>) 
				request.getAttributes().get("org.restlet.http.headers");
		
		if (null == requestHeaders)
		{
		    requestHeaders = new Series<Header>(Header.class);
		    request.getAttributes().put(
		    		"org.restlet.http.headers", requestHeaders);
		}
		
		requestHeaders.add(new Header("specauth", "bad"));
	}

}
