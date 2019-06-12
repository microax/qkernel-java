package com.qkernel.eobuilder;
//
// EOBuilderConfig.java        EOBuilder Configuration Object
// ----------------------------------------------------------------------------
// History:
// --------
// 06/02/19 M. Gill     Initial creation.
// ----------------------------------------------------------------------------
//
import java.lang.*;
import java.util.*;
import java.io.*;
import com.qkernel.*;

@SuppressWarnings("unchecked")

public final class EOBuilderConfig extends Config
{
    //--------------------------------------------------------------------------------
    // METHOD   putROMConfig()
    //
    // PURPOSE: Create default ROM daemon configuration.
    // INPUT:   None.
    // RETURN:  None.
    //--------------------------------------------------------------------------------

    public void putROMConfig()
    {
	super.putROMConfig();
    }
    
    //--------------------------------------------------------------------------------
    // METHOD   MqmConfig()
    //
    // PURPOSE: Constructor ...Reads params from properties file...loads
    //		default ROM configuration for properties that may not be defined.
    //
    // INPUT:   1) Commandline argvs
    //
    // RETURN:  N/A
    //--------------------------------------------------------------------------------
    public EOBuilderConfig(String[] argvs )
    {
	super(argvs);
    }
}
