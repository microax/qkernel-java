package com.qkernel;
//
// MetaQueueBiz.java Business Object to handle front end requests to nodes
// ----------------------------------------------------------------------------
// History:
// --------
// 07/27/03 M. Gill	Initial creation.
// ----------------------------------------------------------------------------
//
import java.lang.*;

public final class MetaQueueBiz extends BusinessObject 
{

    public QMessage dispatch(QMessage req)
    {
	String handlerClass = (String)req.get("CHN-CLASS");
        String handlerMethod= (String)req.get("CHN-METHOD");

        daemon.eventLog.sendMessage(handlerClass+"."+handlerMethod+" requested.");

	QMessage reply = new QMessage();

	String pubUser		= req.getString("CHN-USERNAME");
   	String pubPassword	= req.getString("CHN-PASSWORD");
	String pubLicense	= req.getString("CHN-LICENSE");
	String myUser		= req.getString("QOBJECTusername");
	String myPassword	= req.getString("QOBJECTpassword");
	String myLicense	= req.getString("QOBJECTlicense");

	if( !pubUser.equals(myUser)
	 || !pubPassword.equals(myPassword)
	 || !pubLicense.equals(myLicense) )
	{
            daemon.eventLog.sendMessage("*** ERROR *** Bad username/password or license key in request!");
	    reply.putString("STATUS", "USER_INVALID");
	}
	else
	{
	    //-------------------------------------
	    // We Queue up this request and return
	    //-------------------------------------
	    MetaQueueDispatch d = (MetaQueueDispatch)req.get("QOBJECTdispatch");
	    d.sendMessage(req);
	    reply.putString("STATUS", "OK");
	}
	return( reply);
    }


    //---------------------------------
    // Public constructor
    //---------------------------------
    public MetaQueueBiz(Daemon d)
    {
	super(d);
    }
}

