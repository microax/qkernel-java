package com.qkernel;
/**
 * Default Rest object is instantiated when REST URI
 * is not mapped to a valid REST BusinessObject
 *
 * History:
 * --------
 * 06/12/19 mgill  Initial Creation. 
 *
 */
import java.lang.*;
import java.util.*;
import java.io.*;
import com.qkernel.json.*;

@SuppressWarnings("unchecked")
/**
 * {@code DefaultRestObject}  
 * <p>
 * Default Rest object is instantiated when REST URI
 * is not mapped to a valid REST BusinessObject 
 *
 */
public class DefaultRestObject extends BusinessObject 
{

   /**
    * defaultRestMethod 
    *
    * @param  HTTPServer.Request containing URI/path info
    * @return JSONObect with status and message.
    */
    public JSONObject defaultRestMethod(com.qkernel.http.HTTPServer.Request req)
    {
	//-----------------------------------------
	// let's get the path of bogus request...
	//-----------------------------------------
	String path = req.getPath();
        log("***ERROR*** no route for:"+path);
	
	//--------------------------------------------------
	// We're gonna return a JSON reply so we create
	// a JSON Object...
	//--------------------------------------------------
	JSONObject reply = new JSONObject();
	reply.put("status" ,     "FAIL");
	reply.put("message",     "REST Object: "+path+" not found");
	return(reply);
    }


   /**
    * DefaultRestObject constructor 
    *
    * @param  reference to Daemon object
    *
    */
    public DefaultRestObject(Daemon d)
    { 
	super(d);
    }
}
