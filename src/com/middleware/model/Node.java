package com.middleware.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

import com.middleware.listeners.CreatePermanetAccessPoint;
import com.middleware.listeners.NewAccessPoint;
import com.middleware.listeners.NotifyAccessPoint;

public class Node {

	private static final int BUFSIZE = 100000;
	
	private InetAddress address;
	private int port = 0;
	
	protected int number = 0;
	
	protected NodeState nodeState;
	protected ServerSocket serverSocket;
	protected Socket inSocket, outSocket;
	protected byte[] buffer;
	protected byte [] result;
	
	protected DataReceived dataReceived;
	private NotifyAccessPoint notifyAccessPoint;
	private CreatePermanetAccessPoint createPermanetAccessPoint;
	private NewAccessPoint newAccessPoint;
	
	protected Thread receiver;
	
	boolean connected = false;
	
	public Node(int port) throws SocketException, IOException
	{
		this.port = port;
		setUpNode();
	}
	
	public Node(int port, InetAddress address)
	{
		this.port = port;
		this.address = address;
	}

	public Node(NodeState nodeState, int port) throws SocketException, IOException
	{
		this.port = port;
		this.nodeState = nodeState;
		setUpNode();
	}
	
	private void setUpNode() throws SocketException, IOException
	{
		connected = true;
		serverSocket = new ServerSocket(port);
		
		//set up receiver
		receiver = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try
	        	{
	        		do
	        		{
	        			String line = null;
	        			StringBuilder builder = new StringBuilder();
	        		
	        			buffer = new byte[BUFSIZE];
	        			inSocket = serverSocket.accept();
	        			
	        			SocketAddress clientAddress = inSocket.getRemoteSocketAddress();
	        			System.out.println("Handling client at " + clientAddress);
	        			
	        			InputStream in = inSocket.getInputStream();

	        			BufferedReader inReader = new BufferedReader(new InputStreamReader(in));
	        			
	        			while((line = inReader.readLine()) != null)
	        			{
	        				builder.append(line);
	        			}
	        			
	        			result = builder.toString().getBytes();
	        			char [] header = { builder.toString().charAt(0) };
	        			
	        			String receivedHeader = new String(header);
	        			
	        			inSocket.close();

	        			InetSocketAddress inPacket = (InetSocketAddress)clientAddress;
	        			
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
	        		//serverSocket.close();
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

	public void stop() throws IOException
	{
		serverSocket.close();
	}
	
	public void sendData(MiddlewarePacket packet, Node node) throws IOException
	{
		if(connected)
		{
			outSocket = new Socket(node.getAddress(), node.getPort()); 
			OutputStream out = outSocket.getOutputStream();
			out.write(packet.getMiddleWareData());
		}
	}
	
	public void sendData(MiddlewarePacket packet, InetAddress host, int port) throws IOException
	{
		if(connected)
		{
			outSocket = new Socket(host, port); 
			OutputStream out = outSocket.getOutputStream();
			out.write(packet.getMiddleWareData());
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
