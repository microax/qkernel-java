package com.qkernel;
//
// SmsRemote.java       Proxy for Remote SMS Service 
// ----------------------------------------------------------------------------
// History:
// --------
// 07/05/01 M. Gill	Initial creation.
// ----------------------------------------------------------------------------
//
import java.lang.*;
import java.util.*;
import java.io.*;
import com.qkernel.json.*;

//---------------------------------------------------------------------------
// This is the remote SMS Proxy. SmsRemote is a Joa (Java Object Adapter)
// that implements the smsService interface. A SmsApplication can send 
// messages to a Remote SMSC or GSM Modem via this proxy.
//
//-----------------------------------------------------------------------------
//
public final class SmsRemote extends SmsProxy
{
    private SmsApplication userAgent;
    private String remoteIp;
    private int remotePort;
    private String myNumber;
    private String myIp;
    private int myPort;
    private SmsRemoteServiceQueue serviceQueue;
    private Daemon daemon;

    //---------------------------------------------------------------------------------
    // METHOD   postSmsMessage()
    //
    // PURPOSE: Invoked by Remote SMS service to send sms message to SmsApplication.
    //
    // INPUT:   SmsMessage (via JoaContext)
    // RETURN:  SmsRemoteStatus (via JoaContext)
    //---------------------------------------------------------------------------------
    public void postSmsMessage(JoaContext ctx)
    {
	SmsRemoteStatus replyObj;
	SmsMessage sms;

        daemon.event_log.SendMessage("postSmsMessage() called...");

	sms = (SmsMessage)ctx.ReadObject();

	daemon.event_log.SendMessage("Received SMS from:"+ sms.PhoneNumber 
					+" Message="+sms.Message);

	//-------------------------------------------
	// Send event to our SMS Application
	//-------------------------------------------
	EventMessage evt = new EventMessage();
	evt.Event  = SMS_MESSAGE;
        evt.Data   = (Object)sms;

	userAgent.SendMessage(evt);


	replyObj = new SmsRemoteStatus();
	replyObj.status=0;
	replyObj.errorString ="OK";

	ctx.Reply(replyObj);
	ctx.Close();
    }


    //---------------------------------------------------------------------------------
    // METHOD   sendSmsMessage()
    //
    // PURPOSE: Invoked by application to send sms message.
    //
    // INPUT:   SmsMessage
    // RETURN:  None.
    //---------------------------------------------------------------------------------
    public void sendSmsMessage(SmsMessage m)
    {
	serviceQueue.SendMessage(m);

	daemon.event_log.SendMessage("Sending SMS to:"+ m.PhoneNumber 
					+" Message="+m.Message);
    }

    //---------------------------------------------------------------------------------
    // METHOD   register()
    //
    // PURPOSE: Application registration.
    //
    // INPUT:   SmsApplication
    // RETURN:  None.
    //---------------------------------------------------------------------------------
    public void register(SmsApplication a)
    {
	userAgent = a;

	//---------------------------------------------------------------
	// First, create a Service Queue to handle our remote requests
	//---------------------------------------------------------------
	serviceQueue = new SmsRemoteServiceQueue("SmsRemote Queue", daemon, remoteIp, remotePort);
	serviceQueue.start();

	//-----------------------------------------------------------------
	// Now, we create a ServiceNode that holds our port number 
	// and local Ipaddress ..the remote service will use this 
	// to register us.
	//-----------------------------------------------------------------
	SmsServiceNode serviceNode = new SmsServiceNode();

	serviceNode.phoneNumber	   = myNumber;
	serviceNode.ipAddress	   = myIp;
	serviceNode.port           = myPort;

	//-----------------------------------------------------------------
	// Register with the remote sms service...
	//-----------------------------------------------------------------
	JoaProxy obj        = new JoaProxy(remoteIp, remotePort);
        SmsRemoteStatus rsp = new SmsRemoteStatus();

	try
	{
	    obj.Connect();
	    obj.SendRequest("smsRegister", serviceNode);
  	    rsp =(SmsRemoteStatus)obj.GetReply();
	    obj.Close();

	    if (rsp.status != 0)
	    {
	      	daemon.event_log.SendMessage("***Error : "+rsp.errorString);
		return;
	    }
	    else
	      	daemon.event_log.SendMessage("Remote request OK...");
	}
	catch(Exception e)
	{
	    daemon.event_log.SendMessage("***Error Sending Remote request becasue:"
					 +e.getMessage()); 
	    return;
	}

	//-----------------------------------------------------------------
	// Now, that everything's groovey :-)
	// We can send the GSM_OK event to the registered application
	//-----------------------------------------------------------------
	EventMessage e = new EventMessage();
	e.Event = GSM_OK;
	userAgent.SendMessage(e);

	daemon.event_log.SendMessage("SmsApplication registered");
    }



    //---------------------------------------------------------------------------------
    // METHOD   restart()
    //
    // PURPOSE: Application restart request.
    //
    // INPUT:   SmsApplication
    // RETURN:  None.
    //---------------------------------------------------------------------------------
    public void restart(SmsApplication a)
    {
	userAgent = a;

	//--------------------------------------
	// Not sure what to do here...
	// but,  let's register again :-)
	//--------------------------------------
	register(userAgent);

	daemon.event_log.SendMessage("SmsApplication restarted");
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
    // METHOD  SmsRemote()
    //
    // PURPOSE: Constructor.
    //---------------------------------------------------------------------------------
    public SmsRemote(String mynumber, String myip, int myport, String rip, int port, Daemon d)
    {
	super("SmsRemote Proxy", d);

	myNumber	= mynumber;
	myIp		= myip;
	myPort		= myport;
	remoteIp	= rip;
	remotePort	= port;
	daemon 		= d;

    }
}
