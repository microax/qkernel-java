package com.qkernel;
//
// CloneableObject.java		CloneableObject
// ----------------------------------------------------------------------------
// History:
// --------
// 01/10/01 M. Gill	Initial creation.
// ----------------------------------------------------------------------------
//
import java.lang.*;
import java.io.*;

public class CloneableObject extends Object implements Cloneable, Serializable, SerialVersionId
{
    static final long serialVersionUID = SERIAL_VERSION_UID;

    //--------------------------------------------------------------------------------
    // METHOD   clone()
    //
    // PURPOSE: Produce a byte-for-byte copy of Object. 
    //
    // INPUT:   None.
    // RETURN:  None.
    //--------------------------------------------------------------------------------
    public Object clone()
    {
	Object o = null;

	try
	{
	    o=super.clone();
	}
	catch(Exception e) {}
	return(o);
    }

    public CloneableObject()
    {

    }
}
