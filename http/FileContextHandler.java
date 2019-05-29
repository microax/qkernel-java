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
 * The {@code FileContextHandler} services a context by mapping it
 * to a file or folder (recursively) on disk.
 */
public static class FileContextHandler implements ContextHandler
{
    protected final File base;

    public FileContextHandler(File dir) throws IOException
    {
        this.base = dir.getCanonicalFile();
    }

    public int serve(Request req, Response resp) throws IOException
    {
        return serveFile(base, req.getContext().getPath(), req, resp);
    }
}
