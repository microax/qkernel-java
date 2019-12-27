package com.qkernel.batch;
//
// Qbatch.java	batch processing
// ----------------------------------------------------------------------------
// History:
// --------
// 11/11/19 M. Gill     Initial creation (lifted from metaQ.io's MqmChannel).
// ----------------------------------------------------------------------------
//
import java.lang.*;
import java.util.*;
import com.qkernel.*;

public abstract class Qbatch extends Thread
{
    public 	int retryInterval;
    public 	int retryCount;
    public	Daemon daemon;
    public  	String myName;
    public 	int batchCount;
    public 	int deferredCount;
    public 	com.qkernel.Queue deferred;
    public      com.qkernel.Queue reQueue;

    //--------------------------------------------------------------------------------
    // METHOD put()
    //
    // PURPOSE: Public method to put a batch into a channel
    //
    // INPUT:   QMessage - Batch message sent from Business Object.
    //
    // RETURN:  None.
    //--------------------------------------------------------------------------------
    public void put(QMessage m)
    {
	//----------------------------------------------
	// Init. the retry counter and timer.
	// We allow for the business object to 
	// override the channel defaults.
	//----------------------------------------------
	int v	= m.getInt("CHN-RETRY-TIMER");
	int c	= m.getInt("CHN-RETRY-COUNTER");

	if(c == -1)
	{
	    //-------------------------------------
	    // Turn off channel retries
	    //-------------------------------------
	    m.putInt("CHN-RETRY-TIMER", 0);
	    m.putInt("CHN-RETRY-COUNTER", 0);
	}
	else
	{
	    //---------------------------------------------
	    // Use the channel defaults or the 
	    // override values
	    //---------------------------------------------
	    if(c == 0)
	    	m.putInt("CHN-RETRY-TIMER", retryInterval);
	    if(v == 0)
	    	m.putInt("CHN-RETRY-COUNTER", retryCount);
	}
	//-----------------------------------------------
	// Bump overall batch count in this channel
	//------------------------------------------------
	batchCount++;

	//-----------------------------------------------
	// dispatch to our channel....
	// Chennel implementations figure out how to 
	// manage Queues, message dispatch  and stuff...
	//-----------------------------------------------
	dispatch(m);
    }


    //--------------------------------------------------------------------------------
    // METHOD callback()
    //
    // PURPOSE: Completion callback for channel is called by one of the chennel Queue
    //		processors when it's execution loop is complete.
    //
    //		1) Check error status in QMessage and report to account logging.
    //		2) If errors then check retry count and send to defered channel queue
    //		3) Report to billing when processing the batch completes itself.
    //
    // INPUT:   QMessage - Original message with status
    //
    // RETURN:  None.
    //--------------------------------------------------------------------------------
    public void callback(QMessage m)
    {
	int 	status 		= m.getInt("CHN-STATUS");
	int 	retryTimer	= m.getInt("CHN-RETRY-TIMER");
	int 	retryCounter	= m.getInt("CHN-RETRY-COUNTER");
	String	log		= m.getString("CHN-ACCOUNT-LOG");

	//----------------------------------------------
	// The total batch count is decremented...
	//----------------------------------------------
	batchCount--;

	if(status !=0 && retryCounter !=0)
	{
	    //----------------------------------------------------------------
	    // The batch failed. So, we decrement the re-try counter and place 
	    // it in the deferred queue. The deferred queue wakes up once per 
	    // minute to re-submit batches. ...It's a good idea to make the 
	    // inverval greater than 1 since the granuality is +- 1 minute.
	    //----------------------------------------------------------------
	    daemon.log("Will Re-try up to "+retryCounter+" times every "+retryInterval+" minutes");	

	    m.putInt("CHN-RETRY-TIMER", retryInterval);
	    m.putInt("CHN-RETRY-COUNTER", --retryCounter);
	    m.putBoolean("CHN-ISRETRY", true);
	    deferred.Enqueue(m);
 	    deferredCount++;
	}
	else
	{
	    //----------------------------------------------------------------
	    // At this point either the batch has been successfully processed
	    // or the channel gave up trying - we now let billing figure out 
	    // how to charge for it. Everything billing needs should be 
	    // already recorded in the (QMessage) batch.
	    //----------------------------------------------------------------
	    daemon.log(log);	
	}
    }


