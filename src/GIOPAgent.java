package com.qkernel;
//
// GIOPAgent.java           GIOP User Agent class
// ----------------------------------------------------------------------------
// History:
// --------
// 04/07/18 M. Gill        Renamed GIOP added HTTP support
// 09/15/99 M. Gill        Initial creation.
// ----------------------------------------------------------------------------
//
import java.net.*;
import java.io.*;

public final class GIOPAgent extends Fsm
{
    public Qorb orb;

    private TcpServer    tcp;
    private TcpSslServer ssl;
    private Socket sock;
    private Socket ssl_sock;
    private UserAgentNode ua;
    private DataOutputStream ostream;
    private DataInputStream istream;
    private int channel;
    private Timer timer;


    //----- States
    public static final int GIOP_INIT      = 0;
    public static final int GIOP_READY     = 1;
    public static final int GIOP_CONNECTED = 2;
    public static final int IIOP_SESSION   = 3;
    public static final int HTTP_SESSION   = 4;
    public static final int HTTPS_SESSION  = 5;

    //---- Events
    public static final int INIT_OK     	 = 0;
    public static final int GET_IIOP_REQUEST_CMD = 11;
    public static final int GET_HTTP_REQUEST_CMD = 17;
    public static final int GET_HTTPS_REQUEST_CMD= 18;
    public static final int PROCESS_REQUEST_CMD	 = 12;
    public static final int CLOSE_CMD  		 = 13;
    public static final int IO_DEVICE_ERROR	 = 14;
    public static final int REQUEST_FAILED	 = 15;
    public static final int AGENT_CLOSED         = 16;

    public void SetStateTable()
    {
//-----------------------------------------------------------------------------------------------
//	  CURRENT_ST		COMMAND/EVENT		NEXT_ST			ACTION
//	  ----------		-------------		----------		------
SetEvent( GIOP_INIT,		INIT_OK,		GIOP_READY,		"DoNothing"	);

SetEvent( GIOP_READY,		tcp.TCP_CONNECTED,	GIOP_CONNECTED,		"TcpConnected"	);
SetEvent( GIOP_READY,		IO_DEVICE_ERROR,	GIOP_READY,		"DoNothing"	);
SetEvent( GIOP_READY,		AGENT_CLOSED,	        GIOP_READY,		"DoNothing"	);
SetEvent( GIOP_READY,		GET_IIOP_REQUEST_CMD,	GIOP_READY,		"GetReqIdle"	);
SetEvent( GIOP_READY,		GET_HTTP_REQUEST_CMD,	GIOP_READY,		"GetReqIdle"	);

SetEvent( GIOP_CONNECTED,	GET_IIOP_REQUEST_CMD,	IIOP_SESSION,		"GetIIOPRequest");
SetEvent( GIOP_CONNECTED,	GET_HTTP_REQUEST_CMD,	HTTP_SESSION,		"GetHTTPRequest");
SetEvent( GIOP_CONNECTED,	GET_HTTPS_REQUEST_CMD,	HTTPS_SESSION,	        "GetHTTPSRequest");
SetEvent( GIOP_CONNECTED,	CLOSE_CMD,		GIOP_READY,		"Close"		);
SetEvent( GIOP_CONNECTED,	IO_DEVICE_ERROR,	GIOP_READY,		"Close"	        );
SetEvent( GIOP_CONNECTED,	AGENT_CLOSED,	        GIOP_READY,		"DoNothing"	);

SetEvent( IIOP_SESSION,		PROCESS_REQUEST_CMD,	IIOP_SESSION,		"ProcessIIOP"   );
SetEvent( IIOP_SESSION,		GET_IIOP_REQUEST_CMD,	IIOP_SESSION,		"GetIIOPRequest");
SetEvent( IIOP_SESSION,		REQUEST_FAILED,		GIOP_READY,		"RequestFailed"	);
SetEvent( IIOP_SESSION,		CLOSE_CMD,		GIOP_READY,		"Close"		);
SetEvent( IIOP_SESSION,		IO_DEVICE_ERROR,	GIOP_READY,		"Close"		);
SetEvent( IIOP_SESSION,		AGENT_CLOSED,	        GIOP_READY,		"DoNothing"	);

SetEvent( HTTP_SESSION,		GET_HTTP_REQUEST_CMD,	HTTP_SESSION,		"GetHTTPRequest");
SetEvent( HTTP_SESSION,		CLOSE_CMD,		GIOP_READY,		"Close"		);
SetEvent( HTTP_SESSION,		AGENT_CLOSED,	        GIOP_READY,		"DoNothing"	);
SetEvent( HTTP_SESSION,		REQUEST_FAILED,		GIOP_READY,		"RequestFailed"	);
SetEvent( HTTP_SESSION,		IO_DEVICE_ERROR,	GIOP_READY,		"Close"		);

SetEvent( HTTPS_SESSION,        GET_HTTPS_REQUEST_CMD,	HTTPS_SESSION,		"GetHTTPRequest");
SetEvent( HTTPS_SESSION,        CLOSE_CMD,		GIOP_READY,		"Close"		);
SetEvent( HTTPS_SESSION,        AGENT_CLOSED,	        GIOP_READY,		"DoNothing"	);
SetEvent( HTTPS_SESSION,	REQUEST_FAILED,		GIOP_READY,		"RequestFailed"	);
SetEvent( HTTPS_SESSION,	IO_DEVICE_ERROR,	GIOP_READY,		"Close"		);

//------------------------------------------------------------------------------------------------
    }

