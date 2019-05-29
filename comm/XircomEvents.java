package com.qkernel.comm;
//
// XircomEvents.java   Interface defines Xircom GSM modem events        
// ----------------------------------------------------------------------------
// History:
// --------
// 04/23/01 M. Gill	Initial creation.
// ----------------------------------------------------------------------------
//
import java.lang.*;

public interface XircomEvents
{
    //----- States
    public final int XIRCOM_INIT      = 0;
    public final int XIRCOM_READY     = 1;
    public final int XIRCOM_SEND      = 2;
    public final int XIRCOM_CLEAN     = 3;

    //---- Events

    /*
    public final int INIT_OK	  = 0;
    public final int GSM_OK	  = 1;
    public final int GSM_ERROR	  = 2;
    public final int SMS_MESSAGE  = 3;
    public final int MODEM_DATA   = 10;
    public final int INCOMMING_SMS= 11;
    public final int SMS_OK	  = 12;
    public final int SEND_SMS	  = 13;
    public final int SEND_SMS_OK  = 14;
    public final int SEND_DONE	  = 15;
    public final int CLEAN_MEMORY = 16;
    public final int CLEAN_OK     = 17;
    public final int CLEAN_DONE   = 18;
    */

    //---- Serial Port
    /*
    public final String PORT	  = "COM1";
    public final int BITRATE	  = 9600;
    public final int DATABITS	  = 8;
    public final int STOPBITS	  = 1;
    public final int PARITY	  = 0;
    */
}
