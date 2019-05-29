package com.qkernel;
//
// QmlDocumentBroker.java        QML Document Broker
// ----------------------------------------------------------------------------
// History:
// --------
// 05/30/05 M. Gill	Add ipaddress reporting to log output
// 09/12/02 M. Gill	Moved stat tracking stuff to Qorb/Joa.
// 09/07/02 M. Gill	Add stat tracking.
//
// 09/02/02 M. Gill	Add support for Object Pools.
//
// 01/12/02 M. Gill	Add Support forDaemon.classLoader(). QmlDocumentHandler 
//			Classes can be reloaded when changed on the file system.
//			
// 05/18/01 M. Gill	Use java.lang.reflect.Constructor.newInstance() to
//			pass Daemon reference to document handlers.
//
// 11/05/00 M. Gill	Changed doc handler name to "QMLclass" from "Arg0"
//			Perhaps, in the future the document broker can be 
//			started with a configurable DocumentHandler class name.
// 
// 08/13/00 M. Gill	Changed from abstract class to public final
//			QmlDocumentBroker now uses Class.forname to load
//			instances of document classes.
//
// 07/24/00 M. Gill	Initial creation.
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
//----------------------------------------------------------------------------
// The idea behind QML (Qkernel Markup Language) is to provide a facility 
// for separating presentation , and business logic in a language neutral,  
// platform independent way, without relying on (or precluding ) vender 
// specific services such as Active Server Pages, Cold Fusion, Java Server 
// Pages, and the like...
// The only dependency for using QML is that the supporting business logic 
// resides in a Qkernel application.
//
// QML is based on, and, extends XHTML, the www.w3.org XML DTD 
// (Document Type Definition) of HTML 4.0. QML files are well formed HTML 
// files (.i.e. <p> is represented as <p> </p>  and so on)...QML  extends 
// XHTML with two new tags, <IBLOCK> and <REPLACE>. <IBLOCK>  </IBLOCK> 
// tags indicate that HTML code between these tags will loop (or iterate) based 
// on the required business logic. The <REPLACE> tag indicates that the output 
// be replaced based on the business logic of the application....
// The QML "name=" Attribute is the name of a method that will be invoked when 
// parsed. Two values are passed to QML methods 1) A String representing the 
// default text between the start and end tags; 2) a String representing any 
// additional attributes passed via the "attr=" modifier in a QML tag.
//
// A QML Document Object is created by sub-classing QmlDocumentHandler(), and 
// implementing the user defined QML tag methods. The developer must also 
// implement initSourceDocument() and getDocument()...
// getDocument() is invoked by the QmlDocumentBroker at run time when a QML 
// document is requested.
//
// A QmlDocumentBroker is a Qorb (Qkernel Object Request Broker) that extends 
// the Joa (Java Object Adapter) creating a generic document broker for 
// QML documents .
//----------------------------------------------------------------------------
//
public final class QmlDocumentBroker extends Joa 
{
    private boolean debug    = false;
    private QMessage objPool = null;
    private QMessage objLock = null;

