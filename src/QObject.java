package com.qkernel;
//
// QObject.java        Remote Object
// ----------------------------------------------------------------------------
// History:
// --------
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
    
    public QObject(Daemon d)
    {
      daemon = d;
    }
}

