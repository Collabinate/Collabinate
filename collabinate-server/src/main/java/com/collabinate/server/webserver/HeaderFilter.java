package com.collabinate.server.webserver;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.configuration.Configuration;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.engine.header.Header;
import org.restlet.routing.Filter;
import org.restlet.util.Series;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.collabinate.server.Collabinate;

/**
 * A filter to handle the HTTP headers required by the Collabinate server,
 * such as metering for billing and secret authorization keys.
 * 
 * @author mafuba
 *
 */
public class HeaderFilter extends Filter
{
	private boolean specialAuthorization = false;
	private String specialAuthorizationHeader;
	private String specialAuthorizationValue;
	private boolean readMetering = false;
	private String readMeteringHeader;
	private String readMeteringValue;
	private boolean writeMetering = false;
	private String writeMeteringHeader;
	private String writeMeteringValue;
	
	private final Logger logger = LoggerFactory.getLogger(HeaderFilter.class);
	
	/**
	 * Initializes a HeaderFilter instance.
	 * @param context The context.
	 * @param next The next Restlet.
	 */
	public HeaderFilter(Context context, Restlet next)
	{
		super(context, next);
		initialize();
	}
	
	/**
	 * Sets up any instance members used for checking and setting headers.
	 * Called from the constructor.
	 */
	protected void initialize()
	{
		Configuration config = Collabinate.getConfiguration();
		
		specialAuthorizationHeader = 
				config.getString(CONFIG_SPEC_AUTH_HEADER);
		specialAuthorizationValue =
				config.getString(CONFIG_SPEC_AUTH_VALUE);
		specialAuthorization =
			null != specialAuthorizationHeader &&
			null != specialAuthorizationValue;
		
		readMeteringHeader = 
				config.getString(CONFIG_READ_METERING_HEADER);
		readMeteringValue =
				config.getString(CONFIG_READ_METERING_VALUE);
		readMetering =
			null != readMeteringHeader &&
			null != readMeteringValue;
		
		writeMeteringHeader = 
				config.getString(CONFIG_WRITE_METERING_HEADER);
		writeMeteringValue =
				config.getString(CONFIG_WRITE_METERING_VALUE);
		writeMetering =
			null != writeMeteringHeader &&
			null != writeMeteringValue;
	}
	
	@Override
	protected int beforeHandle(Request request, Response response)
	{
		// handle special authorization headers
		if (specialAuthorization && !specialAuthorizationValue.equals(
				getRequestHeaderValue(request, specialAuthorizationHeader)))
		{
			response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return STOP;
		}
		
		return CONTINUE;
	}
	
	@Override
	protected void afterHandle(Request request, Response response)
	{
		Method requestMethod = request.getMethod();
				
		// do not meter for errors
		if (response.getStatus().isSuccess() || 
			response.getStatus().isRedirection() || 
			response.getStatus().isInformational())
		{
			// add the read metering header if it exists
			if (readMetering && requestMethod.equals(Method.GET))
			{
				setResponseHeaderValue(response, readMeteringHeader,
						readMeteringValue);
			}
			
			// add the write metering header if it exists
			if (writeMetering &&
					(requestMethod.equals(Method.POST) ||
					 requestMethod.equals(Method.PUT)  ||
					 requestMethod.equals(Method.DELETE)))
			{
				setResponseHeaderValue(response, writeMeteringHeader,
						writeMeteringValue);
			}
		}
		
		// remove the protocol and server name from response headers to make
		// URLs correct when accessed through proxies.
		makeHeaderUrlRelative(response, LOCATION_HEADER);
	}
	
	/**
	 * Returns a comma separated string of all the values of the given header
	 * contained in the HTTP request.
	 * 
	 * @param request The request for which to retrieve a header value.
	 * @param header The name of the head for which value(s) will be retrieved.
	 * @return A comma separated string of all the values of the given header.
	 */
	protected String getRequestHeaderValue(Request request, String header)
	{
		@SuppressWarnings("unchecked")
		Series<Header> requestHeaders = (Series<Header>) 
				request.getAttributes().get(RESTLET_HEADERS);
		
		String value = null;
		
		if (null != requestHeaders)
		{
			value = requestHeaders.getValues(header);
		}
		
		return value;
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
	
	/**
	 * Replaces the absolute URL in the given header with the path portion.
	 * 
	 * @param response The response for which the header will be edited.
	 * @param headerName The header to make relative.
	 */
	protected void makeHeaderUrlRelative(Response response,
			String headerName)
	{
		@SuppressWarnings("unchecked")
		Series<Header> responseHeaders = (Series<Header>)
				response.getAttributes().get(RESTLET_HEADERS);
		
		if (null != responseHeaders)
		{
			String location = responseHeaders.getFirstValue(headerName, true);
			
			if (null != location && !location.equals(""))
			{
				try
				{
					responseHeaders.set(headerName,
							new URL(location).getPath(), true);
				}
				catch (MalformedURLException e)
				{
					logger.error(
						"Malformed URL in response header: " + headerName, e);
				}
			}
		}
	}
	
	private static final String RESTLET_HEADERS = "org.restlet.http.headers";
	private static final String CONFIG_SPEC_AUTH_HEADER =
			"collabinate.headers.specialauthorization.name";
	private static final String CONFIG_SPEC_AUTH_VALUE =
			"collabinate.headers.specialauthorization.value";
	private static final String CONFIG_READ_METERING_HEADER =
			"collabinate.headers.readmetering.name";
	private static final String CONFIG_READ_METERING_VALUE =
			"collabinate.headers.readmetering.value";
	private static final String CONFIG_WRITE_METERING_HEADER =
			"collabinate.headers.writemetering.name";
	private static final String CONFIG_WRITE_METERING_VALUE =
			"collabinate.headers.writemetering.value";
	private static final String LOCATION_HEADER = "Location";
}
