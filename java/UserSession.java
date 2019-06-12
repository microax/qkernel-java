package com.qkernel;
//
// UserSession.java        Used for General User Session Variables
// ----------------------------------------------------------------------------
// History:
// --------
// 08/14/01 M. Gill	Initial creation.
// ----------------------------------------------------------------------------
//
import java.lang.*;
import java.io.*;
import java.util.*;
@SuppressWarnings("unchecked")
//
public final class UserSession extends Hashtable implements SerialVersionId
{
    static final long serialVersionUID = SERIAL_VERSION_UID;

    //--------------------------------------------------------------------------------
    // METHOD   get()
    //
    // PURPOSE: get parameter via key
    //
    // INPUT:   Object - userkey, Object - objectKey 
    // RETURN:  Object - parameter
    //--------------------------------------------------------------------------------
    public Object get(Object userKey, Object objectKey)
    {

	Hashtable ht = (Hashtable)super.get(userKey);
	
	if(ht != null)
	    return(ht.get(objectKey) );
	else
	    return(null);
    }

    //--------------------------------------------------------------------------------
    // METHOD   put()
    //
    // PURPOSE: put parameter via key
    //
    //--------------------------------------------------------------------------------
    public void put(Object userKey, Object objectKey, Object obj)
    {
	Hashtable ht = (Hashtable)super.get(userKey);
	
	if(ht != null)
	    ht.put(objectKey, obj);
	else
	{
	    ht = new Hashtable();
	    super.put(userKey, ht);
	    ht.put(objectKey, obj);
	}
    }



    //--------------------------------------------------------------------------------
    // METHOD   remove()
    //
    // PURPOSE: remove parameter via key
    //
    // INPUT:   Object - userkey, Object - objectKey 
    // RETURN:  Object
    //--------------------------------------------------------------------------------
    public Object remove2(Object userKey, Object objectKey)
    {

	Hashtable ht = (Hashtable)super.get(userKey);
	
	if(ht != null)
	    return((Object)ht.remove(objectKey) );
	else
	    return(null);
    }



    //--------------------------------------------------------------------------------
    // METHOD   drop()
    //
    // PURPOSE: Drop user Hashtable
    //
    // INPUT:   Object - userkey
    // RETURN:  None.
    //--------------------------------------------------------------------------------
    public void drop(Object userKey)
    {

	Hashtable ht = (Hashtable)super.get(userKey);
	
	if(ht != null)
	    super.remove(userKey);

    }
}























