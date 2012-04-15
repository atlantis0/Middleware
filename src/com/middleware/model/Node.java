package com.middleware.model;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import com.middleware.listeners.CreatePermanetAccessPoint;
import com.middleware.listeners.NewAccessPoint;
import com.middleware.listeners.NotifyAccessPoint;

public class Node {

	private InetAddress address;
	private int port = 0;
	
	protected int number = 0;
	
	protected NodeState nodeState;
	protected DatagramSocket datagramSocket;
	protected DatagramPacket inPacket, outPacket;
	protected byte[] buffer;
	protected byte [] result;
	
	protected DataReceived dataReceived;
	private NotifyAccessPoint notifyAccessPoint;
	private CreatePermanetAccessPoint createPermanetAccessPoint;
	private NewAccessPoint newAccessPoint;
	
	protected Thread receiver;
	
	boolean connected = false;
	
	public Node(int port) throws SocketException
	{
		this.port = port;
		setUpNode();
	}
	
	public Node(int port, InetAddress address)
	{
		this.port = port;
		this.address = address;
	}

	public Node(NodeState nodeState, int port) throws SocketException
	{
		this.port = port;
		this.nodeState = nodeState;
		setUpNode();
	}
	
	private void setUpNode() throws SocketException
	{
		connected = true;
		datagramSocket = new DatagramSocket(port);
		
		//set up receiver
		receiver = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try
	        	{
	        		do
	        		{
	        			//52k
	        			buffer = new byte[52000];
	    				inPacket = new DatagramPacket(buffer, buffer.length);
	    				datagramSocket.receive(inPacket);
	    				
	    				result = new byte [inPacket.getLength()];
	    				System.arraycopy(inPacket.getData() , 0 , result , 0 , inPacket.getLength());
	    				
	    				byte[] header = new byte[1];
	    				header[0] = result[0];
	    				
	    		        String receivedHeader = new String(header);
	    		        
	    				if(receivedHeader.equals(String.valueOf(Constants.CONNECTION_PROFILE)))
	    				{
	    					notifyAccessPoint.accessPointReceivedData(result, inPacket.getAddress(), inPacket.getPort());
	    				}
	    				if(receivedHeader.equals(String.valueOf(Constants.LEAVING)))
	    				{
	    					notifyAccessPoint.accessPointReceivedData(result, inPacket.getAddress(), inPacket.getPort());
	    				}
	    				else if(receivedHeader.equals(String.valueOf(Constants.CREATE_PERMANENT_AP)))
	    				{
	    					createPermanetAccessPoint.accessPointCreated(true, address, port, number);
	    				}
	    				
	    				else if(receivedHeader.equals(String.valueOf(Constants.PERMANENT_AP_CREATED)))
	    				{
	    					//inPacket.getAddress is the
	    					//address of the new access point
	    					notifyAccessPoint.accessPointReceivedData(result, inPacket.getAddress(), inPacket.getPort());
	    					//broadcast to the rest of the devices
	    					//about the new access point
	    				}
	    				else if(receivedHeader.equals(String.valueOf(Constants.CONNECT_TO_NEW_AP)))
	    				{
	    					//new access point is created 
	    					//connect to it
	    					
	    					byte body[] = new byte[result.length-1];
	    					for(int i=0; i<result.length-1; i++)
	    					{
	    						body[i] = result[i+1];
	    					}
	    					String receivedBody = new String(body);
	    					
	    					String[] cred = receivedBody.split(":");
	    					String username = cred[0];
	    					String password = cred[1];
	    					newAccessPoint.newAccessPointCreated(true, username, password);
	    				}
	    				else if(receivedHeader.equals(String.valueOf(Constants.REQUEST_TABLE)))
	    				{
	    					notifyAccessPoint.accessPointReceivedData(result, inPacket.getAddress(), inPacket.getPort());
	    				}
	    				
    					dataReceived.nodeReceivedData(result);
	    				
	        		}
	        		while(connected);
	        			
	        	}
	        	catch(IOException ioEx)
	        	{
	        		ioEx.printStackTrace();
	        		connected = false;
	        		datagramSocket.close();
	        	}
			}
		});
	}

	public void setDataReceived(DataReceived dataReceived)
	{
		this.dataReceived = dataReceived;
	}
	
	public void setNotifyAccessPoint(NotifyAccessPoint notifyAccessPoint)
	{
		this.notifyAccessPoint = notifyAccessPoint;
	}
	
	public void setCreatePermanetAccessPoint(CreatePermanetAccessPoint createPermanetAccessPoint)
	{
		this.createPermanetAccessPoint = createPermanetAccessPoint;
	}
	
	public void setNewAccessPointCreated(NewAccessPoint newAccessPoint)
	{
		this.newAccessPoint = newAccessPoint;
	}
	
	public void startReceiverThread()
	{
		receiver.start();
	}
	
	public void stopReceiverThread()
	{
		receiver.stop();
	}
	
	public void sendData(MiddlewarePacket packet, Node node) throws IOException
	{
		if(connected)
		{
			outPacket = new DatagramPacket(packet.getMiddleWareData(), packet.getMiddleWareData().length, node.getAddress(), node.getPort()); 
			datagramSocket.send(outPacket);
		}
	}
	
	public void sendData(MiddlewarePacket packet, InetAddress host, int port) throws IOException
	{
		if(connected)
		{
			outPacket = new DatagramPacket(packet.getMiddleWareData(), packet.getMiddleWareData().length, host, port); 
			datagramSocket.send(outPacket);
		}
	}
	
	public void setNodeState(NodeState nodeState)
	{
		this.nodeState = nodeState;
	}
	
	public void setAddress(InetAddress address)
	{
		this.address = address;
	}
	
	public NodeState getNodeState()
	{
		return this.nodeState;
	}
	
	public InetAddress getAddress()
	{
		return this.address;
	}
	
	public int getPort()
	{
		return this.port;
	}
	
	@Override
	public String toString() {
		return "Node [address=" + address + ", port=" + port + ", nodeState="
				+ nodeState + ", connected=" + connected + "]";
	}

}
