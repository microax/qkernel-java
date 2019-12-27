package com.qkernel;
//
// StateTableEntry.java   State Table Container class
// ----------------------------------------------------------------------------
// History:
// --------
// 09/20/97 M. Gill        Initial creation.
// ----------------------------------------------------------------------------
//
import java.lang.reflect.*;
import java.lang.Class.*;

public class StateTableEntry extends Object
{
    int        next_state;
    Method     action;
}

