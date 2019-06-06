package examples;
//
// MqmConfig.java        Service Node Configuration Object
// ----------------------------------------------------------------------------
// History:
// --------
// 06/02/19 M. Gill     Initial creation.
// ----------------------------------------------------------------------------
//
import java.lang.*;
import java.util.*;
import java.io.*;
import com.qkernel.*;

@SuppressWarnings("unchecked")

public final class MqmConfig extends QMessage
{
    private String confFile = "mqm.conf";

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
    	put("myIp",	"localhost");

	//-----------------------------------------------
 	// Respositories and as such
	//-----------------------------------------------
	put("docRoot",	"/home/mgill/projects/metaqueue/master_node/mqm.qml/");
	put("cmsRoot",	"/home/mgill/projects/metaqueue/master_node/CMS_CONTENT/");
	put("cmsTemp",	"/home/mgill/projects/metaqueue/master_node/CMS_TEMPLATES/");
	put("homeUrl",	"metaqueue");
	put("homeClass","/request/MetaQueueDocument?QMLclass=view.Home");

    	//----------------------------------------------
    	// Enabled Brokers and Channels 
    	//----------------------------------------------
    	putBoolean("entityContainer", 		false);
    	putBoolean("objectBroker", 		true);
    	putBoolean("documentBroker",		true);
    	putBoolean("ftpChannel", 		false);
    	putBoolean("emailChannel", 		false);
    	putBoolean("qbusChannel", 		false);
    	putBoolean("tmSmppBroker", 		false);
    	putBoolean("attSmppBroker", 		false);
    	putBoolean("ivrBroker", 		false);
    	putBoolean("sshChannel", 		false);
    	putBoolean("tmSmppChannel", 		false);
    	putBoolean("attSmppChannel", 		false);
    	putBoolean("soapChannel", 		false);
    	putBoolean("ivrChannel", 		false);
	putInt("channelRetryInterval",		2);
	putInt("channelRetryCount",		5);

    	//-------------------------------------------
    	// Service Ports
    	//-------------------------------------------
    	int basePort	= 3000; 

    	putInt("obPort",	basePort + 1);
    	putInt("qbPort",	basePort + 2);
    	putInt("vqPort",   	basePort + 3);

    	//-------------------------------------------
    	// Number of User Agents and Channel Queues 
    	//-------------------------------------------
    	putInt("obAgents",	3);
    	putInt("qbAgents",	3);
    	putInt("vqAgents",   	3);
    	putInt("ftpQueues",	3);
    	putInt("sshQueues",	3);
    	putInt("emailQueues",	3);
    	putInt("tmSmppQueues",	1);
    	putInt("attSmppQueues",	1);
    	putInt("qbusQueues",	3);
    	putInt("soapQueues",	3);
    	putInt("ivrQueues",	3);

	//----------------------------------------
	// SMTP defaults
	//----------------------------------------
	put("smtpHost",			"localhost");
	put("smtpPort",			"25");
	put("daemonEmail",		"emailq@metaqueue.net");

    	//----------------------------------------
    	// WPBC SMPP defaults
    	//---------------------------------------- 
    	put("tmTxSysId",	       "METAQTX1");
    	put("tmTxPassword",	       "MQTX1");
    	put("tmTxSysType",	       "APP");
    	put("tmTxIp",	 	       "www.wpbc.net");
    	putInt("tmTxPort",		2775);
    	put("tmRxSysId",	       "METAQRX1");
    	put("tmRxPassword",	       "MQRX1");
    	put("tmRxSysType",	       "APP");
    	put("tmRxIp",	 	       "www.wpbc.net");
    	putInt("tmRxPort",		2775);

    	put("attTxSysId",	       "METAQTX1");
    	put("attTxPassword",	       "MQTX1");
    	put("attTxSysType",	       "APP");
    	put("attTxIp",	 	       "www.wpbc.net");
    	putInt("attTxPort",		2775);
    	put("attRxSysId",	       "METAQRX1");
    	put("attRxPassword",	       "MQRX1");
    	put("attRxSysType",	       "APP");
    	put("attRxIp",	 	       "www.wpbc.net");
    	putInt("attRxPort",		2775);

