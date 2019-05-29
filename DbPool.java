package com.qkernel;
//
// DbPool.java        Database Connection Pool class
// ----------------------------------------------------------------------------
// History:
// --------
// 09/02/02 M. Gill	add uaLock() to block on max connections.
// 06/21/00 M. Gill	Initial creation.
// ----------------------------------------------------------------------------
//
import java.lang.*;
import java.io.*;
import java.sql.*;
import com.javaexchange.dbConnectionBroker.*;

//----------------------------------------------------------------------------
// The DbPool classs provides a simple interface for handing out and 
// returning database connections from a configurable pool of connections. 
// When constructed, DbPool creates a dynamic pool of connections and manages 
// them via with a background housekeeping thread...This avoids the overhead 
// required in establishing a new database connection (typically 1-2 seconds) 
// by reusing a collection of pre-established connections, and thus  
// profoundly improving the performance of  database-intensive applications.
//
//----------------------------------------------------------------------------
public final class DbPool extends Object
{

    protected DbConnectionBroker MyBroker;
    protected Semaphore uaLock;

    //--------------------------------------------------------------------------------
    // METHOD 	getConnection()
    //
    // PURPOSE:	This method hands out the connections in round-robin order. 
    //
    // INPUT:	None
    //
    // RETURN:  Connection Object
    //--------------------------------------------------------------------------------
    public Connection getConnection()
    {
	uaLock.Wait();

	return(MyBroker.getConnection());
    }



    //--------------------------------------------------------------------------------
    // METHOD 	freeConnection()
    //
    // PURPOSE:	Frees a connection, and places it connection back into the 
    //	        pool for reuse. 
    //
    // INPUT:	Connection Object
    //
    // RETURN:  String
    //--------------------------------------------------------------------------------
    public String freeConnection(Connection conn)
    {
	String rStr ="";

	rStr = MyBroker.freeConnection(conn);

	uaLock.Signal();

	return(rStr);
    }



    //--------------------------------------------------------------------------------
    // METHOD 	getUseCount()
    //
    // PURPOSE: Returns the number of connections in use. 
    //
    // INPUT:	None.
    //
    // RETURN:  int
    //--------------------------------------------------------------------------------
    public int getUseCount()
    {
	return(MyBroker.getUseCount());
    }

    //--------------------------------------------------------------------------------
    // METHOD 	getAge()
    //
    // PURPOSE:	Returns the age of a connection.
    //
    // INPUT:	Connection Object
    //
    // RETURN:  long
    //--------------------------------------------------------------------------------
    public long getAge(Connection conn)
    {
	return(MyBroker.getAge(conn));
    }


    //--------------------------------------------------------------------------------
    // METHOD 	getSize()
    //
    // PURPOSE:	Returns the number of connections in the pool.
    //
    // INPUT:	None.
    //
    // RETURN:  int
    //--------------------------------------------------------------------------------
    public int getSize()
    {
	return(MyBroker.getSize());
    }


    //--------------------------------------------------------------------------------
    // METHOD 	idOfConnection()
    //
    // PURPOSE:	Returns the local JDBC ID for a connection. 
    //
    // INPUT:	Connection
    //
    // RETURN:  int
    //--------------------------------------------------------------------------------
    public int idOfConnection(Connection conn)
    {
	return(MyBroker.idOfConnection(conn));
    }



    //--------------------------------------------------------------------------------
    // METHOD 	destroy()
    //
    // PURPOSE: 	Multi-phase shutdown. having following sequence:
    // 
    // 			1) getConnection() will refuse to return connections. 
    //			2) The housekeeping thread is shut down. Up to the time of 
    //			   millis milliseconds after shutdown of the housekeeping thread, 
    //			   freeConnection() can still be called to return used connections.
    //			3) After millis milliseconds after the shutdown of the 
    //			   housekeeping thread, all connections in the pool are closed. 
    //			4) If any connections were in use while being closed then a 
    //			   SQLException is thrown. 
    // 			5) The log is closed. 
    //
    // INPUT:	Time to wait in milli seconds
    //
    // RETURN: Throws SQLException
    //--------------------------------------------------------------------------------
    public void destroy(int millis) throws SQLException
    {
	MyBroker.destroy(millis);
    }


    //--------------------------------------------------------------------------------
    // METHOD 	destroy()
    //
    // PURPOSE: Less safe shutdown.
    //
    // INPUT:	None
    //
    // RETURN:
    //--------------------------------------------------------------------------------
    public void destroy()
    {
	MyBroker.destroy();
    }



    //--------------------------------------------------------------------------------
    // METHOD 	DbPool()
    //
    // PURPOSE:	Constructor
    //--------------------------------------------------------------------------------
    public DbPool(String dbDriver, 
		  String dbServer, 
		  String dbLogin,
		  String dbPassword, 
		  int minConns, 
		  int maxConns, 
		  String logFileString, 
		  double maxConnTime) throws IOException
    {
	MyBroker = new DbConnectionBroker(dbDriver,dbServer,
						dbLogin, dbPassword,
						minConns, maxConns,
						logFileString, maxConnTime);
	uaLock = new Semaphore(maxConns);

    }
}
