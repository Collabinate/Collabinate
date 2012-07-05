package com.collabinate.server;

public class DefaultServerTest extends CollabinateServerTest
{
	@Override
	CollabinateServer createServer()
	{
		return new DefaultServer();
	}
}
