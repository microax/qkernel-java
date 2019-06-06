package examples;

import java.lang.*;
import java.io.*;
import java.util.*;
import com.qkernel.QMessage;
import com.qkernel.RestProxy;
import com.qkernel.json.*;

@SuppressWarnings("unchecked")

//------------------------------------------------------
// A simple Rest tester
//------------------------------------------------------
public class RestTest
{
    public static void main(String argvs[])
    {
	//------------------------------------
	// set up username,passcode in 
	// authentecation yadda yadda
	// put into our parameter hash
	// and instantiate a Rest() object...
	//------------------------------------
	String     url  ="http://localhost:9000";
	String     auth ="kisa:IAmKisa";
	String     resp ="";
        JSONObject json = null;

	RestProxy rest   = new RestProxy(url, auth);
	QMessage  params = new QMessage();
	params.putString("QOBJECTclass", "examples.Test");
	params.putString("QOBJECTmethod","testJson");
	params.putString("argvs","{test:\"test\"}");

        try
	{
	    //---------------------------------------------
	    // Here we call the REST function for test
	    //---------------------------------------------
	    resp = rest.post("/test/", params);
            json = new JSONObject(resp);
	    
	}
	catch(Exception e)
	{
	    System.out.println("ERROR: "+e.getMessage() );
	}
	
        System.out.println("status = "+json.getString("status"));
	System.out.println("message= "+json.getString("message"));
    }
}
