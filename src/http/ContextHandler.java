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
 * A {@code ContextHandler} serves the content of resources within a context.
 *
 * @see VirtualHost#addContext
 */
public interface ContextHandler
{
    /**
     * Serves the given request using the given response.
     *
     * @param req the request to be served
     * @param resp the response to be filled
     * @return an HTTP status code, which will be used in returning
     *         a default response appropriate for this status. If this
     *         method invocation already sent anything in the response
     *         (headers or content), it must return 0, and no further
     *         processing will be done
     * @throws IOException if an IO error occurs
     */
    int serve(Request req, Response resp) throws IOException;
}
