package com.qkernel;
//
// Mos.java     Message Oriented Server Class
// ----------------------------------------------------------------------------
// History:
// --------
// 12/05/03 M. Gill	Add try/catch to MessageHandler.
//
// 06/03/02 M. Gill	1) Add dump() which returns an ArrayList of 
//			   MessageNodes in Queue.
//			2) Add reporting on shutdownSignal()
//
// 01/12/02 M. Gill	1) Add support for orderly shutdown of Mos threads
//			2) Add sendMessage() it is same as SendMessage().
//
// 07/28/01 M. Gill	Add licenseCheck() support.
//
// 01/09/00 M. Gill     Add GetMessageCount()
// 12/19/99 M. Gill	Add MyName.
// 09/15/99 M. Gill     Move creation of Queue and Semaphore to constructor.
//                      This eliminates the posibility of sending a message 
//                      before the queue is created.
//
// 09/20/97 M. Gill 	Initial creation.
// ----------------------------------------------------------------------------
//
import java.lang.*;
import java.text.*;
import java.util.Date;
import java.util.ArrayList;

//-----------------------------------------------------------------------------
// The Message Oriented Server (Mos) is a base class for a thread of execution. 
// A Mos thread blocks on a semaphore waiting for messages in it's queue. 
//
// Messages/objects are sent to Mos via the a SendMessage() method, which 
// enqueues  a MessageNode into the queue and signals the Mos semaphore.
// A MessageNode object may contain any object type, and thus SendMessage() 
// can send any object to a mos.
//
// MessageNodes also hold information about the message such as the Thread 
// reference of the calling object and the Date/Time of the created message. 
// In this way The Mos  message handler can always identify the originator 
// (by Name), and the creation time of an object passed to a Mos for processing.
//
// The abstract method  MessageHandler() must be implemented to process 
// Mos Messages.
// ----------------------------------------------------------------------------
//
public abstract class Mos extends Thread
{
    private Queue msgQueue;
    private Semaphore msgSema;
    private MessageNode currentMessageNode = null;
    public Daemon daemon;
    public String MyName;
    public boolean reportInit;
    public boolean reportReg;

    //--------------------------------------------------------------------------------
    // METHOD 	run()
    //
    // PURPOSE:	This the main execution loop for Mos.      
    //
    // INPUT:	None.
    //
    // PROCESS:	1) Block waiting for a MessageNode.
    // 		2) Invoke MessageHandler()
    //
    // RETURN:  Never.
    //--------------------------------------------------------------------------------
    public void run()
    {
	String lpw = daemon.licenseCheck("yRUjAvA?");
	if(lpw.equals("micr0s0FtSucKS!") == false)
	{
	    System.out.println("**** INVALID LICENSE KEY!!!! ****");
            System.exit(0);
	}
	
	setName(MyName);

	if((MyName.equals("EVL") == false) && (reportReg==true) )
	    daemon.mosRegister(this);

	if(reportInit)
	    daemon.event_log.SendMessage("Initialization Complete...");

         for( ;; )
         {  
	    msgSema.Wait();

	    if(daemon.waitShutdown() && (getMessageCount() == 0))
	    {   if( !MyName.equals("EVL") )
	        {  daemon.event_log.SendMessage("Shutdown Complete...");
		   daemon.mosExit(this);
		   return;
		}
	    }

	    try
	    {
            	MessageHandler(currentMessageNode = (MessageNode)msgQueue.Dequeue()); 
	    }
	    catch(Exception e)
	    {
		daemon.eventLog.sendMessage(e);
	    }

	    if(!daemon.waitShutdown())
	    	currentMessageNode = null;
         }   
    }
    


    //--------------------------------------------------------------------------------
    // METHOD 	sendMessage()
    //
    // PURPOSE:	Public method used to send a message/object to a Mos.
    //
    // INPUT:	Any Object.
    //
    // PROCESS:	1) Create Message Node.
    // 		2) Get current time and calling thread id.
    // 		3) Pack Message Node and post to message queue.
    //
    // RETURN:	None.
    //--------------------------------------------------------------------------------
    public void sendMessage(Object o)
    {
        MessageNode message = new MessageNode();	// Create a Message Node
	message.thread      = Thread.currentThread();	// Get calling thread
	message.timestamp   = new Date();		// Get current date/time
	message.object      = o;			// Get sending Message

        PostMessage(message);				// Post node to queue
    }


    //--------------------------------------------------------------------------------
    // METHOD 	SendMessage()
    //
    // PURPOSE:	Public method used to send a message/object to a Mos.
    //
    // INPUT:	Any Object.
    //
    // PROCESS:	1) Create Message Node.
    // 		2) Get current time and calling thread id.
    // 		3) Pack Message Node and post to message queue.
    //
    // RETURN:	None.
    //--------------------------------------------------------------------------------
    public void SendMessage(Object o)
    {
        MessageNode message = new MessageNode();	// Create a Message Node
	message.thread      = Thread.currentThread();	// Get calling thread
	message.timestamp   = new Date();		// Get current date/time
	message.object      = o;			// Get sending Message

        PostMessage(message);				// Post node to queue
    }



