package com.qkernel;
//
// VoiceQProxy.java       Proxy for Remote IVR Service 
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
import com.qkernel.json.*;
@SuppressWarnings("unchecked")
//---------------------------------------------------------------------------
// This is the remote IVR Proxy for VoiceQ. VoiceQProxy is a Joa 
// (Java Object Adapter) that implements the IvrService interface. 
// An Application can invoke the Remote IVR service interface via this proxy.
//-----------------------------------------------------------------------------
//
public final class VoiceQProxy extends IvrProxy
{
    private Eos userAgent;
    private String remoteIp;
    private int remotePort;
    private String myIp;
    private int myPort;
    private Daemon daemon;

    //---------------------------------------------------------------------------------
    // METHOD   postIvrEvent()
    //
    // PURPOSE: Invoked by Remote Ivr service to send Ivr message to an Application.
    //
    // INPUT:   IvrRequest (via JoaContext)
    // RETURN:  IvrReply   (via JoaContext)
    //---------------------------------------------------------------------------------
    public void postIvrEvent(JoaContext ctx)
    {
	IvrReply reply;
	IvrRequest ivr;

        daemon.event_log.SendMessage("postIvrEvent() called...");

	ivr = (IvrRequest)ctx.ReadObject();

	//-------------------------------------------
	// Send event to our Ivr Application
	//-------------------------------------------
	IvrMessage ivrMessage = new IvrMessage();
	ivrMessage.Cli     = ivr.getParameter("cli");
	ivrMessage.Message = ivr.getParameter("message");

	EventMessage evt = new EventMessage();
	evt.Event  = IVR_MESSAGE;
        evt.Data   = (Object)ivrMessage;

	userAgent.SendMessage(evt);

	reply = new IvrReply();
	reply.put("status","ok");

	ctx.Reply(reply);
	ctx.Close();
    }




    //---------------------------------------------------------------------------------
    // METHOD   register()
    //
    // PURPOSE: Application registration.
    //
    // INPUT:   IvrApplication
    // RETURN:  Status.
    //---------------------------------------------------------------------------------
    public String register(Eos a)
    {
	String key = daemon.getLicenseKey();
	daemon.event_log.SendMessage("Registering "+key+" with VoiceQ");

	userAgent = a;
	IvrRequest request = new IvrRequest();
	IvrReply   reply;

	request.put("command","register");
	request.put("ipaddress", myIp);
	request.put("port", Integer.valueOf(myPort));

	reply = voiceQRequest(request);

	String status = (String)reply.get("status");
	
	daemon.event_log.SendMessage("VoiceQ registration status="+status);
	return(status);
    }


    //---------------------------------------------------------------------------------
    // METHOD   dialPrompt()
    //
    // PURPOSE: Dial a number and send prompt
    //
    // INPUT:   Prompt, number
    // RETURN:  Status.
    //---------------------------------------------------------------------------------
    public String dialPrompt(String prompt, String number)
    {
	daemon.event_log.SendMessage("Dialing "+number+" to send "+prompt);

	IvrRequest request = new IvrRequest();
	IvrReply   reply;

	request.put("command", "dialprompt");
	request.put("prompt", prompt);
	request.put("number", number);

	reply = voiceQRequest(request);

	String status = (String)reply.get("status");

	daemon.event_log.SendMessage("VoiceQ status="+status);
	return(status);
    }

    //---------------------------------------------------------------------------------
    // METHOD   waitCall()
    //
    // PURPOSE: Setup for inbound call.
    //
    // INPUT:   None.
    // RETURN:  Status.
    //---------------------------------------------------------------------------------
    public String waitCall()
    {
	IvrRequest request = new IvrRequest();
	IvrReply   reply;

	request.put("command", "waitcall");

	reply = voiceQRequest(request);

	return((String)reply.get("status"));
    }

    //---------------------------------------------------------------------------------
    // METHOD   hangup()
    //
    // PURPOSE: Disconnect caller.
    //
    // INPUT:   None.
    // RETURN:  Status.
    //---------------------------------------------------------------------------------
    public String hangup()
    {
	IvrRequest request = new IvrRequest();
	IvrReply   reply;

	request.put("command", "hangup");

	reply = voiceQRequest(request);

	return((String)reply.get("status"));
    }



    //---------------------------------------------------------------------------------
    // METHOD   prompt()
    //
    // PURPOSE: Send Vocie Prompt 
    //
    // INPUT:   Prompt.
    // RETURN:  Status.
    //---------------------------------------------------------------------------------
    public String prompt(String prompt)
    {
	IvrRequest request = new IvrRequest();
	IvrReply   reply;

	request.put("command", "prompt");
	request.put("prompt", prompt);

	reply = voiceQRequest(request);

	return((String)reply.get("status"));
    }


    //---------------------------------------------------------------------------------
    // METHOD   getDigit()
    //
    // PURPOSE: Get DTMF Digits
    //
    // INPUT:   None.
    // RETURN:  Status.
    //---------------------------------------------------------------------------------
    public String getDigit()
    {
	IvrRequest request = new IvrRequest();
	IvrReply   reply;

	request.put("command", "getdigit");

	reply = voiceQRequest(request);

	return((String)reply.get("status"));
    }

    //---------------------------------------------------------------------------------
    // METHOD   quit()
    //
    // PURPOSE: Dial a number and send prompt
    //
    // INPUT:   None.
    // RETURN:  Status.
    //---------------------------------------------------------------------------------
    public String quit()
    {
	IvrRequest request = new IvrRequest();
	IvrReply   reply;

	request.put("command", "quit");

	reply = voiceQRequest(request);

	return((String)reply.get("status"));
    }


    //---------------------------------------------------------------------------------
    // METHOD   restart()
    //
    // PURPOSE: Application restart request.
    //
    // INPUT:   IvrApplication
    // RETURN:  None.
    //---------------------------------------------------------------------------------
    public String restart(Eos a)
    {
	userAgent = a;

	daemon.event_log.SendMessage("Restarting IvrApplication...");

	//--------------------------------------
	// Not sure what to do here...
	// but,  let's register again :-)
	//--------------------------------------
	return(register(userAgent));

    }



    //---------------------------------------------------------------------------------
    // METHOD   voiceQRequest()
    //
    // PURPOSE: Invoke remote voiceQ method
    //
    // INPUT:   IvrRequest
    // RETURN:  IvrReply
    //---------------------------------------------------------------------------------
    public IvrReply voiceQRequest(IvrRequest request)
    {
	IvrReply reply  = new IvrReply();
	JoaProxy obj	= new JoaProxy(remoteIp, remotePort);

	try
	{
	    request.put("license", daemon.getLicenseKey());

	    obj.Connect();
	    obj.SendRequest("ivrRequest", request);
  	    reply =(IvrReply)obj.GetReply();
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
    // METHOD  VoiceQProxy()
    //
    // PURPOSE: Constructor.
    //---------------------------------------------------------------------------------
    public VoiceQProxy(String myip, int myport, String rip, int port, Daemon d)
    {
	super("VoiceQProxy", d);

	myIp		= myip;
	myPort		= myport;
	remoteIp	= rip;
	remotePort	= port;
	daemon 		= d;

    }
}
