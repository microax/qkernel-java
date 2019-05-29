package com.qkernel;
//
// QmlProxy.java        Qml Broker Client Proxy class
// ----------------------------------------------------------------------------
// History:
// --------
// 01/15/02 M. Gill	Initial creation.
// ----------------------------------------------------------------------------
//
import java.lang.*;
import java.io.*;
import java.net.*;


public class QmlProxy extends JoaProxy
{

    public QMessage getDocument(QMessage r) throws Exception
    {
	QMessage rpl = null;

	try
	{
	    connect();
            sendRequest("getDocument", (Object) r);
	    rpl = (QMessage) getReply();
	    close();
	}
	catch(Exception e)
	{
	    close();
	    throw e;
	}

	return(rpl);
    }

    public QmlProxy(String name, int port)
    {
	super(name, port);
    }
}













