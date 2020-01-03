package com.qkernel;
//
// TcpSslServer.java        TCP/IP SSL Server class.
// ----------------------------------------------------------------------------
// History:
// --------
// 12/31/19 M. Gill     Fixed SSL support
// 09/12/02 M. Gill	Support Socket backlog > user agent pool size.
// 09/07/02 M. Gill	Add getThreadCount().
// 01/16/02 M. Gill	Allow binding to InetAddress.
// 09/20/97 M. Gill     Initial creation.
// ----------------------------------------------------------------------------
import java.net.*;
import javax.net.*;
import javax.net.ssl.*;

public final class TcpSslServer extends Eos
{
    public int port_number;
    public int pool;
    public InetAddress address;

    protected Queue ready_list;
    protected Queue accept_list;
    protected SSLServerSocket server_socket;
    protected Semaphore ua_lock;
    protected static final int TCP_OPEN_CMD          =10;
    protected static final int TCP_CLOSE_CMD         =11;
    protected static final int TCP_ACCEPT_OK         =12;

    public  static final int TCP_CONNECTED         =3;

    public void EventHandler(EventMessage e)
    {
        switch(e.Event)
        {
            case TCP_OPEN_CMD:
	    if(address == null)
	    daemon.event_log.SendMessage("Ready to accept connections on port "
				         +port_number+" pool="+pool);
	    else
	    daemon.event_log.SendMessage(address.getHostAddress()+" ready to accept connections on port "
				         +port_number+" pool="+pool);
            tcp_accept();
	    break;

            case TCP_ACCEPT_OK:
            tcp_accept();
            break;

            case TCP_CLOSE_CMD:
            break;

            default :
            break;
        }
    }



    //---------------------------------------------------------------------------
    // Method: tcp_accept()
    // 
    // Purpose: 1) Allocate a user agent to handle the server_socket.
    //		2) Accept new socket connection.
    //
    // INPUTS:	Action routine.
    // RETURN:	N/A
    //---------------------------------------------------------------------------
    public void tcp_accept()
    {
        UserAgentNode user_agent;

        try
        {
            //---This blocks untill there is a new connection
	    accept_list.Enqueue(server_socket.accept() );
        }
        catch(Exception ecpt)
        {
            daemon.log(ecpt);
	    //----------------------------------------------------
	    // We still send Ok, because we want to try again...
	    //----------------------------------------------------
            /*EventMessage e1 = new EventMessage();
            e1.Event        = TCP_ACCEPT_OK;
    	    SendMessage(e1);*/
	    return;

        }
	//----------------------------------------
    	// Wait for UA to become free
    	//----------------------------------------
    	//ua_lock.Wait();   
    	user_agent = (UserAgentNode) ready_list.Dequeue();
	if(user_agent != null)
	{
	//----------------------------------------
    	// Setup UA handle's socket.
    	//----------------------------------------
    	user_agent.ssl_socket = (SSLSocket)accept_list.Dequeue();
    	//----------------------------------------
    	// Notify User Agent that it's connected.
    	//----------------------------------------
        EventMessage e = new EventMessage();
        e.Event        = TCP_CONNECTED;
        e.Data         = user_agent;

    	user_agent.application.SendMessage(e);
	}
    	//----------------------------------------
    	// Notify self that We're OK...
    	//----------------------------------------
        EventMessage e1 = new EventMessage();
        e1.Event        = TCP_ACCEPT_OK;
    	SendMessage(e1);
    }


    // ----------------------------------------------------------------------------
    // Method getThreadCount
    //
    // PURPOSE: Get number of Agents left in Queue
    // ----------------------------------------------------------------------------
    public int getThreadCount()
    {
        return( ready_list.Count() );
    }


    // ----------------------------------------------------------------------------
    // Method getBacklog
    //
    // PURPOSE: Get number of Sockets waiting in Queue
    // ----------------------------------------------------------------------------
    public int getbacklog()
    {
        return( accept_list.Count() );
    }



    // ----------------------------------------------------------------------------
    // Method Register
    //
    // PURPOSE: Register a user agent with TcpServer.
    // ----------------------------------------------------------------------------
    public void Register(Eos application_context)
    {

        UserAgentNode user = new UserAgentNode();
        user.application   = application_context;

        ready_list.Enqueue(user);	 // Make available
	//ua_lock.Signal();                // At least one ua is now available...
    }



    //---------------------------------------------------------------------------
    // Method: public void Release()
    // 
    // Purpose:    Close (accept) socket.
    //
    //---------------------------------------------------------------------------
    public void Release(UserAgentNode user)
    {
	SSLSocket ssl_sock;

        try
        {
	    if(!user.ssl_socket.isClosed())
                user.ssl_socket.close();	// Try to close socket...
        }
        catch(Exception e)
	{
            daemon.log(e);
	}

	if((ssl_sock = (SSLSocket)accept_list.Dequeue()) != null)
	{
	    //----------------------------------------
    	    // Setup UA handle's socket.
    	    //----------------------------------------
    	    user.ssl_socket = ssl_sock;
    	    //----------------------------------------
    	    // Notify User Agent that it's connected.
    	    //----------------------------------------
            EventMessage e = new EventMessage();
            e.Event        = TCP_CONNECTED;
            e.Data         = user;

    	    user.application.SendMessage(e);
	}
	else
	{
    	    ready_list.Enqueue(user);   // Make user agent available for use.
	}
    }


    // ----------------------------------------------------------------------------
    // Method start
    //
    // PURPOSE: initialize TcpServer.
    // ----------------------------------------------------------------------------
    public void start(int portnumber, int number_connect )
    {
        port_number = portnumber;
        pool        = number_connect;

        super.start();
        try
        {
	    SSLServerSocketFactory factory=(SSLServerSocketFactory)SSLServerSocketFactory.getDefault(); 
   	    server_socket   = (SSLServerSocket)factory.createServerSocket(portnumber, number_connect);
        }
        catch( Exception Ecpt) 
        {
            daemon.log(Ecpt);
        }

    	ready_list	= new Queue();
    	accept_list	= new Queue();
    	ua_lock         = new Semaphore(0);

        EventMessage e = new EventMessage();
        e.Event        = TCP_OPEN_CMD;
    	SendMessage(e);
    }

    // ----------------------------------------------------------------------------
    // Method start
    //
    // PURPOSE: initialize TcpServer (bind to Inetaddress)
    // ----------------------------------------------------------------------------
    public void start(int portnumber, int number_connect, InetAddress addr )
    {
        port_number = portnumber;
        pool        = number_connect;
	address     = addr;

        super.start();
        try
        {
	    SSLServerSocketFactory factory=(SSLServerSocketFactory)SSLServerSocketFactory.getDefault(); 
   	    server_socket   = (SSLServerSocket)factory.createServerSocket(portnumber, number_connect, addr);
        }
        catch( Exception Ecpt) 
        {
            daemon.log(Ecpt);
        }

    	ready_list	= new Queue();
    	accept_list	= new Queue();
    	ua_lock         = new Semaphore(0);

        EventMessage e = new EventMessage();
        e.Event        = TCP_OPEN_CMD;
    	SendMessage(e);
    }

    public TcpSslServer(String n, Daemon d)
    {
        super(n, d);
    }
}
