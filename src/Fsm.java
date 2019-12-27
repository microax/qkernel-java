package com.qkernel;
//
// Fsm.java     Finite State Machine Class
// ----------------------------------------------------------------------------
// History:
// --------
// 09/18/03 M. Gill	Add convience methods and stuff ...
// 09/20/97 M. Gill 	Initial creation.
// ----------------------------------------------------------------------------
//
import java.lang.reflect.*;
import java.lang.*;

@SuppressWarnings("unchecked")

public abstract class Fsm extends Eos
{
    public int              current_state;
    public int              current_state_save;
    public int              max_events;
    public int              max_states;
    public EventMessage     LastEvent;
    protected Class            ThisClass; 
    protected Method           method;
    protected  StateTableEntry  StateArray[][]; 

    protected final int i_max_states =25;
    protected final int i_max_events =50;

    //--------------------------------------------------------------------------------
    // METHOD EventHandler
    //
    // PURPOSE:     This is the execution body of the FSM task.
    //              A FSM (Finite State Machine) task may be associated with any 
    //              application context (or initialized StateArray) modified via SetEvent()
    //
    //              EventHandler is invoked by the Eos passing an EventMessage. 
    //              The 'Data' member of an EventMessage can be any object type and is 
    //              always cast within the context of the action methods invoked from 
    //              a FSM. 
    //
    // INPUT:       e = EventMessage
    //
    // PROCESS:     1) Translate event message to event/data.
    //              2) Select action method associated with a translated event.
    //              3) Invoke method by name, using reflection (SetEvent() maps these). 
    //
    // RETURN:      None.
    //--------------------------------------------------------------------------------
    public void EventHandler(EventMessage e)
    {
        int event;
        int l_current_state;
        Method action;

        LastEvent       = e;
        event           = e.Event;
        l_current_state = current_state;
        current_state   = StateArray[l_current_state][event].next_state;
        action          = StateArray[l_current_state][event].action;

        try
	{
	    action.invoke(this);    // Invoke This! :-)
        }
        catch( Exception ept)
        { 
            // This can only happen if the action method throws one...(so, Byte me!)
            //daemon.event_log.SendMessage("Action method could not be invoked because: " 
	    //				  + ept.getMessage());
	    daemon.eventLog.sendMessage(e);
        }
    }




    //--------------------------------------------------------------------------------
    // METHOD InitStateEvent
    //
    // PURPOSE:     Create and initialize StateArray[state][event] 
    //
    // INPUT:       None.
    //
    // PROCESS:     1) Create new StateArray sized by max_states and max_events
    //              2) Initialize next_state = current_state. method = "InvalidEvent"
    //
    // RETURN:      None.
    //--------------------------------------------------------------------------------
    protected void InitStateEvent()
    {
        int i,j;

        StateArray = new StateTableEntry[max_states][max_events];

	try
	{
            method = ThisClass.getMethod("InvalidEvent", (Class)null);
        }
        catch( Exception e){}

        //--------------------------------
        // Initialize default entry.
        //--------------------------------
	for( i=0; i < max_states; i++ )
	{
            for(j=0; j < max_events; j++ )
	    {
                StateArray[i][j] = new StateTableEntry();
	        StateArray[i][j].action     = method;
		StateArray[i][j].next_state = i;
	    }
	}
    }


    //--------------------------------------------------------------------------------
    // METHOD InvalidEvent
    //
    // PURPOSE:     Default action method. Override this to do something useful, 
    //              such as reporting the invalid state/event transition to a logger. 
    //
    // INPUT:       None.
    //
    // PROCESS:     None.
    //
    // RETURN:      None.
    //--------------------------------------------------------------------------------
    public void InvalidEvent()
    {
        return;
    }  

    //--------------------------------------------------------------------------------
    // METHOD DoNothing
    //
    // PURPOSE:     Default action method. (it does nothing) 
    //
    // INPUT:       None.
    //
    // PROCESS:     None.
    //
    // RETURN:      None.
    //--------------------------------------------------------------------------------
    public void DoNothing()
    {
        return;
    }  


    //--------------------------------------------------------------------------------
    // METHOD SetMaxEvents
    //
    // PURPOSE:     Called from start() to set the max number of events within 
    //              a state array. The default , i_max_events should be OK for most 
    //              applications, but you can implement this function to set a new 
    //              value
    //
    // INPUT:       None.
    //
    // PROCESS:     None.
    //
    // RETURN:      None.
    //--------------------------------------------------------------------------------
    public void SetMaxEvents()
    {
        max_events =i_max_events;
    }  


    //--------------------------------------------------------------------------------
    // METHOD getLastData
    //
    //--------------------------------------------------------------------------------
    public Object getLastData()
    {
	return(LastEvent.Data);
    }  

