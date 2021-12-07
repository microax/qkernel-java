package com.qkernel;
//
// Daemon.java        Daemon class
// ----------------------------------------------------------------------------
// History:
// --------
// 08/11/20 M. Gill     Add addTerminationHook() to process signal 15
//                      termination and orderly shutdown.
// 06/25/05 M. Gill	Add log() methods.
// 01/19/04 M. Gill	Add getNodeVersion() an getNodeProvider() methods.
// 04/07/02 M. Gill	Add register(), lookup() & service() methods. 
// 01/16/02 M. Gill	1) Add support to bind to TCP INetAddress.
//			2) Add Mos shutdown facility.
// 01/12/02 M. Gill	1) Added eventLog instance pointing to event_log.
// 			2) Added loadClass() supporting AppClassReloader.
// 07/28/01 M. Gill	Added simple license manager.
// 04/06/00 M. Gill     Added Timer Service
// 12/26/99 M. Gill	Added Logging Thresholds
// 09/20/97 M. Gill	Initial creation.
// ----------------------------------------------------------------------------
//
import java.lang.*;
import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import com.qkernel.license.*;
import com.qkernel.classloader.AppClassReloader;
import java.lang.management.ManagementFactory;

@SuppressWarnings({"unchecked", "deprecation"})
//
//----------------------------------------------------------------------------
// Here is the (potentially useful) Daemon class.  Daemon encapsulates 
// objects and methods that are oft times needed by classes within a qkernel 
// daemon. Including,  event/error logging, signal trapping, orderly shutdown 
// processing and configuration management.  
// A Daemon reference is passed to every Mos object.
//----------------------------------------------------------------------------
public abstract class Daemon
{
    public final int MIN_LOG_THRESHOLD = 0;
    public final int MAX_LOG_THRESHOLD = 10;

    public static EventLog event_log;
    public static EventLog eventLog;

    public static TimerService timer_service;
    public static Semaphore ExitSignal;

    private int logging_threshold;
    private String lkey;
    private String nodeVer;
    private String useBy;

    private String docBase		= "./qorbclasses";
    private static AppClassReloader loader = null;

    private InetAddress iNetAddress =null;
    private boolean shutdownFlag = false;
    private boolean shutdownDone = false;
    private Queue mosList;
    private int mosCount;
    private Hashtable dObjects = null;
    private String jvmVersion ="";
    
    //--------------------------------------------------------------------------------
    // METHOD 	startDaemon()
    //
    // PURPOSE:	Kick off the Daemon object.
    //
    // INPUT:	argvs ( command line arguments)
    //
    // PROCESS:	1) Create Exit Signal.
    //          2) Create Timer Service.
    // 		2) Create and start Loggers
    //
    // RETURN:  None.
    //--------------------------------------------------------------------------------
    public void startDaemon(String argvs[])
    {
	String lpw = licenseCheck("yRUjAvA?");
	if(lpw.equals("micr0s0FtSucKS!") == false)
	{
	    System.out.println(lpw);
            System.exit(0);
	}

	Date d  = new Date();  
        int year=d.getYear()+1900; 
	//---------------------------------------------------------------
	// Get version of this Java Virtual Machine...
	//---------------------------------------------------------------
	String specVersion = ManagementFactory.getRuntimeMXBean().getSpecVersion();
	String specVendor  = ManagementFactory.getRuntimeMXBean().getSpecVendor();
	String specName    = ManagementFactory.getRuntimeMXBean().getSpecName();
	String vmBuild     = ManagementFactory.getRuntimeMXBean().getVmVersion();
	jvmVersion =specVendor+" "+specVersion+" (build "+vmBuild+")";
	String banner   ="*******************************************************************";
	String version  ="SPN Application Node Release "+nodeVer;
	String copyright="Copyright "+year+" Sapien Network Corp. -- All Rights Reserved";
	String license  ="License key:"+lkey+" -- "+useBy+"\n"; 
        String runtime  ="JVM version is "+jvmVersion;

	System.out.println(banner);
	System.out.println(version);
	System.out.println(copyright);
	System.out.println(license);
	System.out.println(runtime);
	System.out.println(banner);

        ExitSignal = new Semaphore(0);
        dObjects   = new Hashtable();
 
	shutdownFlag =false;
	shutdownDone =false;
	mosList = new Queue();
	mosCount=0;

	event_log = new EventLog("EVL", this);
        event_log.start();
	eventLog  = event_log;

	timer_service = new TimerService(this);
	timer_service.start();

	Thread.currentThread().setName("Daemon");	// Set name for Main()

	//-------------------------------
	// Set Initial InetAddress
	//-------------------------------

	//----------------------------------------
	// I'm going to make the default null	
	// which binds to ALL addresses on the 
	// machine...Users should invoke 
	// setInetAddress(host) for their system.
	// Uncomment code below to make default
	// the first IP of the Host.
	//-----------------------------------------
	iNetAddress = null;

/*****************************************************
	try
	{
	    iNetAddress = InetAddress.getLocalHost();
	}
	catch(Exception e)
	{
	    iNetAddress = null;
	    eventLog.sendMessage("Could not bind localhost InetAddress");
	}
********************************************************/

	//-------------------------------------------------
	// Load our famous Reloader :-)
	//-------------------------------------------------
	loader = new AppClassReloader(this, docBase);

	start(argvs);
	addTerminationHook(this);
	WaitForExit();
    }

