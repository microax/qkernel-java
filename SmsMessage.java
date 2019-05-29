package com.qkernel;
//
// SmsMessage.java	SMS Message Node
// ----------------------------------------------------------------------------
// History:
// --------
/// 04/23/01 M. Gill	Initial creation.
// ----------------------------------------------------------------------------
//
import java.lang.*;

public class SmsMessage extends CloneableObject implements SerialVersionId
{
    static final long serialVersionUID = SERIAL_VERSION_UID;

    public String 	Application;
    public int 		ProviderId;
    public String	PhoneNumber;
    public String	Message;
}
