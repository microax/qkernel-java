package com.qkernel.comm;
//
// SmsModemServiceQueue.java	A Mos class to Queue SmsModem requests
// ----------------------------------------------------------------------------
// History:
// --------
// 07/12/01 M. Gill	Initial creation.
// ----------------------------------------------------------------------------
//
import java.lang.*;
import com.qkernel.*;

public final class SmsModemServiceQueue extends Mos
{
    private String remoteHost;
    private int remotePort;

    //--------------------------------------------------------------------------------
    // METHOD MessageHandler()
    //
    // PURPOSE:	Process Message Queue
    //
    // INPUT:	Mos MessageNode.
    //--------------------------------------------------------------------------------
    public void MessageHandler(MessageNode n)
    {
	SmsMessage sms      = (SmsMessage)n.object;
	JoaProxy obj        = new JoaProxy(remoteHost, remotePort);
        SmsRemoteStatus rsp = new SmsRemoteStatus();

	try
	{
	    obj.Connect();
	    obj.SendRequest("postSmsMessage", sms);
  	    rsp =(SmsRemoteStatus)obj.GetReply();
	    obj.Close();

	    if (rsp.status != 0)
	      	daemon.event_log.SendMessage("***Error : "+rsp.errorString);
	    else
	      	daemon.event_log.SendMessage("SmsRemote Request OK...");
	}
	catch(Exception e)
	{
	    daemon.event_log.SendMessage("***Error Sending Remote SMS request becasue:"
					 +e.getMessage()); 
	}
    }

    public void start()
    {
	super.start();
    }

    public SmsModemServiceQueue(String name, Daemon d, String rhost, int rport)
    {
	super(name, d);

	remoteHost = rhost;
	remotePort = rport;

    }
}












