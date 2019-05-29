package com.qkernel;
//
// EntityObject.java        EntityObject class
// ----------------------------------------------------------------------------
// History:
// --------
// 08/20/02 M. Gill	Add support for user supplied cache.
//
// 07/09/02 M. Gill	Add Global EntityQueue support.
//
// 04/18/02 M. Gill	Improved Logging to include stacktrace
//
// 04/14/02 M. Gill	Add executeQuery() supporting ArrayList
//
// 03/26/02 M. Gill	Close connections and statements in finally block
//
// 01/15/02 M. Gill	1) Add EntityCache support.
//			2) Changed create to call EntityQueue.init().
//
// 01/09/01 M. Gill	Add executeMySqlInsert() to insert MySql tables with 
//			AUTO INCREMENT keys. executeMySqlInsert() returns the 
//			new key value via MySql's LAST_INSERT_ID() function.
//
// 12/19/00 M. Gill	Add closeResultSet() to close ResultSet and Statement
//			used in a (ResultSet executeQuery(String)) invocation.
//
// 11/22/00 M. Gill	Replaced executeLoadVector and executeLoadHashtable 
//			with executeQuery() methods. Renamed public instance
//			variables. Add init() method invoked from create().
//			Removed onLoad() and setEntity(), developers should
//			use executeQuery() methods for loading objects.
//			Added entityLock() and entityUnlock methods....
//				
// 09/24/00 M. Gill	Add executeLoadVector and executeLoadHashtable().
//
// 07/23/00 M. Gill	Remove load() call from create(). Containers
//			must explicitly call load()...This supports
//			multi-server-shared-entities where a container
//			may not want to load entity instances.
//
// 07/08/00 M. Gill	Add getTimeString() method.	
// 06/21/00 M. Gill	Initial creation.
// ----------------------------------------------------------------------------
//
import java.lang.reflect.*;
import java.lang.*;
import java.util.*;
import java.sql.*;

@SuppressWarnings("unchecked")
//
// ----------------------------------------------------------------------------
// Classes, which extend EntityObject class, encapsulate the behavior of a 
// relational database entity, and it's business rules...A goal of the qkernel 
// framework is to provide an efficient and flexible mechanism for 
// Object-Relational mapping in a multi-tiered application... 
// 
// Every EntityObject contains a EntityQueue task that may be used to defer 
// processing of insert, update and delete queries, and a Database Connection 
// Pool...EntitiyObject also contains a Hashtable and a Vector which can be 
// used to cache result Entities.
//
// An EntityContainer will initially invoke the create() method to initialize 
// and load an EntityObject. Developers  may optionally  implement the load() 
// method which is (invoked by create()) to load instances of entities into 
// memory. The onload() method provides the easiest way to load entities into 
// memory it will accept any query string to select rows from the database 
// and invoke setEntity() for each row returned. Developers can implement load() 
// to call onLoad() and implement setEntity() to process the resultset...
//
// executeUpdate() and executeQuery() methods are provided, allowing the developer 
// to perform common operations on the database. Additionally , DbPool can be 
// used directly with standard JDBC methods to perform any valid JDBC request.
//
// Future enhancements:
//   1) Add support for java 2 Collections and iterators.  
//   2) Possibility replace Vector and Hashtable classes with a more advanced
//      caching mechanism.
// ----------------------------------------------------------------------------
public abstract class EntityObject extends Object
{
    public DbPool 		entityDbPool;
    public Daemon 		daemon;
    public EntityContainer 	entityContainer;
    public EntityQueue     	entityQueue;
    public EntityQueue     	localEntityQueue;
    public EntityCache		entityCache;

    public ArrayList    	entityArrayList;
    public Vector    		entityVector;
    public Hashtable 		entityHashtable;
    private Semaphore 		mutex;
    private Hashtable		entityRsTable;

    //---------------------------------------------------------------------------------
    // METHOD 	create()
    //
    // PURPOSE:	Invoked by an EntityContainer to initialize/load an EntityObject
    //
    // INPUT:	1) EntityContainer
    //		2) String - Entity name
    //
    // RETURN:  None.
    //---------------------------------------------------------------------------------
    public void create(EntityContainer c, String name)
    {
	entityDbPool 	= c.DbPool;
	daemon 		= c.Daemon;
	entityContainer = c;

	entityVector	= new Vector();
	entityHashtable	= new Hashtable();
	mutex		= new Semaphore(1);
	entityRsTable   = new Hashtable();

	entityCache      = new DefaultEntityCache(name+"Cache", daemon);
	localEntityQueue = new EntityQueue(name+"Queue", daemon);
	localEntityQueue.init(c);

	useGlobalEntityQueue();

	init();
    }

