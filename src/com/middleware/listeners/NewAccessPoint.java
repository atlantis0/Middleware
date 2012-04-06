package com.middleware.listeners;

public interface NewAccessPoint {

	public abstract void newAccessPointCreated(boolean success, String username, String password);
}
