package com.collabinate.server;

public class MemoryCollabinateServerTest extends CollabinateServerTest
{
	@Override
	CollabinateServer createServer()
	{
		return new MemoryCollabinateServer();
	}
}
