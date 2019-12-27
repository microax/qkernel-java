package com.qkernel.servlet.multipart;

import java.io.IOException;

import javax.servlet.ServletInputStream;
import javax.servlet.ReadListener;

//-------------------------------------------------------------------
@SuppressWarnings("unchecked")

public class LimitedServletInputStream extends ServletInputStream {
  
  /** input stream we are filtering */
  private ServletInputStream in;
  
  /** number of bytes to read before giving up */
  private int totalExpected;
  
  /** number of bytes we have currently read */
  private int totalRead = 0;
  
  /**
   * Creates a <code>LimitedServletInputStream</code> with the specified
   * length limit that wraps the provided <code>ServletInputStream</code>.
   */
  public LimitedServletInputStream(ServletInputStream in, int totalExpected) {
    this.in = in;
    this.totalExpected = totalExpected;
  }

  public void setReadListener(ReadListener rl) {

  }

    public boolean isReady() {
        return true;
    }
    public boolean isFinished() {
        return true;
    }
    


    
  /**
   * Implement length limitation on top of the <code>readLine</code> method of
   * the wrapped <code>ServletInputStream</code>.
   *
   * @param b    an array of bytes into which data is read.
   * @param off  an integer specifying the character at which
   *        this method begins reading.
   * @param len  an integer specifying the maximum number of 
   *        bytes to read.
   * @return     an integer specifying the actual number of bytes 
   *        read, or -1 if the end of the stream is reached.
   * @exception  IOException  if an I/O error occurs.
   */
  public int readLine(byte b[], int off, int len) throws IOException {
    int result, left = totalExpected - totalRead;
    if (left <= 0) {
      return -1;
    } else {
      result = (in).readLine(b, off, Math.min(left, len));
    }
    if (result > 0) {
      totalRead += result;
    }
    return result;    
  }

  /**
   * Implement length limitation on top of the <code>read</code> method of 
   * the wrapped <code>ServletInputStream</code>.
   *
   * @return     the next byte of data, or <code>-1</code> if the end of the
   *             stream is reached.
   * @exception  IOException  if an I/O error occurs.
   */
  public int read() throws IOException {
    if (totalRead >= totalExpected) {
      return -1;
    }

    int result = in.read();
    if (result != -1) {
      totalRead++;
    }
    return result;
  }
  
  /**
   * Implement length limitation on top of the <code>read</code> method of 
   * the wrapped <code>ServletInputStream</code>.
   *
   * @param      b     destination buffer.
   * @param      off   offset at which to start storing bytes.
   * @param      len   maximum number of bytes to read.
   * @return     the number of bytes read, or <code>-1</code> if the end of
   *             the stream has been reached.
   * @exception  IOException  if an I/O error occurs.
   */
  public int read( byte b[], int off, int len ) throws IOException {
    int result, left = totalExpected - totalRead;
    if (left <= 0) {
      return -1;
    } else {
      result = in.read(b, off, Math.min(left, len));
    }
    if (result > 0) {
      totalRead += result;
    }
    return result;    
  }
}
