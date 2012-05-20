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

	private static final int BUFSIZE = 5000000;
	
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
	        			char [] header = new char[1];
	        			buffer = new byte[BUFSIZE];
	        			inSocket = serverSocket.accept();
	        			
	        			SocketAddress clientAddress = inSocket.getRemoteSocketAddress();
	        			
	        			InputStream in = inSocket.getInputStream();

	        			BufferedReader inReader = new BufferedReader(new InputStreamReader(in, "ISO-8859-1"));
	        			
	        			int end= 0;
	        			int count = 0;

	        			while((end = inReader.read()) != -1)
	        			{
	        				buffer[count] = (byte)end;
	        				count++;
	        			}
	        			
	        			header[0] = (char)buffer[0];
	        			byte port_byte[] = new byte[4];
	        			
	        			for(int i=0; i<4; i++)
	        			{
	        				int data_at = i+1;
	        				port_byte[i] = buffer[data_at+1];
	        			}
	        			
	        			String port = new String(port_byte, "ISO-8859-1");
	        			int inPacketPort = new Integer(port);
	        			
	        			result = new byte[count-6];
	        			result[0] = buffer[0];
	        			System.arraycopy(buffer, 7, result, 1, result.length -1);
	        		
	        			String receivedHeader = new String(header);
	        			
	        			InetSocketAddress fromClient = (InetSocketAddress)clientAddress;
	        			String full_address = fromClient.getAddress().toString();
	        			
	        			int t = full_address.indexOf("/");
	        			full_address = full_address.subSequence(t+1, full_address.length()).toString();
	        			
	        			InetAddress inPacket = InetAddress.getAllByName(full_address)[0];
	        			
	        			inSocket.close();
	        			
	        			
	    				if(receivedHeader.equals(String.valueOf(Constants.CONNECTION_PROFILE)))
	    				{
	    					notifyAccessPoint.accessPointReceivedData(result, inPacket, inPacketPort);
	    				}
	    				if(receivedHeader.equals(String.valueOf(Constants.LEAVING)))
	    				{
	    					notifyAccessPoint.accessPointReceivedData(result, inPacket, inPacketPort);
	    				}
	    				else if(receivedHeader.equals(String.valueOf(Constants.CREATE_PERMANENT_AP)))
	    				{
	    					createPermanetAccessPoint.accessPointCreated(true, address, inPacketPort, number);
	    				}
	    				
	    				else if(receivedHeader.equals(String.valueOf(Constants.PERMANENT_AP_CREATED)))
	    				{
	    					//inPacket.getAddress is the
	    					//address of the new access point
	    					notifyAccessPoint.accessPointReceivedData(result, inPacket, inPacketPort);
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
	    					notifyAccessPoint.accessPointReceivedData(result, inPacket, inPacketPort);
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
	
	public synchronized void sendData(MiddlewarePacket packet, Node node) throws IOException
	{
		if(connected)
		{
			outSocket = new Socket(node.getAddress(), node.getPort()); 
			OutputStream out = outSocket.getOutputStream();
			out.write(packet.getMiddleWareData());
			out.close();
			outSocket.close();
		}
	}
	
	public synchronized void sendData(MiddlewarePacket packet, InetAddress host, int port) throws IOException
	{
		if(connected)
		{
			outSocket = new Socket(host, port); 
			OutputStream out = outSocket.getOutputStream();
			out.write(packet.getMiddleWareData());
			out.close();
			outSocket.close();
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
