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
 * The {@code MultipartInputStream} decodes an InputStream whose data has
 * a "multipart/*" content type (see RFC 2046), providing the underlying
 * data of its various parts.
 * <p>
 * The {@code InputStream} methods (e.g. {@link #read}) relate only to
 * the current part, and the {@link #nextPart} method advances to the
 * beginning of the next part.
 */
public static class MultipartInputStream extends FilterInputStream
{
    protected final byte[] firstBoundary, fullBoundary; // without/with leading CRLF
    protected final byte[] buf = new byte[4096];
    protected int head, tail; // indices of current part's data in buf
    protected int extra;      // size of extra unprocessed data after tail

    /**
     * Constructs a MultipartInputStream with the given underlying stream.
     *
     * @param in the underlying multipart stream
     * @param boundary the multipart boundary
     * @throws NullPointerException if the given stream or boundary is null
     * @throws IllegalArgumentException if the given boundary's size is not
     *         between 1 and 70
     */
    protected MultipartInputStream(InputStream in, byte[] boundary)
    {
        super(in);
        int len = boundary.length;
        if (len < 1 || len > 70)
            throw new IllegalArgumentException("invalid boundary length");
        firstBoundary = new byte[len + 2]; // --boundary
        firstBoundary[0] = firstBoundary[1] = '-';
        System.arraycopy(boundary, 0, firstBoundary, 2, len);
        fullBoundary = new byte[len + 4]; // CRLF--boundary
        System.arraycopy(CRLF, 0, fullBoundary, 0, 2);
        System.arraycopy(firstBoundary, 0, fullBoundary, 2, len + 2);
    }

    @Override
    public int read() throws IOException
    {
        if (!fill())
            return -1;
        return buf[head++] & 0xFF;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        if (!fill())
            return -1;
        len = Math.min(tail - head, len);
        System.arraycopy(buf, head, b, off, len);
        head += len;
        return len;
    }

    @Override
    public long skip(long n) throws IOException
    {
        if (!fill())
            return 0;
        n = Math.min(tail - head, n);
        head += n;
        return n;
    }

    @Override
    public int available() throws IOException
    {
        return tail - head;
    }

    @Override
    public boolean markSupported()
    {
        return false;
    }

    /**
     * Advances the stream position to the beginning of the next part.
     *
     * @return true if successful, or false if there are no more parts
     * @throws IOException if an error occurs
     */
    public boolean nextPart() throws IOException
    {
        while (skip(buf.length) != 0)
	    ; // skip current part (until boundary)
            int[] boundary = findBoundary();
        // if it's the last boundary, stop advancing
        if (boundary[2] == 1)
            return false;
        // otherwise, the next part starts right after boundary
        head = tail += boundary[1];
        extra -= boundary[1];
        return fill() || extra > 0; // more data in the new part or after it
    }

    /**
     * Fills the buffer with more data from the underlying stream.
     *
     * @return true if there is available data for the current part,
     *         or false if the current part's end has been reached
     * @throws IOException if an error occurs
     */
    protected boolean fill() throws IOException
    {
        // check if we already have more available data
        if (head != tail) // note that if we continue, head == tail below
            return true;
        // if there's no more room, shift extra unread data to beginning of buffer
        if (tail > buf.length - fullBoundary.length - 2)
	{ // room for max boundary
            System.arraycopy(buf, tail, buf, 0, extra);
            head = tail = 0;
        }
        // read more data and look for boundary (or potential partial boundary)
        int[] boundary;
        int read;
        do
	{
            read = super.read(buf, tail + extra, buf.length - tail - extra);
            if (read > 0)
                extra += read;
            boundary = findBoundary();
            // if partial boundary with no data before it, we must continue reading
            // otherwise the return value may be incorrect (supposedly no data)
        } while (read > 0 && boundary[0] == head && boundary[1] == -1);
        // update indices (current chunk ends at potential or real boundary)
        if (read == -1 && boundary[1] == -1) // no more full boundaries
            throw new IOException("missing end boundary");
        tail = boundary[0] > -1 ? boundary[0] : head + extra;
        extra -= tail - head;
        return tail > head; // available data in current part
    }

    /**
     * Finds the first boundary within the buffer's remaining data.
     *
     * @return an array containing the start index of the boundary,
     *         its length, and whether it is the final boundary;
     *         if there is a potential partial boundary cut off at end of
     *         the buffer then its index is returned with a length of -1;
     *         if no potential boundary is found, all returned values are -1
     * @throws IOException if an error occurs
     */
    protected int[] findBoundary() throws IOException
    {
        // see RFC2046#5.1.1 for boundary syntax
        for (int i = head, end = head + extra; i < end; i++)
	{
            // try to match boundary value (leading CRLF is optional at first boundary)
            byte[] boundary = i > 0 || buf[i] != '-' ? fullBoundary : firstBoundary;
            int j = i; // end of selection
            while (j < end && j - i < boundary.length && buf[j] == boundary[j - i])
                j++;
            // if we found the boundary value, expand selection to include full line
            if (j - i == boundary.length && j + 1 < end)
	    {
                // check if end of entire multipart (trailing CRLF is optional here)
                if (buf[j] == '-' && buf[j + 1] == '-')
                    return new int[] { i, j - i + 2, 1 }; // including -- but no CRLF
                // allow linear whitespace after boundary up to CRLF
                while (j < end && (buf[j] == ' ' || buf[j] == '\t'))
                    j++;
                // if it's not cut off it's a full match (or invalid)
                if (j + 1 < end)
		{
                    // ensure trailing CRLF
                    if (buf[j] != '\r' || buf[j + 1] != '\n')
                        throw new IOException("boundary must end with CRLF");
                    return new int[] { i, j - i + 2, 0 }; // including trailing CRLF
                }
            }
            // return potential partial boundary which is cut off at end of buffer
            if (j + 1 >= end)
                return new int[] { i, -1, -1 };
        }
        return new int[] { -1, -1, -1 };
    }
}
