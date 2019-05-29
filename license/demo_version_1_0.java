package com.qkernel.license;
//----------------------------------------------------------------------------
// Qkernel License key, generated via the library production manager.
//
// GenDate:	07-28-01
// Operator:	mgill
//----------------------------------------------------------------------------
import java.lang.*;
import com.qkernel.*;
import java.util.Date;

public final class demo_version_1_0 extends LicenseKey
{
    public String licenseTo()
    {
	return("DEMO VERSION FOR EVALUATION ONLY!");
    }

    public String nodeVersion()
    {
	return("2.0");
    }

    //--------------------------------------------------------
    // External Developer General License:
    //
    // key = demo_version_1_0
    // exp = NEVER
    // -------------------------------------------------------
    public demo_version_1_0(Date date)
    {
	super(date);
    }    
}
