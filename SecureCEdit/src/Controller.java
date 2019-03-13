import java.net.*;
import java.util.*;
import java.io.*;

public class Controller implements Serializable
{
	transient ServerSocket serverSocket;						
	transient ArrayList<ServerThread1> servers;	
	transient Socket cs;
	transient int limit;
	Map<Integer, String> peerList1 = new HashMap<Integer, String> ();
	
	public Controller(ServerSocket serverSocket)
	{
		this.serverSocket = serverSocket;
		servers = new ArrayList<ServerThread1>();
	}
	public void getConnections() throws IOException
	{
		try
		{
			while(true)
			{
				cs = serverSocket.accept();
				ServerThread1 s = new ServerThread1(cs, this);
				s.setDaemon(true);
				s.start();
				addThread(s);
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public synchronized void addThread(ServerThread1 s)
	{
		servers.add(s);
	}

	public synchronized void removeThread(ServerThread1 s)
	{
		servers.remove(s);
	}

	public synchronized void broadcast(ServerThread1 s, String msg)
	{
		for (ServerThread1 c : servers) 
		{
			if (c != s)
			{
					c.send(msg);
			}
		}
		
	}
	
	public synchronized void sendToPeer(ServerThread1 s, String msg)
	{
		limit = 0;
		for (ServerThread1 c : servers) 
		{
			if(limit < 2)
			{
				if (c != s)
				{
					c.send(msg);
				}
			}
			else
			{
				break;
			}
			limit++;
		}
		
	}

	public static void main(String[] args) throws Exception 
	{
		System.out.println("Waiting for peers");
		new Controller(new ServerSocket(4200)).getConnections();
	}
}

class ServerThread1 extends Thread implements Serializable
{
	transient Socket serverSocket;					
	transient Controller server;				
	transient String name;					
	transient BufferedReader in;				
	transient PrintWriter out;	
	transient String line;
	transient String msg;
	ObjectOutputStream oos = null;
	OutputStream output = null;

	public ServerThread1(Socket serverSocket, Controller server) throws IOException 
	{
		this.serverSocket = serverSocket;
		this.server = server;
		if(server.peerList1.size() < 2)
		{
			server.peerList1.put(serverSocket.getPort(), serverSocket.toString());	
		}
		System.out.println("Number of peers in subnet 1: " + server.peerList1.size());
		System.out.println("The peers in subnet 1 are: " + server.peerList1);
	}

	public void run()
	{
		try
		{
			try
	        {
				output = serverSocket.getOutputStream();
				oos = new ObjectOutputStream(output);
	            oos.writeObject(server.peerList1);
	            System.out.println("Peer connection list sent to all peers");
	        }
			catch(IOException ioe)
	        {
				ioe.printStackTrace();
	        }	
			in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
			out = new PrintWriter(serverSocket.getOutputStream(), true);
			name = in.readLine();
			System.out.println(name + " connected");
			out.println("Welcome "+name);
			server.broadcast(this, name + " connected");
			while ((line = in.readLine()) != null)
			{
				msg = name + ":" + line;
				System.out.println(msg);
				server.sendToPeer(this, msg);
			}
			System.out.println(name + " hung up");
			server.broadcast(this, name + " disconnected");
			server.removeThread(this);
			out.close();
			in.close();
			oos.close();
	        output.close();
			serverSocket.close();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	public void send(String msg)
	{
		out.println(msg);
	}
}
