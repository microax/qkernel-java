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
import com.qkernel.json.*;

@SuppressWarnings( "unchecked" )
//
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
	if(m.getBoolean("CHN-RETRY-OVERRIDE"))
	{
            retryInterval=0;
	    retryCount   =0;
	}
        m.putInt("CHN-RETRY-MINUTES", retryInterval);
        m.putInt("CHN-RETRY-COUNTER", retryCount);

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
        int    batchCounter= m.getInt("CHN-RETRY-COUNTER");
	String batchId     = m.getString("CHN-BATCH-ID");
	String     log     = "No status was returned";
	int        status  = 0;
	String batchCompletionTime = m.getString("CHN-BATCH-RUNNING-TIME");
        try{
	    JSONObject param = m.getJSONObject("batchParams");
	    status= param.getInt("status");
	    log   = param.getString("message");
	}
	catch(Exception e){}
	//----------------------------------------------
	// The total batch count is decremented...
	//----------------------------------------------
	batchCount--;

	if(status !=0 && batchCounter !=0)
	{
	    //----------------------------------------------------------------
	    // The batch failed. So, we decrement the re-try counter and place 
	    // it in the deferred queue. The deferred queue wakes up once per 
	    // minute to re-submit batches. ...It's a good idea to make the 
	    // inverval greater than 1 since the granuality is +- 1 minute.
	    //----------------------------------------------------------------
	    daemon.log(batchId+" status is "+status+" -- will Re-try up to "+batchCounter+" times every "+retryInterval+" minutes");	
	    m.put("CHN-RETRY-COUNTER", --batchCounter);
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
	    daemon.log("Batch ID:"+batchId+" is done with status "+status+" in "+batchCompletionTime+"ms");	
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
	deferred 	= new com.qkernel.Queue();
	reQueue		= new com.qkernel.Queue();
        setName(myName);

	for(;;)
	{
	    try
            {
		//---------------------------
                // Wakeup once per minute
                //---------------------------
                sleep(1000 *60);

          	while ((n = (QMessage)deferred.Dequeue()) != null )
            	{
		    //------------------------------------
		    // Get batch from list, and decrement 
		    // its retry timer...
		    //------------------------------------
		    int t = n.getInt("CHN-RETRY-MINUTES");	
               	    if(--t == 0)
                    {
			//--------------------------------
			// re-insert batch into channel
			//--------------------------------
                        n.putInt("CHN-RETRY-MINUTES", retryInterval);
			batchCount++;
			dispatch(n);
        	    }
		    else
		    {
			//-------------------------------
			// We're gonna re-insert this 
			// back into the deferred Queue
			//-------------------------------
                        n.put("CHN-RETRY-MINUTES", t);
			deferred.Enqueue(n);
			break;
		    }
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
	if(retry == 0 || count == 0)
	{
	retryInterval= 0;
	retryCount   = 0;
	}
	else
	{
	retryInterval= retry;
	retryCount   = count;
	}
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
