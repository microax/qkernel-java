package com.qkernel;
//
// EntityContainer.java        EntityContainer Abstract class
// ----------------------------------------------------------------------------
// History:
// --------
// 02/07/09 M. Gill	Add Global EntityQueue to container.
// 01/18/02 M. Gill	Allow for containers with no database.
// 06/21/00 M. Gill     Initial creation.
// ----------------------------------------------------------------------------
//
import java.lang.*;

// ----------------------------------------------------------------------------
// This class creates/loads all of the EnityObjects for a Qkernel server.
//
// The load() method must be implemented. load() must do the following for 
// each EnityObject used in the server:
//	1) instantiate each entity via "new"
//	2) Invoke the create method on the EnityObject
// Note:
// -----
//	Really bad things  can happen if EnityObject methods are invoked before 
//	load() is finished...:-)

// ----------------------------------------------------------------------------
public abstract class EntityContainer
{
    public DbPool DbPool;
    public Daemon Daemon;
    public EntityQueue entityQueue;


    //--------------------------------------------------------------------
    // Developers MUST implement this to load their EntityObjects 
    //
    // Invoke in the following way:
    //
    // 		Obj = new ObjClass();
    // 		Obj.create(this);
    //
    // --------------------------------------------------------------------
    public abstract void load();



    //-------------------------------------------
    // Create a Container with no Database
    //-------------------------------------------
    public void create(Daemon d)
    {
        Daemon =d;
        DbPool = null;
        Daemon.event_log.SendMessage("Container with no DB created...");
    }



    //-------------------------------------------
    // Create a DbPool within the container
    //-------------------------------------------
    public void create(String dbDriver, 
		  String dbServer,
		  String dbLogin,
		  String dbPassword, 
		  int MinConn, 
		  int MaxConn, 
		  String logFile, 
		  double DbResetTime,
		  Daemon d)
    {
        Daemon = d;

	//---------------------------------------
	// Create Database Connection pool.
	//---------------------------------------
	try
	{
	    DbPool = new DbPool(dbDriver, 
				dbServer,
				dbLogin,
				dbPassword, 
				MinConn, 
				MaxConn, 
				logFile, 
				DbResetTime);

            Daemon.event_log.SendMessage("DbPool initialized");
	}
	catch(Exception e)
	{
            Daemon.log("Error creating DbPool because: "+ e.getMessage());
            Daemon.log(e);
	}

        //-----------------------------
    	// Start Global EntityQueue
    	//----------------------------- 
	entityQueue = new EntityQueue("Global EntityQueue", d);
	entityQueue.init(this);
	entityQueue.start();
	
        Daemon.eventLog.sendMessage("Global EntityQueue Created...");

    }


    public EntityContainer()
    {

    }
}










