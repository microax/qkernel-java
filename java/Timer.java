package com.qkernel;
//
// Timer.java     Timer class 
// ----------------------------------------------------------------------------
// History:
// --------
// 01/13/02 M. Gill	Add start(), and stop() to do same as Start()/Stop()
// 04/06/00 M. Gill 	Initial creation.
// ----------------------------------------------------------------------------
//
import java.lang.*;
import java.lang.reflect.*;
import java.util.*;
@SuppressWarnings("unchecked")
//
//-----------------------------------------------------------------------------
// The Timer class is passed to the TimerService to handle asynchronous timers 
// in thread type objects such as Mos, Eos and Fsm. Actually, the only 
// requirement for a class to use Timer ( and the timer service) is that it 
// knows daemon (all Mos type classes know daemon). Daemon creates the 
// TimerService.
//
// Timer holds the "tick"  counter (most likely a one second quantum) and a 
// Method object containing the callback method of the calling object.
//
// The timer service is a thread that wakes up every quantum, and tranverses
// a vector containing Timers (this object)...It decrements the 'tick count'
// and invokes the callback method (and removes from vector), when tick 
// reaches zero.
//
// Public Timer methods include start, stop and the constructor , which is passed 
// the Daemon object and the reference of the calling object....
// In the future , maybe we'll add methods to modify the public attributes 
// instead of allowing  TimerService to do that...maybe not :-)
// ----------------------------------------------------------------------------
//
public final class Timer extends Object
{
    //--------------------------
    // Public attributes
    //--------------------------
    public int    ticks;
    public Method callback;
    public int    stop_flag;

    //------ Other Stuff -------
    protected Object my_object;
    protected Daemon daemon;
    protected Class obj_class;

    //--------------------------------------------------------------------------------
    // METHOD Stop()
    //
    // PURPOSE:	Stops a timer.
    //
    // INPUT:	None.
    //
    // PROCESS:	Set the flag that TimerService checks...
    //		TimerService will remove "this" node when it sees "that" set  :-)
    //
    // RETURN:	None.
    //--------------------------------------------------------------------------------
    public void Stop()
    {
	stop_flag =1;	// Set dat thang...
    } 

    //--------------------------------------------------------------------------------
    // METHOD Stop()
    //
    // PURPOSE:	Stops a timer.
    //
    // INPUT:	None.
    //
    // PROCESS:	Set the flag that TimerService checks...
    //		TimerService will remove "this" node when it sees "that" set  :-)
    //
    // RETURN:	None.
    //--------------------------------------------------------------------------------
    public void stop()
    {
	stop_flag =1;	// Set dat thang...
    } 


    //--------------------------------------------------------------------------------
    // METHOD start()
    //
    // PURPOSE:	Start a timer.
    //
    // INPUT:	1) Number of one second ticks.
    //		2) Name of callback method.
    //
    // PROCESS:	Save tick count into public "tick"...get a method
    //		that matches the method name, then start itself 
    //      	with Daemon's timer service.
    //
    // RETURN:	None.
    //--------------------------------------------------------------------------------
    public void start(int i_ticks, String method_name)
    {
	Start(i_ticks, method_name);
    }


    //--------------------------------------------------------------------------------
    // METHOD Start()
    //
    // PURPOSE:	Start a timer.
    //
    // INPUT:	1) Number of one second ticks.
    //		2) Name of callback method.
    //
    // PROCESS:	Save tick count into public "tick"...get a method
    //		that matches the method name, then start itself 
    //      	with Daemon's timer service.
    //
    // RETURN:	None.
    //--------------------------------------------------------------------------------
    public void Start(int i_ticks, String method_name)
    {
	ticks    = i_ticks;
	stop_flag= 0;

	try
	{
	    callback = obj_class.getMethod(method_name);
	    daemon.timer_service.StartTimer(this); // Start this !!! :-)
	}
	catch(Exception e)
	{
	    daemon.event_log.SendMessage("Timer.start failed because: " +
					  e.getMessage());
	}
    }



    //--------------------------------------------------------------------------------
    // METHOD Timer()
    //
    // PURPOSE:	Timer constructor.   
    //
    // INPUT:	1) Reference to Daemon object.
    //		2) Reference to calling object.
    //
    // PROCESS:	Construct stuff :-)
    //
    // RETURN:	None.
    //--------------------------------------------------------------------------------
    public Timer(Daemon d, Object o)
    {
	my_object    = o;
        daemon       = d;
	obj_class    = o.getClass();
    }
}








