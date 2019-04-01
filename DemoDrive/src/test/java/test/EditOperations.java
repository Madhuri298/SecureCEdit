package test;

import java.io.*;

public class EditOperations implements Serializable
{
	public enum Edit
	{
		ADD,DELETE;
	}
	Edit msgType;
	String msg;
	int start;
	int end;
	public EditOperations(Edit msgType, String msg, int start, int end)
	{
		Edit e = Edit.ADD;
		Edit d = Edit.DELETE;
		this.msgType = msgType;
		this.msg = msg;
		this.start = start;
		this.end = end;
	}
	
	
}
