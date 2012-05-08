 package com.middleware.model;

import java.util.HashMap;

public class RoutingTable {
	
	private HashMap<String, NodeState> routingTable = null;

	public RoutingTable()
	{
		routingTable = new HashMap<String, NodeState>();
	}
	
	public synchronized HashMap<String, NodeState> getTable()
	{
		return routingTable;
	}
	
	public synchronized void addNode(String id ,NodeState nodeState)
	{
		routingTable.put(id, nodeState);
	}
	
	public synchronized void removeNode(String id)
	{
		routingTable.remove(id);
	}
	
	@Override
	public String toString() {
		return "RoutingTable [routingTable=" + routingTable + "]";
	}
}
