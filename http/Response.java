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
 * The {@code Response} class encapsulates a single HTTP response.
 */
public class Response implements Closeable
{
    protected OutputStream out; // the underlying output stream
    protected OutputStream[] encoders = new OutputStream[4]; // chained encoder streams
    protected Headers headers;
    protected boolean discardBody;
    protected int state; // nothing sent, headers sent, or closed
    protected Request req; // request used in determining client capabilities

    /**
     * Constructs a Response whose output is written to the given stream.
     *
     * @param out the stream to which the response is written
     */
    public Response(OutputStream out)
    {
        this.out = out;
        this.headers = new Headers();
    }

    /**
     * Sets whether this response's body is discarded or sent.
     *
     * @param discardBody specifies whether the body is discarded or not
     */
    public void setDiscardBody(boolean discardBody)
    {
        this.discardBody = discardBody;
    }

    /**
     * Sets the request which is used in determining the capabilities
     * supported by the client (e.g. compression, encoding, etc.)
     *
     * @param req the request
     */
    public void setClientCapabilities(Request req) { this.req = req; }

    /**
     * Returns the request headers collection.
     *
     * @return the request headers collection
     */
    public Headers getHeaders() { return headers; }

    /**
     * Returns the underlying output stream to which the response is written.
     * Except for special cases, you should use {@link #getBody()} instead.
     *
     * @return the underlying output stream to which the response is written
     */
    public OutputStream getOutputStream() { return out; }

    /**
     * Returns whether the response headers were already sent.
     *
     * @return whether the response headers were already sent
     */
    public boolean headersSent() { return state == 1; }

    /**
     * Returns an output stream into which the response body can be written.
     * The stream applies encodings (e.g. compression) according to the sent headers.
     * This method must be called after response headers have been sent
     * that indicate there is a body. Normally, the content should be
     * prepared (not sent) even before the headers are sent, so that any
     * errors during processing can be caught and a proper error response returned -
     * after the headers are sent, it's too late to change the status into an error.
     *
     * @return an output stream into which the response body can be written,
     *         or null if the body should not be written (e.g. it is discarded)
     * @throws IOException if an error occurs
     */
    public OutputStream getBody() throws IOException
    {
        if (encoders[0] != null || discardBody)
            return encoders[0]; // return the existing stream (or null)
        // set up chain of encoding streams according to headers
        List<String> te = Arrays.asList(splitElements(headers.get("Transfer-Encoding"), true));
        List<String> ce = Arrays.asList(splitElements(headers.get("Content-Encoding"), true));
        int i = encoders.length - 1;
        encoders[i] = new FilterOutputStream(out)
	{
            @Override
            public void close() {} // keep underlying connection stream open for now
            @Override // override the very inefficient default implementation
            public void write(byte[] b, int off, int len) throws IOException { out.write(b, off, len); }
        };
        if (te.contains("chunked"))
            encoders[--i] = new ChunkedOutputStream(encoders[i + 1]);
        if (ce.contains("gzip") || te.contains("gzip"))
            encoders[--i] = new GZIPOutputStream(encoders[i + 1], 4096);
        else if (ce.contains("deflate") || te.contains("deflate"))
            encoders[--i] = new DeflaterOutputStream(encoders[i + 1]);
        encoders[0] = encoders[i];
        encoders[i] = null; // prevent duplicate reference
        return encoders[0]; // returned stream is always first
    }

    /**
     * Closes this response and flushes all output.
     *
     * @throws IOException if an error occurs
     */
    public void close() throws IOException
    {
        state = -1; // closed
        if (encoders[0] != null)
            encoders[0].close(); // close all chained streams (except the underlying one)
        out.flush(); // always flush underlying stream (even if getBody was never called)
    }

    /**
     * Sends the response headers with the given response status.
     * A Date header is added if it does not already exist.
     * If the response has a body, the Content-Length/Transfer-Encoding
     * and Content-Type headers must be set before sending the headers.
     *
     * @param status the response status
     * @throws IOException if an error occurs or headers were already sent
     * @see #sendHeaders(int, long, long, String, String, long[])
     */
    public void sendHeaders(int status) throws IOException
    {
        if (headersSent())
            throw new IOException("headers were already sent");
        if (!headers.contains("Date"))
            headers.add("Date", formatDate(System.currentTimeMillis()));
        headers.add("Server", "METAQ/2.3");
        out.write(getBytes("HTTP/1.1 ", Integer.toString(status), " ", statuses[status]));
        out.write(CRLF);
        headers.writeTo(out);
        state = 1; // headers sent
    }

