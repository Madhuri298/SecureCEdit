package DS3;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;



public class Peer implements Serializable
{				
	private Socket clientSocket;
	ClientThread clients;
	DiffThread clientDiff;
	String shadow = "";
	String backup = "";
	DemoProj d;
	int m=0;
	int n=0;

	public Peer(Socket clientSocket) throws IOException 
	{		
		this.clientSocket = clientSocket;
		d = new DemoProj(this);
		clientDiff = new DiffThread(clientSocket, d, this);
		clients = new ClientThread(clientSocket, d, this);
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
	transient String line;
	String msg;
	DemoProj d ;
	
	public DiffThread(Socket clientSocket, DemoProj d, Peer peer) throws IOException
	{
		this.clientSocket = clientSocket;
		this.d = d;
		this.peer = peer;
		out = new PrintWriter(clientSocket.getOutputStream(), true);
	}
	
	
	public void send(String msg) 
	{
		this.out.println(msg);	
	}

	public void run() 
	{
		try
		{
			TimerTask task = new TimerTask()
			{
		        public void run()
		        {
		        	msg = d.t.getText();
					//peer.shadow = msg;
	        		diff_match_patch dmp = new diff_match_patch();
					LinkedList<diff_match_patch.Diff> diff = dmp.diff_main(peer.shadow, msg);
					LinkedList<diff_match_patch.Patch> patches = dmp.patch_make(diff);
					String patch = dmp.patch_toText(patches);
					patch = patch.replaceAll("\n", "\t");
					if(!patch.equals("") && !msg.equals(""))
					{
						peer.shadow = msg;
						peer.backup = peer.shadow;
						String temp = patch;
						patch = "";
						patch = peer.m + "," + peer.n + temp;
						send(patch);
						peer.n++;
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
				String msg = d.t.getText();
				diff_match_patch dmp = new diff_match_patch();
				line = line.replaceAll("\t", "\n");
				String a[] = line.split("@@");
				String b[] = a[0].split(",");
				int cm = Integer.parseInt(b[0]);
				peer.m = cm;
				line = "";
				line = line.concat("@@" + a[1] + "@@"+ a[2] );
				List<diff_match_patch.Patch> patches2 = dmp.patch_fromText(line);
				final Object[] returnVal1 = dmp.patch_apply((LinkedList<diff_match_patch.Patch>) patches2, peer.shadow);
				peer.shadow = returnVal1[0].toString();
				final Object[] returnVal2 = dmp.patch_apply((LinkedList<diff_match_patch.Patch>) patches2, msg);
				d.t.setText(returnVal2[0].toString());
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

