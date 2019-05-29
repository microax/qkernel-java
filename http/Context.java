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
 * The {@code Context} annotation decorates methods which are mapped
 * to a context (path) within the server, and provide its contents.
 * <p>
 * The annotated methods must have the same signature and contract
 * as {@link ContextHandler#serve}, but can have arbitrary names.
 *
 * @see VirtualHost#addContexts(Object)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Context
{
    /**
     * The context (path) that this field maps to (must begin with '/').
     *
     * @return the context (path) that this field maps to
     */
    String value();

    /**
     * The HTTP methods supported by this context handler (default is "GET").
     *
     * @return the HTTP methods supported by this context handler
     */
    String[] methods() default "GET";
}
