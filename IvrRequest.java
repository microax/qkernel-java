package com.qkernel;
//
// IvrRequest.java        Ivr request variables
// ----------------------------------------------------------------------------
// History:
// --------
// 08/01/01 M. Gill	Initial creation.
// ----------------------------------------------------------------------------
//
import java.lang.*;
import java.io.*;
import java.util.*;
//
public class IvrRequest extends Hashtable implements Serializable, SerialVersionId 
{
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
	String r;

	r = (String)super.get(key);

	if(r == null)
	    r = "";

        return(r);
    }
}























