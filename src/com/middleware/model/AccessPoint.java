package com.middleware.model;

import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.middleware.listeners.AddressTable;
import com.middleware.listeners.CreatePermanetAccessPoint;
import com.middleware.listeners.NotifyAccessPoint;
import com.middleware.listeners.TempAPToNew;

public class AccessPoint extends Node implements NotifyAccessPoint{

	private boolean monitor = true;
	
	private RoutingTable table = null;
	
	private CreatePermanetAccessPoint createPermanetAccessPoint;
	private TempAPToNew tempToNew;
	private AddressTable addressTable;
	
	public AccessPoint(NodeState state, int port) throws SocketException {
		
		super(state, port);
		//Log.d("better", "access point is listening...");
		this.setNotifyAccessPoint(this);
		table = new RoutingTable();
		
		/* Start monitoring the network
		 * 
		 */
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				while(monitor)
				{
					try
					{
						checkNetworkStatus();
						Thread.sleep(Constants.MONITOR_PERIOD);
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		}).start();
	}
	

	private void addNodeToTable(String id, NodeState nodeState)
	{
		this.table.addNode(id, nodeState);
		//Log.d("better", "node added!" +id);
	}
	
	private void removeNodeFromTable(String id)
	{
		this.table.removeNode(id);
		//Log.d("better", "node removed!" +id);
	}

	public void setCreatePermanetAccessPoint(CreatePermanetAccessPoint createPermanetAccessPoint)
	{
		this.createPermanetAccessPoint = createPermanetAccessPoint;
	}
	
	public void setTempApToNewAccessPoint(TempAPToNew tempToNew)
	{
		this.tempToNew = tempToNew;
	}
	
	public void setAddressTable(AddressTable addressTable)
	{
		this.addressTable = addressTable;
	}
	
	public boolean choosePermanetAccessPoint() throws Exception
	{
		boolean change = false;
		
		Set<String> nodes = table.getRoutingTable().keySet();
		Iterator<String> iter = nodes.iterator();
		
		double max = -1;
		double compare;
		
		String node;
		String[] nodeInfo = null;
		
		double battery;
		double processor;
		double memory;
		
		while(iter.hasNext())
		{
			node = iter.next();
			NodeState nodeState = table.getRoutingTable().get(node);
			
			battery = Double.parseDouble(nodeState.getBatteryLife());
			processor = Double.parseDouble(nodeState.getProcessor());
			memory = Double.parseDouble(nodeState.getMemory());
			
			compare = evaluateNode(battery, processor, memory);
			
			if(compare > max)
			{
				max = compare;
				nodeInfo = node.split(":");
			}
		}
		
		//include the access point itself
		battery = Double.parseDouble(this.nodeState.getBatteryLife());
		processor = Double.parseDouble(this.nodeState.getProcessor());
		memory = Double.parseDouble(this.nodeState.getMemory());
		
		compare = evaluateNode(battery, processor, memory);
		
		if(compare > max)
		{
			//the temporary access point is chosen as a permanent access point
			change = false;
		}
		else
		{
			change = true;
			MiddlewarePacket packet = new MiddlewarePacket();
			byte [] header = {(byte)Constants.CREATE_PERMANENT_AP};
			packet.setPacketData(header, "create_permanet_ap".getBytes());
			this.sendData(packet, InetAddress.getByName(nodeInfo[0]), Integer.parseInt(nodeInfo[1]));
		}
		
		return change;
		
	}

