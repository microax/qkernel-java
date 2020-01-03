package com.qkernel.examples;

import java.lang.*;
import java.io.*;
import java.util.*;
import com.qkernel.*;

@SuppressWarnings("unchecked")

//------------------------------------------------------
// A simple utility to shutdown the  Server
//------------------------------------------------------
public class shutdown
{
    public static void main(String argvs[])
    {
	QMessage r = null;
        Config   c = new Config(argvs);
	String  ip = c.getLoIpAddress();
	int   port = c.getQbusPort();
	
	MqmUtilDelegate p = new MqmUtilDelegate(ip,port);
	QMessage        m = new QMessage();

	m.put("node","ZeroOne");
	m.put("password","xPl0Der");
	try
	{
	    r = p.invoke("com.qkernel.examples.RemoteUtil","shutdown", m, null);
	}
	catch(Exception e)
	{
	    System.out.println("Server not running");
	    /*StringWriter sw = new StringWriter(); 
	    e.printStackTrace(new PrintWriter(sw)); 
	    String message = sw.toString(); 
	    System.out.println(message); */
	    System.exit(0);
	}
        System.out.println("Shutdown status = "+r.getParameter("status"));
    }
}
