package DS2;
import java.net.*;
import java.util.*;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import DS.diff_match_patch;

import java.io.*;

public class Controller implements Serializable
{
	transient ServerSocket serverSocket;						
	transient ArrayList<ServerThread1> servers;	
	transient Socket cs;
	transient int limit;
	Map<Integer, String> peerList1 = new HashMap<Integer, String> ();
	String serverText;
	
	public Controller(ServerSocket serverSocket)
	{
		this.serverSocket = serverSocket;
		servers = new ArrayList<ServerThread1>();
		serverText="";
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
		diff_match_patch dmp = new diff_match_patch();
		msg = msg.replaceAll("\t", "\n");
		String a[] = msg.split("@@");
		String b[] = a[0].split(",");
		int cm = Integer.parseInt(b[0]);
		int cn = Integer.parseInt(b[1]);
		msg = msg.substring(3);
		for (ServerThread1 c : servers) 
		{
			if(c == s)
			{
				if(c.m==cm && c.n==cn)
				{
					List<diff_match_patch.Patch> patches2 = dmp.patch_fromText(msg);
					final Object[] returnVal1 = dmp.patch_apply((LinkedList<diff_match_patch.Patch>) patches2, c.serverShadow);
					c.serverShadow = returnVal1[0].toString();
					c.n++;
					c.backupShadow = c.serverShadow;
					final Object[] returnVal2 = dmp.patch_apply((LinkedList<diff_match_patch.Patch>) patches2, serverText);
					serverText = returnVal2[0].toString();
					/*LinkedList<diff_match_patch.Diff> diff = dmp.diff_main(c.serverShadow, serverText);
					LinkedList<diff_match_patch.Patch> patches = dmp.patch_make(diff);
					String patch = dmp.patch_toText(patches);
					patch = patch.replaceAll("\n", "\t");
					c.serverShadow = serverText;
					String temp = patch;
					patch = "";
					c.m++;
					patch = c.m + "," + c.n + temp;
					c.out.println(patch);*/
				}
			}
			else
			{
				List<diff_match_patch.Patch> patches2 = dmp.patch_fromText(msg);
				final Object[] returnVal1 = dmp.patch_apply((LinkedList<diff_match_patch.Patch>) patches2, c.serverShadow);
				c.serverShadow = returnVal1[0].toString();
				c.backupShadow = c.serverShadow;
				final Object[] returnVal2 = dmp.patch_apply((LinkedList<diff_match_patch.Patch>) patches2, serverText);
				serverText = returnVal2[0].toString();
				LinkedList<diff_match_patch.Diff> diff = dmp.diff_main(c.serverShadow, serverText);
				LinkedList<diff_match_patch.Patch> patches = dmp.patch_make(diff);
				String patch = dmp.patch_toText(patches);
				patch = patch.replaceAll("\n", "\t");
				c.serverShadow = serverText;
				String temp = patch;
				patch = "";
				c.m++;
				patch = c.m + "," + c.n + temp;
				c.out.println(patch);
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
	transient String name;					
	transient BufferedReader in;				
	transient PrintWriter out;	
	transient String line;
	transient String msg;
	ObjectOutputStream oos = null;
	OutputStream output = null;
	int m;
	int n;
	String serverShadow;
	String backupShadow;

	public ServerThread1(Socket serverSocket, Controller server) throws IOException 
	{
		this.serverSocket = serverSocket;
		this.server = server;
	}
	
	public void run()
	{
		try
		{
			in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
			out = new PrintWriter(serverSocket.getOutputStream(), true);
			m=0;
			n=0;
			serverShadow="";
			backupShadow="";
			while ((line = in.readLine()) != null)
			{
				server.broadcast(this, line);
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
