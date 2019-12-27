package com.qkernel.examples;
//
// MqmConfig.java        Service Node Configuration Object
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

public final class MqmConfig extends Config
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
    // INPUT:   1) Daemon
    //		2) Commandline argvs
    //
    // RETURN:  N/A
    //--------------------------------------------------------------------------------
    public MqmConfig( Daemon daemon, String[] argvs )
    {
	super(daemon, argvs);
    }
}
