package com.qkernel.examples;
//
// Mqm.java        Example quernel Server
// ----------------------------------------------------------------------------
// History:
// --------
// 06/02/19 M. Gill	Initial creation.
// ----------------------------------------------------------------------------
//
import java.lang.*;
import com.qkernel.*;

public class Mqm
{
    public  static MqmDaemon mqm; 

    public static void main(String argvs[])
    {
	mqm = new MqmDaemon("ZeroOne");
	mqm.startDaemon(argvs);
    }
}
