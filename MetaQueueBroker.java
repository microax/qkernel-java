package com.qkernel;
//
// MetaQueueBroker.java		metaQueue  Object Broker
// ----------------------------------------------------------------------------
// History:
// --------
// 07/27/03 M. Gill	Initial creation.
// ----------------------------------------------------------------------------
//
import java.lang.*;
import java.lang.reflect.*;
import java.io.*;
import java.util.*;
import java.net.*;
import com.qkernel.json.*;
@SuppressWarnings("unchecked")
//
public final class MetaQueueBroker extends Joa 
{
    private String username;
    private String password;
    public  static MetaQueueDispatch dispatch;

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

        daemon.eventLog.sendMessage(handlerClass+"."+handlerMethod+" is being used to dispatch request");

	//--------------------------------------------------------
	// Sneaky way to poke metaQueue info into biz object :-)
	//--------------------------------------------------------
	argv.putString("QOBJECTusername", username);
	argv.putString("QOBJECTpassword", password);
	argv.putString("QOBJECTlicense"	, daemon.getLicenseKey());
	argv.put("QOBJECTdispatch"	, dispatch);

        //daemon.eventLog.sendMessage("QObject:" +handlerClass+"."+handlerMethod+" requested.");

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
	    objectClass = Class.forName(handlerClass);

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
	JSONObject errorReply = new JSONObject();
        String s = "***ERROR*** JSON requests not yet supported";
        daemon.eventLog.sendMessage(s);
	errorReply.put("status" ,"ERROR");
	errorReply.put("message",s);	    
	return errorReply;
    }
    
    public MetaQueueBroker(String uname, String pword, Daemon daemon)
    {
	super("MetaQueue Object Broker", daemon);
	username = uname;
	password = pword;
	dispatch = new MetaQueueDispatch("MetaQueue Dispatch", daemon);
	dispatch.reportInit=false;
	dispatch.start();
    }
}
