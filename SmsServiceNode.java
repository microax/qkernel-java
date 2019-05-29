package com.qkernel;
//
// SmsServiceNode.java	SMS Service Node
// ----------------------------------------------------------------------------
// History:
// --------
/// 07/12/01 M. Gill	Initial creation.
// ----------------------------------------------------------------------------
//
import java.lang.*;

public class SmsServiceNode extends CloneableObject implements SerialVersionId
{
    static final long serialVersionUID = SERIAL_VERSION_UID;

    public String 	phoneNumber;
    public String 	ipAddress;
    public int		port;

    public SmsServiceNode()
    {

    }
}
