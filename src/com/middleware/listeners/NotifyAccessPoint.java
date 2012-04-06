package com.middleware.listeners;

import java.net.InetAddress;

public interface NotifyAccessPoint {
	
	public abstract void accessPointReceivedData(byte[] data, InetAddress address, int port);

}
