package com.qkernel;
//
// Config.java        Qkernel Service Node Configuration Object
// ----------------------------------------------------------------------------
// History:
// --------
// 06/09/19 M. Gill     Initial creation.
// ----------------------------------------------------------------------------
//
import java.lang.*;
import java.util.*;
import java.io.*;

@SuppressWarnings("unchecked")

public class Config extends QMessage
{
    private String confFile = "examples.conf";
    private Daemon daemon   = null;
    
    //--------------------------------------------------------------------------------
    // METHOD   putROMConfig()
    //
    // PURPOSE: Create default ROM daemon configuration.
    // INPUT:   None.
    // RETURN:  None.
    //--------------------------------------------------------------------------------
    public void putROMConfig()
    {
    	//----------------------------------------------
    	// IP address this Service Node binds to
    	//----------------------------------------------
    	put("myIp",	"wilddog");
    	put("loIp",	"localhost");
    	put("sslKeyStore",         "keystore.jks");
    	put("sslKeyStorePassword", "password");
    	put("sslKeyStoreType",     "JKS");
    	put("sslKeyAlias",         "");
    	//----------------------------------------------
    	// Enabled Brokers and Containers and Channels 
    	//----------------------------------------------
    	putBoolean("entityContainer", 		false);
    	putBoolean("objectBrokerIIOP", 		true);
    	putBoolean("objectBrokerREST", 		true);
    	putBoolean("documentBroker",		false);
    	putBoolean( "useSSL",                   false);
    	putString( "restObjectList", "rest_objects.json");
    	//-------------------------------------------
    	// Service Ports
    	//-------------------------------------------
    	int basePort	= 9000; 
    	putInt("rbPort",	basePort + 0);
    	putInt("obPort",	basePort + 1);
    	putInt("qbPort",   	basePort + 2);
    	//-----------------------------------
    	// Default config. params for DbPool 
    	//-----------------------------------
    	put("jdbcDriver",	"org.gjt.mm.mysql.Driver");
    	put("connStr",		"jdbc:mysql://localhost/examplesDB");
    	putInt("minConn",	5);
    	putInt("maxConn",	5);
    	put("dbPoolLog",	"");
	put("dbName",           "examplesDB");
    	putDouble("dbResetTime",60); 
	//-------------------------------------------
    	// EntityObjects defaults
    	//-------------------------------------------
	putString("containerPackageName", "model");
	putString("containerClassName",   "Example");
	putString("containerDir",         "examples/model/");
	putString("modelPackageName",     "model");
	putString("modelDir",             "examples/model/");
	//-------------------------------------------
    	// Number of User Agents and Channel Queues 
    	//-------------------------------------------
    	putInt("rbAgents",	5);
    	putInt("obAgents",	3);
    	putInt("qbAgents",	3);
    	//-----------------------------------
    	// Default serial devices
    	//-----------------------------------
    	put("device",		 "COM1");
    	put("ttyDevice", 	"/dev/ttyS0");
    	//------------------------------------
    	// This only used for GSM modems
    	//-----------------------------------
    	put("scaNumber", 	"+15555555555");
        //-----------------------------------------------
        // Respositories and as such
        //-----------------------------------------------
        put("docRoot",	"./mqm.qml/");
        put("homeUrl",	"localhost");
        put("homeClass","/request/ExampleDocument?QMLclass=view.Home");
    }

    //---------------------------------------------------------------------------------
    // The following are the "get methods for configuration data...
    // Data points are assumed to be in the conf. file or defined int the ROM defaults.
    //---------------------------------------------------------------------------------

    //----------------------------------------
    // General config. parms
    //---------------------------------------- 
    public String getIpAddress()
    {
	return(getString("myIp"));
    }
    public String getLoIpAddress()
    {
	return(getString("loIp"));
    }
    public String getSslKeyStore()
    {
	return(getString("sslKeyStore"));
    }
    public String getSslKeyStorePassword()
    {
	return(getString("sslKeyStorePassword"));
    }
    public String getSslKeyStoreType()
    {
	return(getString("sslKeyStoreType"));
    }
    public String getSslKeyAlias()
    {
	return(getString("sslKeyAlias"));
    }
    
