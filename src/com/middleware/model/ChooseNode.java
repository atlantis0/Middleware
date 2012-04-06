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
		Object keys[] = routingTable.routingTable.keySet().toArray();
		return (String)keys[0];
	}
}
