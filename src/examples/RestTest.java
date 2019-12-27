package com.qkernel.examples;

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

        try
	{
	    //---------------------------------------------
	    // Here we call the REST function for test
	    //---------------------------------------------
	    resp = rest.post("/api/test", params);
            json = new JSONObject(resp);
	    
	}
	catch(Exception e)
	{
	    System.out.println("ERROR: "+e.getMessage() );
	}
	
        System.out.println("status = "    +json.getString("status"));
	System.out.println("message= "    +json.getString("message"));
	System.out.println("Company Name="+json.getString("Company Name"));
	System.out.println("Address= "    +json.getString("Address"));
	System.out.println("City= "       +json.getString("City"));
	System.out.println("State= "      +json.getString("State"));
	System.out.println("Country= "    +json.getString("Country"));
	System.out.println("Phone= "      +json.getString("Phone"));
	System.out.println("Contact= "    +json.getString("Contact"));
    }
}