    //----------------------------------------
    // JDBC and connection pool stuff
    //---------------------------------------- 
    public String getJdbcDriver()
    {
	return(getString("jdbcDriver"));
    }
    public String getConnStr()
    {
	return(getString("connStr"));
    }
    public String getConnUsername()
    {
	return(getString("dbUsername"));
    }
    public String getConnPassword()
    {
	return(getString("dbPassword"));
    }
    public int getMinConn()
    {
	return(getInt("minConn"));
    }
    public int getMaxConn()
    {
	return(getInt("maxConn"));
    }
    public String getDbPoolLog()
    {
	return(getString("dbPoolLog"));
    }
    public double getDbResetTime()
    {
	return(getDouble("dbResetTime"));
    }
    //----------------------------------------
    // EntityObject Container and Packages
    //---------------------------------------- 
    public String getContainerPackageName()
    {
	return(getString("containerPackageName"));
    }
    public String getContainerClassName()
    {
	return(getString("containerClassName"));
    }
    public String getContainerDir()
    {
	return(getString("containerDir"));
    }
    public String getModelPackageName()
    {
	return(getString("modelPackageName"));
    }
    public String getModelDir()
    {
	return(getString("modelDir"));
    }
    //----------------------------------------
    // Repositories, Containers and as such
    //---------------------------------------- 
    public boolean hasEntityContainer()
    {
	return(getBoolean("entityContainer"));
    }
    public String getDocRoot()
    {
	return(getString("docRoot"));
    }
    public String getHomeUrl()
    {
	return(getString("homeUrl"));
    }
    public String getHomePage()
    {
	return(getHomeUrl() + getString("homeClass"));
    }
    //----------------------------------------
    // Broker Flags, and Agent config
    //---------------------------------------- 
    public boolean useSSL()
    {
	return(getBoolean("useSSL"));
    }
    public boolean hasObjectBroker()
    {
	return(getBoolean("objectBrokerIIOP"));
    }
    public boolean hasRestObjectBroker()
    {
	return(getBoolean("objectBrokerREST"));
    }
    public boolean hasDocumentBroker()
    {
	return(getBoolean("documentBroker"));
    }
    public int getBusinessPort()
    {
	return(getInt("rbPort"));
    }
    public int getBusinessAgentNum()
    {
	return(getInt("rbAgents"));
    }
    public int getQbusPort()
    {
	return(getInt("obPort"));
    }
    public int getQbusAgentNum()
    {
	return(getInt("obAgents"));
    }
    public int getViewAgentNum()
    {
	return(getInt("qbAgents"));
    }
    public int getViewPort()
    {
	return(getInt("qbPort"));
    }
    public String getRestObjectList()
    {
	return(getString("restObjectList"));
    }

    protected void log(String e)
    {
	if(this.daemon != null)
	    this.daemon.eventLog.sendMessage(e);	    
	else
            System.out.println(e);
    }
    

    //--------------------------------------------------------------------------------
    // METHOD   Config()
    //
    // PURPOSE: Constructor ...Reads params from properties file...loads
    //		default ROM configuration for properties that may not be defined.
    //
    // INPUT:   1) Daemon
    //		2) Commandline argvs
    //
    // RETURN:  N/A
    //--------------------------------------------------------------------------------
    public Config( Daemon daemonClass, String[] argvs )
    {
        this.daemon = daemonClass;
        if( argvs.length == 1 )
	    this.confFile = argvs[0];
	putROMConfig();
        Properties props = new Properties();
        try
	{
	    File cf = new File(confFile);
            props.load(new FileInputStream(cf));
            putAll( props );
            log("Using configuration file: "+ cf.getCanonicalPath() );
	}
        catch(Exception e)
	{
	    String estring ="Configuration file not found: "+ confFile;
		   estring = estring+" Using default configuration...";
            log(estring);
	}
    }

    //--------------------------------------------------------------------------------
    // METHOD   Config()
    //
    // PURPOSE: Constructor ...This Constructor uses "Constructor Chaining"
    //          when there is no Daemon class to associate it with.
    //
    // INPUT:   1) Commandline argvs
    //
    // RETURN:  N/A
    //--------------------------------------------------------------------------------
    public Config(String[] argvs )
    {
	this(null, argvs);
    }    
}
