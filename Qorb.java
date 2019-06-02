package com.qkernel;
//
// Qorb.java        Qkernel Object Request Broker class
// ----------------------------------------------------------------------------
// History:
// --------
// 04/07/18 M. Gill     Add HTTP support
//
// 09/12/02 M. Gill	1) Support Socket backlog > user agent pool in start()
//			2) Add stat reporting.
//
// 01/16/02 M. Gill	Add Support to bind TcpServer to InetAddress.
// 07/13/01 M. Gill	Allow for Orbs with no remote interface 
//			(.i.e num_agents = 0)
//
// 09/12/99 M. Gill	Initial creation.
// ----------------------------------------------------------------------------
//
import java.io.*;
import java.lang.*;
import java.net.*;
import com.qkernel.http.*;
// ----------------------------------------------------------------------------
// In Qkernel land, an ORB is simply a class that creates a pool of  
// GIOPAgents and associates them with a TcpServer on some user defined 
// port. The Qorb interface requires that the invokeRequest() method is 
// implemented. invokeIIOPRequest() is invoked by GIOPAgent after receiving a 
// remote request. Qorb makes no assumptions about the implementation of the 
// "Object Adapter", only that it implements invokeRequest() Therefore, it 
// is the responsibility of the Object Adapter to actually marshal (or unpack) 
// the GIOP packets, and invoke the called methods.
// ----------------------------------------------------------------------------
public abstract class Qorb
{
    public  GIOPAgent user_agent[];
    public  TcpServer tcp_server;
    public  Daemon daemon; 
    public  String MyName;
    public  boolean remote_interface;
    public  int poolSize;

    public boolean stats     = false;
    public long totalRequests=0;
    public long sumOfDelta   =0;
    public int peakUsed      =0;
    public long beginTime    =0;

    public final int IIOP    =0; 
    public final int HTTP    =1; 
    public final int HTTPS   =2; 
    
    public int protocol      =IIOP;
    private String protocolName[] = {"IIOP","HTTP","HTTPS"};

    public HTTPServer  http;
    public com.qkernel.http.HTTPServer.VirtualHost host;
    public boolean isWebServer =false;
    
    public void start(int i_port, int i_num_agents)
    {
        int port         =i_port;
        int num_agents   =i_num_agents;
	poolSize         =num_agents;

	if(num_agents == 0)
	{
	    start();
	    return;
	}

	if(protocol == HTTP || protocol == HTTPS)
	{
            http = new HTTPServer(port);    
            host = http.getVirtualHost(null); // default host
            host.addContext("/", new com.qkernel.http.HTTPServer.ContextHandler()
            {
	        public int serve(com.qkernel.http.HTTPServer.Request req,
				 com.qkernel.http.HTTPServer.Response resp) throws IOException
		{
		    invokeRequestHTTP(req, resp);
                    return 0;
		}
            });
	}
        //-------------------------------
        // Startup a TCP server
        //-------------------------------
        tcp_server = new TcpServer(("TCP"+ port), daemon);
	InetAddress addr = daemon.getInetAddress();
	tcp_server.reportReg=false;

	if(addr == null)
            tcp_server.start(port, num_agents);
	else
            tcp_server.start(port, num_agents, addr);
	
        //--------------------------------------------------------------
        // Create GIOP User Agents and 
        // associate them with our TCP transport and orb
        //--------------------------------------------------------------
        user_agent = new GIOPAgent[num_agents];

        for( int i =0; i < num_agents; i++)
	{
            user_agent[i] = new GIOPAgent((MyName+" Agent" + i), 
					  daemon, this);
	    user_agent[i].reportInit =false;
	    user_agent[i].start(tcp_server, i);
        }
	remote_interface = true;
	daemon.eventLog.sendMessage("Initialized "+num_agents+ " "+MyName+" agents using "+protocolName[protocol]);
    }


    //--------------------------------------------------------------------
    // This is same as above but also requires specification 
    // of the socket backlog
    //---------------------------------------------------------------------
    public void start(int i_port, int i_num_sockets, int i_num_agents)
    {
        int port         =i_port;
	int num_sockets  =i_num_sockets;
        int num_agents   =i_num_agents;
	poolSize         =num_agents;

	if(num_agents == 0 || num_sockets == 0)
	{
	    start();
	    return;
	}
	
	if(protocol == HTTP || protocol == HTTPS)
	{
            http = new HTTPServer(port);    
            host = http.getVirtualHost(null); // default host
            host.addContext("/", new com.qkernel.http.HTTPServer.ContextHandler()
            {
	        public int serve(com.qkernel.http.HTTPServer.Request req,
				 com.qkernel.http.HTTPServer.Response resp) throws IOException
		{
		    invokeRequestHTTP(req, resp);
                    return 0;
		}
            });
	}
        //-------------------------------
        // Startup a TCP server
        //-------------------------------
        tcp_server = new TcpServer(("TCP"+ port), daemon);
	InetAddress addr = daemon.getInetAddress();
	tcp_server.reportReg=false;

	if(addr == null)
            tcp_server.start(port, num_sockets);
	else
            tcp_server.start(port, num_sockets, addr);
	
        //--------------------------------------------------------------
        // Create GIOP User Agents and 
        // associate them with our TCP transport and orb
        //--------------------------------------------------------------
        user_agent = new GIOPAgent[num_agents];

        for( int i =0; i < num_agents; i++)
	{
            user_agent[i] = new GIOPAgent((MyName+" Agent" + i), 
					  daemon, this);
	    user_agent[i].reportInit =false;
	    user_agent[i].start(tcp_server, i);
        }
	remote_interface = true;
	daemon.eventLog.sendMessage("Initialized "+num_agents+ " "+MyName+" agents using "+protocolName[protocol]);
    }