    //--------------------------------------------------------------------------------
    // METHOD 	register()
    //
    // PROCESS	Register an Object with Daemon
    //
    // INPUT:	String - name of object
    //		Object - object
    //
    // RETURN:  None.
    //--------------------------------------------------------------------------------
    public void register(String s, Object o)
    {
	dObjects.put(s, o);
    }

    //--------------------------------------------------------------------------------
    // METHOD 	service()
    //
    // PROCESS	Return a reference to a Mos Obejct registered with Daemon. 
    //
    // INPUT:	String - name of object
    //
    // RETURN:  Mos Object
    //--------------------------------------------------------------------------------
    public Mos service(String s)
    {
	return((Mos)dObjects.get(s));
    }

    //--------------------------------------------------------------------------------
    // METHOD 	lookup()
    //
    // PROCESS	Return a reference to an Obejct registered with Daemon. 
    //
    // INPUT:	String - name of object
    //
    // RETURN:  Object
    //--------------------------------------------------------------------------------
    public Object lookup(String s)
    {
	return(dObjects.get(s));
    }




    //--------------------------------------------------------------------------------
    // METHOD 	setClassRoot()
    //
    // RETURN:  None.
    //--------------------------------------------------------------------------------
    public void setClassRoot(String root)
    {
	docBase = root;

	//-------------------------------------------------
	// Load our famous Reloader :-)
	//-------------------------------------------------
	loader = new AppClassReloader(this, docBase);

    }



    //--------------------------------------------------------------------------------
    // METHOD 	start()
    //
    // PURPOSE:	Abstract method is called to start any Daemon
    //
    // INPUT:	argvs ( command line arguments)
    //
    // RETURN:  None.
    //--------------------------------------------------------------------------------
    public abstract void start(String argvs[] );



    //--------------------------------------------------------------------------------
    // METHOD 	getInetAddress()
    //
    // PURPOSE:	Return the current iNetAddress setting.      
    //
    // INPUT:	None.
    //
    // PROCESS:	None.
    //
    // RETURN:  iNetAddress
    //--------------------------------------------------------------------------------
    public InetAddress getInetAddress()
    {
	return(iNetAddress);
    }

    //--------------------------------------------------------------------------------
    // METHOD 	setInetAddress()
    //
    // PURPOSE:	Set InetAddress (binding address) for this Daemon.      
    //
    // INPUT:	String = Host Name.
    //
    // PROCESS:	None.
    //
    // RETURN:  none
    //--------------------------------------------------------------------------------
    public void setInetAddress(String host)
    {
	try
	{
	    iNetAddress = InetAddress.getByName(host);
	}
	catch(Exception e)
	{
	    iNetAddress = null;
	    eventLog.sendMessage("***ERROR*** Could not bind InetAddress for "
				+host+" becasue: "+e.getMessage());
	}
    }

    //--------------------------------------------------------------------------------
    // METHOD 	log()
    //
    // PURPOSE:	Shorthand for eventLog.sendMessage(e)      
    //
    // INPUT:	String -- Log message
    //
    // PROCESS:	Calls evetLog.sendMessage
    //
    // RETURN:  None.
    //--------------------------------------------------------------------------------
    public void log(String e)
    {
	eventLog.sendMessage(e);
    }

    //--------------------------------------------------------------------------------
    // METHOD 	log()
    //
    // PURPOSE:	Shorthand for eventLog.sendMessage(e)      
    //
    // INPUT:	Exception -- Log message
    //
    // PROCESS:	Calls evetLog.sendMessage
    //
    // RETURN:  None.
    //--------------------------------------------------------------------------------
    public void log(Exception e)
    {
	eventLog.sendMessage(e);
    }

    //--------------------------------------------------------------------------------
    // METHOD 	GetLoggingThreshold()
    //
    // PURPOSE:	Return the current Logging Threshold.      
    //
    // INPUT:	None.
    //
    // PROCESS:	None.
    //
    // RETURN:  Integer between MIN_LOG_THRESHOLD AND MAX_LOG_THRESHOLD
    //--------------------------------------------------------------------------------
    public int GetLoggingThreshold()
    {
	return(logging_threshold);
    }

