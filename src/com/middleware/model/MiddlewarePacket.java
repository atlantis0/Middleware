package com.middleware.model;

public class MiddlewarePacket {
	
	private final byte[] newLine = "\n".getBytes();
	private byte[] header;
	private byte[] data;
	private byte[] full_data;
	
	private int port;
	
	public MiddlewarePacket(int port)
	{
		header = null;
		data = null;
		full_data = null;
		this.port = port;
	}
	
	
	public void setPacketData(byte [] header, byte []data)
	{
		this.header = header;	
		String prePort = new Integer(port).toString() + ";";
		byte data_prefix[] = prePort.getBytes();
		
		this.data = data;

		full_data = new byte[header.length + data_prefix.length + data.length + newLine.length];
		
		System.arraycopy(this.header, 0, full_data, 0, header.length);
		System.arraycopy(data_prefix, 0, full_data, header.length, data_prefix.length);
		System.arraycopy(this.data, 0, full_data, header.length + data_prefix.length, data.length);
		System.arraycopy(this.newLine, 0, full_data, header.length + data_prefix.length + data.length, newLine.length);
	}
	
	
	public int getPort()
	{
		return this.port;
	}
	
	public byte[] getHeader()
	{
		return this.header;
	}
	
	public byte[] getBareData()
	{
		return this.data;
	}
	
	public byte[] getMiddleWareData()
	{
		return this.full_data;
	}


}
