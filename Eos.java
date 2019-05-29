package com.qkernel;
//
// Eos.java     Event Oriented Server Class
// ----------------------------------------------------------------------------
// History:
// --------
// 09/18/03 M. Gill	Add sendEvent()
// 09/20/97 M. Gill 	Initial creation.
// ----------------------------------------------------------------------------
//
import java.util.Date;

public abstract class Eos extends Mos
{
    public void MessageHandler(MessageNode n)
    {
        EventMessage e = (EventMessage) n.object;
        e.thread       = n.thread;
        e.timestamp    = n.timestamp;

        EventHandler(e);
    }

    public void sendEvent(int event, Object data)
    {
	EventMessage e = new EventMessage();
	e.putEvent(event);
	e.putData(data);

	sendMessage(e);
    }

    public void sendEvent(int event)
    {
	EventMessage e = new EventMessage();
	e.putEvent(event);

	sendMessage(e);
    }


    public abstract void EventHandler(EventMessage e); 





    public Eos(String n, Daemon d)
    {
        super(n, d);
    }
}