    //---------------------------------------------------------------------------------
    // METHOD 	create()
    //
    // PURPOSE:	Invoked by an EntityContainer to initialize/load an EntityObject
    //
    // INPUT:	1) EntityContainer
    //		2) EntityCache
    //		3) String - Entity name
    //
    // RETURN:  None.
    //---------------------------------------------------------------------------------
    public void create(EntityContainer c, EntityCache cache, String name)
    {
	entityDbPool 	= c.DbPool;
	daemon 		= c.Daemon;
	entityContainer = c;

	entityVector	= new Vector();
	entityHashtable	= new Hashtable();
	mutex		= new Semaphore(1);
	entityRsTable   = new Hashtable();

	entityCache      = cache;
	localEntityQueue = new EntityQueue(name+"Queue", daemon);
	localEntityQueue.init(c);

	useGlobalEntityQueue();

	init();
    }


    //---------------------------------------------------------------------------------
    // METHOD 	executeQuery()
    //
    // PURPOSE:	Wraps the JDBC method of the same name, handling some the trival
    //		work, such as:
    //			- Obtaining a JDBC connection
    //			- creating and closing statements
    //			- catching exceptions
    //
    //		This method invokes a callback method for each row retrived.
    //
    // INPUT:	Query String
    //		Method name of callback
    //
    // RETURN:  None.
    //---------------------------------------------------------------------------------
    public void executeQuery( String query, String callback)
    {
	Connection conn = null;
   	Statement stmt  = null;
    	ResultSet rs;

        Method method = null;
        Object[] arguments;
	Class[]  params;
        Class ThisClass;

  	try
    	{
	    conn 	= entityDbPool.getConnection();
	    stmt 	= conn.createStatement();
	    String qstr	= query;
	    rs 		= stmt.executeQuery(qstr);

    	    ThisClass   = this.getClass();
       	    arguments   = new Object[] {rs};
	    params      = new Class[] {  Class.forName("java.sql.ResultSet")};
            method 	= ThisClass.getMethod(callback, params);

	    for(int i=0; rs.next(); i++)
 	    {    
	    	method.invoke(this, arguments);
	    }
	}
	catch(Exception e)
	{
            daemon.eventLog.sendMessage(1, "*** Error in "+callback+"() or this query: "+ query);
	    daemon.eventLog.sendMessage(1, e);
    	}     
	finally
	{
	    try
	    {
	    stmt.close();
            entityDbPool.freeConnection(conn);
	    }
	    catch(Exception z){}
	}

    }


    //---------------------------------------------------------------------------------
    // METHOD 	executeQuery()
    //
    // PURPOSE:	Wraps the JDBC method of the same name, handling some the trival
    //		work, such as:
    //			- Obtaining a JDBC connection
    //			- creating and closing statements
    //			- catching exceptions
    //
    //		This method invokes a callback method that processes a vector.
    //
    //
    // INPUT:	Query String
    //		Method name of callback
    //		A Vector reference. 
    //
    // RETURN:  None.
    //---------------------------------------------------------------------------------
    public void executeQuery( String query, String callback, Vector v)
    {
	Connection conn = null;
   	Statement stmt  = null;
    	ResultSet rs;

        Method method = null;
        Object[] arguments;
	Class[]  params;
        Class ThisClass;

  	try
    	{
	    conn 	= entityDbPool.getConnection();
	    stmt 	= conn.createStatement();
	    String qstr	= query;
	    rs 		= stmt.executeQuery(qstr);

    	    ThisClass   = this.getClass();
       	    arguments   = new Object[] {rs, v};
	    params      = new Class[] { Class.forName("java.sql.ResultSet"), v.getClass() };
            method 	= ThisClass.getMethod(callback, params);

	    for(int i=0; rs.next(); i++)
 	    {    
	    	method.invoke(this, arguments);
	    }
	}
	catch(Exception e)
	{
            daemon.eventLog.sendMessage(1, "*** Error in "+callback+"() or this query: "+ query);
	    daemon.eventLog.sendMessage(1, e);
	}
	finally
	{
	    try
	    {
	    stmt.close();
            entityDbPool.freeConnection(conn);
	    }
	    catch(Exception z){}
	}
    }