    //--------------------------------------------------------------------------------
    // METHOD	PostMessage()
    //
    // PURPOSE:	Post messages into message queue.
    //
    // INPUT:	MessageNode.
    //
    // PROCESS:	1) Enqueue message into queue
    //		2) Signal Mos.
    //          3) Bump message count
    //
    // RETURN:	None.
    //--------------------------------------------------------------------------------
    private void PostMessage(MessageNode m)
    {
	if(daemon.waitShutdown())
   	{    if( !MyName.equals("EVL") )
		return;
	}

        msgQueue.Enqueue(m);
        msgSema.Signal();
    }




    //--------------------------------------------------------------------------------
    // METHOD	shutdownSignal()
    //
    // PURPOSE:	Cause a Mos to shutdown by sending Signal without a message
    //
    //--------------------------------------------------------------------------------
    public void shutdownSignal()
    {
	int size;
	ArrayList l     = null;
	StringBuffer b  = new StringBuffer();
	String newline  = System.getProperty("line.separator");
        String object   ="";

	l    = dump(); 
	size = l.size();

  	DateFormat myDateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, 
						      DateFormat.SHORT);

	b.append(MyName+" is shutting down after "+l.size()+" Objects are processed...");  

	if(currentMessageNode != null)
	{
	    b.append(newline+"The following Object is being processed by MessageHandler():"+newline);

	    String sender = currentMessageNode.thread.getName();
	    b.append("Sender: "+sender+newline);

	    String time = myDateFormat.format(currentMessageNode.timestamp);
	    b.append("Time: "+time+newline);

	    if(currentMessageNode.object instanceof EventMessage)
	    {
		EventMessage em = (EventMessage)currentMessageNode.object;
		object ="Event:"+em.Event+" Data:"+em.Data.toString();
	    }
	    else
	    {
	        object = currentMessageNode.object.toString();
	    }

	    b.append("Object: "+object+newline);
	    b.append("********************************************************");
	}

	if(size > 0)
	{
	b.append(newline+"The following Objects are in Queue:"+newline);  
	
	for( int i =0; i < size; i++)
	{
	    MessageNode n = new MessageNode();
	    n             = (MessageNode)l.get(i);

	    String sender = n.thread.getName();
	    b.append("Sender: "+sender+newline);

	    String time = myDateFormat.format(n.timestamp);
	    b.append("Time: "+time+newline);

	    object = n.object.toString();
	    b.append("Object: "+object+newline);
	    b.append("********************************************************"+newline);

	}
	}
	daemon.eventLog.sendMessage(b.toString());
	msgSema.Signal();
    }


    //--------------------------------------------------------------------------------
    // METHOD	getMessageCount()
    //
    // PURPOSE:	Return the current Message count.
    //
    // INPUT:	None.
    //
    // PROCESS:	None.
    //
    // RETURN:	msgQueue.Count()
    //--------------------------------------------------------------------------------
    public int getMessageCount()
    {
        return(msgQueue.Count());
    }

    //--------------------------------------------------------------------------------
    // METHOD	GetMessageCount()
    //
    // PURPOSE:	Return the current Message count.
    //
    // INPUT:	None.
    //
    // PROCESS:	None.
    //
    // RETURN:	msgQueue.Count()
    //--------------------------------------------------------------------------------
    public int GetMessageCount()
    {
        return(msgQueue.Count());
    }


    //--------------------------------------------------------------------------------
    // METHOD	dump()
    //
    // PURPOSE:	Return an ArrayList of MessageNodes currently in Queue.
    //
    // INPUT:	None.
    //
    // PROCESS:	None.
    //
    // RETURN:	msgQueue.dumpQueue()
    //--------------------------------------------------------------------------------
    public ArrayList dump()
    {
        return(msgQueue.dumpQueue());
    }



    //--------------------------------------------------------------------------------
    // METHOD	MessageHandler()
    //
    // PURPOSE: Abstract method invoked by run() to process Mos Message Nodes.
    //
    // INPUT:	MessageNode.
    //
    // PROCESS:	Abstract.
    //
    // RETURN:	None.
    //--------------------------------------------------------------------------------
    public abstract void MessageHandler(MessageNode m);


    //--------------------------------------------------------------------------------
    // METHOD Mos()
    //
    // PURPOSE:	Mos constructor.   
    //
    // INPUT:	Name of Mos instance.
    // 		Referance to Daemon object.
    //
    // PROCESS:	Create message queue and Semaphore objects...set MyName
    //
    // RETURN:	None.
    //--------------------------------------------------------------------------------
    public Mos(String name, Daemon d)
    {
        msgQueue = new Queue();
        msgSema  = new Semaphore(0);
        MyName   = name;
	reportInit = true;
	reportReg  = true;

        daemon =d;
    }
}
