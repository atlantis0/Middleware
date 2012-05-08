package com.middleware.model;

public class ChooseNode {

	private RoutingTable routingTable = null;
	
	public ChooseNode(RoutingTable routingTable)
	{
		this.routingTable = routingTable;
	}
	
	public String evaluate()
	{
		//the algorithm
		Object keys[] = routingTable.getTable().keySet().toArray();
		return (String)keys[0];
	}
}