    /**
     * Sends the response headers, including the given response status
     * and description, and all response headers. If they do not already
     * exist, the following headers are added as necessary:
     * Content-Range, Content-Type, Transfer-Encoding, Content-Encoding,
     * Content-Length, Last-Modified, ETag, Connection  and Date. Ranges are
     * properly calculated as well, with a 200 status changed to a 206 status.
     *
     * @param status the response status
     * @param length the response body length, or zero if there is no body,
     *        or negative if there is a body but its length is not yet known
     * @param lastModified the last modified date of the response resource,
     *        or non-positive if unknown. A time in the future will be
     *        replaced with the current system time.
     * @param etag the ETag of the response resource, or null if unknown
     *        (see RFC2616#3.11)
     * @param contentType the content type of the response resource, or null
     *        if unknown (in which case "application/octet-stream" will be sent)
     * @param range the content range that will be sent, or null if the
     *        entire resource will be sent
     * @throws IOException if an error occurs
     */
    public void sendHeaders(int status, long length, long lastModified,
            String etag, String contentType, long[] range) throws IOException
    {
        if (range != null)
	{
            headers.add("Content-Range", "bytes " + range[0] + "-" +
                range[1] + "/" + (length >= 0 ? length : "*"));
            length = range[1] - range[0] + 1;
            if (status == 200)
                status = 206;
        }
	
        String ct = headers.get("Content-Type");
        if (ct == null)
	{
            ct = contentType != null ? contentType : "application/octet-stream";
            headers.add("Content-Type", ct);
        }
        if (!headers.contains("Content-Length") && !headers.contains("Transfer-Encoding"))
	{
            // RFC2616#3.6: transfer encodings are case-insensitive and must not be sent to an HTTP/1.0 client
            boolean modern = req != null && req.getVersion().endsWith("1.1");
            String accepted = req == null ? null : req.getHeaders().get("Accept-Encoding");
            List<String> encodings = Arrays.asList(splitElements(accepted, true));
            String compression = encodings.contains("gzip") ? "gzip" :
                                 encodings.contains("deflate") ? "deflate" : null;
            if (compression != null && (length < 0 || length > 300) && isCompressible(ct) && modern)
	    {
                headers.add("Transfer-Encoding", "chunked"); // compressed data is always unknown length
                headers.add("Content-Encoding", compression);
            }
	    else if (length < 0 && modern)
	    {
                headers.add("Transfer-Encoding", "chunked"); // unknown length
            }
	    else if (length >= 0)
	    {
                headers.add("Content-Length", Long.toString(length)); // known length
            }
        }
        if (!headers.contains("Vary")) // RFC7231#7.1.4: Vary field should include headers
            headers.add("Vary", "Accept-Encoding"); // that are used in selecting representation
        if (lastModified > 0 && !headers.contains("Last-Modified")) // RFC2616#14.29
            headers.add("Last-Modified", formatDate(Math.min(lastModified, System.currentTimeMillis())));
        if (etag != null && !headers.contains("ETag"))
            headers.add("ETag", etag);
        if (req != null && "close".equalsIgnoreCase(req.getHeaders().get("Connection"))
                && !headers.contains("Connection"))
            headers.add("Connection", "close"); // #RFC7230#6.6: should reply to close with close
        sendHeaders(status);
    }

    /**
     * Sends the full response with the given status, and the given string
     * as the body. The text is sent in the UTF-8 charset. If a
     * Content-Type header was not explicitly set, it will be set to
     * text/html, and so the text must contain valid (and properly
     * {@link HTTPServer#escapeHTML escaped}) HTML.
     *
     * @param status the response status
     * @param text the text body (sent as text/html)
     * @throws IOException if an error occurs
     */
    public void send(int status, String text) throws IOException
    {
        byte[] content = text.getBytes("UTF-8");
        sendHeaders(status, content.length, -1,
            "W/\"" + Integer.toHexString(text.hashCode()) + "\"",
            "text/html; charset=utf-8", null);
        OutputStream out = getBody();
        if (out != null)
            out.write(content);
    }

    /**
     * Sends an error response with the given status and detailed message.
     * An HTML body is created containing the status and its description,
     * as well as the message, which is escaped using the
     * {@link HTTPServer#escapeHTML escape} method.
     *
     * @param status the response status
     * @param text the text body (sent as text/html)
     * @throws IOException if an error occurs
     */
    public void sendError(int status, String text) throws IOException
    {
        send(status, String.format(
            "<!DOCTYPE html>%n<html>%n<head><title>%d %s</title></head>%n" +
            "<body><h1>%d %s</h1>%n<p>%s</p>%n</body></html>",
            status, statuses[status], status, statuses[status], escapeHTML(text)));
    }

    /**
     * Sends an error response with the given status and default body.
     *
     * @param status the response status
     * @throws IOException if an error occurs
     */
    public void sendError(int status) throws IOException
    {
        String text = status < 400 ? ":)" : "sorry it didn't work out :(";
        sendError(status, text);
    }

    /**
     * Sends the response body. This method must be called only after the
     * response headers have been sent (and indicate that there is a body).
     *
     * @param body a stream containing the response body
     * @param length the full length of the response body
     * @param range the sub-range within the response body that should be
     *        sent, or null if the entire body should be sent
     * @throws IOException if an error occurs
     */
    public void sendBody(InputStream body, long length, long[] range) throws IOException
    {
        OutputStream out = getBody();
        if (out != null)
	{
            if (range != null)
	    {
                long offset = range[0];
                length = range[1] - range[0] + 1;
                while (offset > 0)
		{
                    long skipped = body.skip(offset);
                    if (skipped == 0)
                        throw new IOException("can't skip to " + range[0]);
                    offset -= skipped;
                }
            }
            transfer(body, out, length);
        }
    }

    /**
     * Sends a 301 or 302 response, redirecting the client to the given URL.
     *
     * @param url the absolute URL to which the client is redirected
     * @param permanent specifies whether a permanent (301) or
     *        temporary (302) redirect status is sent
     * @throws IOException if an IO error occurs or url is malformed
     */
    public void redirect(String url, boolean permanent) throws IOException
    {
        try
	{
            url = new URI(url).toASCIIString();
        }
	catch (URISyntaxException e)
	{
            throw new IOException("malformed URL: " + url);
        }
        headers.add("Location", url);
        // some user-agents expect a body, so we send it
        if (permanent)
            sendError(301, "Permanently moved to " + url);
        else
            sendError(302, "Temporarily moved to " + url);
    }
}
