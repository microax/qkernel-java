package com.qkernel.batch;
//
// BatchQueue.java    Qbatch Processing Queue
// ----------------------------------------------------------------------------
// History:
// --------
// 11/11/19 M. Gill     Initial creation.
//                      (lifted from metaQ.io's MqmChannelQueue)
// ----------------------------------------------------------------------------
//
import java.lang.*;
import java.util.*;
import com.qkernel.*;
//
//----------------------------------------------------------------------------
// This is a batch-processing task for channels. 
// Batches (Qmessage's ) are sent from the Channel. 
// batchProcess() handles this.... sendMessage() invokes a callback to the 
// channel when batchProcess() returns. 
//----------------------------------------------------------------------------
public abstract class BatchQueue extends Mos
{
    public	Qbatch    channel;

    //--------------------------------------------------------------------------------
    // METHOD MessageHandler()
    //
    // PURPOSE: Send batch to batchProcess() , then invoke channel.callback()
    //
    // INPUT:   MessageNode
    //
    // RETURN:  None.
    //--------------------------------------------------------------------------------
    public void MessageHandler(MessageNode n)
    {
	QMessage m = (QMessage)n.object;

	batchProcess(m);
	channel.callback(m);
    }

    //--------------------------------------------------------------------------------
    // METHOD start()
    //
    // PURPOSE: Starts the Mos  thread
    //
    // INPUT:   none
    //
    // PROCESS: calls initialize()
    //
    // RETURN:  None.
    //--------------------------------------------------------------------------------
    public void start()
    {
        super.start();
    }

    //---------------------------------------------------------------------
    // BatchQueues must implement batchProcess()...
    // batchProcess() is called by MessageHandler() to process a batch
    //
    // Let Love and Wisdom guide implementation :-)
    // --------------------------------------------------------------------
    public abstract void batchProcess(QMessage m);


    //--------------------------------------------------------------------
    // Public constructor....Constructs stuff :-)
    //--------------------------------------------------------------------
    public BatchQueue(String name, Qbatch c, Daemon d)
    {
	super(name,d);
	channel= c;
	reportInit=false;
    }
}

