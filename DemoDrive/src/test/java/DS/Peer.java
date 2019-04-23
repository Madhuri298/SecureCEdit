package DS;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

public class Peer implements Serializable
{				
	private Socket clientSocket;
	ClientThread clients;
	DiffThread clientDiff;
	String shadow;
	DemoProj d;

	public Peer(Socket clientSocket) throws IOException 
	{		
		this.clientSocket = clientSocket;
		d = new DemoProj(this);
		clientDiff = new DiffThread(clientSocket, d, this);
		clients = new ClientThread(clientSocket, d, this);
		//clients.setDaemon(true);
		clientDiff.start();
		clients.start();
	}
	
	public static void main(String[] args) throws IOException 
	{
		new Peer(new Socket("localhost", 4100));
	}
	
}

class DiffThread extends Thread implements Serializable
{
	transient Socket clientSocket;			
	transient Peer peer;			
	transient PrintWriter out;		
	transient ObjectInputStream ois = null;
	transient InputStream inp = null;
	Map<Integer, String> peerList1 = new HashMap<Integer, String> ();
	transient String line;
	DemoProj d ;
	
	public DiffThread(Socket clientSocket, DemoProj d, Peer peer) throws IOException
	{
		this.clientSocket = clientSocket;
		this.d = d;
		this.peer = peer;
		out = new PrintWriter(clientSocket.getOutputStream(), true);
	}
	
	/*public void send(String msg) 
	{
		int port = clientSocket.getLocalPort();
		if(peerList1.containsKey(port))
		{
			this.out.println(msg);
		}
		else
		{
			
		}
	}*/
	
	public void send(String msg) 
	{
		this.out.println(msg);	
	}
	
	/*public void createHashmap()
	{
		try
	    {
			inp = clientSocket.getInputStream();
	        ois = new ObjectInputStream(inp);
	        peerList1 = (HashMap) ois.readObject();	  
	    }
		catch(IOException ioe)
	    {
			ioe.printStackTrace();
	        return;
	    }
		catch(ClassNotFoundException c)
	    {
	        System.out.println("Class not found");
	        c.printStackTrace();
	        return;
	    }
	}*/

	public void run() 
	{
		try
		{
			//createHashmap();
			peer.shadow = "";
			TimerTask task = new TimerTask()
			{
		        public void run()
		        {
		        	String msg = d.t.getText();
	        		diff_match_patch dmp = new diff_match_patch();
					LinkedList<diff_match_patch.Diff> diff = dmp.diff_main(peer.shadow, msg);
					LinkedList<diff_match_patch.Patch> patches = dmp.patch_make(diff);
					String patch = dmp.patch_toText(patches);
					patch = patch.replaceAll("\n", "\t");
					if(!patch.equals("") && !msg.equals(peer.shadow))
					{
						peer.shadow = msg;
						send(patch);
					}
		        }
		    };
		    Timer timer = new Timer("Timer");
		    long delay = 1000L;
		    long period = 10000L;
		    timer.scheduleAtFixedRate(task, delay, period);
		}
		finally
		{
		}
	}
}

class ClientThread extends Thread implements Serializable
{
	transient Socket clientSocket;			
	transient Peer peer;		
	transient BufferedReader in;
	transient String line;
	DemoProj d ;
	
	public ClientThread(Socket clientSocket, DemoProj d, Peer peer) throws IOException
	{
		this.clientSocket = clientSocket;
		this.d = d;
		this.peer = peer;
		in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	}
	
	public void run() 
	{
		try
		{
			while((line = in.readLine()) != null)
			{	
				diff_match_patch dmp = new diff_match_patch();
				line = line.replaceAll("\t", "\n");
				List<diff_match_patch.Patch> patches2 = dmp.patch_fromText(line);
				String msg = d.t.getText();
				final Object[] returnVal1 = dmp.patch_apply((LinkedList<diff_match_patch.Patch>) patches2, peer.shadow);
				peer.shadow = returnVal1[0].toString();
				final Object[] returnVal2 = dmp.patch_apply((LinkedList<diff_match_patch.Patch>) patches2, msg);
	        	if(msg != returnVal2[0].toString())
	        	{
	        		d.t.setText(returnVal2[0].toString());
	        	}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
		}
		try
		{
			in.close();
			clientSocket.close();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
}