    //---------------------------------------------------------------------------------
    // METHOD 	executeQuery()
    //
    // PURPOSE:	Wraps the JDBC method of the same name, handling some the trival
    //		work, such as:
    //			- Obtaining a JDBC connection
    //			- creating and closing statements
    //			- catching exceptions
    //
    //		This method invokes a callback method that processes an ArrayList.
    //
    //
    // INPUT:	Query String
    //		Method name of callback
    //		An ArrayList reference. 
    //
    // RETURN:  None.
    //---------------------------------------------------------------------------------
    public void executeQuery( String query, String callback, ArrayList v)
    {
	Connection conn = null;
   	Statement stmt  = null;
    	ResultSet rs;

        Method method = null;
        Object[] arguments;
	Class[]  params;
        Class ThisClass;

  	try
    	{
	    conn 	= entityDbPool.getConnection();
	    stmt 	= conn.createStatement();
	    String qstr	= query;
	    rs 		= stmt.executeQuery(qstr);

    	    ThisClass   = this.getClass();
       	    arguments   = new Object[] {rs, v};
	    params      = new Class[] { Class.forName("java.sql.ResultSet"), v.getClass() };
            method 	= ThisClass.getMethod(callback, params);

	    for(int i=0; rs.next(); i++)
 	    {    
	    	method.invoke(this, arguments);
	    }
	}
	catch(Exception e)
	{
            daemon.eventLog.sendMessage(1, "*** Error in "+callback+"() or this query: "+ query);
	    daemon.eventLog.sendMessage(1, e);
	}
	finally
	{
	    try
	    {
	    stmt.close();
            entityDbPool.freeConnection(conn);
	    }
	    catch(Exception z){}
	}
    }

    //---------------------------------------------------------------------------------
    // METHOD 	executeQuery()
    //
    // PURPOSE:	Wraps the JDBC method of the same name, handling some the trival
    //		work, such as:
    //			- Obtaining a JDBC connection
    //			- creating and closing statements
    //			- catching exceptions
    //
    //		This method invokes a callback method that processes a hashtable.
    //
    // INPUT:	Query String
    //		Method name of callback
    //		A Hashtable reference. 
    //
    // RETURN:  None.
    //---------------------------------------------------------------------------------
    public void executeQuery( String query, String callback, Hashtable h)
    {
	Connection conn = null;
   	Statement stmt  = null;
    	ResultSet rs;

        Method method = null;
        Object[] arguments;
	Class[]  params;
        Class ThisClass;

  	try
    	{
	    conn 	= entityDbPool.getConnection();
	    stmt 	= conn.createStatement();
	    String qstr	= query;
	    rs 		= stmt.executeQuery(qstr);

    	    ThisClass   = this.getClass();
       	    arguments   = new Object[] {rs, h};
	    params      = new Class[] { Class.forName("java.sql.ResultSet"), h.getClass() };
            method 	= ThisClass.getMethod(callback, params);

	    for(int i=0; rs.next(); i++)
 	    {    
	    	method.invoke(this, arguments);
	    }
	}
	catch(Exception e)
	{
            daemon.eventLog.sendMessage(1, "*** Error in "+callback+"() or this query: "+ query);
	    daemon.eventLog.sendMessage(1, e);
	}
	finally
	{
	    try
	    {
	    stmt.close();
            entityDbPool.freeConnection(conn);
	    }
	    catch(Exception z){}
	}
    }


