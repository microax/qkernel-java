package com.qkernel;
//
// QObjectBroker.java		A Qkernel Object Broker
// ----------------------------------------------------------------------------
// History:
// --------
// 04/18/18 M. Gill     Add JSON/HTTP support.
// 04/18/02 M. Gill	Log stacktrace when target throws exception
// 01/15/02 M. Gill	Initial creation.
// ----------------------------------------------------------------------------
//
import java.lang.*;
import java.lang.reflect.*;
import java.io.*;
import java.util.*;
import java.net.*;
import com.qkernel.classloader.*;
import com.qkernel.json.*;
@SuppressWarnings("unchecked")
//
public final class QObjectBroker extends Joa 
{
    private boolean debug = false;

    //--------------------------------------------------------------------------------
    // METHOD 	jsonObjectRequest()
    //
    // PURPOSE:	Called remotely to create an instance of a QObject and invoke a method.
    //
    // INPUT:    string     "QOBJECTclass"
    //           string     "QOBJECTmethod"
    //           JSONObject "argvs"
    //
    // PROCESS: 1) Create the QObject instance and invoke method.
    //		2) Return JSON object to remote client.
    //
    // RETURN:  JSONObject
    //--------------------------------------------------------------------------------
    public JSONObject jsonObjectRequest(QMessage q, JSONObject argvs)
    {
        Method method = null;
        Class objectClass =null;
        QObject objHandler = null;
        Constructor documentConstructor =null;
	JSONObject errorReply = new JSONObject();
        JSONObject reply;

        String clazz = q.getString("QOBJECTclass");
	String m     = q.getString("QOBJECTmethod");
	
        daemon.eventLog.sendMessage("QObject:" +clazz+"."+m+" requested.");

	if (clazz == null)
	{
	    String s = "***ERROR*** No QOBJECTclass defined!";
            daemon.eventLog.sendMessage(s);
	    errorReply.put("status" ,"ERROR");
	    errorReply.put("message",s);	    
	    return errorReply;            
	}
	//--------------------------------------
	// Get an instance of handlerClass. 
	//--------------------------------------
	try
	{
	    if(debug)
	    {
		objectClass = daemon.loadClass(clazz);
	    }
	    else
	    {
            	objectClass = Class.forName(clazz);
	    }
	    documentConstructor = objectClass.getConstructor(new Class[] {com.qkernel.Daemon.class});
	    objHandler = (QObject)documentConstructor.newInstance(new Object[] {daemon});
	}
	catch(Exception e)
	{
	    String s = "***ERROR*** "+clazz+" Does not exist or could  not be loaded";
            daemon.eventLog.sendMessage(s);
	    errorReply.put("status" ,"ERROR");
	    errorReply.put("message",s);	    
	    return errorReply;
	}
	//---------------------------------------------
	// Invoke objectRequest() on QObject.
	//---------------------------------------------
	try
	{
            method = objectClass.getMethod(m, new Class[] {argvs.getClass()} );
	    reply = (JSONObject)method.invoke(objHandler, new Object[] {argvs} );
	}
	catch(Exception e)
	{
	    String s = "***ERROR*** "+clazz+"."+m+" No Such Method";
            daemon.eventLog.sendMessage(s);
	    errorReply.put("status" ,"ERROR");
	    errorReply.put("message",s);	    
	    return errorReply;
	}
	//---------------------
	// Return results.
	//---------------------
        return (reply);	    
    }


    
    //--------------------------------------------------------------------------------
    // METHOD 	objectRequest()
    //
    // PURPOSE:	Called remotely to create an instance of a QObject and invoke a method.
    //
    // INPUT:	JoaContext
    //
    // PROCESS:	1) Invoke JoaContext.readObject() to retrieve QMessage object.
    //		   QOBJECTclass => The requested QObject class
    //		2) Create the QObject instance and invoke it's 
    //		   objectRequest() method.
    //		3) Return QMessage object to remote client.
    //		4) Close JoaContext. 
    //
    // RETURN:  QMessage (via JoaContext.reply())
    //--------------------------------------------------------------------------------
    public void objectRequest(JoaContext ctx)
    {
        QMessage  argv;
	QMessage  replyObj = null;
        Method method = null;
        Class objectClass =null;
        QObject objHandler = null;
        Constructor documentConstructor =null;
        String handlerClass;
	String handlerMethod;

	argv = (QMessage)ctx.readObject();
	handlerClass = (String)argv.get("QOBJECTclass");
        handlerMethod= (String)argv.get("QOBJECTmethod");

        daemon.eventLog.sendMessage("QObject:" +handlerClass+"."+handlerMethod+" requested.");

	if (handlerClass == null)
	{
            daemon.eventLog.sendMessage("***ERROR*** No QOBJECTclass defined!");
	    ctx.close();
	    return;
	}
	//--------------------------------------
	// Get an instance of handlerClass. 
	//--------------------------------------
	try
	{
	    if(debug)
	    {
		objectClass = daemon.loadClass(handlerClass);
	    }
	    else
	    {
            	objectClass = Class.forName(handlerClass);
	    }
	    documentConstructor = objectClass.getConstructor(new Class[] {com.qkernel.Daemon.class});
	    objHandler = (QObject)documentConstructor.newInstance(new Object[] {daemon});
	}
	catch(Exception e)
	{
            daemon.eventLog.sendMessage("***ERROR*** "+ handlerClass +
					 " Does not exist or could  not be loaded");
	    //e.printStackTrace();
	    ctx.close();
	    return;
	}
	//---------------------------------------------
	// Invoke objectRequest() on QObject.
	//---------------------------------------------
	try
	{
            method = objectClass.getMethod(handlerMethod,
					     new Class[] {argv.getClass()} );
	    replyObj = (QMessage)method.invoke(objHandler, new Object[] {argv} );
	}
	catch(Exception e)
	{
            daemon.eventLog.sendMessage(1, e);
	    ctx.close();
	    return;
	}
	//---------------------
	// Return results.
	//---------------------
        ctx.reply(replyObj);
        ctx.close();
	    
    }



    //--------------------------------------------------------------------------------
    // METHOD 	setDebugMode()
    //
    // PURPOSE:	Set debug mode where QObjects are reloaded on modification.
    //
    // INPUT:	None.
    //
    // PROCESS:	Set debug flag;
    //
    // RETURN:  None.
    //--------------------------------------------------------------------------------
    public void setDebugMode()
    {
	if(debug)
	{
            daemon.eventLog.sendMessage("QObjectBroker is already in debug mode!");
	    return;
	}

	debug = true;

        daemon.eventLog.sendMessage("QObjectBroker is now in debug mode...");
    }


    public QObjectBroker(String name, Daemon daemon)
    {
	super(name, daemon);
        setHTTP();

	debug = false;
    }
}
