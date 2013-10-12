package com.collabinate.server.webserver;

import java.util.List;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ClientInfo;
import org.restlet.security.User;
import org.restlet.security.Verifier;

import com.collabinate.server.engine.CollabinateAdmin;

/**
 * Verifier that uses tenants in a graph database to authenticate against.
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
	 * Initializes the verifier with the graph.
	 * 
	 * @param graph the database to authenticate against.
	 */
	public CollabinateVerifier(CollabinateAdmin admin)
	{
		this.admin = admin;
//		try
//		{
//			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
//		}
//		catch (NoSuchAlgorithmException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	/**
	 * Compares that two secrets are equal and not null.
	 * 
	 * @param secret1 The input secret.
	 * @param secret2 The output secret.
	 * @return True if both are equal.
	 */
	public static boolean compare(char[] secret1, char[] secret2)
	{
		boolean result = false;

		if ((secret1 != null) && (secret2 != null))
		{
			// None is null
			if (secret1.length == secret2.length)
			{
				boolean equals = true;

				for (int i = 0; (i < secret1.length) && equals; i++)
				{
					equals = (secret1[i] == secret2[i]);
				}

				result = equals;
			}
		}

		return result;
	}

	/**
	 * Called back to create a new user when valid credentials are provided.
	 * 
	 * @param identifier The user identifier.
	 * @param request The request handled.
	 * @param response The response handled.
	 * @return The {@link User} instance created.
	 */
	protected User createUser(String identifier, Request request,
			Response response)
	{
		return createUser(identifier);
	}

	/**
	 * Called back to create a new user when valid credentials are provided.
	 * 
	 * @param identifier The user identifier.
	 * @return The {@link User} instance created.
	 */
	protected User createUser(String identifier)
	{
		return new User(identifier);
	}
	
	private boolean isValidApiVersion(String version)
	{
		//TODO Make this better.
		return version.equals("1");
	}

	/**
	 * Returns the tenant identified in the URI.
	 * 
	 * @param request The request to inspect.
	 * @param response The response to inspect.
	 * @return The tenant slug from the URI.
	 */
	protected String getTenant(Request request, Response response)
	{
		List<String> segments = request.getOriginalRef().getSegments();
		if (segments.size() > 1 && isValidApiVersion(segments.get(0)))
			return segments.get(1);
		else
			return null;
	}

	/**
	 * Returns the user identifier.
	 * 
	 * @param request The request to inspect.
	 * @param response The response to inspect.
	 * @return The user identifier.
	 */
	protected String getIdentifier(Request request, Response response)
	{
		return request.getChallengeResponse().getIdentifier();
	}

	/**
	 * Returns the secret provided by the user.
	 * 
	 * @param request The request to inspect.
	 * @param response The response to inspect.
	 * @return The secret provided by the user.
	 */
	protected char[] getSecret(Request request, Response response)
	{
		return request.getChallengeResponse().getSecret();
	}

	/**
	 * Verifies that the proposed secret is correct for the specified request.
	 * By default, it compares the inputSecret of the request's authentication
	 * response with the one obtain by the {@link ChallengeResponse#getSecret()}
	 * method and sets the {@link org.restlet.security.User} instance of the
	 * request's {@link ClientInfo} if successful.
	 * 
	 * @param request The request to inspect.
	 * @param response The response to inspect.
	 * @return Result of the verification based on the RESULT_* constants.
	 */
	public int verify(Request request, Response response)
	{
		int result = RESULT_VALID;

		if (request.getChallengeResponse() == null)
		{
			result = RESULT_MISSING;
		}
		else
		{
			String tenant = getTenant(request, response);
			String identifier = getIdentifier(request, response);
			char[] secret = getSecret(request, response);
			result = verify(tenant, identifier, secret);

			if (result == RESULT_VALID)
			{
				request.getClientInfo().setUser(
						createUser(identifier, request, response));
			}
		}

		return result;
	}

	/**
	 * Verifies that the identifier/secret couple is valid. It throws an
	 * IllegalArgumentException in case the identifier is either null or does
	 * not identify a user.
	 * 
	 * @param tenantId the URL slug for the tenant.
	 * @param identifier The user identifier to match.
	 * @param secret The provided secret to verify.
	 * @return Result of the verification based on the RESULT_* constants.
	 */
	public int verify(String tenantId, String identifier, char[] secret)
	{
//		Vertex tenant = graph.getVertex("_TENANT-" + tenantId);
//		HashMap map = tenant.getProperty("tokens");
//		char[] storedSecret = (char[])map.get(identifier);
//		if (compare(storedSecret, hash(secret, identifier)))
//		{
			return RESULT_VALID;
//		}
//		
//		return RESULT_INVALID;
	}
	
//	private char[] hash(char[] secret, String salt)
//	{
//		
//	}
}
