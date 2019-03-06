import java.io.*; 
import java.net.*; 
import java.util.Scanner; 

public class Peer3
{ 
	public static void main(String[] args) throws IOException 
	{ 
		try
		{ 
			Scanner scn = new Scanner(System.in); 
			InetAddress ip = InetAddress.getByName("localhost"); 
			Socket s = new Socket(ip, 5056); 
			DataInputStream dis = new DataInputStream(s.getInputStream()); 
			DataInputStream diss = new DataInputStream(System.in); 
			DataOutputStream dos = new DataOutputStream(s.getOutputStream()); 
			String line = "";
			while (true) 
			{ 
				if(!line.equalsIgnoreCase("exit")) 
				{
					line = diss.readLine();
	            	dos.writeUTF(line);
	            	dis = new DataInputStream(new BufferedInputStream(s.getInputStream())); 
	            	line = dis.readUTF();
	            	System.out.println(line); 
					//String tosend = scn.nextLine();
				}
				else
				{
					System.out.println("Closing this connection : " + s); 
					s.close(); 
					System.out.println("Connection closed"); 
					break; 
				} 
			} 
			scn.close(); 
			dis.close(); 
			dos.close(); 
			diss.close();
		}
		catch(Exception e)
		{ 
			e.printStackTrace(); 
		} 
	} 
} 
