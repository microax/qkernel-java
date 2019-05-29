package com.qkernel;
//
// SmsRemoteStatus.java		SMS Remote Status
// ----------------------------------------------------------------------------
// History:
// --------
/// 07/12/01 M. Gill	Initial creation.
// ----------------------------------------------------------------------------
//
import java.lang.*;

public class SmsRemoteStatus extends CloneableObject implements SerialVersionId
{
    static final long serialVersionUID = SERIAL_VERSION_UID;

    public int		status;
    public String 	errorString;

    public SmsRemoteStatus()
    {

    }
}
