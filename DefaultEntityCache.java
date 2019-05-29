package com.qkernel;
//
// DefaultEntityCache.java     Cache Service for EntityObjects
// ----------------------------------------------------------------------------
// History:
// --------
//
// 08/25/02 M. Gill	Interface changed for put() and remove()
// 08/20/02 M. Gill	Renamed DefaultEntityCache...implements EntityCache
// 08/10/02 M. Gill	Add upper limit to cache.
// 07/02/02 M. Gill	Add removeAll() to drop all objects from cache.
// 01/15/02 M. Gill 	Initial creation.
// ----------------------------------------------------------------------------
//
import java.lang.*;
import java.util.*;

@SuppressWarnings("unchecked")

//-----------------------------------------------------------------------------
//
//-----------------------------------------------------------------------------
//
public final class DefaultEntityCache extends Thread implements EntityCache
{
    public     Daemon daemon;

    protected  Hashtable objTable;
    protected  Hashtable expTable;
    protected  String    myName;
    protected  int       myTicks;
    protected  int 	 myObjs;
    protected  int       peakSize;

    public 	final int K24HOUR		= 1400;
    public 	final int NOOBJLIMIT		= 0;
 
    //--------------------------------------------------------------------------------
    // METHOD 	run()
    //
    // PURPOSE:	This the main execution loop for Cache Service.      
    //
    // RETURN:  Never.
    //--------------------------------------------------------------------------------
    public void run()
    {
	Enumeration enm;
    	EntityCacheNode t;

	String eStr;

	setName(myName);

	if(myObjs ==0)
	    eStr ="Ready expire="+myTicks+" minutes";
	else
	    eStr ="Ready maxObjects="+myObjs+"  expire="+myTicks+" minutes";
	    
	daemon.eventLog.sendMessage(eStr);

        for( ;; )
        {
	    try
	    {
		//---------------------------
		// Wakeup once per minute
		//---------------------------
            	sleep(60000);

		if(!expTable.isEmpty())
   		{
		    enm = expTable.elements();

   		    while (enm.hasMoreElements())
		    {
			t = (EntityCacheNode)enm.nextElement();

			if(--t.ticks == 0)
			{
			    //----------------------------
			    // Expire unread nodes
			    //----------------------------
			    objTable.remove(t.key);
			    expTable.remove(t.key);
			}
		    }
		}
	    }
	    catch(Exception e)
	    {
		daemon.event_log.SendMessage("run block failed because: " + 
					     e.getMessage());
	    }
        }
    }


    //--------------------------------------------------------------------------------
    // METHOD remove()
    //
    // PURPOSE:	Remove all Objects from Cache
    //
    // INPUT:	None.
    //
    // RETURN:	None.
    //--------------------------------------------------------------------------------
    public void removeAll()
    {
	try
	{

	expTable.clear();
	objTable.clear();

	}catch (Exception e){}

    }

    //--------------------------------------------------------------------------------
    // METHOD remove()
    //
    // PURPOSE:	Remove an Object from Cache
    //
    // INPUT:	Object - key
    //
    // RETURN:	Object.
    //--------------------------------------------------------------------------------
    public Object remove(Object key)
    {
	           expTable.remove(key);
	Object o = objTable.remove(key);

        return(o);
    }


    //--------------------------------------------------------------------------------
    // METHOD get()
    //
    // PURPOSE:	Get an Object from Cache
    //
    // INPUT:	Object - key
    //
    // RETURN:	Object in objTable.
    //--------------------------------------------------------------------------------
    public Object get(Object key)
    {
	EntityCacheNode node =null;

	node       = (EntityCacheNode)expTable.get(key);

	if(node != null)
	    node.ticks = myTicks;

	return(objTable.get(key));
    }


