package com.middleware.model;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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

	protected RoutingTable table = null;
	
	private CreatePermanetAccessPoint createPermanetAccessPoint;
	private TempAPToNew tempToNew;
	private AddressTable addressTable;
	
	
	public AccessPoint(NodeState state, int port) throws SocketException {
		
		super(state, port);
		//Log.d("better", "access point is listening...");
		this.setNotifyAccessPoint(this);
		table = new RoutingTable();
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
		
		Set<String> nodes = table.routingTable.keySet();
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
			NodeState nodeState = table.routingTable.get(node);
			
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


	private double evaluateNode(double battery, double processor, double memory)
	{
		return 0.6*(battery/Constants.MAX_BATTERY) + 0.25*(processor/Constants.MAX_PROCESSOR_SPEED) +0.15*(memory/Constants.MAX_MEMORY);
	}
	
	public void checkNetworkStatus()
	{
		Set<String> nodes = table.routingTable.keySet();
		
		Iterator<String> iter = nodes.iterator();
		
		try
		{
			while(iter.hasNext())
			{
				String s = null;
				
				Runtime runtime = Runtime.getRuntime();
			    Process proc = runtime.exec("ping -c 1   " + iter.next().split(":")[0]);
			    
			    proc.waitFor();
			    BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			    

			    // read the output from the command
			    //Log.d("better", "Here is the standard output of the command");
			    while ((s = stdInput.readLine()) != null)
			    {
			      //Log.d("better", s);
			    }
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
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
			this.addressTable.nodeAdded(node);
			this.addNodeToTable(address_n+":"+String.valueOf(port), nodeState);
			this.number++;
			
		}
		
		else if(receivedHeader.equals(String.valueOf(Constants.CREATE_PERMANENT_AP)))
		{
			this.createPermanetAccessPoint.accessPointCreated(true, address, port, number);
		}
		
		else if(receivedHeader.equals(String.valueOf(Constants.REQUEST_TABLE)))
		{
			if(this.table.routingTable.size() > 1)
			{
				MiddlewarePacket packet = new MiddlewarePacket();
				byte [] header_p = {(byte)Constants.TABLE_DATA};
				
				HashMap<String, NodeState> temp = this.table.routingTable;
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
			
			Set<String> nodes = table.routingTable.keySet();
			
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
    
	@Override
	public String toString() {
		return "AccessPoint [table=" + table + ", nodeState=" + nodeState + "]";
	}
	
}
