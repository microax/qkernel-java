package examples;
//
import java.lang.*;
import java.util.*;
import java.io.*;
import com.qkernel.*;
import com.qkernel.json.*;

@SuppressWarnings("unchecked")

//
public class Test extends BusinessObject 
{

    public JSONObject testJson(JSONObject json)
    {
	daemon.eventLog.sendMessage("examples.Test.testJson() called:"+json.toString());

	JSONObject reply = new JSONObject();
	reply.put("status" , "OK");
	reply.put("message", "testJson() good");
	return(reply);
    }


    //--------------------------------------------------------------------------------
    // METHOD 	Test()
    //
    // PURPOSE:	Constructor.
    // INPUT:	Daemon
    //--------------------------------------------------------------------------------
    public Test(Daemon d)
    { 
	super(d);
    }
}

