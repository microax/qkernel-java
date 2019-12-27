package com.qkernel.comm;
//
// SmsModem.java       Proxy for Local or Remote GSM Modem 
// ----------------------------------------------------------------------------
// History:
// --------
// 07/05/01 M. Gill	Initial creation.
// ----------------------------------------------------------------------------
//
import java.lang.*;
import java.util.*;
import java.io.*;
import com.qkernel.*;
import com.qkernel.json.*;

public final class SmsModem extends SmsProxy implements XircomEvents
{
    private SmsApplication userAgent;
    private XircomGsm modem;
    private String remoteIp;
    private int remotePort;
    private String myNumber;
    private String device;
    private SmsModemServiceQueue serviceQueue;
    private Daemon daemon;
    private boolean modem_running= false;

    //---------------------------------------------------------------------------------
    // METHOD   smsRegister()
    //
    // PURPOSE: Invoked by Remote SMS SmsApplication to register with this service.
    //
    // INPUT:   SmsServiceNode (via JoaContext)
    // RETURN:  SmsRemoteStatus (via JoaContext)
    // 
    // NOTE:	*** This can only be invoked when Qorb.remote_interface == true ***
    //---------------------------------------------------------------------------------
    public void smsRegister(JoaContext ctx)
    {
	SmsRemoteStatus replyObj;
	SmsServiceNode sn;

        daemon.event_log.SendMessage("smsRegister() called...");

	//-------------------------------------------------------
	// Get Remote Service Node...
	//-------------------------------------------------------
	sn = (SmsServiceNode)ctx.ReadObject();

	daemon.event_log.SendMessage("Registration request from:"+ sn.ipAddress 
					+" on port:"+sn.port);
	remoteIp  = sn.ipAddress;
	remotePort= sn.port;
	//---------------------------------------------------------------
	// Create a Service Queue to handle message dispatch...
	//---------------------------------------------------------------
	if(modem_running == false)
	{
	serviceQueue = new SmsModemServiceQueue("SmsModem Queue", daemon, remoteIp, remotePort);
	serviceQueue.start();
	//----------------------------------------------------------------
	// Create and start the GSM modem driver...
	//----------------------------------------------------------------
	modem = new XircomGsm(device, BITRATE, DATABITS,STOPBITS,PARITY, myNumber, daemon);
	modem.start(this);
	modem_running = true;
	}

	replyObj = new SmsRemoteStatus();
	replyObj.status=0;
	replyObj.errorString ="OK";

	ctx.Reply(replyObj);
	ctx.Close();
    }


    //---------------------------------------------------------------------------------
    // METHOD   postSmsMessage()
    //
    // PURPOSE: Invoked by Remote SMS SmsApplication to send a SmsMessage.
    //
    // INPUT:   SmsMessage (via JoaContext)
    // RETURN:  SmsRemoteStatus (via JoaContext)
    // 
    // NOTE:	*** This can only be invoked when Qorb.remote_interface == true ***
    //---------------------------------------------------------------------------------
    public void postSmsMessage(JoaContext ctx)
    {
	SmsRemoteStatus replyObj;
	SmsMessage sms;

        daemon.event_log.SendMessage("postSmsMessage() called...");

	//-------------------------------------------------------
	// Get Remote Service Node...
	//-------------------------------------------------------
	sms = (SmsMessage)ctx.ReadObject();

	if(modem_running == true)
	{
	    daemon.event_log.SendMessage("Sending SMS to:"+ sms.PhoneNumber 
					+" Message="+sms.Message);

	    EventMessage msg = new EventMessage();
	    msg.Event = SEND_SMS;
	    msg.Data  = sms;
	    modem.SendMessage(msg);
	}

	replyObj = new SmsRemoteStatus();
	replyObj.status=0;
	replyObj.errorString ="OK";

	ctx.Reply(replyObj);
	ctx.Close();
    }



    //---------------------------------------------------------------------------------
    // METHOD   onSmsMessage()
    //
    // PURPOSE: Invoked by modem when sms message received.
    //
    // INPUT:   SmsMessage
    // RETURN:  None.
    //---------------------------------------------------------------------------------
    public void onSmsMessage(SmsMessage m)
    {
	daemon.event_log.SendMessage("Received SMS from:"+ m.PhoneNumber 
					+" Message="+m.Message);
	if(remote_interface == true)
	{
	    //-------------------------------------------
	    // Send event to remote SMS Application
	    //-------------------------------------------
	    serviceQueue.SendMessage(m);
	}
	else
	{
	    //-------------------------------------------
	    // Send event to local SMS Application
	    //-------------------------------------------
	    EventMessage evt = new EventMessage();
	    evt.Event  = SMS_MESSAGE;
            evt.Data   = (Object)m;
	    userAgent.SendMessage(evt);
	}
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

	if(modem_running == true)
	{
	    daemon.event_log.SendMessage("Sending SMS to:"+ m.PhoneNumber 
					+" Message="+m.Message);

	    EventMessage msg = new EventMessage();
	    msg.Event = SEND_SMS;
	    msg.Data  = m;
	    modem.SendMessage(msg);
	}
    }

    //---------------------------------------------------------------------------------
    // METHOD   register()
    //
    // PURPOSE: Local Application registration.
    //
    // INPUT:   SmsApplication
    // RETURN:  None.
    //---------------------------------------------------------------------------------
    public void register(SmsApplication a)
    {
	userAgent = a;

	if(modem_running ==false)
	{
	    modem = new XircomGsm(device, BITRATE, DATABITS,STOPBITS,PARITY, myNumber, daemon);
	    modem.start(this);
	    modem_running = true;
	}

	EventMessage e = new EventMessage();
	e.Event = GSM_OK;
	userAgent.SendMessage(e);

	daemon.event_log.SendMessage("Local SmsApplication registered");
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

	daemon.event_log.SendMessage("Local SmsApplication restarted");
    }

    public JSONObject jsonObjectRequest(QMessage request, JSONObject argvs)
    {
	return new JSONObject();
    }


    //---------------------------------------------------------------------------------
    // METHOD  SmsModem()
    //
    // PURPOSE: Constructor.
    //---------------------------------------------------------------------------------
    public SmsModem(String mynumber, String mydevice, String ip, int port, Daemon d)
    {
	super("SmsModem Proxy", d);

	myNumber	= mynumber;
	device		= mydevice;
	remoteIp	= ip;
	remotePort	= port;
	daemon 		= d;

    }
    //---------------------------------------------------------------------------------
    // METHOD  SmsModem()
    //
    // PURPOSE: Constructor.
    //---------------------------------------------------------------------------------
    public SmsModem(String mynumber, String mydevice, Daemon d)
    {
	super("SmsModem Proxy", d);

	myNumber	= mynumber;
	device		= mydevice;
	daemon 		= d;
    }
}
