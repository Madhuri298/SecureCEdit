package test;
import java.io.*;
import java.lang.reflect.Field;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.GapContent;
import javax.swing.undo.CompoundEdit;

public class Peer implements Serializable, DocumentListener
{				
	private Socket clientSocket;
	ClientThread clients;

	public Peer(Socket clientSocket) throws IOException 
	{		
		//input = new Scanner(System.in);
		this.clientSocket = clientSocket;
		clients = new ClientThread(clientSocket, this);
		clients.setDaemon(true);
		clients.start();
	}
	
	public void changedUpdate(DocumentEvent e)
	{
	}
	
	//insert event of document listener
	public void insertUpdate(DocumentEvent e)
	{
		try
		{
			//String msg = e.getDocument().getText(0, e.getDocument().getLength());
			Element change = clients.d.t.getDocument().getDefaultRootElement();
			if (change == null)
			{
		        System.out.println("null");
		    }
			else
			{
				int start = e.getOffset();
				int end = start + e.getLength();
				String text = change.getDocument().getText(start, 1);
				//System.out.println("Added " + text + " at location: " + start);
				//clients.send(msg);
				EditOperations a = new EditOperations(EditOperations.Edit.ADD, text,start,end);
				try
				{
					clients.sendObject(a);
				} 
				catch (IOException e1)
				{
					e1.printStackTrace();
				}
			}
		    
		} 
		catch (BadLocationException e1) 
		{
			
			e1.printStackTrace();
		}
	}
	
	//remove event of document listener
	public void removeUpdate(DocumentEvent e)
	{
		//String msg = clients.d.t.getText();
		String removedString = getRemovedString(e);
		if(!removedString.equals(""))
		{
			int start = e.getOffset();
			int end = start + e.getLength();
			EditOperations a = new EditOperations(EditOperations.Edit.DELETE, removedString,start,end);
			try
			{
				clients.sendObject(a);
			} 
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
		}			
	}
	
	//function to get the removed character
	public static String getRemovedString(DocumentEvent e)
	{
	    try
	    {
	        Field editsField = null;
	        Field[] fields = CompoundEdit.class.getDeclaredFields();
	        for(Field f : fields)
	        {
	            if(f.getName().equals("edits"))
	            {
	                editsField = f;
	                break;
	            }
	        }
	        editsField.setAccessible(true);
	        List edits = (List) editsField.get(e);
	        if(edits.size() != 1)
	        {
	            return null;
	        }

	        Class<?> removeUndo = null;
	        for(Class<?> c : GapContent.class.getDeclaredClasses())
	        {
	            if(c.getSimpleName().equals("RemoveUndo"))
	            {
	                removeUndo = c;
	                break;
	            }
	        }

	        Object removeUndoInstance = edits.get(0);
	        fields = removeUndo.getDeclaredFields();

	        Field stringField = null;
	        for(Field f : fields)
	        {
	            if(f.getName().equals("string"))
	            {
	                stringField = f;
	                break;
	            }
	        }

	        stringField.setAccessible(true);
	        return (String) stringField.get(removeUndoInstance);
	    }
	    catch(SecurityException e1)
	    {
	        e1.printStackTrace();
	    }
	    catch(IllegalArgumentException e1)
	    {
	        e1.printStackTrace();
	    }
	    catch(IllegalAccessException e1)
	    {
	        e1.printStackTrace();
	    }
	    return null;
	}
	
	public static void main(String[] args) throws IOException 
	{
		new Peer(new Socket("localhost", 4100));
	}	
}

class ClientThread extends Thread implements Serializable
{
	transient Socket clientSocket;			
	transient Peer peer;				
	Map<Integer, String> peerList1 = new HashMap<Integer, String> ();
	int peerId;
	transient ObjectOutputStream oos = null;
	transient ObjectInputStream ois = null;
	transient InputStream inp = null;
	transient ObjectInputStream eois = null;
	transient InputStream eis = null;
	transient ObjectOutputStream eoos = null;
	transient OutputStream eos = null;
	EditOperations a;
	DemoProj d ;
	
	public ClientThread(Socket clientSocket, Peer peer) throws IOException
	{
		this.clientSocket = clientSocket;
		this.peer = peer;
		d = new DemoProj(peer);
		eos = this.clientSocket.getOutputStream();
		eoos = new ObjectOutputStream(eos);
		
	}

	//function to send the object to Controller
	public void sendObject(EditOperations a) throws IOException 
	{
		eoos.writeObject(a);
	}
		
	public void run() 
	{
		
		try
		{
			eis = clientSocket.getInputStream();
			eois = new ObjectInputStream(eis);		
			ArrayList<String> at = new ArrayList<String>();
			try
			{
				while((at = (ArrayList<String>) eois.readObject()) != null)
				{
					String text = d.t.getText(Integer.parseInt(at.get(1)), 1);
					String msg = at.get(0);
					int start = Integer.parseInt(at.get(1));
					int end = Integer.parseInt(at.get(2));
					String msgType = String.valueOf(at.get(3));
					System.out.println(text + "," + msg + "," + start + "," + end + "," + msgType);
					if(msgType.equals(EditOperations.Edit.ADD.toString()))
					{
						if(!text.equals(msg))
						{
							d.t.insert(msg, start);
						}
					}
					else if(msgType.equals(EditOperations.Edit.DELETE.toString()))
					{
						if(!text.equals("\n"))
						{
							d.t.replaceRange(null, start, end);
						}
					}
					eois = new ObjectInputStream(eis);
					at.clear();
				}
			} 
			catch (ClassNotFoundException e)
			{
				e.printStackTrace();
			} 
			catch (NumberFormatException e)
			{
				e.printStackTrace();
			} 
			catch (BadLocationException e) 
			{
				e.printStackTrace();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
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
}
