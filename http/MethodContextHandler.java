package com.qkernel.http;

import java.io.*;
import java.lang.annotation.*;
import java.lang.reflect.*;
import java.net.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;


/**
 * The {@code MethodContextHandler} services a context
 * by invoking a handler method on a specified object.
 * <p>
 * The method must have the same signature and contract as
 * {@link ContextHandler#serve}, but can have an arbitrary name.
 *
 * @see VirtualHost#addContexts(Object)
 */
public static class MethodContextHandler implements ContextHandler
{
    protected final Method m;
    protected final Object obj;

    public MethodContextHandler(Method m, Object obj) throws IllegalArgumentException
    {
        this.m = m;
        this.obj = obj;
        Class<?>[] params = m.getParameterTypes();
        if (params.length != 2
            || !Request.class.isAssignableFrom(params[0])
            || !Response.class.isAssignableFrom(params[1])
            || !int.class.isAssignableFrom(m.getReturnType()))
                throw new IllegalArgumentException("invalid method signature: " + m);
    }

    public int serve(Request req, Response resp) throws IOException {
        try
	{
            return (Integer)m.invoke(obj, req, resp);
        }
	catch (InvocationTargetException ite)
	{
            throw new IOException("error: " + ite.getCause().getMessage());
        }
	catch (Exception e)
	{
            throw new IOException("error: " + e);
        }
    }
}

