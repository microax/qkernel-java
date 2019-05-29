package com.qkernel;
//
// QMessage.java        Holds Request/Reply stuff for ORB's
// ----------------------------------------------------------------------------
// History:
// --------
// 14/04/18 M. Gill     Add JSONObject support.
//
// 09/20/02 M. Gill	Force serialVersionUID so that different versions of 
//			qkernel are compatible over the wire.
//
// 07/12/02 M. Gill	Add getBoolean(), and putBoolean()
//
// 04/18/02 M. Gill	Modify getString() to convert any object that is not
//			a String[] to a String via Object.toString().
//
// 04/12/02 M. Gill	Modify getString() to return first parameter in 
//			a String[]. Add getStringArray() and putStringArray()
//
// 03/26/02 M. Gill	Add String support in getInt, getFloat etc...
// 01/15/02 M. Gill	Initial creation.
// ----------------------------------------------------------------------------
//
import java.lang.*;
import java.io.*;
import java.util.*;
import com.qkernel.json.*;
//

@SuppressWarnings("unchecked")

public class QMessage extends Hashtable implements Serializable, SerialVersionId
{
    //----------------------------------------------------------
    // Force serialVersionUID so that different versions 
    // of qkernel are compatible...
    //---------------------------------------------------------- 
    static final long serialVersionUID = SERIAL_VERSION_UID;

    //--------------------------------------------------------------------------------
    // METHOD   getParameter()
    //
    // PURPOSE: get String parameter via key
    //
    // INPUT:   Object - key
    // RETURN:  String - parameter or ""
    //--------------------------------------------------------------------------------
    public String getParameter(Object key)
    {
        return(getString(key));
    }


    //--------------------------------------------------------------------------------
    // METHOD   getString()
    //
    // PURPOSE: get String parameter via key
    //
    // INPUT:   Object - key
    // RETURN:  String - parameter or ""
    //--------------------------------------------------------------------------------
    public String getString(Object key)
    {
	String r  =null;
	Object o  =null;
	String[]a =null;

	o = super.get(key);

	if(o == null)
	    r = "";
 	else if(o instanceof String[])
	{
	    a = (String[])o;
	    r = a[0];
	}
	else if(o instanceof String)
	{
	    r = (String)o;
	}
	else if(o instanceof Integer)
	{
	    Integer x = (Integer)o;
	    r = x.toString();
	}
	else if(o instanceof Float)
	{
	    Float x = (Float)o;
	    r = x.toString();
	}
	else if(o instanceof Double)
	{
	    Double x = (Double)o;
	    r = x.toString();
	}
	else if(o instanceof JSONObject)
	{
	    JSONObject x = (JSONObject)o;
	    r = x.toString(4);
	}
	else
	{
	    r="Not an immutable object";
	}

        return(r);
    }



    //--------------------------------------------------------------------------------
    // METHOD   getStringArray()
    //
    // PURPOSE: get String[] parameters via key
    //
    // INPUT:   Object - key
    // RETURN:  String[] - parameter or null
    //--------------------------------------------------------------------------------
    public String[] getStringArray(Object key)
    {
	String[] strAry = null;

	try 
	{
	    strAry = (String [])super.get(key);
	} 
	catch( Exception e) 
	{
	    strAry = new String [] { getString(key) };
	}

	return(strAry);
    }

    //--------------------------------------------------------------------------------
    // METHOD   getInt()
    //
    // PURPOSE: get int parameter via key
    //
    // INPUT:   Object - key
    // RETURN:  int 
    //--------------------------------------------------------------------------------
    public int getInt(Object key)
    {
	Integer i = null;
	Object o  = null;

	o = super.get(key);

  	try
	{
	if(o != null)
	{
 	    if(o instanceof String)
	    {
		i = Integer.valueOf((String)o);
	    }
	    else
	    {
		i = (Integer)o;
	    }
	}
	else
	{
	    i = Integer.valueOf(0);
	}
	}
	catch(Exception e)
	{
	    i = Integer.valueOf(0);
	}
        return(i.intValue());
    }

    //--------------------------------------------------------------------------------
    // METHOD   getLong()
    //
    // PURPOSE: get long parameter via key
    //
    // INPUT:   Object - key
    // RETURN:  long
    //--------------------------------------------------------------------------------
    public long getLong(Object key)
    {
	Long i = null;
	Object o  = null;

	o = super.get(key);

  	try
	{
	if(o != null)
	{
 	    if(o instanceof String)
	    {
		i = Long.valueOf((String)o);
	    }
	    else
	    {
		i = (Long)o;
	    }
	}
	else
	{
	    i = Long.valueOf(0);
	}
	}
	catch(Exception e)
	{
	    i = Long.valueOf(0);
	}
        return(i.longValue());
    }

    //--------------------------------------------------------------------------------
    // METHOD   getFloat()
    //
    // PURPOSE: get float parameter via key
    //
    // INPUT:   Object - key
    // RETURN:  float
    //--------------------------------------------------------------------------------
    public float getFloat(Object key)
    {
	Float i = null;
	Object o  = null;

	o = super.get(key);

  	try
	{
	if(o != null)
	{
 	    if(o instanceof String)
	    {
		i = Float.valueOf((String)o);
	    }
	    else
	    {
		i = (Float)o;
	    }
	}
	else
	{
	    i = Float.valueOf(0);
	}
	}
	catch(Exception e)
	{
	    i = Float.valueOf(0);
	}
        return(i.floatValue());
    }

