package com.qkernel;
//
// JoaContext.java        Java Object Adapter Context class
// ----------------------------------------------------------------------------
// History:
// --------
// 04/07/18 M. Gill     Add HTTP support.
// 01/12/02 M. Gill	Added reply(), readObject(), and close()
// 09/12/99 M. Gill	Initial creation.
// ----------------------------------------------------------------------------
//
import java.lang.*;
import java.io.*;

//----------------------------------------------------------------------------
// One might imagine that the Java Object Adapter Context (JoaContext) holds 
// the application context for classes that extend Joa...It does! :-)
// ...Methods for reading and writing objects from/to a Joa are provided by 
// the JoaContext. This Class is passed as an argument to Joa methods and 
// contains the ObjectInputStream and, GIOPAgent references to the current 
// Joa request.
// Joa Initially invokes JoaContext::ReadObject() to obtain the method name 
// (A serialized String) requested by the Qorb. It then invokes this method, 
// passing  JoaContext as an argument. The Joa Method is expected to use 
// JoaContext::ReadObject() to read the serialized object representing its 
// arguments....JoaContext will provide methods to reply to Joa requests.
// Minimally, ReplyString() presents a quick and efficant method for sending 
// Joa replies over the wire (GIOP)
//----------------------------------------------------------------------------
public class JoaContext extends Object
{

    //--------------------------------------------------
    // Here's all the good stuff...streams, user agent
    // etc..
    //--------------------------------------------------
    public GIOPAgent                agent;
    public ByteArrayInputStream     bistream;
    public ByteArrayOutputStream    bostream;
    public ObjectInputStream        input_stream;
    public ObjectOutputStream       output_stream;


    //--------------------------------------------------------------------------------
    // METHOD   reply()
    //
    // PURPOSE: Send a Reply to the calling (remote) object.
    //
    // INPUT:   Object
    //
    // PROCESS: 1) Convert Object into Byte array.
    //          2) Invoke GIOPAgent::SendReply() method to squirt back over the wire. 
    //
    // RETURN:  None.
    //--------------------------------------------------------------------------------
    public void reply(Object obj)
    {
	Reply(obj);
    }

    //--------------------------------------------------------------------------------
    // METHOD   Reply()
    //
    // PURPOSE: Send a Reply to the calling (remote) object.
    //
    // INPUT:   Object
    //
    // PROCESS: 1) Convert Object into Byte array.
    //          2) Invoke GIOPAgent::SendReply() method to squirt back over the wire. 
    //
    // RETURN:  None.
    //--------------------------------------------------------------------------------
    public void Reply(Object obj)
    {
	byte[] buff;
	int len;

	try
	{
	    output_stream.writeObject(obj);
	    output_stream.flush();

	    buff = bostream.toByteArray();
	    len  = bostream.size();

	    agent.SendReply(buff,(byte)3, len);
	}
	catch ( Exception e){}
    }


    //--------------------------------------------------------------------------------
    // METHOD   readObject()
    //
    // PURPOSE: Read serialized object from JoaContext::input_stream.
    //
    // INPUT:   None.
    //
    // PROCESS: 
    //
    // RETURN:  None.
    //--------------------------------------------------------------------------------
    public Object readObject()
    {
	return(ReadObject());
    }


    //--------------------------------------------------------------------------------
    // METHOD   ReadObject()
    //
    // PURPOSE: Read serialized object from JoaContext::input_stream.
    //
    // INPUT:   None.
    //
    // PROCESS: 
    //
    // RETURN:  None.
    //--------------------------------------------------------------------------------
    public Object ReadObject()
    {
    	Object o;

    	try
	{
	    o = input_stream.readObject();
	}
        catch( Exception e)
	{
	    return("JoaContext.ReadObject() Failed");
	}

	return(o);
    }
 

    //--------------------------------------------------------------------------------
    // METHOD   close()
    //
    // PURPOSE: Close input_stream
    //
    // INPUT:   None.
    //
    // PROCESS: 
    //
    // RETURN:  None.
    //--------------------------------------------------------------------------------
    public void close()
    {
	Close();
    }

    //--------------------------------------------------------------------------------
    // METHOD   Close()
    //
    // PURPOSE: Close input_stream
    //
    // INPUT:   None.
    //
    // PROCESS: 
    //
    // RETURN:  None.
    //--------------------------------------------------------------------------------
    public void Close()
    {
	try
	{
	    input_stream.close();
	    agent.Close();
	 
	}
	catch( Exception e){System.out.println("Exception in JoaContext.Close()");}
    }


    //--------------------------------------------------------------------------------
    // METHOD   JoaContext()
    //
    // PURPOSE: Constructor
    //
    // INPUT:   1) byte array to be used as Input stream
    //          2) GIOPAgent referance.
    //
    // PROCESS: Creates input_stream from byte array, and sets 'agent'.
    //
    // RETURN:  None.
    //--------------------------------------------------------------------------------
    public JoaContext(byte [] i_buff, GIOPAgent i_agent)
    {
        agent 	= i_agent;

	try
	{
	    bistream		= new ByteArrayInputStream(i_buff);
            input_stream 	= new ObjectInputStream(bistream);

            bostream  		= new ByteArrayOutputStream();
	    output_stream 	= new ObjectOutputStream(bostream);
        }
	catch( Exception e) {}

    }
}




























