package examples;

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

        //////////////////////////////////////////////////////////////////////
        //put configuration defaults here
        //////////////////////////////////////////////////////////////////////
        Properties props = new Properties();
//        props.put( "business_host", "localhost" );
        props.put( "obPort", "3001" );
        try
    	{
    	    File cf = new File(argvs[0]); 
    	    props.load(new FileInputStream(cf));
    	    System.out.println("Using configuration file: " 
    	                       + cf.getCanonicalPath() );
    	}
    	catch(Exception e)
    	{
    	    System.out.println("Configuration file not found: ");
    	}

//	String ip = (String)props.get("business_host");
	String ip = "localhost";
	int port = Integer.parseInt((String)props.get("obPort"));
	MqmUtilDelegate p = new MqmUtilDelegate(ip,port);

	QMessage m = new QMessage();

	m.put("node","ZeroOne");
	m.put("password","xPl0Der");

	try
	{
	    r = p.invoke("examples.RemoteUtil","shutdown", m, null);
	}
	catch(Exception e)
	{
	 System.out.println("Server not running");
	 System.exit(0);
	}

	 System.out.println("Shutdown status = "+r.getParameter("status"));
    }
}
