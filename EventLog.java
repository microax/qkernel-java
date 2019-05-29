package com.qkernel;
//
// EventLog.java
// ----------------------------------------------------------------------------
// History:
// --------
// 04/18/02 M. Gill	Add sendMessage(int threshild, Exception e), and 
//			sendMessage(Exception e)
//
// 03/05/02 M. Gill	Update to support logging thresholds
// 09/20/97 M. Gill	Initial creation.
// ----------------------------------------------------------------------------
//
import java.io.*;
import java.lang.*;
import java.text.*;
import java.util.Date;


public final class EventLog extends Mos
{

    private DateFormat MyDateFormat;	// Display format


    //--------------------------------------------------------------------------------
    // METHOD MessageHandler()
    //
    // PURPOSE:	Format and print messages.   
    //
    // INPUT:	Mos MessageNode.
    //
    // PROCESS:	1) Format and print  output string
    //
    // RETURN:	None.
    //--------------------------------------------------------------------------------
    public void MessageHandler(MessageNode n)
    {
	String Sender 	= n.thread.getName();
	String Message 	= (String)n.object;
	String Time	= MyDateFormat.format(n.timestamp);

	System.out.println(Time +" "+ Sender +" "+ Message);
    }



    //--------------------------------------------------------------------------------
    // METHOD sendMessage()
    //
    // PURPOSE:	Send lowest priority message
    //
    // INPUT:	String => message
    //
    // PROCESS:	1) Check current logging threashold...send on MAX
    //          2) Ivoke Mos.sendMessage()
    //
    // RETURN:	None.
    //--------------------------------------------------------------------------------
    public void sendMessage(String message)
    {
	if( daemon.GetLoggingThreshold() == daemon.MAX_LOG_THRESHOLD)
	{
	    super.sendMessage(message);
	}
    }



    //--------------------------------------------------------------------------------
    // METHOD sendMessage()
    //
    // PURPOSE:	Send message if threshold equal or greater to current threshold
    //
    // INPUT:	int    => threshold
    //		String => message
    //
    // PROCESS:	1) Compare current logging threashold to threshold
    //          2) Ivoke Mos.sendMessage()
    //
    // RETURN:	None.
    //--------------------------------------------------------------------------------
    public void sendMessage(int threshold, String message)
    {
	if( daemon.GetLoggingThreshold() >= threshold)
	{
	    super.sendMessage(message);
	}
    }

    //--------------------------------------------------------------------------------
    // METHOD sendMessage()
    //
    // PURPOSE:	Log Exception if threshold equal or greater to current threshold
    //
    // INPUT:	int       => threshold
    //		Exception => exception
    //
    // PROCESS:	1) Compare current logging threashold to threshold
    //          2) Create a StringWriter
    //		3) Print the stacktrace to the writer
    //		4) Use sendMessage() to send the stacktrace
    //
    // RETURN:	None.
    //--------------------------------------------------------------------------------
    public void sendMessage(int threshold, Exception exception)
    {
	if( daemon.GetLoggingThreshold() >= threshold)
	{
	    StringWriter sw = new StringWriter(); 
	    exception.printStackTrace(new PrintWriter(sw)); 
	    String message = sw.toString(); 

	    super.sendMessage(message);
	}
    }



    //--------------------------------------------------------------------------------
    // METHOD sendMessage()
    //
    // PURPOSE:	Log Exception if threshold equal MAX_LOG_THRESHOLD
    //
    // INPUT:	Exception => exception
    //
    // PROCESS:	1) Compare current logging threashold to MAX
    //          2) Create a StringWriter
    //		3) Print the stacktrace to the writer
    //		4) Use sendMessage() to send the stacktrace
    //
    // RETURN:	None.
    //--------------------------------------------------------------------------------
    public void sendMessage(Exception exception)
    {
	if( daemon.GetLoggingThreshold() == daemon.MAX_LOG_THRESHOLD)
	{
	    StringWriter sw = new StringWriter(); 
	    exception.printStackTrace(new PrintWriter(sw)); 
	    String message = sw.toString(); 

	    super.sendMessage(message);
	}
    }


    //------------------------------------
    // This is to support old code
    //------------------------------------
    public void SendMessage(String message)
    {
	sendMessage(message);
    }



    //--------------------------------------------------------------------------------
    // METHOD EventLog()
    //
    // PURPOSE:	EventLog constructor.   
    //
    // INPUT:	Name of EventLog instance.
    // 		Referance to Daemon object.
    //
    // PROCESS:	1) Invoke Mos constructor
    //          2) Set Date/Time display format.
    //
    // RETURN:	None.
    //--------------------------------------------------------------------------------
    public EventLog(String n, Daemon d)
    {
        super(n, d);

	//-----------------------------------------------------------------
	// Change this to produce differant Date/time display formats
	// All outputs are currently DATE/TIME + SENDER + MESSAGE
	//-----------------------------------------------------------------
	MyDateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, 
						      DateFormat.SHORT);
    }
}

