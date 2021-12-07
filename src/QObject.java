package com.qkernel;
//
// QObject.java        Remote Object
// ----------------------------------------------------------------------------
// History:
// --------
// 12/10/20 M. Gill     Add Exception logging
// 01/15/02 M. Gill	Initial Creation
// ----------------------------------------------------------------------------
//
public abstract class QObject extends Object
{

    public Daemon daemon;

    public void log(String message)
    {
	daemon.eventLog.sendMessage(message);
    }

    public void log(Exception e)
    {
	daemon.eventLog.sendMessage(e);
    }
    
    public QObject(Daemon d)
    {
      daemon = d;
    }
}

