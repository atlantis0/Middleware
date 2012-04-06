package com.middleware.model;

public class MiddlewarePacket {
	
	private byte[] header;
	private byte[] data;
	private byte[] full_data;
	
	public MiddlewarePacket()
	{
		header = null;
		data = null;
		full_data = null;
	}
	
	
	public void setPacketData(byte [] header, byte []data)
	{
		this.header = header;
		this.data = data;
		
		full_data = new byte[header.length + data.length];
		System.arraycopy(this.header, 0, full_data, 0, header.length);
		System.arraycopy(this.data, 0, full_data, this.header.length, data.length);
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
