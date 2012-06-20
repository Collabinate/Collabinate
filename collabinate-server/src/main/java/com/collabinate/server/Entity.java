package com.collabinate.server;

public class Entity
{
	private String _id;
	
	public String getId()
	{
		return _id;
	}
	
	public void setId(String id)
	{
		_id = id;
	}
	
	public Entity(String id)
	{
		setId(id);
	}
}