    //---------------------------------------------------------------------------------
    // METHOD 	executeQuery()
    //
    // PURPOSE:	Wraps the JDBC method of the same name, handling some the trival
    //		work, such as:
    //			- Obtaining a JDBC connection
    //			- creating and closing statements
    //			- catching exceptions
    //
    //		This method invokes a callback method that processes a hashtable.
    //
    // INPUT:	Query String
    //		Method name of callback
    //		Name of entity. 
    //
    // RETURN:  None.
    //---------------------------------------------------------------------------------
    public void executeQuery( String query, String callback, String ename)
    {
	int i =0;
	Connection conn = null;
   	Statement stmt  = null;
    	ResultSet rs;

        Method method = null;
        Object[] arguments;
	Class[]  params;
        Class ThisClass;

	daemon.event_log.SendMessage("Loading "+ename+"...");

  	try
    	{
	    conn 	= entityDbPool.getConnection();
	    stmt 	= conn.createStatement();
	    String qstr	= query;
	    rs 		= stmt.executeQuery(qstr);
	    ThisClass   = this.getClass();
       	    arguments   = new Object[] {rs};
	    params      = new Class[] { Class.forName("java.sql.ResultSet") };

            method 	= ThisClass.getMethod(callback, params);

	    for(i=0; rs.next(); i++)
 	    {    
	  	method.invoke(this, arguments);
	    }
	}
	catch(Exception e)
	{
            daemon.eventLog.sendMessage(1, "*** Error in "+callback+"() or this query: "+ query);
	    daemon.eventLog.sendMessage(1, e);
	}
	finally
	{
	    try
	    {
	    stmt.close();
            entityDbPool.freeConnection(conn);
	    }
	    catch(Exception z){}
	}
	daemon.event_log.SendMessage(i +" "+ename+" records loaded...");

    }



    //--------------------------------------------------------------------------------
    // METHOD 	executeQuery()
    //
    // PURPOSE:	Wraps the JDBC method of the same name, handling some the trival
    //		work, such as:
    //			- Obtaining a JDBC connection
    //			- creating and closing statements
    //			- catching exceptions
    //
    // INPUT:	Query String
    //
    // RETURN:  a ResultSet. 
    //
    //                     ******** NOTE ********
    // This method does NOT close the statement after use. It depends
    // on the calling method invoking closeResultSet() to close the statement
    // and the resultset. This method will save the context of the query
    // result in a private Hashtable. 
    //--------------------------------------------------------------------------------
    public ResultSet executeQuery(String qstring)
    {
        Connection conn = null;
        Statement stmt  = null;
        ResultSet rs =null;

	try
	{
            conn    = entityDbPool.getConnection();
            stmt    = conn.createStatement();

            rs = stmt.executeQuery(qstring);

	    //-------------------------------------------------------
	    // Save the Statement in a private Hashtable, so that 
	    // closeResultSet can retrieve later to close the 
	    // Statement (and ResultSet)
	    //------------------------------------------------------- 
	    entityRsTable.put(rs, stmt);

	}
	catch(SQLException e)
	{
            daemon.eventLog.sendMessage(1, "*** Error in this query: "+ qstring);
	    daemon.eventLog.sendMessage(1, e);
	}
	finally
	{
            entityDbPool.freeConnection(conn);
	}

	return(rs);
    }




    //--------------------------------------------------------------------------------
    // METHOD 	executeUpdate()
    //
    // PURPOSE:	Wraps the JDBC method of the same name, handling some the trival
    //		work, such as:
    //			- Obtaining a JDBC connection
    //			- creating and closing statements
    //			- catching exceptions
    //
    // INPUT:	Query String
    //
    // RETURN:  int = number of rows affected by operation.
    //--------------------------------------------------------------------------------
    public int executeUpdate(String qstring)
    {
        Connection conn = null;
        Statement stmt  = null;
        int rc =0;

	try
	{
            conn    = entityDbPool.getConnection();
            stmt    = conn.createStatement();

            rc = stmt.executeUpdate(qstring);
	}
	catch(SQLException e)
	{
            daemon.eventLog.sendMessage(1, "*** ERROR in executeUpdate() using: "+ qstring);
	    daemon.eventLog.sendMessage(1, e);
	}
	finally
	{
	    try
	    {
            stmt.close();
            entityDbPool.freeConnection(conn);
	    }
	    catch(Exception z){}
	}

	return(rc);
    }