    //--------------------------------------------------------------------------------
    // METHOD   getDouble()
    //
    // PURPOSE: get double parameter via key
    //
    // INPUT:   Object - key
    // RETURN:  double
    //--------------------------------------------------------------------------------
    public double getDouble(Object key)
    {
	Double i = null;
	Object o  = null;

	o = super.get(key);

  	try
	{
	if(o != null)
	{
 	    if(o instanceof String)
	    {
		i = Double.valueOf((String)o);
	    }
	    else
	    {
		i = (Double)o;
	    }
	}
	else
	{
	    i = Double.valueOf(0);
	}
	}
	catch(Exception e)
	{
	    i = Double.valueOf(0);
	}
        return(i.doubleValue());
    }

    //--------------------------------------------------------------------------------
    // METHOD   getBoolean()
    //
    // PURPOSE: get boolean parameter via key
    //
    // INPUT:   Object - key
    // RETURN:  boolean
    //--------------------------------------------------------------------------------
    public boolean getBoolean(Object key)
    {
	Boolean i = null;
	Object o  = null;

	o = super.get(key);

  	try
	{
	if(o != null)
	{
 	    if(o instanceof String)
	    {
		i = Boolean.valueOf((String)o);
	    }
	    else
	    {
		i = (Boolean)o;
	    }
	}
	else
	{
	    i = Boolean.valueOf(false);
	}
	}
	catch(Exception e)
	{
	    i = Boolean.valueOf(false);
	}
        return(i.booleanValue());
    }


    //--------------------------------------------------------------------------------
    // METHOD   getJSONObject()
    //
    // PURPOSE: get JSONObject via key
    //
    // INPUT:   Object - key
    // RETURN:  JSONObject
    //--------------------------------------------------------------------------------
    public JSONObject getJSONObject(Object key)
    {
	JSONObject o  = null;

	o = (JSONObject)super.get(key);

        return(o);
    }


    //--------------------------------------------------------------------------------
    // METHOD   putInt()
    //
    // PURPOSE: put int parameter via key
    //
    // INPUT:   Object - key
    // 		int  - value
    // RETURN:  None
    //--------------------------------------------------------------------------------
    public void putInt(Object key, int value)
    {
	super.put(key,  Integer.valueOf(value));
    }


    //--------------------------------------------------------------------------------
    // METHOD   putLong()
    //
    // PURPOSE: put long parameter via key
    //
    // INPUT:   Object - key
    // 		long  - value
    // RETURN:  None
    //--------------------------------------------------------------------------------
    public void putLong(Object key, long value)
    {
	super.put(key,  Long.valueOf(value));
    }

    //--------------------------------------------------------------------------------
    // METHOD   putFloat()
    //
    // PURPOSE: put float parameter via key
    //
    // INPUT:   Object - key
    // 		float  - value
    // RETURN:  None
    //--------------------------------------------------------------------------------
    public void putFloat(Object key, float value)
    {
	super.put(key,  Float.valueOf(value));
    }


    //--------------------------------------------------------------------------------
    // METHOD   putDouble()
    //
    // PURPOSE: put double parameter via key
    //
    // INPUT:   Object - key
    // 		double  - value
    // RETURN:  None
    //--------------------------------------------------------------------------------
    public void putDouble(Object key, double value)
    {
	super.put(key,  Double.valueOf(value));
    }

    //--------------------------------------------------------------------------------
    // METHOD   putBoolean()
    //
    // PURPOSE: put boolean parameter via key
    //
    // INPUT:   Object   - key
    // 		boolean  - value
    // RETURN:  None
    //--------------------------------------------------------------------------------
    public void putBoolean(Object key, boolean value)
    {
	super.put(key,  Boolean.valueOf(value));
    }

    //--------------------------------------------------------------------------------
    // METHOD   putString()
    //
    // PURPOSE: put String parameter via key
    //
    // INPUT:   Object - key
    // 		String - value
    // RETURN:  None
    //--------------------------------------------------------------------------------
    public void putString(Object key, String value)
    {
	super.put(key, value);
    }

    //--------------------------------------------------------------------------------
    // METHOD   putStringArray()
    //
    // PURPOSE: put String[] parameter via key
    //
    // INPUT:   Object - key
    // 		String[]  - value
    // RETURN:  None
    //--------------------------------------------------------------------------------
    public void putStringArray(Object key, String[] value)
    {
	super.put(key,  value);
    }


    //--------------------------------------------------------------------------------
    // METHOD   putJSONObject()
    //
    // PURPOSE: put JSONObject parameter via key
    //
    // INPUT:   Object      - key
    // 		JSONObject  - value
    // RETURN:  None
    //--------------------------------------------------------------------------------
    public void putJSONObject(Object key, JSONObject value)
    {
	super.put(key, value);
    }
    
}
