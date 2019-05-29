package com.qkernel;
//
// QObjectProxy.java        QObject Client Proxy class
// ----------------------------------------------------------------------------
// History:
// --------
// 01/15/02 M. Gill	Initial creation.
// ----------------------------------------------------------------------------
//
import java.lang.*;
import java.io.*;
import java.net.*;
@SuppressWarnings("unchecked")

public class QObjectProxy extends JoaProxy
{

    public QMessage objectRequest(String oname, String mname, QMessage r) throws Exception
    {
	QMessage rpl = null;

	r.put("QOBJECTclass", oname);
	r.put("QOBJECTmethod", mname);

	try
	{
	    connect();
            sendRequest("objectRequest", (Object) r);
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

    public QObjectProxy(String name, int port)
    {
	super(name, port);
    }
}
