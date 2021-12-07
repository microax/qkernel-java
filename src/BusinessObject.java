package com.qkernel;
/**
 * BusinessObject    Base class for API's and Batch Processes
 *
 * History:
 * --------
 * 12-21-20 M. Gill     Update for MetaQ Native.
 * 01-14-02 M. Gill     Initial Creation. 
 */
import java.lang.*;
import java.util.*;
import java.io.*;
import com.qkernel.*;
import com.qkernel.json.*;
import com.qkernel.http.HTTPServer.*;
/**
 * {@code BusinessObject} 
 * <p>
 * Protected Methods:
 * ------------------
 * statusOK()
 * statusFAIL()
 * statusTRUE()
 * statusFALSE()
 * getString()
 * getInt()
 * getJSONArray()
 * getBasicAuthValue()
 * getRawBody()
 * getBearerToken()
 * sleep()
 * timeDeltaToString()
 * timeDelta()
 */
public abstract class BusinessObject extends QObject
{
   /**
    * getString -- returns String from 
    *              JSONObject parameter string
    *              (returns "" if exception caught)
    *
    * @param  JSONObject json  
    * @param  string     key 
    * @return string     value
    */
    protected String getString(JSONObject json, String key)
    {
	String js = "";
        try
	{
	    js =json.getString(key);
	}
	catch(Exception e)
	{
	    js = "";
	}
	return(js);
    }

   /**
    * getInt -- returns int from 
    *           JSONObject parameter string
    *           (returns 0 if exception caught)
    *
    * @param  JSONObject json  
    * @param  string     key 
    * @return int        value
    */
    protected int getInt(JSONObject json, String key)
    {
	int js = 0;
        try
	{
	    js =json.getInt(key);
	}
	catch(Exception e)
	{
	    js = 0;
	}
	return(js);
    }

    /**
    * getJSONArray -- returns JSONArray from 
    *                 JSONObject parameter string
    *                 (returns null if exception caught)
    *
    * @param  JSONObject json  
    * @param  string     key 
    * @return JSONArray 
    */
    protected JSONArray getJSONArray(JSONObject json, String key)
    {
	JSONArray js = null;
        try
	{
	    js =json.getJSONArray(key);
	}
	catch(Exception e)
	{
	    js = null;
	}
	return(js);
    }

   /**
    * statusOK -- sets JSON status OK to true
    *             and log message  
    *
    * @param  JSONObject reply  
    * @param  string message
    */
    protected void statusTRUE(JSONObject json, String message)
    {
        log(message);
	json.put("json_status_ok",true);
	json.put("json_message"  ,message);    
    }

   /**
    * statusFAIL -- sets JSON status OK to false
    *               and log status message
    *
    * @param  JSONObject reply  
    * @param  string message
    */
    protected void statusFALSE(JSONObject json, String message)
    {
        log(message);
	json.put("json_status_ok", false);
	json.put("json_message"  , message);    
    }
    
   /**
    * statusOK -- sets Ok JSON status with message 
    *             and log 
    *
    * @param  JSONObject reply  
    * @param  string message
    */
    protected void statusOK(JSONObject json, String message)
    {
        log(message);
	json.put("json_status" , "OK");
	json.put("json_message", message);    
    }

   /**
    * statusFAIL -- sets FAIL JSON status with message 
    *               and log 
    *
    * @param  JSONObject reply  
    * @param  string message
    */
    protected void statusFAIL(JSONObject json, String message)
    {
        log(message);
	json.put("json_status" , "FAIL");
	json.put("json_message", message);    
    }


   /**
    * getHeaderValue  
    *
    * @param  Request req object  
    * @param  String  header key
    *
    * @return String  value from header
    */
    protected String getHeaderValue(Request req, String key)
    {
	String rValue ="";
	try
	{
	    rValue = req.getHeaders().get(key);
	}
	catch(Exception e)
	{
	    log("Can't find header value for: "+key);
	}
	
        return(rValue);
    }
    
   /**
    * getBasicAuthValue  
    *
    * @param  Request req  
    * @return String  value from basic auth header
    */
    protected String getBasicAuthValue(Request req)
    {
	String rValue ="";
	
	try
	{
	//--------------------------------------------------------------
	// We're supporting HTTP BASIC Authorzation.
	// Header must contain Base64 encoded values
	//--------------------------------------------------------------
	String head   = req.getHeaders().get("Authorization");
        String[] auth = head.split(" ");
        byte[] bytes  = Base64.getDecoder().decode(auth[1]);
        rValue        = new String(bytes);
	}
	catch(Exception e)
	{
	    log("***ERROR: Basic Authorization Failure");
	}

	return(rValue);
    }

   /**
    * getRawBody  
    *
    * @param  Request req  
    * @return String  Raw body from http stream
    */
    protected String getRawBody(Request req)
    {
	String rValue ="";
	
	try
	{
            InputStream in = req.getBody();
	    rValue = com.qkernel.http.HTTPServer.readToken(in, -1, "UTF-8", 2097152);
	}
	catch(Exception e)
	{
	    log("***ERROR: can't read http body");
	}
	return(rValue);
    }

   /**
    * getBearerToken
    *
    * @param  Request req  
    * @return String  value from bearer token header
    */
    protected String getBearerToken(Request req)
    {
	//--------------------------------------------------------------
	// We're supporting HTTP Bearer Token.
	//--------------------------------------------------------------
	String head   = req.getHeaders().get("Authorization");
        //String[] auth = head.split(" ");

	//return(auth[1]);
	return(head);
    }
    
   /**
    * sleep 
    * 
    * param int milliseconds 
    */
    protected void sleep(int ticks)
    {
	try {
            Thread.sleep(ticks);
        }
	catch(Exception e){log("sleep failed???");}
    }
    
   /**
    * timeDeltaToString 
    *
    * @param  long  -- initial time in milliseconds.
    * @return String-- current time minus t in milliseconds
    */
    protected  String timeDeltaToString(long t)
    {
	Date   now   = new Date();
	return((Long.valueOf(now.getTime()-t)).toString());
    }

   /**
    * timeDelta         
    *
    * @param  long -- initial time in milliseconds.
    * @return long -- current time minus t in milliseconds
    */
    protected long timeDelta(long t)
    {
	Date   now = new Date();
	return(now.getTime() - t);
    }
    
    
    public BusinessObject(Daemon d)
    {
      super(d);
    }
}

