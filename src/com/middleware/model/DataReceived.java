package com.middleware.model;

public interface DataReceived {
	
	public abstract void nodeReceivedData(byte[] data);
	/*
	 * If status is true, node is added to the network
	 * else a node is false node is removed from the network
	 */
	public abstract void tableStatus(boolean status, Node node);

}