    //--------------------------------------------------------------------------------
    // METHOD   run()
    //
    // PURPOSE: This the main execution loop for the deferred queue timer.      
    //
    // RETURN:  Never.
    //--------------------------------------------------------------------------------
    public void run()
    {
        QMessage n;
        String eStr;
	deferred 	= new com.qkernel.Queue();
	reQueue		= new com.qkernel.Queue();
        setName(myName);
	if(retryCount !=0)
        {
	eStr ="Ready. Retry interval="+retryInterval+" minutes. Retry count="+ retryCount;
        daemon.log(eStr);
        }

	for(;;)
	{
	    try
            {
		//---------------------------
                // Wakeup once per minute
                //---------------------------
                sleep(60000);

          	while ((n = (QMessage)deferred.Dequeue()) != null )
            	{
		    //------------------------------------
		    // Get batch from list, and decrement 
		    // its retry timer...
		    //------------------------------------
		    int t= n.getInt("CHN-RETRY-TIMER");
		    n.putInt("CHN-RETRY-TIMER", --t);

               	    if(t == 0)
                    {
			//--------------------------------
			// re-insert batch into channel
			//--------------------------------
			batchCount++;
			dispatch(n);
			deferredCount--;
        	    }
		    else
		    {
			//-------------------------------
			// We're gonna re-insert this 
			// back into the deferred Queue
			//-------------------------------
			reQueue.Enqueue(n);
		    }
                }

		while((n = (QMessage)reQueue.Dequeue()) != null)
		{
		    //---------------------------------
		    // Re-insert into deferred Queue
		    //---------------------------------
		    deferred.Enqueue(n);
		}
	    }
	    catch(Exception e)
	    {
                daemon.log(e);
	    }
	}
    }


    //--------------------------------------------------------------------------------
    // METHOD getBatchCount()
    //
    // PURPOSE: Returns the number of batches (or QMessage entries) in a channel
    //
    // INPUT:   none
    //
    // RETURN:  batchCount
    //--------------------------------------------------------------------------------
    public int getBatchCount()
    {
	return(batchCount);
    }

    //--------------------------------------------------------------------------------
    // METHOD getDeferredCount()
    //
    // PURPOSE: Returns the number of batches (or QMessage entries) in deferred queue
    //
    // INPUT:   none
    //
    // RETURN:  deferredCount
    //--------------------------------------------------------------------------------
    public int getDeferredCount()
    {
	return(deferredCount);
    }


    //--------------------------------------------------------------------------------
    // METHOD start()
    //
    // PURPOSE: Starts the deferred Queue service thread
    //
    // INPUT:   none
    //
    // PROCESS: calls initialize()
    //
    // RETURN:  None.
    //--------------------------------------------------------------------------------
    public void start(int retry, int count)
    {
	retryInterval	= retry;
	retryCount	= count;
	initialize();        
	super.start();
    }

    public void start()
    {
	retryInterval	= 0;
	retryCount	= 0;
	initialize();        
	super.start();
    }
    

    //---------------------------------------------------------------------
    // Qbatch classes must implement dispatch()...
    // dispatch() is called by put() to place a batch into the batch ...
    //
    // NOTE:
    // ------
    // The implementation of the dispatch algorithm is Qbatch dependent 
    // and depends on the structure and nature of it's Queuing system and 
    // how to optimize channel efficiency. Thus, the choice of dispatch 
    // used can have dramatic effects on Service Node performance...
    //
    // Let Love and Wisdom guide implementation :-)
    // --------------------------------------------------------------------
    public abstract void dispatch(QMessage m);

    //---------------------------------------------------------------------
    // Qbatch classes must implement initialize()...
    // initialize() needs to create the list of Processing Queues, as well
    // as init. things as required by dispatch() see above...
    // --------------------------------------------------------------------
    public abstract void initialize();


    //-------------------------------------------------------
    // Public constructor....Constructs stuff :-)
    //-------------------------------------------------------
    public Qbatch(String name, Daemon d)
    {

	daemon = d;
	myName = name;
	batchCount=0;
	deferredCount=0;
    }
}
