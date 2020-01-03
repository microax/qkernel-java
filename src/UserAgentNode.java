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
import javax.net.ssl.*;

public class UserAgentNode
{
    public Eos    application;
    public Socket socket;
    public SSLSocket ssl_socket;
}

