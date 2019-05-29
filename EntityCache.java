package com.qkernel;
//
// EntityCache.java     Interface for EntityObject Cache Service
// ----------------------------------------------------------------------------
// History:
// --------
//
// 08/25/02 M. Gill	Modified remove() and put() to return Object
// 08/20/02 M. Gill	Initial Creation.
// ----------------------------------------------------------------------------
//
import java.lang.*;

//
//-----------------------------------------------------------------------------
//
public interface EntityCache
{

    //--------------------------------------------------------------------------------
    // METHOD remove()
    //
    // PURPOSE:	Remove all Objects from Cache
    //
    // INPUT:	None.
    //
    // RETURN:	None.
    //--------------------------------------------------------------------------------
    public void removeAll();

    //--------------------------------------------------------------------------------
    // METHOD remove()
    //
    // PURPOSE:	Remove an Object from Cache
    //
    // INPUT:	Object - key
    //
    // RETURN:	Object
    //--------------------------------------------------------------------------------
    public Object remove(Object key);

    //--------------------------------------------------------------------------------
    // METHOD get()
    //
    // PURPOSE:	Get an Object from Cache
    //
    // INPUT:	Object - key
    //
    // RETURN:	Object in objTable.
    //--------------------------------------------------------------------------------
    public Object get(Object key);


    //--------------------------------------------------------------------------------
    // METHOD put()
    //
    // PURPOSE:	Put an Object into Cache
    //
    // INPUT:	Object - key
    //		Object - obj
    //
    // PROCESS:
    //		0) Check objTable.size and upper limit criteria.
    //		1) Create new EntityCacheNode, and place into expTable
    //		2) Put obj into objTable
    //
    // RETURN:	None.
    //--------------------------------------------------------------------------------
    public Object put(Object key, Object obj);

    public void start();
    public void start(int expTime);
    public void start(int expTime , int maxObj);

}
