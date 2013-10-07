package com.collabinate.server.engine;

import com.collabinate.server.Tenant;

/**
 * The interface for administrative functions against a Collabinate server.
 * 
 * @author mafuba
 *
 */
public interface CollabinateAdmin
{
	public Tenant createTenant(String tenantId);
}
