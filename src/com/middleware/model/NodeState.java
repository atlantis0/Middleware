package com.middleware.model;

public class NodeState {

	private String memory = null;
	private String batteryLife = null;
	private String processor = null;
	
	private boolean status = false;
	private boolean canCreate = false;
	
	
	public NodeState(String memory, String processor)
	{
		this.memory = memory;
		this.processor = processor;
	}
	
	public NodeState(String memory, String processor, String batteryLife)
	{
		this.memory = memory;
		this.processor = processor;
		this.batteryLife = batteryLife;
	}
	
	public void setBatteryLife(String batteryLife)
	{
		this.batteryLife = batteryLife;
	}
	
	public String getBatteryLife()
	{
		return this.batteryLife;
	}
	
	public String getMemory()
	{
		return this.memory;
	}
	
	public String getProcessor()
	{
		return this.processor;
	}
	
	public void setStatus(boolean status)
	{
		this.status = status;
	}
	
	public boolean getStatus()
	{
		return this.status;
	}
	
	public void setCanCreate(boolean canCreate)
	{
		this.canCreate = canCreate;
	}
	
	public boolean canCreate()
	{
		return this.canCreate;
	}
	
	@Override
	public String toString()
	{
		Boolean y = this.status;
		Boolean able = this.canCreate;
		return this.memory + "," + this.batteryLife + "," + this.processor + "," + y.toString() + "," + able.toString();
	}
	
}
