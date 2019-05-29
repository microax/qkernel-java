package com.qkernel;
//
// GIOPHeader.java        GIOP Header  class
// ----------------------------------------------------------------------------
// History:
// --------
// 09/12/99 M. Gill        Initial creation.
// ----------------------------------------------------------------------------
//
import java.lang.*;

public class GIOPHeader
{
    public byte magic0;	// G
    public byte magic1;	// I
    public byte magic2;	// O
    public byte magic3;	// P

    public byte version_major;
    public byte version_minor;

    public byte byte_order;	// 0 => Big 1 => little
    public byte message_type;	// request, response, locate request etc.

    public int  message_size;
    public byte message_buffer[];

}






