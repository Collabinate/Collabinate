package com.collabinate.server.webserver;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.engine.header.Header;
import org.restlet.routing.Filter;
import org.restlet.util.Series;

/**
 * Performs handling for every request/response in the system.
 * 
 * @author mafuba
 *
 */
public class GlobalFilter extends Filter
{
	/**
	 * Constructor.
	 * 
	 * @param context The Context.
	 */
	public GlobalFilter(Context context)
	{
		super(context);
	}
	
	@Override
	protected void afterHandle(Request request, Response response)
	{
		// handle CORS
		setResponseHeaderValue(response, CORS_ORIGIN_HEADER, "*");
	}
	
	/**
	 * Adds the given header and value to the HTTP response.
	 * 
	 * @param response The response for which to set a header value.
	 * @param header The name of the header to set.
	 * @param value The value to set for the given header.
	 */
	protected void setResponseHeaderValue(Response response, String header, 
			String value)
	{
		@SuppressWarnings("unchecked")
		Series<Header> responseHeaders = (Series<Header>) 
				response.getAttributes().get(RESTLET_HEADERS);
		
		if (null == responseHeaders)
		{
		    responseHeaders = new Series<Header>(Header.class);
		    response.getAttributes().put(RESTLET_HEADERS, responseHeaders);
		}
		
		responseHeaders.add(new Header(header, value));
	}
	
	private static final String RESTLET_HEADERS = "org.restlet.http.headers";
	private static final String CORS_ORIGIN_HEADER =
			"Access-Control-Allow-Origin";
}
