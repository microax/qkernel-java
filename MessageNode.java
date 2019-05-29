package com.qkernel;
//
// MessageNode.java   Container for Mos Messages
// ----------------------------------------------------------------------------
// History:
// --------
// 07/12/01 M. Gill	Made members public.
// 09/20/97 M. Gill	Initial creation.
// ----------------------------------------------------------------------------
//
import java.util.Date;

public class MessageNode
{
    public Object     object;	    // Mos Messages can be of any object type
    public Thread     thread;	    // Thread which invoked SendMessage()
    public Date       timestamp;    // Node creation Date/Time stamp.


    public Object getObject()
    {
	return(this.object);
    }

    public Thread getThread()
    {
	return(this.thread);
    }

    public Date getTimestamp()
    {
	return(this.timestamp);
    }

    public void setObject(Object o)
    {
	this.object = o;
    }

    public void setThread(Thread o)
    {
	this.thread = o;
    }

    public void setTimestamp(Date o)
    {
	this.timestamp = o;
    }
}

