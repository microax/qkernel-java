package com.qkernel;
//
// Queue.java       QUEUE operations (Enqueue & Dequeue)
// ----------------------------------------------------------------------------
// History:
// --------
// 08/10/02 M. Gill	Add peek() 
// 06/03/02 M. Gill	Add dumpQueue().
// 01/09/00 M. Gill     Add Count().
// 09/20/97 M. Gill     Initial creation.
// ----------------------------------------------------------------------------
//
import java.util.ArrayList;

public class Queue
{
    private FIFONode Head;
    private FIFONode Tail;
    private int Qcount;

    public synchronized void Enqueue(Object MyObject)
    {
        FIFONode node = new FIFONode(); // Create Node.
        node.o = MyObject;              // Copy Object reference
        node.next = null;               //
        if (Head != null)               // Check if list is empty...
            Tail.next = node;           // Link to end of list
        else
            Head = node;                // Link as first in list (was empty)
         Tail = node;                   // Adjust Tail pointer.
	 Qcount++;                      // Adjust queue count
    }


    public  synchronized Object Dequeue()
    {
        Object MyObject;

        if (Head == null)
            return null;

        MyObject = Head.o;
        Head     = Head.next;
        if ( Head == null)
            Tail = null;

        Qcount--;
        return MyObject;
    }

    public synchronized ArrayList dumpQueue()
    {
	ArrayList <Object> l = new ArrayList <Object> ();
	FIFONode n  = new FIFONode();
	Object o    = null;

	for(n = Head; n != null;)
   	{
	    o = n.o;
	    l.add(o);
	    n = n.next;
	}
	return(l);
    }

    public synchronized Object peek()
    {
        return Head.o;
    }

    public synchronized int Count()
    {
        return Qcount;
    }




    public Queue()
    {
        Head = null;
        Tail = null;
        Qcount =0;
    }
}


