package com.qkernel;
//
// EventMessage.java   Event Message Container class
// ----------------------------------------------------------------------------
// History:
// --------
// 09/18/03 M. Gill	Add getData() and getEvent()
// 09/20/97 M. Gill	Initial creation.
// ----------------------------------------------------------------------------
//
import java.util.Date;

public class EventMessage
{
    //----------These problobly don't belong here anymore
    //-----------------------------------------------------
    public static final int ACTION_EVENT =0;
    public static final int IO_EVENT     =1;
    public static final int IO_ERROR     =2;
    public static final int USER_EVENT   =100;

    public int        Event;
    public Object     Data;
    Thread            thread;		// Thread which invoked SendMessage()
    Date              timestamp;	// Node creation Date/Time stamp.

    public Object getData()
    {
	return(this.Data);
    }

    public int getEvent()
    {
	return(this.Event);
    }

    public void putEvent(int event)
    {
	this.Event = event;
    }

    public void putData(Object data)
    {
	this.Data = data;
    }


}

