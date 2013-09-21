package com.collabinate.server;

import org.restlet.security.SecretVerifier;
import org.restlet.security.Verifier;

import com.tinkerpop.blueprints.Graph;

/**
 * Verifier that uses tenants in a graph database to authenticate against.
 * 
 * @author mafuba
 *
 */
public class GraphVerifier extends SecretVerifier implements Verifier
{
	private Graph graph;
	
	/**
	 * Initializes the verifier with the graph.
	 * 
	 * @param graph the database to authenticate against.
	 */
	public GraphVerifier(Graph graph)
	{
		this.graph = graph;
	}
	
	@Override
	public int verify(String identifier, char[] secret)
	{
		return RESULT_VALID;
	}

}
