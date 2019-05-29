package com.qkernel;
//
// TimerService.java     Timer Service for Async Thread timers.
// ----------------------------------------------------------------------------
// History:
// --------
//
// 04/06/00 M. Gill 	Initial creation.
// ----------------------------------------------------------------------------
//
import java.lang.*;
import java.util.*;

@SuppressWarnings("unchecked")
//-----------------------------------------------------------------------------
//
// ----------------------------------------------------------------------------
//
public final class TimerService extends Thread
{
    public    Daemon daemon;
    protected Vector timer_queue;
 

    //--------------------------------------------------------------------------------
    // METHOD 	run()
    //
    // PURPOSE:	This the main execution loop for TimerService.      
    //
    // INPUT:	None.
    //
    // PROCESS:
    //
    // RETURN:  Never.
    //--------------------------------------------------------------------------------
    public void run()
    {
	Enumeration enm;
    	Timer t;

	setName("Timer Service:");

	daemon.event_log.SendMessage("Ready...");

        for( ;; )
        {  
	    try
	    {
            	sleep(1000);

		if(!timer_queue.isEmpty())
   		{
		    enm = timer_queue.elements();

   		    while (enm.hasMoreElements())
		    {
			t = (Timer)enm.nextElement();

			if(t.stop_flag == 1)
			{
			    timer_queue.removeElement(t);
			}
			else
			{
			    if(--t.ticks == 0)
			    {
				t.callback.invoke(t.my_object);
				timer_queue.removeElement(t);
			    }
			}
		    }
		}
	    }
	    catch(Exception e)
	    {
		daemon.event_log.SendMessage("run block failed becasue: " + 
					     e.getMessage());
	    }   
        }   
    }
    

    public void StartTimer(Timer t)
    {
	timer_queue.addElement(t);
    }


    public void start()
    {
        super.start();
        timer_queue = new Vector();

    }


    //--------------------------------------------------------------------------------
    // METHOD TimerService()
    //
    // PURPOSE:	timerService constructor.   
    //
    // INPUT:	Referance to Daemon object.
    //
    // PROCESS:	Construct stuff :-)
    //
    // RETURN:	None.
    //--------------------------------------------------------------------------------
    public TimerService(Daemon d)
    {

        daemon =d;
    }
}






