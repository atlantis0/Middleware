 package com.middleware.model;

import java.util.HashMap;

public class RoutingTable {
	
	public HashMap<String, NodeState> routingTable = null;

	public RoutingTable()
	{
		routingTable = new HashMap<String, NodeState>();
	}
	
	public void addNode(String id ,NodeState nodeState)
	{
		routingTable.put(id, nodeState);
	}
	
	public void removeNode(String id)
	{
		routingTable.remove(id);
	}
	
	@Override
	public String toString() {
		return "RoutingTable [routingTable=" + routingTable + "]";
	}
}