    public void TcpConnected()
    {
        ua  = (UserAgentNode) LastEvent.Data;
        if(orb.protocol == orb.HTTPS)
            ssl_sock= ua.ssl_socket;
        else
	    sock=ua.socket;
	
        //daemon.event_log.SendMessage("Remote client connected...");

        try
        {
	    // timeout in milliseconds
	    // and we buffer anyway, to improve latency
            if(orb.protocol == orb.HTTPS)
	    {
            ssl_sock.setSoTimeout(10000);
            ssl_sock.setTcpNoDelay(true);
            }
            else
            {
            sock.setSoTimeout(10000);
            sock.setTcpNoDelay(true);
            }
            if(orb.protocol == orb.IIOP) // HTTP creates it's own streams...
	    {
                ostream = new DataOutputStream(sock.getOutputStream());
	        istream = new DataInputStream(sock.getInputStream());
            }
        }
        catch (Exception e ) 
        {
            EventMessage evt = new EventMessage();
            evt.Event=IO_DEVICE_ERROR;
            SendMessage(evt);	    

            daemon.event_log.SendMessage("***ERROR*** TcpConnected() failed because: "+
					 e.getMessage());
	    return;
	}

    	EventMessage evt = new EventMessage();
        if(orb.protocol == orb.HTTPS)
	{
	    evt.Event=GET_HTTPS_REQUEST_CMD;
	}
        else if(orb.protocol == orb.HTTP)
	{
	    evt.Event=GET_HTTP_REQUEST_CMD;
	}
	else
	{
	    evt.Event=GET_IIOP_REQUEST_CMD;
	}
	
	SendMessage(evt);	    
    }


    public void GetRequestTimeout()
    {
        daemon.eventLog.sendMessage("***GIOP GetRequest Timeout !!!");

	Close();

	//EventMessage evt = new EventMessage();
	//evt.Event=REQUEST_FAILED;
	//SendMessage(evt);
    }


    public void GetIIOPRequest()
    {
	GIOPHeader h = new GIOPHeader();

        //daemon.event_log.SendMessage("GetRequest() Called...");

	//--------------------------------------
	// Set timer on GIOP Header request
	//--------------------------------------
	timer.Start(10, "GetRequestTimeout");

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

	    timer.Stop();

	    istream.readFully(h.message_buffer, 0, h.message_size);

        }
        catch(Exception e)
   	{
	    timer.Stop();

            EventMessage evt = new EventMessage();
            evt.Event=IO_DEVICE_ERROR;
            SendMessage(evt);
	    daemon.event_log.SendMessage("GIOP request failed because: "
					 + e.getMessage());	    
	    return;
	}