    //--------------------------------------------------------------------------------
    // METHOD 	getLoggingThreshold()
    //
    // PURPOSE:	Return the current Logging Threshold.      
    //
    // INPUT:	None.
    //
    // PROCESS:	None.
    //
    // RETURN:  Integer between MIN_LOG_THRESHOLD AND MAX_LOG_THRESHOLD
    //--------------------------------------------------------------------------------
    public int getLoggingThreshold()
    {
	return(logging_threshold);
    }


    //--------------------------------------------------------------------------------
    // METHOD 	setLoggingThreshold()
    //
    // PURPOSE:	Set logging threshold
    //
    // INPUT:	Integer between MIN_LOG_THRESHOLD AND MAX_LOG_THRESHOLD.
    //
    // PROCESS:	1) Check MIN/MAX criteria.
    // 		2) Set logging_threshold.
    //
    // RETURN:  None.
    //--------------------------------------------------------------------------------
    public void setLoggingThreshold(int v)
    {
        if((v >= MIN_LOG_THRESHOLD) && (v <= MAX_LOG_THRESHOLD))
	{
	    logging_threshold = v;
	}
    }

    //--------------------------------------------------------------------------------
    // METHOD 	SetLoggingThreshold()
    //
    // PURPOSE:	Set logging threshold
    //
    // INPUT:	Integer between MIN_LOG_THRESHOLD AND MAX_LOG_THRESHOLD.
    //
    // PROCESS:	1) Check MIN/MAX criteria.
    // 		2) Set logging_threshold.
    //
    // RETURN:  None.
    //--------------------------------------------------------------------------------
    public void SetLoggingThreshold(int v)
    {
        if((v >= MIN_LOG_THRESHOLD) && (v <= MAX_LOG_THRESHOLD))
	{
	    logging_threshold = v;
	}
    }


    //--------------------------------------------------------------------------------
    // METHOD 	WaitForExit()
    //
    // PURPOSE:	This function is called by main() to block main() until 
    //         	the program is terminited via a kill signal or some other 
    //          orderly shutdown.
    //
    // INPUT:	None.
    //
    // PROCESS:	1) Block on ExitSignal.
    // 		2) Invoke System.exit() when signeled.
    //
    // RETURN:  Never.
    //--------------------------------------------------------------------------------
    public void WaitForExit()
    {
	ExitSignal.Wait();    // Hangout till someone shuts this baby down
        System.exit(0);
    }



    //--------------------------------------------------------------------------------
    // METHOD 	exit()
    //
    // PURPOSE:	Called to invoke a shutdown....This method signals the ExitSignal.
    //
    // INPUT:	None.
    //
    // PROCESS:	None.
    //
    // RETURN:  Never.
    //--------------------------------------------------------------------------------
    public void exit()
    {
        ExitSignal.Signal();
    }


    //--------------------------------------------------------------------------------
    // METHOD 	shutdown()
    //
    // PURPOSE:	Called to invoke a shutdown.
    //
    // INPUT:	None.
    //
    // PROCESS:	Wait for all Mos tasks to shutdown their Queues.
    //
    // RETURN:  Never.
    //--------------------------------------------------------------------------------
    public void shutdown()
    {
	eventLog.sendMessage("**** SHUTDOWN IN PROGRESS! ****");

	shutdownFlag = true;
        Mos m;
	//----------------------------------------
	// Send shutdown signal to each Mos
	//----------------------------------------
	while( (m= (Mos)mosList.Dequeue()) != null)
	{
	    m.shutdownSignal();
	}
    }


    //--------------------------------------------------------------------------------
    // METHOD 	shutdown()
    //
    // PURPOSE:	Called to invoke a shutdown....force shutdown after n milliseconds
    //
    // INPUT:	wait time is miliseconds
    //
    // PROCESS:	Force shutdown in n milliseconds.
    //
    // RETURN:  Never.
    //--------------------------------------------------------------------------------
    public void shutdown(int ticks)
    {
	eventLog.sendMessage("**** SHUTDOWN FORCED IN "+ticks+" SECONDS!!! ****");

	shutdown();

	com.qkernel.Timer t = new Timer(this, this);

	t.start(ticks, "exit");
    }


    //--------------------------------------------------------------------------------
    // METHOD 	waitShutdown()
    //
    // PURPOSE:	Check shutdownFlag
    //
    // RETURN:  true or false
    //--------------------------------------------------------------------------------
    public boolean waitShutdown()
    {
	return(shutdownFlag);
    }

