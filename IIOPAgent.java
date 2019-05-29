package com.qkernel;
//
// IIOPAgent.java           IIOP User Agent class
// ----------------------------------------------------------------------------
// History:
// --------
// 09/15/99 M. Gill        Initial creation.
// ----------------------------------------------------------------------------
//
import java.net.*;
import java.io.*;

public final class IIOPAgent extends Fsm
{
    public Qorb orb;

    private TcpServer tcp;
    private Socket sock;
    private UserAgentNode ua;
    private DataOutputStream ostream;
    private DataInputStream istream;
    private int channel;
    private Timer timer;


    //----- States
    public static final int IIOP_INIT      = 0;
    public static final int IIOP_READY     = 1;
    public static final int IIOP_CONNECTED = 2;
    public static final int IIOP_SESSION   = 3;

    //---- Events
    public static final int INIT_OK     	= 0;
    public static final int GET_REQUEST_CMD 	= 11;
    public static final int PROCESS_REQUEST_CMD	= 12;
    public static final int CLOSE_CMD  		= 13;
    public static final int IO_DEVICE_ERROR	= 14;
    public static final int REQUEST_FAILED	= 15;
    public static final int AGENT_CLOSED        = 16;

    public void SetStateTable()
    {
//-----------------------------------------------------------------------------------------------
//	  CURRENT_ST		COMMAND/EVENT		NEXT_ST			ACTION
//	  ----------		-------------		----------		------
SetEvent( IIOP_INIT,		INIT_OK,		IIOP_READY,		"DoNothing"	);

SetEvent( IIOP_READY,		tcp.TCP_CONNECTED,	IIOP_CONNECTED,		"TcpConnected"	);
SetEvent( IIOP_READY,		IO_DEVICE_ERROR,	IIOP_READY,		"DoNothing"	);
SetEvent( IIOP_READY,		AGENT_CLOSED,	        IIOP_READY,		"DoNothing"	);
SetEvent( IIOP_READY,		GET_REQUEST_CMD,	IIOP_READY,		"GetReqIdle"	);

SetEvent( IIOP_CONNECTED,	GET_REQUEST_CMD,	IIOP_SESSION,		"GetRequest"	);
SetEvent( IIOP_CONNECTED,	CLOSE_CMD,		IIOP_READY,		"Close"		);
SetEvent( IIOP_CONNECTED,	IO_DEVICE_ERROR,	IIOP_READY,		"Close"	        );
SetEvent( IIOP_CONNECTED,	AGENT_CLOSED,	        IIOP_READY,		"DoNothing"	);


SetEvent( IIOP_SESSION,		PROCESS_REQUEST_CMD,	IIOP_SESSION,		"ProcessRequest");
SetEvent( IIOP_SESSION,		GET_REQUEST_CMD,	IIOP_SESSION,		"GetRequest"	);
SetEvent( IIOP_SESSION,		REQUEST_FAILED,		IIOP_READY,		"RequestFailed"	);
SetEvent( IIOP_SESSION,		CLOSE_CMD,		IIOP_READY,		"Close"		);
SetEvent( IIOP_SESSION,		IO_DEVICE_ERROR,	IIOP_READY,		"Close"		);
SetEvent( IIOP_SESSION,		AGENT_CLOSED,	        IIOP_READY,		"DoNothing"	);

//------------------------------------------------------------------------------------------------
    }

    public void TcpConnected()
    {
        ua  = (UserAgentNode) LastEvent.Data;
        sock= ua.socket;

        //daemon.event_log.SendMessage("Remote client connected...");

        try
        {
            ostream = new DataOutputStream(sock.getOutputStream());
	    istream = new DataInputStream(sock.getInputStream());

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
	evt.Event=GET_REQUEST_CMD;
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


    public void GetRequest()
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

 
    public void ProcessRequest()
    {
        // daemon.event_log.SendMessage("ProcessRequest called...");

	GIOPHeader h = new GIOPHeader();

        h = (GIOPHeader) LastEvent.Data;

	orb.InvokeRequest(h, this);

    	EventMessage evt = new EventMessage();
	evt.Event=GET_REQUEST_CMD;
	SendMessage(evt);	    
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
        EventMessage evt = new EventMessage();
        evt.Event=AGENT_CLOSED;
        SendMessage(evt);	    

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

        tcp.Release(ua);

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

    public IIOPAgent(String name, Daemon d, Qorb i_orb)
    {
        super(name, d);

	orb = i_orb;
    }
}



































