package com.middleware.listeners;

import java.net.InetAddress;

public interface CreatePermanetAccessPoint {

	public abstract void accessPointCreated(boolean success, InetAddress address, int port, int number);
	
}
