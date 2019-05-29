package com.qkernel.classloader;
//
// AppClassReloader.java        ReLoads Class files
// ----------------------------------------------------------------------------
// History:
// --------
// 01/08/02 N. Bakholdina	Initial creation.
// ----------------------------------------------------------------------------
//
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.DirContext;
import javax.naming.Context;
import java.lang.*;

import java.io.File;
import java.util.Hashtable;

import com.qkernel.*;


public class AppClassReloader
{

    private Object monitor     = new Object();
    private ClassLoader loader = null;

    private String docBase     = null;
    private Daemon daemon      = null;

    private void initLoader()
    {
        try
        {

            //init loader with the absolute base directory
            loader = new AppClassLoader ( docBase );


            //add relative path to Class directories
            //note: it will be appended to the base directory
            ((AppClassLoader)loader).addRepository("/");

            //call start
            ((AppClassLoader) loader).start(daemon);

        } 
	catch (Exception e ) 
	{
	    daemon.event_log.SendMessage("AppClassReloader.initLoader() failed...");
	}
    }


    public Class reload( String className)
    {
        synchronized( monitor )
	{
            try
            {
        	// if loader is null is will be initialized
	        // if repository class was modified, the loader is stopped and re-initialized
        	if ( null == loader || ((AppClassLoader)loader).modified())
        	{
		    if ( null != loader )
            	    	((AppClassLoader)loader).stop();

		    loader = null;
		    initLoader();
	    	}

	    	Class clazz = loader.loadClass(className);
	    	return clazz;
	    }
            catch (Exception e )
	    { 

	    }
        }

	return null;
    }

/*

    public Class loadClass(String myClass)
    {
	return(reload(myClass));
    }

    public Class loadClass2(String myClass)
    {
	Class c = null;
        try{
	    c = loader.loadClass(myClass);
	}catch(Exception e){}

	return(c);
    }

*/

    public AppClassReloader(Daemon d, String docroot)
    {
	daemon  = d;
	docBase = docroot;
    }

}