	/*
	 * This function should be called 
	 * quite often to check the status of the network.
	 * Create A thread on node initialization and call this method 
	 * according to the time constant.
	 */
	private void checkNetworkStatus()
	{
		Set<String> nodes = table.getRoutingTable().keySet();
		
		if(!nodes.isEmpty())
		{
			try
			{
				Iterator<String> iter = nodes.iterator();
				
				boolean reacheable;
				InetAddress nodeAddress = null;
				String address[] = null;
				String unreachableAddresses = "";
				
				int count = 0;
				
				while(iter.hasNext())
				{
					address = iter.next().split(":");
					nodeAddress = InetAddress.getByName(address[0]);
					
					reacheable = nodeAddress.isReachable(Constants.PING_TIMEOUT);
					
					if(!reacheable)
					{
						/*
						 * If a node is not reachable 
						 * simple remove the node from the table and
						 * notify the rest of the nodes
						 */
						String remove = address[0]+":"+String.valueOf(address[1]);
						this.removeNodeFromTable(remove);
						unreachableAddresses += (remove + ",");
						count++;
					}
				}
				
				/*
				 * There is at least one node
				 * which is not available!
				 */
				if(count != 0)
				{
					MiddlewarePacket packet = new MiddlewarePacket();
					byte [] header = {(byte)Constants.DISCONNECTED};
					packet.setPacketData(header, unreachableAddresses.getBytes());
					
					broadCastCommand(packet);
				}

			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
	}

	private double evaluateNode(double battery, double processor, double memory)
	{
		//normalize data
		battery = (battery - Constants.MIN_BATTERY)/(Constants.MAX_BATTERY-Constants.MIN_BATTERY);
		processor = (processor - Constants.MIN_PROCESSOR_SPEED)/(Constants.MAX_PROCESSOR_SPEED-Constants.MIN_PROCESSOR_SPEED);
		memory = (memory - Constants.MIN_MEMORY)/(Constants.MAX_MEMORY-Constants.MIN_MEMORY);
		
		return 0.6*battery + 0.25*processor +0.15*memory;
	}
	
	
	private void broadCastCommand(MiddlewarePacket packet)
	{
		Set<String> nodes = table.getRoutingTable().keySet();
		Iterator<String> iter = nodes.iterator();
		
		String address[] = null;
		InetAddress nodeAddress = null;
		
		while(iter.hasNext())
		{
			try
			{
				address = iter.next().split(":");
				nodeAddress = InetAddress.getByName(address[0]);
				this.sendData(packet, nodeAddress, new Integer(address[1]));
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
    @Override
	public void accessPointReceivedData(byte[] data, final InetAddress address, int port) {
		
		byte[] header = new byte[1];
		header[0] = data[0];
		
		byte body[] = new byte[data.length-1];
		for(int i=0; i<data.length-1; i++)
		{
			body[i] = result[i+1];
		}
		
        String receivedHeader = new String(header);
        final String receivedBody = new String(body);
        
		if(receivedHeader.equals(String.valueOf(Constants.CONNECTION_PROFILE)))
		{
			//save the information onto the routing table
			String []nodeInfo = receivedBody.split(",");
			
			NodeState nodeState = new NodeState(nodeInfo[0], nodeInfo[2], nodeInfo[1]);
			nodeState.setStatus(Boolean.valueOf(nodeInfo[3]));
			
			String address_n = address.toString().substring(1, address.toString().length());
			
			Node node = new Node(port, address);
			String key = address_n+":"+String.valueOf(port);
			this.addNodeToTable(key, nodeState);
			
			/*
			 * Listener for the access point activity
			 */
			this.addressTable.nodeAdded(node);
			/*
			 * to notify other clients
			 */
			MiddlewarePacket packet = new MiddlewarePacket();
			byte [] packetHeader = {(byte)Constants.NEW_NODE};
			packet.setPacketData(packetHeader, key.getBytes());
			broadCastCommand(packet);

			this.number++;
			
		}
		
		else if(receivedHeader.equals(String.valueOf(Constants.LEAVING)))
		{
			String key = address.toString().replace("/", "")+":"+String.valueOf(port);
			this.removeNodeFromTable(key);
			
			MiddlewarePacket packet = new MiddlewarePacket();
			byte [] packetHeader = {(byte)Constants.DISCONNECTED};
			packet.setPacketData(packetHeader, key.getBytes());
			broadCastCommand(packet);
		}
		
		else if(receivedHeader.equals(String.valueOf(Constants.CREATE_PERMANENT_AP)))
		{
			this.createPermanetAccessPoint.accessPointCreated(true, address, port, number);
		}
		
		else if(receivedHeader.equals(String.valueOf(Constants.REQUEST_TABLE)))
		{
			if(this.table.getRoutingTable().size() > 1)
			{
				MiddlewarePacket packet = new MiddlewarePacket();
				byte [] header_p = {(byte)Constants.TABLE_DATA};
				
				/*
				 * Remove the senders address and 
				 * send the rest
				 */
				HashMap<String, NodeState> temp = this.table.getRoutingTable();
				String id = address.toString().replace("/", "");
				id = id + ":" +new Integer(port).toString();
				temp.remove(id);
				
				packet.setPacketData(header_p, temp.keySet().toString().getBytes());
				
				try
				{
					sendData(packet, address, port);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			
	
		}
		
		else if(receivedHeader.equals(String.valueOf(Constants.PERMANENT_AP_CREATED)))
		{
			//variable address is the
			//address of the new access point
			
			//wait a predefined time while access point initializes it's enviroment
			try { Thread.sleep(15000); }catch(InterruptedException e) { e.printStackTrace(); }
			
			//broadcast to the rest of the devices
			//about the new access point
			
			Set<String> nodes = table.getRoutingTable().keySet();
			
			Iterator<String> iter = nodes.iterator();
			
			try
			{
				while(iter.hasNext())
				{
					
					String node = iter.next();
					String[] nodeInfo = node.split(":");
					
					MiddlewarePacket packet = new MiddlewarePacket();
					byte [] header_p = {(byte)Constants.CONNECT_TO_NEW_AP};
					packet.setPacketData(header_p, receivedBody.getBytes());
					
					if(!(address.equals(InetAddress.getByName(nodeInfo[0]))))
					{
						InetAddress host = InetAddress.getByName(nodeInfo[0]);
						int port_p = Integer.parseInt(nodeInfo[1]);
						sendData(packet, host, port_p);
						
						Thread.sleep(1000);
					}
				}
				// now connect yourself;
				tempToNew.temporaryAccessPointConnectToNewAP(true, receivedBody);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
		}
	}
    
    public void setMonitor(boolean monitor)
    {
    	this.monitor = monitor;
    }
    
    public boolean getMonitor()
    {
    	return this.monitor;
    }
    
    public RoutingTable getRoutingTable()
    {
    	return this.table;
    }
    
	@Override
	public String toString() {
		return "AccessPoint [table=" + table + ", nodeState=" + nodeState + "]";
	}
	
}
