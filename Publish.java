package com.qkernel;

import java.lang.*;
@SuppressWarnings("unchecked")

public class Publish extends BusinessDelegate implements SerialVersionId
{
    static final long serialVersionUID = SERIAL_VERSION_UID;

    private String myTopic;
    private String myUsername;
    private String myPassword;

    //--------------------------------------------------------------------------------
    // METHOD   send()
    //
    // PURPOSE: Sends a MetaQueueMessage to a metaQueue.NET Service Node
    //
    // INPUT:   MetaQueueMessage ==> request
    //
    // RETURN:  Throws Exception if channel is not found or unavailable
    //--------------------------------------------------------------------------------
    public QMessage send(MetaQueueMessage m) throws Exception
    {
	QMessage qmessage = new QMessage();
	
	qmessage.putString("PUB-TOPIC"		, myTopic);
	qmessage.putString("PUB-USERNAME"	, myUsername);
	qmessage.putString("PUB-PASSWORD"	, myPassword);
	qmessage.putString("PUB-SMS"		, m.getSmsText());
	qmessage.putString("PUB-TEXTBODY"	, m.getText());
	qmessage.putString("PUB-FIRSTNAME"	, m.getSenderFirstName());
	qmessage.putString("PUB-LASTNAME"	, m.getSenderLastName());
	qmessage.putString("PUB-EMAIL"		, m.getSenderEmail());
	qmessage.putString("PUB-DATE"		, m.getMessageTime());
	qmessage.putString("PUB-PHONE"		, m.getSmsNumber());
	qmessage.putString("PUB-SOURCE"		, m.getPubSource());

	if(m.getAttachment() != null)
	{ 	
	    qmessage.put("PUB-ATTACHMENT"	,  m.getAttachment());
	}
	
	QMessage r = new QMessage();
	try
	{
	    r = invoke("business.pubsub.PubBiz","publish",qmessage,null);
	}
	catch(Exception e)
	{
	    throw e;
	}
	return(r);
    }

    public String getOrbAddress(Daemon d)
    {
	return("");
    }

    public int getOrbPort(Daemon d)
    {
	return(0);
    }

    public boolean isRemote()
    {
	return(true);
    }

    public Publish(String topic, String host, int port, String username, String password)
    {
	super(host,port);
	myTopic    = topic;
	myUsername = username;
	myPassword = password;
    }
}


