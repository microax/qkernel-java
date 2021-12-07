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

public final class ZeroOne extends LicenseKey
{

    public String licenseTo()
    {
	return("General Public License Granted by MetaQ.io");
    }

    public String nodeVersion()
    {
	return("2.0");
    }

    //--------------------------------------------------------
    // Internal Developer General License:
    //
    // key = ZeroOne
    // exp = NEVER
    // -------------------------------------------------------
    public ZeroOne(Date date)
    {
	super(date);
    }
}