    //--------------------------------------------------------------------------------
    // METHOD 	mosRegister(MOS)
    //
    // PURPOSE:	Register MOS with daemon
    //
    //--------------------------------------------------------------------------------
    public void mosRegister(Mos m)
    {
	mosList.Enqueue(m);
        mosCount++;
    }


    //--------------------------------------------------------------------------------
    // METHOD 	mosExit(Mos)
    //
    // PURPOSE:	Mos exits for shutdown
    //
    //--------------------------------------------------------------------------------
    public void mosExit(Mos m)
    {
	mosCount--;

	if(mosCount < 1)
	{
	    shutdownDone = true;
	    try{Thread.sleep(2000);}catch(Exception e){}
	    exit();
	}
    }



    //--------------------------------------------------------------------------------
    // METHOD 	getLicenseKey()
    //
    // PURPOSE: Returns License key.
    //--------------------------------------------------------------------------------
    public String getLicenseKey()
    {
	return(lkey);
    }

    //--------------------------------------------------------------------------------
    // METHOD 	getNodeVersion()
    //
    // PURPOSE: Returns Node Version.
    //--------------------------------------------------------------------------------
    public String getNodeVersion()
    {
	return(nodeVer);
    }

    //--------------------------------------------------------------------------------
    // METHOD 	getNodeProvider()
    //
    // PURPOSE: Returns Node Provider.
    //--------------------------------------------------------------------------------
    public String getNodeProvider()
    {
	return(useBy);
    }


    //--------------------------------------------------------------------------------
    // METHOD 	licenseCheck()
    //
    // PURPOSE: Checks for valid Daemon license key.
    //
    // RETURN:  Secret code.
    //--------------------------------------------------------------------------------
    public String licenseCheck(String question)
    {
	String secret_code ="";
        Method method = null;
        Method method2 = null;
        Method method3 = null;
        Object[] arguments = null;
        Object[] cargs = null;
	Class[]  params = null;
	Class[]  cparms = null;
        Class KeyClass =null;
        Constructor KeyConstructor =null;
        LicenseKey license_key = null;

	Date right_now = new Date();

	try
	{
            KeyClass		= Class.forName("com.qkernel.license."+lkey);
	    cargs		= new Object[]{ right_now };
	    cparms		= new Class[] { java.util.Date.class };
	    KeyConstructor	= KeyClass.getConstructor(cparms);
	    license_key         = (LicenseKey)KeyConstructor.newInstance(cargs);
       	    arguments		= new Object[]{ question };
            params		= new Class[] { question.getClass() };

	}
	catch(Exception e)
	{
	    return(secret_code = "*** ERROR! Could not find LicenseKey for:"+lkey);
	}

	try
        {
            method      = KeyClass.getMethod("licenseCheck", params);
	    secret_code = (String)method.invoke(license_key, arguments);
	    
            method2     = KeyClass.getMethod("licenseTo");
	    useBy       = (String)method2.invoke(license_key);

            method3     = KeyClass.getMethod("nodeVersion");
	    nodeVer     = (String)method3.invoke(license_key);
	    
        }
        catch( Exception e)
	{
	    return(secret_code = "*** ERROR! "+lkey+" EXPIRED!");
	}
	return(secret_code);
    }



    //--------------------------------------------------------------------------------
    // METHOD 	loadClass()
    //
    // PURPOSE:	Load/Reload a Class using AppClassLoader.
    //
    // INPUT:	Class name
    //
    // PROCESS:	load class.
    //
    // RETURN:  Class throws Exception of error.
    //--------------------------------------------------------------------------------
    public Class loadClass(String cn) throws Exception
    {
	String cname = cn;
	Class clazz = null;

    	try
	{
	    clazz = loader.reload(cname);
	}
	catch(Exception e)
	{
	    throw e;
	}
	return(clazz);
    }

    //--------------------------------------------------------------------------------
    // METHOD 	addTerminationHook()
    //
    // PURPOSE:	Handle kill signal 15 (terminate)
    //
    // PROCESS:	N/A
    // RETURN:  N/A
    //--------------------------------------------------------------------------------
    public void addTerminationHook(Daemon d)
    {
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                try
                {
	            setName("SHUTDOWN");
                    d.shutdown();
	        }
                catch (Exception e)
                {
		    log(e);
                }
            }
        });
    }

    

    //--------------------------------------------------------------------------------
    // METHOD 	Daemon()
    //
    // PURPOSE:	Contructor...Set default logging threshold.
    //
    // INPUT:	License Key.
    //
    // PROCESS:	None.
    //
    // RETURN:  None.
    //--------------------------------------------------------------------------------
    public Daemon(String key)
    {
	lkey = key;
	logging_threshold = MAX_LOG_THRESHOLD;
    }

}
