package com.qkernel.servlet;

// CookieGen.java 	Generate and retrieve unique cookies for servlets  
// ---------------------------------------------------------------------------
// History:
// --------
// 05/27/05 M. Gill	Modified to allow setting cookie parameters.
// 05/27/02 M. Gill	Moved to com.qkernel.servlet package.
// 06/29/00 M. Gill	Initial creation.
// ---------------------------------------------------------------------------
//
import java.lang.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
//
//----------------------------------------------------------------------------
// GenCookie has two public methods. GookieGen is constructed with a reference
// to the HttpServletRequest and HttpServletResponse objects.   
// getCookieValue()  returns the current value of a unique String for cookie 
// and places it into the browser. If a cookie exists is will return the value 
// for that cookie. 
//----------------------------------------------------------------------------
public final class CookieGen extends Object
{
    public HttpServletRequest  request;
    public HttpServletResponse response;

    private int timeToLive = -1;	// Init. as session cookie.

    //--------------------------------------------------------------------------------
    // METHOD 	getCookieValue()
    //
    // PURPOSE: Returns a String value for a cookie. 
    //          If no cookie is set for value then it creates one 
    //		and returns its value.
    //
    // INPUT:	String - Name of cookie cookie
    //		int    - time to live
    //
    // RETURN:  String - Cookie value
    //--------------------------------------------------------------------------------
    public String getCookieValue(String cname, int expires)
    {
	timeToLive = expires;

	return(getCookieValue(cname, true));
    }


    //--------------------------------------------------------------------------------
    // METHOD 	getCookieValue()
    //
    // PURPOSE: Returns a String value for a cookie. 
    //          If no cookie is set for value then it creates one 
    //		and returns its value.
    //
    // INPUT:	String - Name of cookie cookie
    //		int    - create=1 no create=0
    //
    // RETURN:  String - Cookie value
    //--------------------------------------------------------------------------------
    public String getCookieValue(String cname, boolean create_flag)
    {
	String cookieValue 	= new String("");
	Cookie[] cookieList 	= request.getCookies();
	String cookieName	= cname;

	if(cookieList != null)
	{
	    for(int i=0; i < cookieList.length; i++)
	    {
		String name = cookieList[i].getName();
		if(name.equals(cookieName))
		{
		    //-----------------------------------
		    // We found a cookie!...
		    // return this value.
		    //-----------------------------------
		    return(cookieList[i].getValue());
		}
   	    }
	    if(create_flag)
	    {
	    	//-----------------------------------------------------------------
	    	// 1) Create a new cookie string. 
	    	// 2) Add this cookie to the browser
	    	// 3) Return the value of this cookie
	    	//-----------------------------------------------------------------
	    	cookieValue = genCookie();
	    	Cookie c    = new Cookie(cookieName, cookieValue);
	    	c.setMaxAge(timeToLive);  // Set expires value
	    	response.addCookie(c);
	    	return(cookieValue);
	    }
	}
	else if(create_flag)
	{
	    //-----------------------------------------------------------------
	    // 1) Create a new cookie string. 
	    // 2) Add this cookie to the browser
	    // 3) Return the value of this cookie
	    //-----------------------------------------------------------------
	    cookieValue = genCookie();
	    Cookie c    = new Cookie(cookieName, cookieValue);
	    c.setMaxAge(timeToLive);  	// Set expires value
	    response.addCookie(c);
	    return(cookieValue);
	}

	return(cookieValue);
    }




    //--------------------------------------------------------------------------------
    // METHOD 	genCookie()
    //
    // PURPOSE: Creates and returns a String value for a cookie. 
    //
    // INPUT:   None.	
    //
    // RETURN:  String - Cookie value
    //--------------------------------------------------------------------------------
    public String genCookie()
    { 
      	Date now 	= new Date();
	long ticks  	= now.getTime();	// Get current time in milliseconds
        Random rand	= new Random();		// Create random seed (kinda random)
        long someNumber = rand.nextLong();	// get the next long random number
        Long rNumber;

	someNumber	= ticks + rand.nextLong(); // Make a little more random :-) 
 	rNumber 	= Long.valueOf(someNumber);// So we can make into a string
 
	return(rNumber.toString());		   // Return a (hopefully) random string
    }


    //--------------------------------------------------------------------------------
    // METHOD 	CookieGen()
    //
    // PURPOSE:	Public constructor for Object<--CookieGen
    //
    // INPUT:	1) HttpServletRequest
    //		2) HttpServletREsponse
    //--------------------------------------------------------------------------------
    public CookieGen(HttpServletRequest req, HttpServletResponse res)
    {
	request = req;
 	response= res;

    }
}









