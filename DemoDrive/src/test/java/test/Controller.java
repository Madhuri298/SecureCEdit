package test;
import java.net.*;
import java.util.*;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.io.*;

public class Controller implements Serializable
{
	transient ServerSocket serverSocket;						
	transient ArrayList<ServerThread1> servers;	
	transient Socket cs;
	transient int limit;
	ObjectOutputStream eoos = null;
	OutputStream eos = null;
	Map<Integer, String> peerList1 = new HashMap<Integer, String> ();
	
	public Controller(ServerSocket serverSocket) throws IOException
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

	//function to send messages to all peers
	public synchronized void broadcast(ServerThread1 s, String msg)
	{
		for (ServerThread1 c : servers) 
		{
			if (c != s)
			{
					c.out.print(msg);
			}
		}
		
	}
	
	//function to send messages to specific peers
	public synchronized void sendToPeer(ServerThread1 s, EditOperations a) throws IOException
	{
		for (ServerThread1 c : servers) 
		{
				if (c != s)
				{
					//c.out.println(msg);
					eos = cs.getOutputStream();
					eoos = new ObjectOutputStream(eos);
					ArrayList<String> at = new ArrayList<String>();
					at.add(0, a.msg);
					at.add(1, String.valueOf(a.start));
					at.add(2, String.valueOf(a.end));
					at.add(3, String.valueOf(a.msgType));
		            eoos.writeObject(at);
				}
		}
		
	}
	
	public static void main(String[] args) throws Exception 
	{
		System.out.println("Waiting for peers");
		new Controller(new ServerSocket(4100)).getConnections();
	}
}

class ServerThread1 extends Thread implements Serializable
{
	transient Socket serverSocket;					
	transient Controller server;								
	transient BufferedReader in;				
	transient PrintWriter out;	
	transient String line;
	transient String msg;
	ObjectOutputStream oos = null;
	OutputStream output = null;
	ObjectInputStream ois = null;
	InputStream is = null;
	EditOperations a;
	
	public ServerThread1(Socket serverSocket, Controller server) throws IOException 
	{
		this.serverSocket = serverSocket;
		this.server = server;
	}
	
	public void run()
	{
		try
		{
			is = serverSocket.getInputStream();
			ois = new ObjectInputStream(is);
			EditOperations b = null;
			try
			{
				while((a = (EditOperations) ois.readObject()) != null)
				{
					if(!a.equals(b))
					{
						server.sendToPeer(this, a);
						//ois = new ObjectInputStream(is);
						b = a;
					}
				}
			} 
			catch (ClassNotFoundException e)
			{
				e.printStackTrace();
			}
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
		
}
