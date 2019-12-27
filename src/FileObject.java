package com.qkernel;

import java.io.*;
import com.qkernel.servlet.multipart.*;

//----------------------------------------------------

public class FileObject extends CloneableObject implements SerialVersionId
{
    static final long serialVersionUID = SERIAL_VERSION_UID;

    private String fileName;     
    private String filePath;
    private String contentType;   
    private byte [] image = null;
    private long imageSize;

    public FileObject(String fileName, 
		      String contentType, 
		      String filePath, 
		      byte [] image, long imageSize)
    {
	this.image = new byte[(int)imageSize + 1];

	this.fileName    = fileName;
	this.filePath    = filePath;
	this.contentType = contentType;
	this.image       = image;
	this.imageSize   = imageSize;
    }

  
    public String getFileName() 
    {
	return fileName;
    }


    public String getFilePath() 
    {
	return filePath;
    }


    public String getContentType() 
    {
	return contentType;
    }
  

    public byte [] getImage() 
    {
    	return image;
    }

    public long getImageSize()
    {
	return imageSize;
    }

    public long writeTo(File fileOrDirectory) throws IOException 
    {
	long written = 0;
    
	OutputStream fileOut = null;
    	try 
	{
      	    // Only do something if this part contains a file
      	    if (fileName != null) 
	    {
        	// Check if user supplied directory
        	File file;
        	if (fileOrDirectory.isDirectory()) 
		{
          	    // Write it to that dir the user supplied, 
          	    // with the filename it arrived with
          	    file = new File(fileOrDirectory, fileName);
        	}
        	else 
		{
          	    // Write it to the file the user supplied,
          	    // ignoring the filename it arrived with
         	     file = fileOrDirectory;
        	}

            	fileOut = new BufferedOutputStream(new FileOutputStream(file));
            	written = write(fileOut);
	    }
	}
    	finally 
    	{
      	    if (fileOut != null) 
		fileOut.close();
    	}

	return written;
    }


    public long writeTo(OutputStream out) throws IOException 
    {
    	long size=0;

	// Only do something if this part contains a file

    	if (fileName != null) 
	{
      	    // Write it out
      	    size = write( out );
    	}

    	return size;
    }


    long write(OutputStream out) throws IOException 
    {
    	// decode macbinary if this was sent

    	if (contentType.equals("application/x-macbinary")) 
	{
	    out = new MacBinaryDecoderOutputStream(out);
    	}

      	out.write(getImage(), 0, (int)getImageSize());

        return (getImageSize());
    }  

}
