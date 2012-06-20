package com.collabinate.server;

import static org.junit.Assert.*;

import org.junit.Test;

public class EntityTest
{
	@Test
	public void shouldCreateValidEntity()
	{
		String userId = "testEntity";
		Entity entity = new Entity(userId);
		assertEquals(entity.getId(), userId);
	}
	
}
