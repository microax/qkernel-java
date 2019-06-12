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
 * The {@code SocketHandlerThread} handles accepted sockets.
 */
protected class SocketHandlerThread extends Thread
{
    @Override
    public void run()
    {
        setName(getClass().getSimpleName() + "-" + port);
        try
	{
            while (!serv.isClosed())
	    {
                final Socket sock = serv.accept();
                executor.execute(new Runnable()
		{
                    public void run()
		    {
                        try
			{
                            try
			    {
                                sock.setSoTimeout(socketTimeout);
                                sock.setTcpNoDelay(true); // we buffer anyway, so improve latency
                                handleConnection(sock.getInputStream(), sock.getOutputStream());
                            }
			    finally
			    {
                                try
				{
                                    // RFC7230#6.6 - close socket gracefully
                                    sock.shutdownOutput(); // half-close socket (only output)
                                    transfer(sock.getInputStream(), null, -1); // consume input
                                }
				finally
				{
                                    sock.close(); // and finally close socket fully
                                }
                            }
                        }
			catch (IOException ignore) {}
                    }
                } );
            }
        }
	catch (IOException ignore) {}
    }
}
