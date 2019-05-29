package com.qkernel.comm;
//
// XircomGsm.java        Xircom GSM modem server
// ----------------------------------------------------------------------------
// History:
// --------
// 07/12/01 M. Gill	Changed to support SmsModem proxy.
// 04/22/01 M. Gill	Initial creation.
// ----------------------------------------------------------------------------
//
import java.lang.*;
import java.util.*;
import java.io.*;
import javax.comm.*;
import com.qkernel.*;


public final class XircomGsm extends Fsm implements XircomEvents, 
						    SerialPortEventListener, 
						    SmsServiceEvents
{
    static CommPortIdentifier portId;
    static Enumeration portList;
    private SmsModem userAgent;
    private InputStream inputStream;
    private InputStreamReader reader;
    private BufferedReader inputBuffer;
    private SerialPort serialPort;
    static OutputStream outputStream;
    private String phoneNumber;
    private String message;
    private String currentIndex;

    private boolean sending;
    private boolean reading_message;
    private boolean deleting;

    private String device;
    private int bitrate;
    private int databits;
    private int stopbits;
    private int parity;
    private String scaNumber;
    private Daemon daemon;

    private com.qkernel.Queue smsQueue;
    private com.qkernel.Queue rmQueue;

    public void SetStateTable()
    {
//-------------------------------------------------------------------------------
//        CURRENT_ST	COMMAND/EVENT	NEXT_ST		ACTION
//        ----------	-------------	----------	------
SetEvent( XIRCOM_INIT,	INIT_OK,	XIRCOM_INIT,	"InitXircom"		);
SetEvent( XIRCOM_INIT,	GSM_OK,		XIRCOM_READY,	"GsmOk"			);

SetEvent( XIRCOM_READY,	MODEM_DATA,	XIRCOM_READY,	"ReadData"  		);
SetEvent( XIRCOM_READY,	SMS_MESSAGE,	XIRCOM_READY,	"SmsToAgent"		);
SetEvent( XIRCOM_READY,	SEND_SMS,	XIRCOM_SEND,	"SmsToModem"		);
SetEvent( XIRCOM_READY,	GSM_OK,		XIRCOM_READY,	"DoNothing"		);
SetEvent( XIRCOM_READY,	GSM_ERROR,	XIRCOM_INIT,	"GsmError"		);

SetEvent( XIRCOM_SEND,	MODEM_DATA,	XIRCOM_SEND,	"ReadData"  		);
SetEvent( XIRCOM_SEND,	SMS_MESSAGE,	XIRCOM_SEND,	"SmsToAgent"		);
SetEvent( XIRCOM_SEND,	SEND_SMS,	XIRCOM_SEND,	"QueueSmsToModem"	);
SetEvent( XIRCOM_SEND,	SEND_SMS_OK,	XIRCOM_SEND,	"NextSmsToModem"	);
SetEvent( XIRCOM_SEND,	SEND_DONE,	XIRCOM_CLEAN,	"SmsToModemDone"	);
SetEvent( XIRCOM_SEND,	GSM_OK,		XIRCOM_SEND,	"DoNothing"		);
SetEvent( XIRCOM_SEND,	GSM_ERROR,	XIRCOM_INIT,	"GsmError"		);

SetEvent( XIRCOM_CLEAN,	CLEAN_MEMORY,	XIRCOM_CLEAN,	"CleanMemory"  		);
SetEvent( XIRCOM_CLEAN,	MODEM_DATA,	XIRCOM_CLEAN,	"ReadData"  		);
SetEvent( XIRCOM_CLEAN,	SMS_MESSAGE,	XIRCOM_CLEAN,	"SmsToAgent"		);
SetEvent( XIRCOM_CLEAN,	SEND_SMS,	XIRCOM_CLEAN,	"QueueSmsToModem"	);
SetEvent( XIRCOM_CLEAN,	CLEAN_OK,	XIRCOM_CLEAN,	"CleanMemory"		);
SetEvent( XIRCOM_CLEAN,	CLEAN_DONE,	XIRCOM_READY,	"CleanDone"		);
SetEvent( XIRCOM_CLEAN,	GSM_OK,		XIRCOM_CLEAN,	"DoNothing"		);
SetEvent( XIRCOM_CLEAN,	GSM_ERROR,	XIRCOM_INIT,	"GsmError"		);

//--------------------------------------------------------------------------------
    }


    //---------------------------------------------------------------------------------
    // METHOD   InitXircom()
    //
    // PURPOSE: Action Method to initialize Xircom GSM modem.
    //
    // INPUT:   None.
    // RETURN:  None.
    //---------------------------------------------------------------------------------
    public void InitXircom()
    {
	EventMessage evt= new EventMessage();

	writeString("ATV1\r");  //Make result codes verbose (when on)
	writeString("ATQ0\r");	//Turn on result codes	
	writeString("AT\r");	//Make modem say 'OK'
	try{ sleep(2000);}catch(Exception e){}

	writeString("AT+CSCA=\""+ scaNumber + "\"\r");

	evt.Event= GSM_OK;
	SendMessage(evt);

       	daemon.event_log.SendMessage("InitXircom() complete...");
    }



    //---------------------------------------------------------------------------------
    // METHOD   ReadData()
    //
    // PURPOSE: Action Method to read modem data.
    //
    // INPUT:   None.
    // RETURN:  None.
    //---------------------------------------------------------------------------------
    public void ReadData()
    {
	String str= (String)LastEvent.Data;
	EventMessage evt= new EventMessage();

	StringTokenizer tok = new StringTokenizer(str, ":");

	if(tok.countTokens() > 0)
	{
	    String tstr = tok.nextToken();

	    if(tstr.equals("+CMTI"))
	    {
	    	StringTokenizer tok2 = new StringTokenizer(str, ",");

		//-------------------------------------
		// We have an incomming SMS message.
		// First get the index number, then
		// send a request to read that index
		// message..this will invoke a +CMGR 
		// response...
		//-------------------------------------
		String tmp = tok2.nextToken();
		String idx = tok2.nextToken();
		writeString("AT+CMGR="+idx+"\r");
		currentIndex = idx;
		rmQueue.Enqueue(idx);
		return;
	    }
	    if(tstr.equals("+CMGR"))
	    {
	    	StringTokenizer tok3 = new StringTokenizer(str, ",");

		String tmp1 = tok3.nextToken();
		String tmp2 = tok3.nextToken();
		phoneNumber = tmp2.substring(3,tmp2.length() -1);
		reading_message = true;
		return;
	    }
	    if(tstr.equals("> +CMGS"))
	    {
		evt.Event = SEND_SMS_OK;
		SendMessage(evt);
		return;
	    }
	    if(tstr.equals("+CMGS"))
	    {
		evt.Event = SEND_SMS_OK;
		SendMessage(evt);
		return;
	    }
	}

	if(reading_message)
  	{
	    SmsMessage sms	= new SmsMessage();
	    sms.ProviderId	= 1;
	    sms.PhoneNumber	= phoneNumber;
	    sms.Message	= str;

	    evt.Event = SMS_MESSAGE;
	    evt.Data  = sms; 
	    SendMessage(evt);
	    reading_message=false;


	    daemon.event_log.SendMessage("Incomming SMS from:"+ sms.PhoneNumber 
					+" Message="+sms.Message);
	    return;
	}

	if(deleting)
  	{
	    if(str.equals("OK"))
	    {
		evt.Event = CLEAN_OK;
		SendMessage(evt);
		deleting=false;
		return;
	    }
	}

	return;
    }


    //---------------------------------------------------------------------------------
    // METHOD   SmsToAgent()
    //
    // PURPOSE: Action Method to Send SMS message to user agent.
    //
    // INPUT:   None.
    // RETURN:  None.
    //---------------------------------------------------------------------------------
    public void SmsToAgent()
    {
	SmsMessage sms = (SmsMessage)LastEvent.Data;

	userAgent.onSmsMessage(sms);
    }

    //---------------------------------------------------------------------------------
    // METHOD   SmsToModem()
    //
    // PURPOSE: Action Method to Send SMS message to modem.
    //
    // INPUT:   None.
    // RETURN:  None.
    //---------------------------------------------------------------------------------
    public void SmsToModem()
    {
	SmsMessage m  = (SmsMessage)LastEvent.Data;
	writeSmsToModem(m);
    }

    //---------------------------------------------------------------------------------
    // METHOD   QueueSmsToModem()
    //
    // PURPOSE: Action Method to Queue a SMS message for output.
    //
    // INPUT:   None.
    // RETURN:  None.
    //---------------------------------------------------------------------------------
    public void QueueSmsToModem()
    {
	SmsMessage m  = (SmsMessage)LastEvent.Data;
	String phone  =m.PhoneNumber.substring(1); 
	String message=m.Message;

	smsQueue.Enqueue(m);
    }


    //---------------------------------------------------------------------------------
    // METHOD   NextSmsToModem()
    //
    // PURPOSE: Action Method to send a SMS message for output from the smsQueue.
    //
    // INPUT:   None.
    // RETURN:  None.
    //---------------------------------------------------------------------------------
    public void NextSmsToModem()
    {
	SmsMessage m = (SmsMessage)smsQueue.Dequeue();
	if(m != null)
	{
	    writeSmsToModem(m);
	}
	else
	{
	    EventMessage evt = new EventMessage();
	    evt.Event = SEND_DONE;
	    SendMessage(evt);
	}
    }


    //---------------------------------------------------------------------------------
    // METHOD   SmsToModemDone()
    //
    // PURPOSE: Action Method called when smsQueue is empty.
    //
    // INPUT:   None.
    // RETURN:  None.
    //---------------------------------------------------------------------------------
    public void SmsToModemDone()
    {
	daemon.event_log.SendMessage("SMS DONE...");

	EventMessage evt = new EventMessage();
	evt.Event = CLEAN_MEMORY;
	SendMessage(evt);
    }

    //---------------------------------------------------------------------------------
    // METHOD   CleanMemory()
    //
    // PURPOSE: Remove received messages from modem memory.
    //
    // INPUT:   None.
    // RETURN:  None.
    //---------------------------------------------------------------------------------
    public void CleanMemory()
    {
	EventMessage evt= new EventMessage();
	String s 	= (String)rmQueue.Dequeue();

	if(s != null)
	{
	    daemon.event_log.SendMessage("Purge message index:"+s+"...");
	    writeString("AT+CMGD="+s+"\r");
	    deleting=true;
	}
	else
	{
	    evt.Event = CLEAN_DONE;
	    SendMessage(evt);
	    deleting=false;
	}
    }


    //---------------------------------------------------------------------------------
    // METHOD   CleanDone()
    //
    // PURPOSE: Modem memory cleanup complete.
    //
    // INPUT:   None.
    // RETURN:  None.
    //---------------------------------------------------------------------------------
    public void CleanDone()
    {
	daemon.event_log.SendMessage("Memory purge complete...");

	SmsMessage m = (SmsMessage)smsQueue.Dequeue();

	if(m != null)
	{
	    EventMessage evt = new EventMessage();
	    evt.Event = SEND_SMS;
	    evt.Data  = m;
	    SendMessage(evt);
	}
    }


    //---------------------------------------------------------------------------------
    // METHOD   GsmOk()
    //
    // PURPOSE: Action Method.
    //
    // INPUT:   None.
    // RETURN:  None.
    //---------------------------------------------------------------------------------
    public void GsmOk()
    {
	daemon.event_log.SendMessage("GSM OK...");
    }

    //---------------------------------------------------------------------------------
    // METHOD   GsmError()
    //
    // PURPOSE: Action Method.
    //
    // INPUT:   None.
    // RETURN:  None.
    //---------------------------------------------------------------------------------
    public void GsmError()
    {
	daemon.event_log.SendMessage("GSM Error...");
	writeString("AT\r");	// Try to make OK again...
    }



    //---------------------------------------------------------------------------------
    // METHOD   writeSmsToModem()
    //
    // PURPOSE: Write SMS message to modem via writeString().
    //
    // INPUT:   SmsMessage.
    // RETURN:  None.
    //---------------------------------------------------------------------------------
    public void writeSmsToModem(SmsMessage m)
    {
	String phone  =m.PhoneNumber.substring(1); 
	String message=m.Message;
	String cmd    ="AT+CMGS=\""+ phone + "\"\r";
	writeString(cmd);

	try{ sleep(2000);}catch(Exception e){}
	writeString(message +'\032');

	daemon.event_log.SendMessage("Outbound SMS Message Length="+message.length());
    }

    //---------------------------------------------------------------------------------
    // METHOD   writeString()
    //
    // PURPOSE: Write a String to the Xircom modem.
    //
    // INPUT:   String
    // RETURN:  None.
    //---------------------------------------------------------------------------------
    public void writeString(String str)
    {
	try 
	{
//	    daemon.event_log.SendMessage(str);
	    outputStream.write(str.getBytes());
	    outputStream.flush();
	} 
	catch (IOException e) 
	{
	    daemon.event_log.SendMessage("**ERROR** writeString() failed");
	}
    }



    //---------------------------------------------------------------------------------
    // METHOD   serialEvent()
    //
    // PURPOSE: Callback method invoked by the searial port reader.
    //
    // INPUT:   SerialPortEvent
    // RETURN:  None.
    //---------------------------------------------------------------------------------
     public void serialEvent(SerialPortEvent event) {

        switch(event.getEventType()) 
	{
            case SerialPortEvent.BI:
            case SerialPortEvent.OE:
            case SerialPortEvent.FE:
            case SerialPortEvent.PE:
            case SerialPortEvent.CD:
            case SerialPortEvent.CTS:
            case SerialPortEvent.DSR:
            case SerialPortEvent.RI:
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
            break;

            case SerialPortEvent.DATA_AVAILABLE:

            try 
	    {
		while(inputBuffer.ready())
		{
		    String str =inputBuffer.readLine();
		    if(str.length() != 0)
	  	    {
		    	EventMessage evt = new EventMessage();
		    	evt.Event = MODEM_DATA;
		    	evt.Data  = str; 
		    	SendMessage(evt);

		    	//daemon.event_log.SendMessage(evt.Data);
		    }
		}
            } 
	    catch (IOException e) {}
            break;
        }
    }


    //---------------------------------------------------------------------------------
    // METHOD   start()
    //
    // PURPOSE: Starts GSM modem server.
    //
    // INPUT:   agent - user agent which started this...
    // RETURN:  None.
    //---------------------------------------------------------------------------------
    public void start(SmsModem agent)
    {
    	userAgent = agent;
	smsQueue  = new com.qkernel.Queue();
	rmQueue   = new com.qkernel.Queue();
	sending   = false;
	deleting  = false;
	reading_message = false;

	portList = CommPortIdentifier.getPortIdentifiers();

	daemon.event_log.SendMessage("Scaning serial devices...");

        while (portList.hasMoreElements()) 
	{
 	    portId = (CommPortIdentifier) portList.nextElement();
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) 
	    {
	    	daemon.event_log.SendMessage(portId.getName() + " found...");

		if (portId.getName().equals(device)) 
		{
		    try
		    {
			serialPort = (SerialPort) portId.open("XircomGsm", 2000);
		    } 
		    catch (PortInUseException e) 
		    {
			daemon.event_log.SendMessage("**ERROR** Could not open " + device);
			break;
		    }
        	    try 
		    {
			outputStream= serialPort.getOutputStream();
      			inputStream = serialPort.getInputStream();
			reader      = new InputStreamReader(inputStream);
			inputBuffer = new BufferedReader(reader);
        	    } 
		    catch (IOException e) 
		    {
			daemon.event_log.SendMessage("**ERROR ** Could not create i/o steams ");
			break;
		    }
		    try 
		    {
 			serialPort.addEventListener(this);
		    } 
		    catch (TooManyListenersException e)
		    {
			daemon.event_log.SendMessage("**ERROR ** Could add listener ");
			break;
		    }

        	    serialPort.notifyOnDataAvailable(true);
        	    try 
		    {
			serialPort.setSerialPortParams(bitrate,
						       databits,
						       stopbits,
						       parity);
        	    } 
		    catch (UnsupportedCommOperationException e)
		    {
			daemon.event_log.SendMessage("**ERROR ** Could set serial port params ");
			break;
		    }
                }
            }
        }
	daemon.event_log.SendMessage("Using "+ device + " at "+ bitrate + " baud");
        super.start();
    }


    //---------------------------------------------------------------------------------
    // METHOD  XircomGsm()
    //
    // PURPOSE: Constructor.
    //---------------------------------------------------------------------------------
    public XircomGsm(String port, int rate, int bits, int stop, int par, String sca, Daemon d)
    {
	super("XircomGsm", d);

	device 		= port;
	bitrate		= rate;
	databits	= bits;
	stopbits	= stop;
	parity		= par;
        scaNumber	= sca;
	daemon 		= d;

    }
}
