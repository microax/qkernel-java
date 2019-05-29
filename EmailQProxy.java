package com.qkernel;
//
// EmailQProxy.java       Proxy for Remote Email Service 
// ----------------------------------------------------------------------------
// History:
// --------
// 08/01/01 M. Gill	Initial creation.
// ----------------------------------------------------------------------------
//
import java.lang.*;
import java.util.*;
import java.io.*;
import com.qkernel.json.*;

@SuppressWarnings("unchecked")

//---------------------------------------------------------------------------
// This is the remote Proxy for EmailQ. 
// 
// ** Should be like other proxies...but, this version is a temporary patch.
//-----------------------------------------------------------------------------
//
public final class EmailQProxy extends Joa
{
    private Eos userAgent;
    private String remoteIp;
    private int remotePort;
    private String myIp;
    private int myPort;
    private Daemon daemon;


    //---------------------------------------------------------------------------------
    // METHOD   register()
    //
    // PURPOSE: Application registration.
    //
    // INPUT:   Application
    // RETURN:  Status.
    //---------------------------------------------------------------------------------
    public String register(Eos a)
    {
	String key = daemon.getLicenseKey();
	daemon.event_log.SendMessage("Registering "+key+" with EmailQ");

	userAgent = a;
	JoaRequest request = new JoaRequest();
	JoaReply   reply;

	request.put("command","register");
	request.put("ipaddress", myIp);
	request.put("port", Integer.valueOf(myPort));

	reply = emailQRequest(request);

	String status = (String)reply.get("status");
	
	daemon.event_log.SendMessage("EmailQ registration status="+status);
	return(status);
    }


    //---------------------------------------------------------------------------------
    // METHOD   sendEmail()
    //
    // PURPOSE: Send an Email
    //
    // INPUT:   From, To, Body
    // RETURN:  Status.
    //---------------------------------------------------------------------------------
    public String sendEmail(String from, String to, String subject, String body)
    {
	daemon.event_log.SendMessage("Sending email to: "+to);

	JoaRequest request = new JoaRequest();
	JoaReply   reply;

	request.put("command", "sendemail");
	request.put("from", from);
	request.put("to", to);
	request.put("subject", subject);
	request.put("body", body);

	reply = emailQRequest(request);

	String status = (String)reply.get("status");

	daemon.event_log.SendMessage("EmailQ status="+status);
	return(status);
    }



    //---------------------------------------------------------------------------------
    // METHOD   emailQRequest()
    //
    // PURPOSE: Invoke remote EmailQ method
    //
    // INPUT:   JoaRequest
    // RETURN:  JoaReply
    //---------------------------------------------------------------------------------
    public JoaReply emailQRequest(JoaRequest request)
    {
	JoaReply reply  = new JoaReply();
	JoaProxy obj	= new JoaProxy(remoteIp, remotePort);

	try
	{
	    request.put("license", daemon.getLicenseKey());

	    obj.Connect();
	    obj.SendRequest("emailRequest", request);
  	    reply =(JoaReply)obj.GetReply();
	    obj.Close();
	}
	catch(Exception e)
	{
	    daemon.event_log.SendMessage("***Error Sending Remote request becasue:"
					 +e.getMessage()); 
	    reply.put("status", "proxyerror");
	    return(reply);
	}
	return(reply);
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
    
    //---------------------------------------------------------------------------------
    // METHOD  EmailQProxy()
    //
    // PURPOSE: Constructor.
    //---------------------------------------------------------------------------------
    public EmailQProxy(String myip, int myport, String rip, int port, Daemon d)
    {
	super("EmailQProxy", d);

	myIp		= myip;
	myPort		= myport;
	remoteIp	= rip;
	remotePort	= port;
	daemon 		= d;

    }
}