    //--------------------------------------------------------------------------------
    // METHOD 	getDocument()
    //
    // PURPOSE:	Called remotely by a servlet, to process a browser request, 
    // 		to render a QML document.
    //
    // INPUT:	JoaContext
    //
    // PROCESS:	1) Invoke JoaContext.ReadObject() to retrieve QMessage object.
    //		   Arg0 => The requested Document object
    //		2) Create the QmlDocumentHandler instance and invoke it's 
    //		   getDocument() method.
    //		3) Return HTTPReply object to remote client.
    //		4) Close JoaContext. 
    //
    // RETURN:  QMessage (via JoaContext.Reply())
    //--------------------------------------------------------------------------------
    public void getDocument(JoaContext ctx)
    {
        QMessage argv;
	QMessage   replyObj = null;
        Method method = null;
        Class documentClass =null;
        QmlDocumentHandler docHandler = null;
        Constructor documentConstructor =null;
        String handlerClass;
	Queue q       = null;
	Semaphore sem = null;

	argv = (QMessage)ctx.readObject();
	handlerClass = (String)argv.get("QMLclass");
        daemon.eventLog.sendMessage(argv.getString("REMOTE_ADDR")+":"+handlerClass+ " requested...");

	if (handlerClass == null)
	{
            daemon.eventLog.sendMessage("***ERROR*** No QMLclass defined in templete!");
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
		documentClass = daemon.loadClass(handlerClass);
	        documentConstructor = documentClass.getConstructor(new Class[] {com.qkernel.Daemon.class});
	        docHandler = (QmlDocumentHandler)documentConstructor.newInstance(new Object[] {daemon});
	    }
	    else if((sem = (Semaphore)objLock.get(handlerClass)) == null)
	    {
            	documentClass = Class.forName(handlerClass);
	        documentConstructor = documentClass.getConstructor(new Class[] {com.qkernel.Daemon.class});
	        docHandler = (QmlDocumentHandler)documentConstructor.newInstance(new Object[] {daemon});
	    }
	    else
	    {
            	documentClass = Class.forName(handlerClass);
		q= (Queue)objPool.get(handlerClass);
		//daemon.eventLog.sendMessage("Trying to get "+handlerClass+" from object pool");
		sem.Wait();
		docHandler = (QmlDocumentHandler)q.Dequeue();
		//daemon.eventLog.sendMessage(handlerClass+" retrieved from object pool");
	    }
	}
	catch(Exception e)
	{
            daemon.eventLog.sendMessage("***ERROR*** could not load:" +e.getMessage());
            //daemon.eventLog.sendMessage(e);
	    ctx.close();
	    return;
	}
	//---------------------------------------------
	// Invoke getDocument() on QmlDocumentHandler.
	//---------------------------------------------
	try
	{
            method = documentClass.getMethod("getDocument",
					     new Class[] {argv.getClass()} );
	    replyObj = (QMessage)method.invoke(docHandler, new Object[] {argv} );

	}
	catch(Exception e)
	{
            daemon.eventLog.sendMessage(e);

	    ctx.close();
	    return;
	}
	finally
	{
	    if(q != null)
	    {    
	    	q.Enqueue(docHandler);
	        sem.Signal();
	    }
	}

	//---------------------
	// Return results.
	//---------------------
        ctx.reply(replyObj);
        ctx.close();
	    
    }


    //--------------------------------------------------------------------------------
    // METHOD 	createPool()
    //
    // PURPOSE:	Create an Object pool for QmlDocumentHandlers
    //
    // INPUT:	1) Class name
    //		2) Pool size
    //
    // RETURN:  None.
    //--------------------------------------------------------------------------------
    public void createPool(String clazz, int size)
    {
    	int i;
	boolean fail       = false;
    	Semaphore userLock = null;
	Queue     pool     = null;
	Queue     q 	   = (Queue)objPool.get(clazz);

        Class documentClass             = null;
        QmlDocumentHandler docHandler   = null;
        Constructor documentConstructor = null;
/*
	long mem = Runtime.getRuntime().totalMemory();
*/
	if(q != null)
	{
            daemon.eventLog.sendMessage("Pool for "+clazz+" already created!");
	    return;
	}
	else
	{
	    fail     = false;
	    userLock = new Semaphore(size);
	    pool     = new Queue();

    	    for(i=0; i<size; i++)
    	    {
	        try
	   	{
		//-------------------------------------
		// Create a new instance of the class
		//-------------------------------------
            	documentClass = Class.forName(clazz);
	    	documentConstructor = documentClass.getConstructor(new Class[] {com.qkernel.Daemon.class});
	    	docHandler = (QmlDocumentHandler)documentConstructor.newInstance(new Object[] {daemon});
		//-------------------------------------
		// put into pool (Queue) ...
		//------------------------------------- 
	    	pool.Enqueue(docHandler);
		}
	   	catch(Exception e)
	        {
            	    daemon.eventLog.sendMessage(e);
		    fail = true;
		    break;
		}
    	    }
	    if(!fail)
	    {
		objPool.put(clazz, pool);
		objLock.put(clazz, userLock);

	        daemon.eventLog.sendMessage("Created Object pool of "+clazz+" depth="+size);
	    }
	    else
	    {
	        daemon.eventLog.sendMessage("***ERROR*** Object pool "+clazz+" depth="+size+" could not be created!");
	    }
	}
/*
	long mem2 = Runtime.getRuntime().totalMemory();
	long total = mem2 - mem;
        daemon.eventLog.sendMessage("Object pool consumes "+ total +" bytes out of "+mem2 );
*/
    }




    //--------------------------------------------------------------------------------
    // METHOD 	setDebugMode()
    //
    // PURPOSE:	Set debug mode where QmlDocumentHandlers are reloaded on modification.
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
            daemon.eventLog.sendMessage("QmlDocumentBroker is already in debug mode!");
	    return;
	}

	debug = true;

        daemon.eventLog.sendMessage("QmlDocumentBroker is now in debug mode...");
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
    

    public QmlDocumentBroker(String name, Daemon daemon)
    {
	super(name, daemon);

	debug 	= false;
    	objPool = new QMessage();
    	objLock = new QMessage();
    }
}
