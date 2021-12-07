package com.qkernel;
//
// Joa.java        Java Object Adapter  class
// ----------------------------------------------------------------------------
// History:
// --------
// 04/07/18 M. Gill     Add HTTP support.
// 09/12/02 M. Gill	Support Qorb "stats" feature in a weird way...
// 09/12/99 M. Gill	Initial creation.
// ----------------------------------------------------------------------------
//
import java.util.*;
import java.lang.annotation.*;
import java.lang.reflect.*;
import java.lang.*;
import com.qkernel.http.*;
import com.qkernel.json.*;
import java.util.Base64;
@SuppressWarnings("unchecked")

// ----------------------------------------------------------------------------
// Java Object Adapter (Joa ) extends a Qorb (Qkernel Object Request Broker). 
// In Qkernel land, an ORB is simply a class that creates a pool of  
// GIOPAgents and associates them with a TcpServer on some user defined 
// port. The Qorb interface requires that the InvokeRequest() method is 
// implemented. InvokeRequest() is invoked by GIOPAgent after receiving a 
// remote request. Qorb makes no assumptions about the implementation of the 
// "Object Adapter", only that it implements InvokeRequest() Therefore, it 
// is the responsibility of the Object Adapter to actually marshal (or unpack) 
// the GIOP packets, and invoke the called methods.
//
// Joa uses  Java serialization  to pass object request/reply attributes
// over the wire. A service object is created by extending the Joa with methods
// that serve remote (Joa) requests.
// -----------------------------------------------------------------------------
public abstract class Joa extends Qorb
{

    //--------------------------------------------------------------------------------
    // METHOD   InvokeRequestHTTP()
    //
    // PURPOSE: This is the implementation of the Qorb::InvokeRequestHTTP() abstract
    //          method. All remote requests are invoked via this method.
    //
    // INPUT:   1) HTTPServer.Request
    //          2) HTTPServer.Response
    //
    // PROCESS: 
    //
    // RETURN:  None.
    //--------------------------------------------------------------------------------
    public void invokeRequestHTTP(com.qkernel.http.HTTPServer.Request req,
				  com.qkernel.http.HTTPServer.Response resp)
    {
        try
	{
	JSONObject reply;
	QMessage   r = new QMessage();

	if(isAuthRequired())
	{
	//-----------------------------------------
	// Set username:password http "BASIC AUTH" 
	// authentication parameters from Request
	//-----------------------------------------
	setBasicAuth(r, req);
        }
        //-----------------------------------------
	// Route request REST URI to object/method
	//-----------------------------------------
	routeQObjectRequest(r, req);
	
	//--------------------------------------
	// invoke web service object/method...
	//--------------------------------------
	reply = jsonObjectRequest(r, req);
	
	//-----------------------------------------------
	// send response headers...
	//-----------------------------------------------
	resp.getHeaders().add("Access-Control-Allow-Origin", "*");
	resp.getHeaders().add("Access-Control-Allow-Credentials", "true");
	resp.getHeaders().add("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
	resp.getHeaders().add("Access-Control-Max-Age", "3600");
	resp.getHeaders().add("Access-Control-Allow-Headers", "Content-Type, Accept, authorization");
	resp.getHeaders().add("Content-Type", "application/json");

	//-----------------------------------
	// send a nicely formated JSON reply
	// to requesting agent...
	//-----------------------------------
	resp.send(200, reply.toString(4));
	}
	catch(Exception e)
	{
	    try
	    {
	    String s = "Object/Authorzation error";

            JSONObject reply  = new JSONObject();
	    reply.put("json_status" ,"ERROR");
	    reply.put("json_message",s);
	    resp.getHeaders().add("Content-Type", "application/json");
	    resp.send(200, reply.toString(4));
	    daemon.eventLog.sendMessage(e);
            }
	    catch(Exception e1)
	    {
		//daemon.eventLog.sendMessage(e1);
	    }
	}
    }

    
    //--------------------------------------------------------------------------------
    // METHOD   invokeRequestIIOP()
    //
    // PURPOSE: This is the implementation of the Qorb::invokeRequestIIOP() abstract
    //          method. All remote requests are invoked via this method.
    //
    // INPUT:   1) GIOPHeader.
    //          2) Reference to calling GIOPAgent
    //
    // PROCESS: 1) Create new JoaContext.
    //          2) Read method name from JoaContext.
    //          3) Invoke method by name.
    //
    // RETURN:  None.
    //--------------------------------------------------------------------------------
    public void invokeRequestIIOP(GIOPHeader h, GIOPAgent agent)
    {
        String method_name = null;
        Method method = null;
        Object[] arguments;
	Class[]  params;
        Class ThisClass;

        JoaContext ctx = new JoaContext(h.message_buffer, agent);

        ThisClass   = this.getClass();
       	arguments   = new Object[] {ctx};
        params      = new Class[] { ctx.getClass() };

	try
        {

	    method_name = (String)ctx.ReadObject();
            method = ThisClass.getMethod(method_name, params);

	    if(stats)
	    {
	        totalRequests++;
	   	beginTime = System.currentTimeMillis();
	    }

	    method.invoke(this, arguments);

	    if(stats)
	    {
		sumOfDelta = sumOfDelta + (System.currentTimeMillis() - beginTime);
	    }
        }
        catch( Exception e)
	{
	    e.printStackTrace();
            daemon.event_log.SendMessage("Joa.invokeRequest() failed because: "
					  + e.getMessage());
	    ctx.Close();
	}
    }

    private void setBasicAuth(QMessage r, com.qkernel.http.HTTPServer.Request req) throws Exception
    {
	try
	{
	//--------------------------------------------------------------
	// We're supporting HTTP BASIC Authorzation.
	// Header must contain Base64 encoded username and password
        // It's the responsibility of the Object Broker to process
	// user priviliges on objects.
	//--------------------------------------------------------------
	String head     = req.getHeaders().get("Authorization");
        String[] auth   = head.split(" ");
        byte[] bytes    = Base64.getDecoder().decode(auth[1]);
        String userNpass= new String(bytes);
	String[] up     = userNpass.split(":");
	
        r.put("QOBJECTusername" ,up[0]);
        r.put("QOBJECTpassword" ,up[1]);
	}
	catch(Exception e)
	{
	    daemon.eventLog.sendMessage("***ERROR: Authorization Failure");
	    throw e;
	}
    }

    private void routeQObjectRequest(QMessage r, com.qkernel.http.HTTPServer.Request req)
    {
	try
	{
	    JSONObject json = objectRoutes.getJSONObject(req.getPath());
	    r.putString("QOBJECTclass", json.getString("className"));
            r.putString("QOBJECTmethod",json.getString("methodName"));	
        }
	catch(Exception e)
	{
        r.putString("QOBJECTclass" , "com.qkernel.DefaultRestObject");
        r.putString("QOBJECTmethod", "defaultRestMethod");	
	}
    }

    
    public abstract JSONObject jsonObjectRequest(QMessage request, com.qkernel.http.HTTPServer.Request req);



    //--------------------------------------------------------------------------------
    // METHOD   Joa()
    //
    // PURPOSE: Constructor
    //
    // INPUT:   1) Service name (String)
    //          2) Daemon reference
    //
    // PROCESS: Constructs stuff :-)
    //
    // RETURN:  None.
    //--------------------------------------------------------------------------------
    public Joa(String name, Daemon daemon)
    {
	super(name, daemon);
    }
}
