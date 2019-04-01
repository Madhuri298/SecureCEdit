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
	
	public synchronized void sendToPeer(ServerThread1 s, EditOperations a) throws IOException
	{
		limit = 0;
		for (ServerThread1 c : servers) 
		{
			if(limit < 2)
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
		if(server.peerList1.size() < 2)
		{
			server.peerList1.put(serverSocket.getPort(), serverSocket.toString());	
		}
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
	        }
			catch(IOException ioe)
	        {
				ioe.printStackTrace();
	        }	
			//in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
			//out = new PrintWriter(serverSocket.getOutputStream(), true);
			/*while ((line = in.readLine()) != null)
			{
				if(!line.equals(msg))
				{
					server.sendToPeer(this, line);
					msg = line;
				}
			}*/
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
						ois = new ObjectInputStream(is);
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
