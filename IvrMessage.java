package com.qkernel;
//
// IvrMessage.java	Ivr Message Node
// ----------------------------------------------------------------------------
// History:
// --------
/// 08/01/01 M. Gill	Initial creation.
// ----------------------------------------------------------------------------
//
import java.lang.*;

public class IvrMessage extends CloneableObject implements SerialVersionId
{
    static final long serialVersionUID = SERIAL_VERSION_UID;

    public String	Cli;
    public String	Message;
}