    //--------------------------------------------------------------------------------
    // METHOD getLastEvent
    //
    //--------------------------------------------------------------------------------
    public int getLastEvent()
    {
	return(LastEvent.Event);
    }  

    //--------------------------------------------------------------------------------
    // METHOD getCurrentState
    //
    //--------------------------------------------------------------------------------
    public int getCurrentState()
    {
	return(current_state);
    }  


    //--------------------------------------------------------------------------------
    // METHOD SetMaxStates
    //
    // PURPOSE:     Called from start() to set the max number of states within 
    //              a state array. The default , i_max_states should be OK for most 
    //              applications, but you can implement this function to set a new 
    //              value
    //
    // INPUT:       None.
    //
    // PROCESS:     None.
    //
    // RETURN:      None.
    //--------------------------------------------------------------------------------
    public void SetMaxStates()
    {
        max_states =i_max_states;
    }  


    //--------------------------------------------------------------------------------
    // METHOD SetEvent
    //
    // PURPOSE:     This is called (usually from SetStateTable() ) to define 
    //              a State/Event Transition within the state array. 
    //              The java.lang.reflect package (JDK 1.1 or greater) supports
    //              classes that allow methods to be invoked by name. 
    //              ...this is called "Reflection". SetEvent() gets action methods 
    //              as strings,  then uses getMethod() to translate to a Method class.
    //
    // INPUT:       1) Current State
    //              2) Event
    //              3) Next State
    //              4) Action method.
    //
    // PROCESS:     1) Use ThisClass.getMethod() to set a method reference. 
    //              2) Set Next State and method in State array indexed by
    //                 Current State and Event. ...NOTE: action methods must be public 
    //
    // RETURN:      None.
    //--------------------------------------------------------------------------------
    public void SetEvent(int cs, int evt, int ns, String action)
    {
        try
	{
            method = ThisClass.getMethod(action);
        }
        catch( Exception e)
        {
            daemon.event_log.SendMessage("Action method \""+ action +"\" not found");
        }
	StateArray[cs][evt].next_state = ns;
	StateArray[cs][evt].action     = method;
    }

    //---------------------------------------------------------------------------------
    // METHOD setEvent()
    //
    // PURPOSE:	calls SetEvent() above
    // 
    //--------------------------------------------------------------------------------
    public void setEvent(int cs, int evt, int ns, String action)
    {
	SetEvent(cs, evt, ns, action);
    }
 
    //--------------------------------------------------------------------------------
    // METHOD SetStateTable
    //
    // PURPOSE:     Implement this method and Use SetEvent() to create 
    //              an application context within the StateArray  
    //
    // INPUT:       None.
    //
    // PROCESS:     Following is the recommended form for SetStateTable():
    //
    // public void SetStateTable()
    // {
    // //--------------------------------------------------------------------------------
    // //        CURRENT_ST      COMMAND/EVENT     NEXT_ST         ACTION
    // //        ----------      -------------     ----------      ------
    // SetEvent( STATE_0,        EVENT_0,          STATE_1,        "ActionOne"     );
    // SetEvent( STATE_0,        EVENT_1,          STATE_1,        "ActionOne"     );
    //
    // SetEvent( STATE_1,        EVENT_2,          STATE_1,        "ActionTwo"     );
    // SetEvent( STATE_1,        EVENT_0,          STATE_2,        "ActionThree"   );
    //
    // SetEvent( STATE_2,        EVENT_0,          STATE_2,        "ActionFour"    );
    // SetEvent( STATE_2,        EVENT_1,          STATE_0,        "ActionFour"    );
    // }
    //
    //--------------------------------------------------------------------------------
    public abstract void SetStateTable();  




    //--------------------------------------------------------------------------------
    // METHOD start
    //
    // PURPOSE:     Implementation of thread->Mos->Eos start(method)
    //
    // INPUT:       None.
    //
    // PROCESS:     See below
    //
    // RETURN:      None.
    //--------------------------------------------------------------------------------
    public void start()
    {
        super.start();                     // You never know what Eos is doing these days...
        ThisClass = this.getClass();       // Get this class identifier
        SetMaxEvents();                    // Implementors can override this...
        SetMaxStates();                    // Implementors can override this too 
	InitStateEvent();                  // Init state array...
	//---------------------------------------------------------
        // You MUST implement this!..it's the application context!
        //---------------------------------------------------------
        SetStateTable();
        //--------------------------------------
        // Send Init_OK event as first event... 
        // (we always define 0 as INIT_OK)
        // -------------------------------------
        EventMessage e = new EventMessage();
        e.Event =0;                        
        SendMessage(e);
        return;                           // Have a nice day :-)
    }

    public Fsm(String n, Daemon d)
    {
        super(n, d);
    }
}
