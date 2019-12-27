package com.qkernel;
//
// UserAgentNode.java   Handle for tcp user agentsclass
// ----------------------------------------------------------------------------
// History:
// --------
// 09/20/97 M. Gill        Initial creation.
// ----------------------------------------------------------------------------
//
import java.net.*;

public class UserAgentNode
{
    public Eos    application;
    public Socket socket;
    public Socket ssl_socket;
}

