import java.io.*; 
import java.text.*; 
import java.util.*; 
import java.net.*; 

public class Peer1 
{ 
	public static void main(String[] args) throws IOException 
	{ 
		ServerSocket ss = new ServerSocket(5056); 
		while (true) 
		{ 
			Socket s = null; 
			try
			{ 
				s = ss.accept(); 
				System.out.println("A new client is connected : " + s); 
				DataInputStream dis = new DataInputStream(s.getInputStream()); 
				DataOutputStream dos = new DataOutputStream(s.getOutputStream()); 
				DataInputStream diss = new DataInputStream(System.in);
				Thread t = new ClientHandler(s, dis, dos, diss); 
				t.start(); 	
			} 
			catch (Exception e)
			{ 
				s.close(); 
				e.printStackTrace(); 
			} 
		} 
	} 
} 

class ClientHandler extends Thread 
{ 
	final DataInputStream dis;
	DataInputStream diss;
	final DataOutputStream dos; 
	final Socket s; 

	public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos, DataInputStream diss) 
	{ 
		this.s = s; 
		this.dis = dis; 
		this.dos = dos; 
		this.diss = diss;
	} 

	@Override
	public void run() 
	{ 
		String line = "";
		while (true) 
		{ 
			try
			{ 
				if(!line.equalsIgnoreCase("exit")) 
				{ 
					line = dis.readUTF();
					System.out.println(line); 	
					diss  = new DataInputStream(System.in);
					line = diss.readLine();
					dos.writeUTF(line);
				} 
				else
				{
					System.out.println("Client " + this.s + " sends exit..."); 
					System.out.println("Closing this connection."); 
					this.s.close(); 
					System.out.println("Connection closed"); 
					break; 
				}
				
			} 
			catch (IOException e)
			{ 
				e.printStackTrace(); 
			} 
		} 
		
		try
		{ 
			this.dis.close(); 
			this.dos.close(); 
			this.diss.close();
			
		}
		catch(IOException e)
		{ 
			e.printStackTrace(); 
		} 
	} 
} 