    //--------------------------------------------------------------------------------
    // METHOD 	executeMySqlInsert()
    //
    // PURPOSE:	Used to insert MySql tables with AUTO INCREMENT keys.
    //		Returns the new key value via MySql's LAST_INSERT_ID() function.
    //
    // INPUT:	Query String
    //
    // RETURN:  int = LAST_INSERT_ID()
    //--------------------------------------------------------------------------------
    public int executeMySqlInsert(String qstring)
    {
        Connection conn = null;
        Statement stmt  = null;
        int rc =0;

	try
	{
            conn    = entityDbPool.getConnection();
            stmt    = conn.createStatement();

            stmt.executeUpdate(qstring);

	    //------------------------------------------------------------------
	    // Retrieve new AUTO_INCREMENT Inventory_id 
	    //------------------------------------------------------------------
 	    ResultSet r = stmt.executeQuery("SELECT LAST_INSERT_ID() AS AutoId");

	    r.next();
	    rc = r.getInt("AutoId");

	}
	catch(SQLException e)
	{
	    daemon.eventLog.sendMessage(1, "executeMySqlInsert() failed using: "+qstring);
	    daemon.eventLog.sendMessage(1, e);
	}
	finally
	{
	    try
	    {
            stmt.close();
            entityDbPool.freeConnection(conn);
	    }
	    catch(Exception z){}
	}
	return(rc);
    }


    //--------------------------------------------------------------------------------
    // METHOD 	closeResultSet()
    //
    // PURPOSE:	Close an open Statement and Associated ResultSet.
    //
    // INPUT:	ResultSet
    //
    // RETURN:  None.
    //--------------------------------------------------------------------------------
    public void  closeResultSet(ResultSet rs)
    {
	try
	{
	    Statement s = (Statement)entityRsTable.get(rs);
	    s.close();
	    entityRsTable.remove(rs);
	}
	catch(Exception e)
	{
            daemon.event_log.SendMessage("*** ERROR closeResultSet() failed becasue" + 
					 e.getMessage());
	}
    }

    //--------------------------------------------------------------------------------
    // METHOD 	getTimeString()
    //
    // PURPOSE:	Uses java.util.Date and java.sql.Timestamp to generate a String 
    //		representing the current Date/Time in the format: 
    //		yyyy-mm-dd hh.mm.ss.ffffffff 
    //
    // INPUT:	None.
    //
    // RETURN:  String => Current date/time
    //--------------------------------------------------------------------------------
    public String getTimeString()
    {
	//------------------------------------------
	// The time is now...format so that our
	// Database understands 
	//--------------------------------------------
	java.util.Date date 	= new java.util.Date();
	java.sql.Timestamp time = new java.sql.Timestamp(date.getTime());
	String createTime	= time.toString();

	return(createTime);
    }


    //--------------------------------------------------------------------------------
    // METHOD 	entityLock()
    //
    // PURPOSE:	Lock a code section for mutual exclusion.
    //
    // INPUT:	None.
    //
    // RETURN:  None.
    //--------------------------------------------------------------------------------
    public void entityLock()
    {
	mutex.Wait();
    }


    //--------------------------------------------------------------------------------
    // METHOD 	entityUnlock()
    //
    // PURPOSE:	Unlock a code section from mutual exclusion.
    //
    // INPUT:	None.
    //
    // RETURN:  None.
    //--------------------------------------------------------------------------------
    public void entityUnlock()
    {
	mutex.Signal();
    }

    //--------------------------------------------------------------------------------
    // METHOD 	useGlobalEntityQueue()
    //
    // PURPOSE:	
    //
    // INPUT:	None.
    //
    // RETURN:  None.
    //--------------------------------------------------------------------------------
    public void useGlobalEntityQueue()
    {
	entityQueue = entityContainer.entityQueue;
    }

    //--------------------------------------------------------------------------------
    // METHOD 	useLocalEntityQueue()
    //
    // PURPOSE:	
    //
    // INPUT:	None.
    //
    // RETURN:  None.
    //--------------------------------------------------------------------------------
    public void useLocalEntityQueue()
    {
	entityQueue = localEntityQueue;
	entityQueue.start();
    }


    //--------------------------------------------------------------------------------
    // METHOD	load() 	
    //
    // PURPOSE:	Invoked from create() by the EntityContainer.
    // 		Implement this to have data selected when 
    //		the entity is created. 
    //
    //--------------------------------------------------------------------------------
    public void load()
    {


    }

    //--------------------------------------------------------------------------------
    // METHOD	init() 	
    //
    // PURPOSE:	Invoked from create() by the EntityContainer.
    // 		Implement this to provide additional EntityObject initialzation.
    //
    //--------------------------------------------------------------------------------
    public void init()
    {


    }

    

    //--------------------------------------------------------------------------------
    // METHOD 	EntityObject
    //
    // PURPOSE:	Constructor
    //
    // INPUT:	None.
    //--------------------------------------------------------------------------------
    public EntityObject()
    {

    }
}