    public void start()
    {
	remote_interface = false;
    }


    //--------------------------------------------------------------------------------
    // METHOD 	getThreadCount()
    //
    // PURPOSE:	Return Current Thread pool count(num_agents)
    //
    //--------------------------------------------------------------------------------
    public int getThreadCount()
    {
	int count = 0;

	if(remote_interface)
	{
	    count = tcp_server.getThreadCount();
	}

	return(count);
    }


    //--------------------------------------------------------------------------------
    // METHOD 	getUsedThreadCount()
    //
    // PURPOSE:	Return Used Thread pool count(num_agents)
    //
    //--------------------------------------------------------------------------------
    public int getUsedThreadCount()
    {
	int used = 0;

	if(remote_interface)
	{
	    used = poolSize - tcp_server.getThreadCount();
	}

	return(used);
    }


    //--------------------------------------------------------------------------------
    // METHOD 	getPeakUsedThreadCount()
    //
    // PURPOSE:	Return Peak Used Thread pool count(num_agents)
    //
    //--------------------------------------------------------------------------------
    //public int getPeakUsedThreadCount()
    //{
    //	return(peakUsed);
    //}


    //--------------------------------------------------------------------------------
    // METHOD 	getThreadPoolSize()
    //
    // PURPOSE:	Return the size of the Thread pool (num_agents)
    //
    //--------------------------------------------------------------------------------
    public int getThreadPoolSize()
    {
	return(poolSize);
    }


    //--------------------------------------------------------------------------------
    // METHOD 	getAverageCallTime()
    //
    // PURPOSE:	Return Average Call time in milliseconds
    //
    //--------------------------------------------------------------------------------
    public long getAverageCallTime()
    {
	long rtn = 0;

	if(sumOfDelta > 0)
	    rtn = sumOfDelta/totalRequests;

	return(rtn);
    }


    //--------------------------------------------------------------------------------
    // METHOD 	getTotalRequests()
    //
    // PURPOSE:	Return Total Requests
    //
    //--------------------------------------------------------------------------------
    public long getTotalRequests()
    {
	return(totalRequests);
    }



    //--------------------------------------------------------------------------------
    // METHOD 	setStatsOn()
    //
    // PURPOSE:	Set stat trace mode.
    //
    // INPUT:	None.
    //
    // PROCESS:	Set stats flag;
    //
    // RETURN:  None.
    //--------------------------------------------------------------------------------
    public void setStatsOn()
    {
	if(stats)
	{
            daemon.eventLog.sendMessage(MyName+" is already in stats mode!");
	    return;
	}

	stats = true;

        daemon.eventLog.sendMessage(MyName+" is now in stat. reporting mode...");
    }

    public void setIIOP()
    {
	protocol = IIOP;
    }
    public void setHTTP()
    {
	protocol = HTTP;
    }
    public void setHTTPS()
    {
	protocol = HTTPS;
    }
    public void setHTML()
    {
	isWebServer =true;
    }



    //--------------------------------------------------------------------
    // Object Adapters extend the Qorb class...
    // It is the responsibility of the Object Adapter for
    // retriving data , and decoding data from the GIOPHeader...
    // Object Adapters must provide implementations of (remote) request 
    // methods , and generate appropriate reply objects.
    // --------------------------------------------------------------------
    public abstract void invokeRequestIIOP(GIOPHeader h, GIOPAgent agent);


    //--------------------------------------------------------------------
    // Object Adapters extend the Qorb class...
    // It is the responsibility of the Object Adapter for
    // retriving data , and decoding data from the HTTPServer...
    // Object Adapters must provide implementations of (remote) request 
    // methods , and generate appropriate reply objects.
    // --------------------------------------------------------------------
    public abstract void invokeRequestHTTP(com.qkernel.http.HTTPServer.Request req,
					   com.qkernel.http.HTTPServer.Response resp);

    public Qorb(String i_name, Daemon i_daemon)
    {
	daemon = i_daemon;
	MyName = i_name;
	stats  = false;
    }
}






