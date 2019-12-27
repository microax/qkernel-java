package com.qkernel;
//
// BusinessDelegate.java        Business Delegate 
// ----------------------------------------------------------------------------
// History:
// --------
// 06/28/02 M. Gill	Changed prototype for getOrbAddress() and 
//			getOrbPort() to support daemon configured params.
//
// 01/15/02 M. Gill	Initial creation.
// ----------------------------------------------------------------------------
//
import java.lang.*;
import java.lang.reflect.*;
import java.io.*;
import java.util.*;
import java.net.*;

@SuppressWarnings("unchecked")
//----------------------------------------------------------------------------
// Developers will create Business Delegates to reduce the coupling logic 
// between a client and a Business Service. In Qkernel land, Business Services
// are implemented as BusinessObjects and may be invoked by the QObjectBroker via 
// QObjectProxy or accessed locally via reflection. BusinessObects are usually 
// Session Facades. Clients are usually JSP helpers or QML view controllers
// but, any Java Object may can access a BusinessObject via BusinessDelegate.
//
// Notes:
// ------
// 1) Service Locator support would be nice 
// 2) Redundancy support should be implemented here as well
// 3) Obviously, BusinessDelegate may implement interfaces on these (target) 
//    objects, using invoke() to do the work. But, this implies that there is an 
//    IDL...Anyone reading this is more than welcome to write an IDL supporting 
//    qkernel Business Delegates and Facades :-)
//----------------------------------------------------------------------------
public abstract class BusinessDelegate extends Object 
{
    private String remoteHost="";
    private int    remotePort=0;

    //---------------------------------------
    //     Implement these
    //---------------------------------------
    public abstract String  getOrbAddress(Daemon d);
    public abstract int     getOrbPort(Daemon d);
    public abstract boolean isRemote();

    //------------------------------------------------------------
    // Method:  invoke()
    //
    // Purpose: Invoke method in local or remote BusinessObject
    //
    // Input:	1) Class name
    //		2) Method name
    //		3) Method Arguments (params)
    //		4) Daemon (remote objects set this to null)
    //
    // Return:	QMessage
    // Throws:	Exception
    //------------------------------------------------------------
    public QMessage invoke(String clazz, String handlerMethod, QMessage params, Daemon daemon)
				throws Exception
    {
    	String ip         = "";
    	int    port       = 0;
	QMessage replyObj =null;

	if(isRemote())
	{
	    if(daemon != null)
	    {
	    	ip  = getOrbAddress(daemon);
	    	port= getOrbPort(daemon);
    	    }
	    else
	    {
	    	ip  = getDefaultOrbAddress();
	    	port= getDefaultOrbPort();
	    }

	    QObjectProxy myProxy = new QObjectProxy(ip, port);

	    try
	    {
		replyObj = myProxy.objectRequest(clazz, handlerMethod, params);

		if(replyObj == null)
		    throw new Exception("QMessage is null");
	    }
	    catch(Exception e)
	    {
		throw e;
	    }
	}
	else
	{
	try
	{
    	    Class objectClass   = null;
    	    QObject objHandler  = null;
    	    Constructor documentConstructor = null;

            objectClass = Class.forName(clazz);
	    documentConstructor = objectClass.getConstructor(new Class[] {com.qkernel.Daemon.class});
	    objHandler = (QObject)documentConstructor.newInstance(new Object[] {daemon});

            Method method = objectClass.getMethod(handlerMethod, new Class[] {params.getClass()} );
	    replyObj = (QMessage)method.invoke(objHandler, new Object[] {params} );

	    if(replyObj == null)
		throw new Exception("QMessage is null");
	}
	catch(Exception e)
	{
	    throw e;
	}
	}
	return(replyObj);
    }


    //------------------------------------------------------------
    // Method:  getDefaultOrbAddress()
    //
    // Purpose: Return default ORB Address
    //------------------------------------------------------------
    public String getDefaultOrbAddress()
    {
	return(remoteHost);
    }


    //------------------------------------------------------------
    // Method:  getDefaultOrbPort()
    //
    // Purpose: Return default ORB Port
    //------------------------------------------------------------
    public int getDefaultOrbPort()
    {
	return(remotePort);
    }


    //------------------------------------------------------------
    // Method:  BusinessDelegate()
    //
    // Purpose: Public Constructor
    //
    // Input:	1) Remote Host Address
    //		2) Remote Port
    //------------------------------------------------------------
    public BusinessDelegate(String host, int port)
    {
	if(host == null)
	{
	    host ="";
	}
	if(port < 0)
	{
	    port =0;
	}

	remoteHost = host;
	remotePort = port;
    }

    //------------------------------------------------------------
    // Method:  BusinessDelegate()
    //
    // Purpose: Public Constructor
    //
    // Input:	None
    //------------------------------------------------------------
    public BusinessDelegate()
    {

    }


}
