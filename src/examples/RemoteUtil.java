package com.qkernel.examples;
//
// RemoteUtil.java  Remote Utils for service Node Daemon
// ----------------------------------------------------------------------------
// History:
// --------
// 06/02/19 M. Gill	Initial creation.
// ----------------------------------------------------------------------------
//
import java.lang.*;
import java.util.*;
import java.io.*;
import com.qkernel.*;
@SuppressWarnings("unchecked")

//
//----------------------------------------------------------------------------
// RemoteUtil is a QObject which contains (hopefully ) useful methods
// which modify runtime parameters within a Service/Master Node.
//
// Forcing a shutdown appears to be one of these methods...
// I'm sure others will follow :-)
//----------------------------------------------------------------------------
public class RemoteUtil extends BusinessObject 
{

    //--------------------------------------------------------------------------------
    // METHOD 	shutdown()
    //
    // PURPOSE:	Called remotely to shutdown the Service Node
    //
    // INPUT:	QMessage
    //
    // RETURN:  QMessage 
    //--------------------------------------------------------------------------------
    public QMessage shutdown(QMessage req)
    {
	QMessage reply = new QMessage();

	String key = req.getParameter("node");
	String pass= req.getParameter("password");
	Integer t  = (Integer)req.get("time");

	if(!key.equals(daemon.getLicenseKey()))
	{
	    daemon.eventLog.sendMessage("*** ERROR ****"+ 
		                        "Shutdown request using invalid node id!");
	    reply.put("status","error");
	    return(reply);
	}
	if(!pass.equals("xPl0Der"))
	{
	    daemon.eventLog.sendMessage("*** ERROR ****"+
                                        "Shutdown request using invalid code!");
	    reply.put("status","error");
	    return(reply);
	}
	if(t != null)
	    daemon.shutdown(t.intValue());
	else
	    daemon.shutdown();

	//--------------------------------
	// Shutdown O-KAY-DOE-KAY :-)
 	//--------------------------------
	reply.put("status","ok");
	return(reply);
    }



    //--------------------------------------------------------------------------------
    // METHOD 	RemoteUtil()
    //
    // PURPOSE:	Constructor.
    // INPUT:	Daemon
    //--------------------------------------------------------------------------------
    public RemoteUtil(Daemon d)
    { 
	super(d);
    }
}

