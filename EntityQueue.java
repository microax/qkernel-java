package com.qkernel;
//
// EntityQueue.java        A Mos class to Queue Entity Update/Inserts
// ----------------------------------------------------------------------------
// History:
// --------
// 07/09/02 M. Gill	protect start() from being called more than once
//
// 04/18/02 M. Gill	Improved error logging.
//
// 03/27/02 M. Gill	Close connection and statement in finally block
//
// 09/22/00 M. Gill	Changed start() to call super.start() after 
//			init. Container and DbPool...this fixes potential
//		   	race condition bug.
//
// 06/21/00 M. Gill	Initial creation.
// ----------------------------------------------------------------------------
//
import java.lang.*;
import java.sql.*;

// ----------------------------------------------------------------------------
//
// ----------------------------------------------------------------------------
public final class EntityQueue extends Mos
{
    public EntityContainer Container;
    public DbPool DbPool;

    protected boolean started = false;

    //--------------------------------------------------------------------------------
    // METHOD MessageHandler()
    //
    // PURPOSE:	Process Message Queue
    //
    // INPUT:	Mos MessageNode.
    //--------------------------------------------------------------------------------
    public void MessageHandler(MessageNode n)
    {
	Connection conn	= null;
   	Statement stmt	= null;
	String Query 	= (String)n.object;

	try
     	{
	    conn		= DbPool.getConnection();
	    stmt		= conn.createStatement();
	    int result 		= stmt.executeUpdate(Query);
	}
	catch(Exception e)
	{
            daemon.eventLog.sendMessage(1, "*** ERROR in executeUpdate() using: "+ Query);
	    daemon.eventLog.sendMessage(1, e);
	}
	finally
	{
	    try
	    {
	    stmt.close();
	    DbPool.freeConnection(conn);
	    }
	    catch(Exception z){}
	}
    }



    public void put(String str)
    {
	if(daemon.waitShutdown())
	{
	    Semaphore s = new Semaphore(0);
	    s.Wait();
	}

	if(started)
	    SendMessage(str);
	else
	    daemon.event_log.SendMessage("***ERROR *** put() before entityQueue.start() !");	    
    }


    public void start()
    {
	if(!started)
	{
	super.start();
	started = true;
	}
    }

    public void init(EntityContainer c)
    {
	Container = c;
	DbPool    = c.DbPool;
    }

    public EntityQueue(String name, Daemon d)
    {
	super(name, d);

	started = false;

    }
}












