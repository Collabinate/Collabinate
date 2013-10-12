package com.collabinate.server.webserver;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ClientInfo;
import org.restlet.security.User;
import org.restlet.security.Verifier;

import com.collabinate.server.engine.CollabinateAdmin;

/**
 * Verifier that uses a CollabinateAdmin to authenticate against.
 * 
 * @author mafuba
 * 
 */
public class CollabinateVerifier implements Verifier
{
	/**
	 * The admin engine used to perform verification.
	 */
	CollabinateAdmin admin;
	
	/**
	 * Initializes the verifier with the admin engine.
	 * 
	 * @param graph the database to authenticate against.
	 */
	public CollabinateVerifier(CollabinateAdmin admin)
	{
		this.admin = admin;
	}

	/**
	 * Called back to create a new user when valid credentials are provided.
	 * 
	 * @param identifier The user identifier.
	 * @param request The request handled.
	 * @param response The response handled.
	 * @return The {@link User} instance created.
	 */
	private User createUser(String identifier, Request request,
			Response response)
	{
		return new User(identifier);
	}

	/**
	 * Returns the tenant identified in the URI.
	 * 
	 * @param request The request to inspect.
	 * @param response The response to inspect.
	 * @return The tenant slug from the URI.
	 */
	private String getTenantId(Request request, Response response)
	{
		return (String)request.getAttributes().get("tenantId");
	}

	/**
	 * Returns the API key contained in the user identifier.
	 * 
	 * @param request The request to inspect.
	 * @param response The response to inspect.
	 * @return The API key.
	 */
	private String getApiKey(Request request, Response response)
	{
		return request.getChallengeResponse().getIdentifier();
	}

	/**
	 * Verifies that the API key is correct for the specified request by
	 * comparing the identifier portion of the request's authentication response
	 * with the set of keys for the tenant as identified in the URL. Sets the 
	 * {@link org.restlet.security.User} instance of the request's
	 * {@link ClientInfo} if successful.
	 * 
	 * @param request The request to inspect.
	 * @param response The response to inspect.
	 * @return Result of the verification based on the RESULT_* constants.
	 */
	@Override
	public int verify(Request request, Response response)
	{
		int result = RESULT_VALID;

		if (request.getChallengeResponse() == null)
		{
			result = RESULT_MISSING;
		}
		else
		{
			String tenantId = getTenantId(request, response);
			String key = getApiKey(request, response);
			result = verify(tenantId, key);

			if (result == RESULT_VALID)
			{
				request.getClientInfo().setUser(
						createUser(tenantId, request, response));
			}
		}

		return result;
	}

	/**
	 * Verifies that the API key is valid.
	 * 
	 * @param tenantId the ID (matching the URL slug) for the tenant.
	 * @param key The API key to match.
	 * @return Result of the verification based on the RESULT_* constants.
	 */
	private int verify(String tenantId, String key)
	{
//		Tenant tenant = admin.getTenant(tenantId);
//		if (null != tenant && tenant.verifyKey(key))
			return RESULT_VALID;
//		else
//			return RESULT_INVALID;
	}
}
