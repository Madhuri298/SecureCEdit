import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Peer implements Serializable
{
	private Scanner input;					
	ClientThread clients;		
	private boolean disconnected = false;				

	public Peer(Socket clientSocket) throws IOException 
	{		
		input = new Scanner(System.in);
		clients = new ClientThread(clientSocket, this);
		clients.setDaemon(true);
		clients.start();
		System.out.println("Enter your name");
		String name = input.nextLine(); 
		clients.out.println(name);
	}

	public void handleUser() throws IOException
	{
		while (!disconnected)
		{
			clients.send(input.nextLine());
		}
	}


	public void hangUp()
	{
		disconnected = true;
	}
	
	public static void main(String[] args) throws IOException
	{
		new Peer(new Socket("localhost", 4200)).handleUser();
	}
}

class ClientThread extends Thread implements Serializable
{
	transient Socket clientSocket;			
	transient Peer peer;		
	transient BufferedReader in;	
	transient PrintWriter out;		
	Map<Integer, String> peerList1 = new HashMap<Integer, String> ();
	transient String line;
	int peerId;
	transient ObjectOutputStream oos = null;
	transient ObjectInputStream ois = null;
	transient InputStream inp = null;
	
	
	public ClientThread(Socket clientSocket, Peer peer) throws IOException
	{
		this.clientSocket = clientSocket;
		this.peer = peer;
		in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		out = new PrintWriter(clientSocket.getOutputStream(), true);
		
	}

	public void send(String msg)
	{
		int port = clientSocket.getLocalPort();
		//System.out.println(port);
		if(peerList1.containsKey(port))
		{
			this.out.println(msg);
		}
		else
		{
			
		}
	}
	
	public void run() 
	{
		
		try
		{
			try
		    {
				inp = clientSocket.getInputStream();
		        ois = new ObjectInputStream(inp);
		        peerList1 = (HashMap) ois.readObject();
		        System.out.println("Peer List 1: " + peerList1);
		        
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
			while ((line = in.readLine()) != null)
			{	
				System.out.println(line);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			peer.hangUp();
			System.out.println("Peer4 disconnected");
		}
		try
		{
			out.close();
			in.close();
	        ois.close();
	        inp.close();
			clientSocket.close();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}	
}