    	//----------------------------------------
    	// ivrd defaults
    	//---------------------------------------- 
    	put("ivrIp",		"booboo");
    	put("ivrDocs",		"/home/voice_prompts/");
    	putInt("ivrPort",	1700);

    	//-----------------------------------
    	// Default config. params for DbPool 
    	//-----------------------------------
    	put("jdbcDriver",	"org.gjt.mm.mysql.Driver");
    	put("connStr",		"jdbc:mysql://localhost/metaqueueDB?user=apache");
    	putInt("minConn",	5);
    	putInt("maxConn",	5);
    	put("dbPoolLog",	"");	
    	putDouble("dbResetTime",60); 

    	//-----------------------------------
    	// Default serial devices
    	//-----------------------------------
    	put("device",		 "COM1");
    	put("ttyDevice", 	"/dev/ttyS0");

    	//------------------------------------
    	// This only used for GSM modems
    	//-----------------------------------
    	put("scaNumber", 	"+12063130004");

    }


    //---------------------------------------------------------------------------------
    // The following are the "get methods for configuration data...
    // Data points are assumed to be in the conf. file or defined int the ROM defaults.
    //---------------------------------------------------------------------------------


    //----------------------------------------
    //
    // General config. parms
    //
    //---------------------------------------- 
    public String getIpAddress()
    {
	return(getString("myIp"));
    }

    //----------------------------------------
    //
    // JDBC and connection pool stuff
    //
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
    //
    // Repositories, Containers and as such
    //
    //---------------------------------------- 
    public String getDocRoot()
    {
	return(getString("docRoot"));
    }
    public String getCmsTemplateRoot()
    {
	return(getString("cmsTemp"));
    }
    public String getContentPath()
    {
	return(getString("cmsRoot"));
    }
    public String getHomeUrl()
    {
	return(getString("homeUrl"));
    }
    public String getHomePage()
    {
	return(getHomeUrl() + getString("homeClass"));
    }
    public boolean hasEntityContainer()
    {
	return(getBoolean("entityContainer"));
    }
    public String getFileRepository(int siteid)
    {
	String cp = getContentPath();
	return(cp+siteid+"/CMS_FILES/");
    }
    public String getTemplateRoot(int siteid)
    {
	String cp = getCmsTemplateRoot();
	return(cp+siteid+"/");
    }



    //----------------------------------------
    //
    // Broker Flags, and Agent config
    //
    //---------------------------------------- 
    public boolean hasObjectBroker()
    {
	return(getBoolean("objectBroker"));
    }
    public boolean hasDocumentBroker()
    {
	return(getBoolean("documentBroker"));
    }
    public boolean hasTmSmppBroker()
    {
	return(getBoolean("tmSmppBroker"));
    }
    public boolean hasAttSmppBroker()
    {
	return(getBoolean("attSmppBroker"));
    }
    public boolean hasIvrBroker()
    {
	return(getBoolean("ivrBroker"));
    }
    public int getViewPort()
    {
	return(getInt("qbPort"));
    }
    public int getBusinessPort()
    {
	return(getInt("obPort"));
    }
    public int getIvrPort()
    {
	return(getInt("vqPort"));
    }
    public int getViewAgentNum()
    {
	return(getInt("qbAgents"));
    }
    public int getBusinessAgentNum()
    {
	return(getInt("obAgents"));
    }
    public int getIvrAgentNum()
    {
	return(getInt("vqAgents"));
    }


    //----------------------------------------
    //
    // Channel Flags
    //
    //---------------------------------------- 
    public int getChannelRetryInterval()
    {
	return(getInt("channelRetryInterval"));
    }
    public int getChannelRetryCount()
    {
	return(getInt("channelRetryCount"));
    }

    public boolean hasFtpChannel()
    {
	return(getBoolean("ftpChannel"));
    }
    public boolean hasSshChannel()
    {
	return(getBoolean("sshChannel"));
    }
    public boolean hasEmailChannel()
    {
	return(getBoolean("emailChannel"));
    }
    public boolean hasTmSmppChannel()
    {
	return(getBoolean("tmSmppChannel"));
    }
    public boolean hasAttSmppChannel()
    {
	return(getBoolean("attSmppChannel"));
    }
    public boolean hasQbusChannel()
    {
	return(getBoolean("qbusChannel"));
    }
    public boolean hasSoapChannel()
    {
	return(getBoolean("soapChannel"));
    }
    public boolean hasIvrChannel()
    {
	return(getBoolean("ivrChannel"));
    }


    //----------------------------------------
    //
    // Channel Queues
    //
    //---------------------------------------- 
    public int getNumFtpQueues()
    {
	return(getInt("ftpQueues"));
    }
    public int getNumSshQueues()
    {
	return(getInt("sshQueues"));
    }
    public int getNumEmailQueues()
    {
	return(getInt("emailQueues"));
    }
    public int getNumTmSmppQueues()
    {
	return(getInt("tmSmppQueues"));
    }
    public int getNumAttSmppQueues()
    {
	return(getInt("attSmppQueues"));
    }
    public int getNumQbusQueues()
    {
	return(getInt("qbusQueues"));
    }
    public int getNumSoapQueues()
    {
	return(getInt("soapQueues"));
    }
    public int getNumIvrQueues()
    {
	return(getInt("ivrQueues"));
    }

    //----------------------------------------
    // SMTP configuration
    //----------------------------------------
    public String getSmtpHost()
    {
	return(getString("smtpHost"));
    }
    public String getSmtpPort()
    {
	return(getString("smtpPort"));
    }
    public String getDaemonEmailAddress()
    {
	return(getString("daemonEmail"));
    }



    //----------------------------------------
    //
    // WPBC SMPP Getters
    //
    //---------------------------------------- 
    public String getTmTxSysId()
    {
	return(getString("tmTxSysId"));
    }
    public String getTmTxPassword()
    {
	return(getString("tmTxPassword"));
    }
    public String getTmTxSysType()
    {
	return(getString("tmTxSysType"));
    }
    public String getTmTxIp()
    {
	return(getString("tmTxIp"));
    }
    public int getTmTxPort()
    {
	return(getInt("tmTxPort"));
    }
    public String getTmRxSysId()
    {
	return(getString("tmRxSysId"));
    }
    public String getTmRxPassword()
    {
	return(getString("tmRxPassword"));
    }
    public String getTmRxSysType()
    {
	return(getString("tmRxSysType"));
    }
    public String getTmRxIp()
    {
	return(getString("tmRxIp"));
    }
    public int getTmRxPort()
    {
	return(getInt("tmRxPort"));
    }
    public String getAttTxSysId()
    {
	return(getString("attTxSysId"));
    }
    public String getAttTxPassword()
    {
	return(getString("attTxPassword"));
    }
    public String getAttTxSysType()
    {
	return(getString("attTxSysType"));
    }
    public String getAttTxIp()
    {
	return(getString("attTxIp"));
    }
    public int getAttTxPort()
    {
	return(getInt("attTxPort"));
    }
    public String getAttRxSysId()
    {
	return(getString("attRxSysId"));
    }
    public String getAttRxPassword()
    {
	return(getString("attRxPassword"));
    }
    public String getAttRxSysType()
    {
	return(getString("attRxSysType"));
    }
    public String getAttRxIp()
    {
	return(getString("attRxIp"));
    }
    public int getAttRxPort()
    {
	return(getInt("attRxPort"));
    }

    //----------------------------- END OF GETTERS ----------------------------------




    //--------------------------------------------------------------------------------
    // METHOD   MqmConfig()
    //
    // PURPOSE: Constructor ...Reads params from properties file...loads
    //		default ROM configuration for properties that may not be defined.
    //
    // INPUT:   1) Daemon
    //		2) Commandline argvs
    //
    // RETURN:  N/A
    //--------------------------------------------------------------------------------
    public MqmConfig( Daemon daemon, String[] argvs )
    {
        if( argvs.length == 1 ) this.confFile = argvs[0];

	putROMConfig();

        Properties props = new Properties();

        try
	{
	    File cf = new File(confFile);

            props.load(new FileInputStream(cf));
            putAll( props );
            daemon.eventLog.sendMessage("Using configuration file: "
                                         + cf.getCanonicalPath() );
	}
        catch(Exception e)
	{
	    String estring ="Configuration file not found: "+ confFile;
		   estring = estring+" Using default configuration...";
 
            daemon.eventLog.sendMessage(estring);
	}
    }
}

