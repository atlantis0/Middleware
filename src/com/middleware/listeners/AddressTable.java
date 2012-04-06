package com.middleware.listeners;

import com.middleware.model.Node;

public interface AddressTable {
	
	public abstract void nodeAdded(Node node);
	public abstract void nodeRemoved(Node node);

}