	EventMessage evt = new EventMessage();
	evt.Event=PROCESS_REQUEST_CMD;
	evt.Data = h;
	SendMessage(evt);
	//daemon.event_log.SendMessage("GIOP input message size="+h.message_size);

    }

 
    public void ProcessIIOP()
    {
        // daemon.event_log.SendMessage("ProcessRequest called...");

	GIOPHeader h = new GIOPHeader();

        h = (GIOPHeader) LastEvent.Data;

	orb.invokeRequestIIOP(h, this);

    	EventMessage evt = new EventMessage();
	evt.Event=GET_IIOP_REQUEST_CMD;
	SendMessage(evt);	    
    }

    public void GetHTTPRequest()
    {
        try
	{
	try
	{
	    orb.http.handleConnection(sock.getInputStream(), sock.getOutputStream());
	}
	finally
        {
	    EventMessage evt = new EventMessage();
	    evt.Event=CLOSE_CMD;
	    SendMessage(evt);	    
        }
	}
	catch (IOException ignore) {}
    }

    public void GetHTTPSRequest()
    {
        try
	{
	try
	{
	    orb.http.handleConnection(ssl_sock.getInputStream(), ssl_sock.getOutputStream());
	}
	finally
        {
	    EventMessage evt = new EventMessage();
	    evt.Event=CLOSE_CMD;
	    SendMessage(evt);	    
        }
	}
	catch (IOException ignore) {}
    }


    
    public void SendReply( byte[] b, byte type, int len)
    {
        GIOPHeader h = new GIOPHeader();

        //daemon.event_log.SendMessage("SendReply() called...");

        try
        {
	    //daemon.event_log.SendMessage("GIOP output message size="+len);

            ostream.writeByte('G');
            ostream.writeByte('I');
            ostream.writeByte('O');
            ostream.writeByte('P');
            ostream.writeByte('2');
            ostream.writeByte('0');
            ostream.writeByte(0);       // Big Endian...
            ostream.writeByte(type);    // Meessage type
            ostream.writeInt(len);      // Message length

            ostream.write(b, 0, len);
	    ostream.flush();
        }
        catch(Exception e)
   	{
            EventMessage evt = new EventMessage();
            evt.Event=IO_DEVICE_ERROR;
            SendMessage(evt);	    
	    daemon.event_log.SendMessage("GIOP reply to remote client failed because: "
					 + e.getMessage());	    
	}
    }

    public void RequestFailed()
    {
        daemon.event_log.SendMessage("Remote Request Failed...");
    }

    public void GetReqIdle()
    {
        //daemon.event_log.SendMessage("Get Request Command Ignored...");
    }


    public void Close()
    {
	if(orb.protocol == orb.HTTPS)
	{
	    closeSSL();
	    return;
	}
	
        EventMessage evt = new EventMessage();
        evt.Event=AGENT_CLOSED;
        SendMessage(evt);	    

	if(orb.protocol == orb.IIOP)
	{
            try
	    {
	        istream.close();
	        ostream.close();
	        //istream=null;
                //ostream=null;
            }
            catch(Exception e)
            {
                daemon.event_log.SendMessage("Could not close i/o streams because: " + e.getMessage());
	    }
        }
	else
	{
	    try
	    {
            try
            {
                // RFC7230#6.6 - close socket gracefully
	        sock.shutdownOutput();  // half-close socket (only output)
                orb.http.transfer(sock.getInputStream(), null, -1); // consume input
            }
            finally
            {
                sock.close(); // and finally close socket fully
	    }
	    }
	    catch (IOException ignore) {}
        }
	
        tcp.Release(ua);

        //daemon.event_log.SendMessage("Connection Released...");
    }




    public void closeSSL()
    {
        EventMessage evt = new EventMessage();
        evt.Event=AGENT_CLOSED;
        SendMessage(evt);	    
	try
	{
            try
            {
                // RFC7230#6.6 - close socket gracefully
	        ssl_sock.shutdownOutput(); // half-close socket (only output)
                orb.http.transfer(ssl_sock.getInputStream(), null, -1); // consume input
            }
            finally
            {
                ssl_sock.close(); // and finally close socket fully
	    }
	}
	catch (IOException ignore) {}	
        ssl.Release(ua);

        //daemon.event_log.SendMessage("Connection Released...");
    }


    
    public void start(TcpServer i_tcp, int chan)
    {
        super.start();
        channel = chan;
        tcp = i_tcp;
        tcp.Register(this);
        timer = new Timer(daemon, this);
    }

    public void start(TcpSslServer i_ssl, int chan)
    {
        super.start();
        channel = chan;
        ssl = i_ssl;
        ssl.Register(this);
        timer = new Timer(daemon, this);
    }

    public GIOPAgent(String name, Daemon d, Qorb i_orb)
    {
        super(name, d);

	orb = i_orb;
    }
}
