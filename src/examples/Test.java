package com.qkernel.examples;
//
import java.lang.*;
import java.util.*;
import java.io.*;
import com.qkernel.*;
import com.qkernel.json.*;
import com.qkernel.examples.model.*;

@SuppressWarnings("unchecked")
//
public class Test extends BusinessObject 
{
    private  MqmConfig    config     = (MqmConfig)daemon.lookup("Configuration Object");
    private  MqmContainer container  = (MqmContainer)daemon.lookup("EntityContainer Object");
    private  eo_account   account    = container.account;
    
    public JSONObject testJson(com.qkernel.http.HTTPServer.Request req)
    {
	daemon.eventLog.sendMessage(req.getPath()+" called...");
	//---------------------------------------------------
	// We're gonna get the account info for main account
	// normaly ya wouldn't hardcode the primary key but
	// this is a simple demo :-)
	//---------------------------------------------------
	vo_account acct = account.find(1);

	//--------------------------------------------------
	// We're gonna return a JSON reply so we create
	// a JSON Object...
	//--------------------------------------------------
	JSONObject reply = new JSONObject();
	reply.put("status" ,     "OK");
	reply.put("message",     "using DSN: "+config.getConnStr());
	reply.put("Company Name",acct.getString("accountCompany")); 
	reply.put("Address",     acct.getString("accountAddress")); 
	reply.put("City",        acct.getString("accountCity")); 
	reply.put("State",       acct.getString("accountState")); 
	reply.put("Country",     acct.getString("accountCountry")); 
	reply.put("Phone",       acct.getString("accountPhone")); 
	reply.put("Contact",     acct.getString("accountContactName")); 
	return(reply);
    }

    public JSONObject ping(com.qkernel.http.HTTPServer.Request req)
    {
	daemon.eventLog.sendMessage(req.getPath()+" called...");
	JSONObject reply = new JSONObject();
	reply.put("status" ,     "OK");
	reply.put("message",     "pong");
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

