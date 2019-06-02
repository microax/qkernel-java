package examples;
//
// MqmDaemon.java	Example Service Node Daemon
// ----------------------------------------------------------------------------
// History:
// --------
// 06/02/19 M. Gill	Initial creation.
// ----------------------------------------------------------------------------
//
import java.lang.*;
import java.util.*;
import com.qkernel.*;
//
//----------------------------------------------------------------------------
// An Application Node within a network.
//
// example.conf  defines configuration information in a property file format. 
// This configuration information includes the flags witch determine the 
// services provided in a MqmDaemon, as well as the setup of these services. 
// MqmConfig provides default values if example.conf is missing (or 
// incorrectly configured)  MqsDaemon retrieves Application Node parameters, 
// access control and state information from the Service Node.
//
// Currently the core services are:
// --------------------------------
// MessageQ     (Publish-Subscribe messaging)
// VoiceQ       (Voice processing service)
// EmailQ       (Interactive Email Service)
// SmsQ         (Short Message Service via SMPP)
// ContentQ     (Content Management)
// SOAP		(SOAP Object Services)
// FTP		(FTP Service)
// SSH		(SSH Transfers)
//
//----------------------------------------------------------------------------
public class MqmDaemon extends Daemon
{
    //-------------------------------------------------------------------
    // Important Static Object references
    //------------------------------------------------------------------- 
    public  static MqmConfig         config;
    public  static MqmContainer      mqmContainer;
    public  static QmlDocumentBroker documentBroker;
    public  static QObjectBroker     objectBroker;

    //--------------------------------------------------------------------------------
    // METHOD   start()
    //
    // PURPOSE: start() is invoked after the Daemon initializes itself.
    //		This is where Qkernel Application Nodes init. and start their 
    //		services - it is the Qkernel equivilant of main(). In fact,
    //		main() indirectly calls this method by invoking startDaemon().
    //
    // INPUT:   String argvs[] - this is the same command line argvs sent to main()
    // RETURN:  Never
    //--------------------------------------------------------------------------------
    public void start(String argvs[])
    {
        String CONFIG       = "Configuration Object";
        String CONTAINER    = "EntityContainer Object";

	//----------------------------------------------------------------
	// Set default Configuration parameters
	//----------------------------------------------------------------
	config			= new MqmConfig(this , argvs);
	register(CONFIG, config);

	//-------------------------------------------
	// Set InetAddress for this Service Node
	//-------------------------------------------
	setInetAddress(config.getIpAddress());

	if(config.hasEntityContainer())
	{
	//-----------------------------------------------------
	// Create an Entity Object Container 
	//-----------------------------------------------------
        String jdbcDriver   	=config.getJdbcDriver();
        String connStr      	=config.getConnStr();
        String dbUsername   	=config.getConnUsername();
        String dbPassword   	=config.getConnPassword();
        int minConn         	=config.getMinConn();
        int maxConn         	=config.getMaxConn();
        String dbPoolLog    	=config.getDbPoolLog();
        double dbResetTime  	=config.getDbResetTime();

	mqmContainer = new MqmContainer();
	mqmContainer.create( jdbcDriver, 
			   	  connStr,
				  "",
				  "", 
				  minConn, 
				  maxConn, 
				  dbPoolLog, 
				  dbResetTime,
				  this);
	//-------------------------------
	// Load Entity Objects
	//-------------------------------
	mqmContainer.load();
	register(CONTAINER, mqmContainer);
	}


	//--------------------------------------------------------------------
	// Here we check the configured input channel types ( object brokers)
	// for the master/service node, and start them. 
	// SOAP-XML requests are handled by servlet adaptors that create 
	// QMessages sent to QBUS.
	// 
	// Currently we have Brokers for:
	// 	QML Documents
	//	QBUS Business Objects ( Native Qkernel Enhanced RMI)
	//	T-MOBILE/CINGULAR SMPP Short Number( via WBPC)
	//	ATT SMPP Short Number( via WBPC)
	//	IVR Broker ( manages ivrd requests)
	//--------------------------------------------------------------------
	if(config.hasDocumentBroker())
	{
	    int qbPort		=config.getViewPort();
	    int qbAgents	=config.getViewAgentNum();

	    //---------------------------------------------------
	    // Create the Document Broker
	    //---------------------------------------------------
	    documentBroker = new QmlDocumentBroker("Document Broker", this);
	    documentBroker.start(qbPort, qbAgents);
	    //documentBroker.setDebugMode();
	}
	if(config.hasObjectBroker())
	{
	    int obPort		=config.getBusinessPort();
	    int obAgents	=config.getBusinessAgentNum();

	   //-------------------------------------------------------
	   // Create the Object Broker ( handles all QBUS requests)
	   //-------------------------------------------------------
	    objectBroker = new QObjectBroker("Qbus Object Broker", this);
            objectBroker.setIIOP();
            //objectBroker.setObjectList(qbusObjectList);
	    //objectBroker.setBanList(qbusBanList);
	    objectBroker.start(obPort, obAgents);
	   //objectBroker.setDebugMode();

	   //-------------------------------------------------------
	   // Create the Object Broker ( handles all JSON requests)
	   //-------------------------------------------------------
	    objectBroker = new QObjectBroker("JSON Object Broker", this);
            objectBroker.setHTTP();
            //objectBroker.setObjectList(jsonObjectList);
	    //objectBroker.setBanList(jsonBanList);
	    objectBroker.start(9000, obAgents);
	}
	
	//--------------------------------------------------------
	// That's it!...We're running a service node :-)
	//--------------------------------------------------------
	eventLog.sendMessage("MQM Daemon initialized");
    }

    public MqmDaemon(String key)
    {
	super(key);
    }
}
