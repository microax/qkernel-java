package com.qkernel;
//
// JoaProxy.java        Java Object Adapter Client Proxy class
// ----------------------------------------------------------------------------
// History:
// --------
// 01/14/02 M. Gill	Add sendRequest, getReply, and close().
// 09/12/99 M. Gill	Initial creation.
// ----------------------------------------------------------------------------
//
import java.lang.*;
import java.io.*;
import java.net.*;
// ----------------------------------------------------------------------------
// Java Object Adapter Client Proxy (JoaProxy ) is used to create and send 
// requests to remote objects that extend the Joa interface...
// Joa uses  Java serialization  to pass object request/reply attributes
// over the wire. A service object is created by extending the Joa with methods
// that serve remote (Joa) requests.
// -----------------------------------------------------------------------------
public class JoaProxy extends Object
{

    public String ServerName;
    public int ServerPort;

    public Socket                MySocket;
    public DataOutputStream      ostream;
    public DataInputStream       istream;
    public ObjectOutputStream    oostream;
    public ObjectInputStream     oistream;
    public ByteArrayOutputStream bostream;
    public ByteArrayInputStream  bistream;



    //------------------------------------------------------------
    // Method:  sendRequest()
    // Purpose: Remote Invokation...
    //------------------------------------------------------------
    public void sendRequest(String i_method, Object obj) throws Exception
    {
        SendRequest(i_method, obj);
    }

    //------------------------------------------------------------
    // Method:  SendRequest()
    // Purpose: Remote Invokation...
    //------------------------------------------------------------
    public void SendRequest(String i_method, Object obj) throws Exception
    {
        int len;
        byte[] buff;
        int type =3;

	try
	{
            oostream.writeObject(i_method);
	    oostream.writeObject(obj);
	    oostream.flush();

	    buff = bostream.toByteArray();
	    len  = bostream.size();

	    ostream.writeByte('G');
            ostream.writeByte('I');
            ostream.writeByte('O');
            ostream.writeByte('P');
            ostream.writeByte('2');
            ostream.writeByte('0');
            ostream.writeByte(0);       // Big Endian...
            ostream.writeByte(type);    // Meessage type
            ostream.writeInt(len);      // Message length

            ostream.write(buff, 0, len);
	    /*for(int i=0; i < len; i++)
	    {
		ostream.writeByte(buff[i]);
	    }
	    */
	    ostream.flush();
            oostream.close();
        }
        catch(Exception e)
	{
	    throw e;
	}
    }



    //------------------------------------------------------------
    // Method:  getReply()
    // Purpose: Get String reply from remote server.
    //------------------------------------------------------------
    public Object  getReply() throws Exception
    {
	return(GetReply());
    }

    //------------------------------------------------------------
    // Method:  GetReply()
    // Purpose: Get String reply from remote server.
    //------------------------------------------------------------
    public Object  GetReply() throws Exception
    {
        Object obj;
	GIOPHeader h = new GIOPHeader();

	try
	{
	    h.magic0 		= istream.readByte();	// G
	    h.magic1 		= istream.readByte();	// I
	    h.magic2 		= istream.readByte();	// O
	    h.magic3 		= istream.readByte();	// P

	    h.version_major 	= istream.readByte();	// 2
	    h.version_minor 	= istream.readByte();	// 0

	    h.byte_order 	= istream.readByte();	// 0 => Big 1 => little
	    h.message_type 	= istream.readByte();	// request, response, locate request etc.
	    h.message_size 	= istream.readInt();

            h.message_buffer    = new byte[h.message_size];

	    istream.readFully(h.message_buffer, 0, h.message_size);
	    /*for(int i =0; i< h.message_size; i++)
	    {
		h.message_buffer[i] = istream.readByte();
	    }
	    */
            bistream = new ByteArrayInputStream(h.message_buffer);
	    oistream = new ObjectInputStream(bistream);

	    obj = oistream.readObject();

	    return(obj);
        }
        catch(Exception e)
   	{
	    throw e;
	}
    }

    public void close()
    {
        Close();
    }

    public void Close()
    {
        try
	{
	    istream.close();
	    ostream.close();
	    MySocket.close();
	}
	catch(Exception e) {}
    }



    //------------------------------------------------------------
    // Method:  connectServer()
    // Purpose: Create socket connection and all sorts of streams
    //          for a Joa server
    //------------------------------------------------------------
    public void connect()
    {
	Connect();
    }

    //------------------------------------------------------------
    // Method:  ConnectServer()
    // Purpose: Create socket connection and all sorts of streams
    //          for a Joa server
    //------------------------------------------------------------
    public void Connect()
    {

        try
	{
	    MySocket = new Socket(ServerName, ServerPort);
	    MySocket.setTcpNoDelay(true);
	    MySocket.setSoLinger(false,0);
	    //MySocket.setSoTimeout(4000);
            istream  = new DataInputStream( MySocket.getInputStream());
            ostream  = new DataOutputStream( MySocket.getOutputStream());
            bostream = new ByteArrayOutputStream();
	    oostream = new ObjectOutputStream(bostream);
	}
        catch (Exception e )
	{
	    // System.out.println("Could not connect because: " + e.getMessage());
	}
    }


    public JoaProxy(String name, int port)
    {
	ServerName = name;
	ServerPort = port;
    }
}