    //--------------------------------------------------------------------------------
    // METHOD put()
    //
    // PURPOSE:	Put an Object into Cache
    //
    // INPUT:	Object - key
    //		Object - obj
    //
    // PROCESS:
    //		0) Check objTable.size and upper limit criteria.
    //		1) Create new EntityCacheNode, and place into expTable
    //		2) Put obj into objTable
    //
    // RETURN:	Object.
    //--------------------------------------------------------------------------------
    public Object put(Object key, Object obj)
    {
	EntityCacheNode node = null;
	Object          o    = null;

	if(myObjs == NOOBJLIMIT ||  myObjs > objTable.size())
	{ 
	node 		= new EntityCacheNode();
	node.key	= key;
	node.ticks	= myTicks;

	    expTable.put(key, node);
	o = objTable.put(key, obj);

	if(objTable.size() > peakSize)
	    peakSize = objTable.size();
	}
	return(o);
    }

    //--------------------------------------------------------------------------------
    // METHOD size()
    //
    // PURPOSE:	Return current Cache size
    //
    // RETURN:	int size.
    //--------------------------------------------------------------------------------
    public int size()
    {
	return(objTable.size() );
    }


    //--------------------------------------------------------------------------------
    // METHOD peakSize()
    //
    // PURPOSE:	Return largest Cache size
    //
    // RETURN:	int size.
    //--------------------------------------------------------------------------------
    public int peakSize()
    {
	return(peakSize );
    }



    //--------------------------------------------------------------------------------
    // METHOD start2()
    //
    // PURPOSE:	Starts the Cache Service Thread
    //
    // INPUT:	int => Max number of objects
    //
    // PROCESS:	Set myTicks.
    //
    // RETURN:	None.
    //--------------------------------------------------------------------------------
//    public void start2(int objMax)
//    {
//	start(K24HOUR, objMax);
//    }


    //--------------------------------------------------------------------------------
    // METHOD start2()
    //
    // PURPOSE:	Starts the Cache Service Thread
    //
    // INPUT:	int => Cache expire time in munutes.
    //		int => Max number of objects
    //
    // PROCESS:	Set myTicks.
    //
    // RETURN:	None.
    //--------------------------------------------------------------------------------
//    public void start2(int t, int objMax)
//    {
//	start(t, objMax);
//    }


    //--------------------------------------------------------------------------------
    // METHOD start()
    //
    // PURPOSE:	Starts the Cache Service Thread
    //
    // INPUT:	int => Cache expire time in munutes.
    //		int => Max number of objects
    //
    // PROCESS:	Set myTicks.
    //
    // RETURN:	None.
    //--------------------------------------------------------------------------------
    public void start(int t, int objMax)
    {
	myObjs  = objMax;
	myTicks = t;
        super.start();
    }

    //--------------------------------------------------------------------------------
    // METHOD start()
    //
    // PURPOSE:	Starts the Cache Service Thread
    //
    // INPUT:	int => Cache expire time in munutes.
    //
    // PROCESS:	Set myTicks.
    //
    // RETURN:	None.
    //--------------------------------------------------------------------------------
    public void start(int t)
    {
	myTicks = t;
	myObjs  = NOOBJLIMIT;
        super.start();
    }



    //--------------------------------------------------------------------------------
    // METHOD start()
    //
    // PURPOSE:	Starts the Cache Service Thread
    //
    // INPUT:	none
    //
    // PROCESS:	Set myTicks to default.
    //
    // RETURN:	None.
    //--------------------------------------------------------------------------------
    public void start()
    {
	myTicks = K24HOUR; 	   // default = 24 hours
	myObjs  = NOOBJLIMIT;      // no upper limit on Objects
        super.start();
	
    }



    //--------------------------------------------------------------------------------
    // METHOD EntityCache()
    //
    // PURPOSE:	Public Contructor
    //
    // INPUT:	Referance to Daemon object.
    //
    // PROCESS:	Construct stuff :-)
    //
    // RETURN:	None.
    //--------------------------------------------------------------------------------
    public DefaultEntityCache(String n, Daemon d)
    {
	myName =n;
        daemon =d;

	peakSize =0;

	expTable = new Hashtable();
	objTable = new Hashtable();
    }
}
