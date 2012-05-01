package com.middleware.model;


public class Constants {
	
	public static final char CONNECTION_PROFILE = 'A';
	public static final char CREATE_PERMANENT_AP = 'B';
	public static final char PERMANENT_AP_CREATED = 'C';
	public static final char CONNECT_TO_NEW_AP = 'E';
	public static final char REQUEST_TABLE = 'F';
	public static final char TABLE_DATA = 'G';
	public static final char LEAVING = 'H';
	public static final char DISCONNECTED = 'I';
	public static final char NEW_NODE = 'J';
	public static final char DATA = 'D';

	
	public static final int TEMP_AP_PORT = 4444;
	public static final int PERMANET_AP_PORT = 3333;
	
	
	public static final int MIN_BATTERY = 0;
	public static final int MAX_BATTERY = 100;
	public static final float MIN_PROCESSOR_SPEED = 0.256f;
	public static final float MAX_PROCESSOR_SPEED = 3f;
	public static final float MIN_MEMORY = 500000000;
	public static final long MAX_MEMORY = 8000000000l;
	
	
	public static final int PING_TIMEOUT = 2000;
	public static final int MONITOR_PERIOD = 6000;
	//this is partly provided be the SDK it self
	//but applications may need to define this 
	//or modify it according to their specific use
	public static final long NETWORK_TERMINATE_TIMEOUT = 900000; //around 15 minutes
	
}
