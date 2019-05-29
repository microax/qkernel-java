package com.qkernel;
//
// MetaQueueDispatch.java	A Mos Object Dispatcher
// ----------------------------------------------------------------------------
// History:
// --------
// 07/27/03 M. Gill     Initial creation.
// ----------------------------------------------------------------------------
//
import java.lang.*;
import java.util.*;
import java.lang.reflect.*;
import java.io.*;
import java.net.*;
@SuppressWarnings("unchecked")

//
public final class MetaQueueDispatch extends Mos
{

    //--------------------------------------------------------------------------------
    // METHOD MessageHandler()
    //
    //
    // RETURN:  None.
    //--------------------------------------------------------------------------------
    public void MessageHandler(MessageNode n)
    {
	QMessage m = (QMessage)n.object;

	MetaQueueMessage argv = new MetaQueueMessage();
        Method method = null;
        Class objectClass =null;
        QObject objHandler = null;
        Constructor documentConstructor =null;
        String handlerClass;
	String handlerMethod;

	handlerClass = (String)m.get("CHN-CLASS");
        handlerMethod= (String)m.get("CHN-METHOD");

	argv.setTopic(		m.getString("PUB-TOPIC"));
	argv.setText(		m.getString("PUB-TEXTBODY"));
	argv.setSmsText(	m.getString("PUB-SMS"));
	argv.setSmsNumber(	m.getString("PUB-PHONE"));
	argv.setMessageTime(	m.getString("PUB-DATE"));
	argv.setSmsApplication( m.getString("PUB-SOURCE"));
	argv.setPubSource(	m.getString("PUB-SOURCE"));
	argv.setSenderFirstName(m.getString("PUB-FIRSTNAME"));
	argv.setSenderLastName( m.getString("PUB-LASTNAME"));
	argv.setSenderUserName( m.getString("PUB-USERNAME"));

	Object attachment = 	m.get("PUB-ATTACHMENT");
	if( attachment != null)
	{
	    argv.setAttachment((FileObject)attachment);
	}

	if (handlerClass == null)
	{
            daemon.eventLog.sendMessage("***ERROR*** No QOBJECTclass defined!");
	    return;
	}
	//--------------------------------------
	// Get an instance of handlerClass. 
	//--------------------------------------
	try
	{
	    objectClass = Class.forName(handlerClass);

	    documentConstructor = objectClass.getConstructor(new Class[] {com.qkernel.Daemon.class});
	    objHandler = (QObject)documentConstructor.newInstance(new Object[] {daemon});
	}
	catch(Exception e)
	{
            daemon.eventLog.sendMessage("***ERROR*** "+ handlerClass +
					 " Does not exist or could  not be loaded");
	    //e.printStackTrace();
	    return;
	}
	//---------------------------------------------
	// Invoke objectRequest() on QObject.
	//---------------------------------------------
	try
	{
            method = objectClass.getMethod(handlerMethod,
					     new Class[] {argv.getClass()} );
	    method.invoke(objHandler, new Object[] {argv} );
	}
	catch(Exception e)
	{
            daemon.eventLog.sendMessage(e);
	}
    }

    //--------------------------------------------------------------------------------
    // METHOD start()
    //
    // PURPOSE: Starts the Mos  thread
    //
    // INPUT:   none
    //
    // PROCESS: calls initialize()
    //
    // RETURN:  None.
    //--------------------------------------------------------------------------------
    public void start()
    {
        super.start();
    }

    //--------------------------------------------------------------------
    // Public constructor....Constructs stuff :-)
    //--------------------------------------------------------------------
    public MetaQueueDispatch(String name, Daemon d)
    {
	super(name,d);
    }
}

