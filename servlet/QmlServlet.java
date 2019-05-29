
package com.qkernel.servlet;

// QmlServlet.java        Servlet to Get a QML Document
// ---------------------------------------------------------------------------
// History:
// --------
// 04/05/08 N. Oertel Added functionality to allow monitoring an upload's progress
//                    Added doGet with argv passed to it to allow for overriding
//                    with method to get upload progress and put it in argv before
//                    doing the normal processing
// 05/27/05 M. Gill	Add getCookieExpires()
// 09/03/02 M. Gill	Add support to maxView.
// 06/28/02 M. Gill	Moved initFilter() to be before getOrbAddress().
// 06/10/02 M. Gill	Add support for sending FileObject in response
// 06/07/02 M. Gill	Fixed bug in doPost() when handling 
//			multipart/form-data requests.
// 05/27/02 M. Gill	1) Support multipart/form-data encoding.
//			2) Moved to com.qkernel.servlet package.
// 04/26/02 M. Gill	Use getHeaderNames() to get all headers in request.
// 04/11/02 M. Gill	Support multi-valued parameters in request 
// 03/19/02 M. Gill	Add initFilter(), and requestFilter() 
// 01/15/02 M. Gill	Initial creation.
// ---------------------------------------------------------------------------
//
import com.qkernel.QMessage;
import com.qkernel.QmlProxy;
import com.qkernel.FileObject;
import com.qkernel.upload.UploadListener;
import com.qkernel.upload.MonitoredDiskFileItemFactory;
import com.qkernel.upload.MonitoredOutputStream;
import com.qkernel.upload.UploadInfo;
import com.qkernel.servlet.multipart.*;
//
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.lang.*;
import java.io.*;
import java.net.*;

import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
@SuppressWarnings("unchecked")
//---------------------------------------------------------------------------
// QmlServlet is an Http Servlet class invoked from a browser request to render
// a QML document....QML documents are Business objects associated with 
// a QML text file. A QML text file, is an extended XHTML document with 
// tags that invoke business methods in the business tier.
//---------------------------------------------------------------------------
//
public abstract class QmlServlet extends HttpServlet 
{
    public static int maxView = Integer.MAX_VALUE;

    public String OrbAddress;
    public int    OrbPort;
    public String CookieId;
    public String DefaultUrl;
    public int    CookieExpires;

    public abstract String getOrbAddress();
    public abstract int    getOrbPort();
    public abstract String getCookieId();
    public abstract String getDefaultUrl();

    public int getCookieExpires()
    {
	return(-1);  // default is session cookie
    }

    public String getBusyUrl()
    {
	return(getDefaultUrl());
    }
       
    public synchronized void setMaxView(int n)
    {
	maxView = n;
    }

    public synchronized boolean isViewReady()
    {
	boolean rt = false;

	synchronized (this)
	{
	    if(maxView > 0)
	    {
	        rt =true;
	   	maxView--;
	    }
	}
	return(rt);
    }


    public synchronized void releaseView()
    {
	synchronized (this)
	{
   	   maxView++;
	}
    }


    public void initFilter(ServletConfig cfg)
    {

    }

    public void requestFilter(HttpServletRequest req, QMessage argv)
    {

    }

    //-----------------------------------------------------------------------
    // Here's all of the important global stuff,
    //----------------------------------------------------------------------- 
    public void init(ServletConfig cfg)
	throws ServletException
    {
	super.init(cfg);

	initFilter(cfg);

	OrbAddress 	= getOrbAddress();
    	OrbPort    	= getOrbPort();
	CookieId        = getCookieId();
	DefaultUrl	= getDefaultUrl();
	CookieExpires   = getCookieExpires();
    }


    //-----------------------------------------------------------------------
    // A Servlet by any other name, is still, a Servlet...
    // Here we implement the ever so popular doGet method
    //-----------------------------------------------------------------------
    public void doGet(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException
    {
        QMessage argv	= new QMessage();

        doGet(req, res, argv);
    }

    //-----------------------------------------------------------------------
    // Actual implementation of the doGet method including the argv parameter
    // Implemented to allow another class to override doGet for additional
    // processing while passing arguments through argv (initially implemented
    // for use with upload progress monitoring)
    //-----------------------------------------------------------------------
    public void doGet(HttpServletRequest req, HttpServletResponse res, QMessage argv)
	throws ServletException, IOException
    {

    //----------------------------------------------------------------
	// Here we gather all of the important stuff from our
	// request stream, and pack it , into our request object.
	//----------------------------------------------------------------
	Enumeration enm = req.getParameterNames();
	while(enm.hasMoreElements())
	{
	    String str = (String)enm.nextElement();
	    String[] sa= req.getParameterValues(str);

	    if(sa.length > 1)
	        argv.put(str, sa);
	    else
	    	argv.put(str, req.getParameter(str));
	}

	doGetHeaders(req, res, argv);

	doQmlRequest(req, res, argv);
    }


    //--------------------------------------------------------------------
    // Implement doPost to support hidden forms and perhaps other 
    // post requests from externel services...If content type is not 
    // "multipart/form-data", process 
    // via doGet...Otherwise handle the multipart goo...
    //--------------------------------------------------------------------
    public void doPost (HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException
    {
	Hashtable parameters = new Hashtable();

	String requestType = req.getContentType();
	requestType        = requestType.toLowerCase();

	if(!requestType.startsWith("multipart/form-data"))
	{
	//--------------------------------------------
	// this is a regular urlencode thing...
	//--------------------------------------------
	    doGet(req, res);
	}
	else
	{
	//--------------------------------------------
	// This is a multipart/form-data request...
	//--------------------------------------------
            QMessage argv = new QMessage();
        try 
	{
   	    ByteArrayOutputStream        bostream;
//    	    BufferedOutputStream   output_stream;

	    doGetHeaders(req, res, argv);

	    MultipartParser mp = new MultipartParser(req, 10*1024*1024); // 10MB
	    com.qkernel.servlet.multipart.Part part;

            while ((part = mp.readNextPart()) != null) 
	    {
         	String name = part.getName();
        	if (part.isParam()) 
		{
		    /*
		    ----------------- Original Code ------------------
          	    ParamPart paramPart = (ParamPart) part;
         	    String        value = paramPart.getStringValue();
	    	    argv.put(name, value);
		    --------------------------------------------------
		    */

		    //-------------- New Code ---------------------------
        	    ParamPart paramPart = (ParamPart) part;
        	    String value = paramPart.getStringValue();
        	    Vector existingValues = (Vector)parameters.get(name);
        	    if (existingValues == null) 
		    {
          		existingValues = new Vector();
          		parameters.put(name, existingValues);
        	    }
        	    existingValues.addElement(value);
		    //----------------------------------------------------
        	}
        	else if (part.isFile()) 
		{
		    FilePart filePart = (FilePart) part;
          	    String fileName   = filePart.getFileName();

          	    if (fileName != null) 
		    {
// begin original implementation
//            		bostream      = new ByteArrayOutputStream();
//            		output_stream = new BufferedOutputStream(bostream);
//			filePart.writeTo(output_stream);
//			output_stream.flush();
//                byte[] image  = bostream.toByteArray();
//                        long size     = image.length;
//
//                FileObject fo = new FileObject(fileName,
//                                   filePart.getContentType(),
//                                   filePart.getFilePath(),
//                                   image,
//                                   size
//                                  );
//                argv.put(name, fo);
// end original implementation


                /** begin new implementation */
                /** create a listener for the upload */
                UploadListener listener = new UploadListener(req, 0);

                /** create the output stream */
                bostream = new ByteArrayOutputStream();

                /** begin a monitored output stream */
                MonitoredOutputStream os = new MonitoredOutputStream(bostream, listener);

                /** write the upload to the stream */
                filePart.writeTo(os);
                os.flush();

                byte[] image  = bostream.toByteArray();
                long size     = image.length;

                FileObject fo = new FileObject(fileName,
                                   filePart.getContentType(),
                                   filePart.getFilePath(),
                                   image,
                                   size
                                  );
                argv.put(name, fo);

                /** clear the listener so it is no longer in the user's session */
                listener.clearInfo();
                /** end new implementation */
                  }
        	}
      	    }
    	}
        catch (IOException e){ }

	try 
	{
	Enumeration enm = parameters.keys();
	while(enm.hasMoreElements())
	{
	    String str = (String)enm.nextElement();

      	    Vector values = (Vector)parameters.get(str);

      	    if (values != null && values.size() != 0) 
	    {
      		String[] valuesArray = new String[values.size()];
		Enumeration enm2 = values.elements();
		for( int j=0; j < values.size(); j++)
		    valuesArray[j] = (String)enm2.nextElement();

      		/* values.copyInto(valuesArray); */

	    	if(valuesArray.length > 1)
	            argv.put(str, valuesArray);
	        else
	    	    argv.put(str, valuesArray[0]);
      	    }
	}
        }
    	catch (Exception e) {}

	    doQmlRequest(req, res, argv);
	}
    }



    //----------------------------------------------------------------------
    // Send everything we got to a QmlDocumentBroker... or similar ORB :-)
    //----------------------------------------------------------------------
    protected void doQmlRequest(HttpServletRequest  req, 
				HttpServletResponse res, QMessage argv)
	                           throws ServletException, IOException
    {

	if( isViewReady() )
	{
        QmlProxy obj = new QmlProxy(OrbAddress, OrbPort);
	    QMessage rep = new QMessage();

	    //------------------------------
	    //  Execute the filter
	    //------------------------------
	    requestFilter(req, argv);

	    //-----------------------------------------------------------------
	    // Connect to the remote object, send the request, and get the 
	    // reply object...
	    //-----------------------------------------------------------------
    	    try
	    {
	    	rep =obj.getDocument(argv);

	    	String TargetUrl	=(String)rep.get("TargetUrl");
	    	String Arg0		=(String)rep.get("Arg0");
	    	String Arg2		=(String)rep.get("Arg2");

	    	if(Arg0.equals("html"))
    	    	{
	    	    //-----------------------------
	    	    // Print HTML Document
	    	    //-----------------------------
		    String fStr=(String)rep.get("Arg1");

		    PrintWriter out = res.getWriter();
		    res.setContentType(Arg2);
	            out.write(fStr);
		    out.flush();
		    out.close();
	    	}
	    	else if(Arg0.equals("binary"))
	    	{
	    	    //-----------------------------
	    	    // Print Binary File
	    	    //-----------------------------
		    FileObject fo=(FileObject)rep.get("Arg1");

  		    ServletOutputStream ostream = res.getOutputStream();
		    DataOutputStream out        = new DataOutputStream(ostream);

		    res.setContentType(Arg2);
	            out.write(fo.getImage(), 0, (int)fo.getImageSize());
		    out.flush();
		    out.close();
	    	}
	    	else if(Arg0.equals("redirect"))
	    	{
	    	    //---------------------------
	    	    // Target must be redirect...
	    	    //---------------------------
		    res.sendRedirect("http://"+TargetUrl);
	    	}
	    	else
	    	{
	    	    //-----------------------------------
	    	    // Something went wrong...
	    	    // Send the default error.
	    	    //-----------------------------------
	    	    //res.sendRedirect(HttpReferer);
		    res.sendRedirect(DefaultUrl);
	    	}
	    }
	    catch(Exception e)
	    {
	    	//-----------------------------------
	    	// Something went wrong...
	    	// Send the default.
	    	//-----------------------------------
	    	//res.sendRedirect(HttpReferer);
	    	res.sendRedirect(DefaultUrl);
   	    }
	    finally
	    {
		//--------------------------
	 	// Always release our view
	   	//--------------------------
	    	releaseView();
	    }
	}
	else
	{
	    //-----------------------------------
	    // There are no views avail.
	    //-----------------------------------
	    res.sendRedirect(getBusyUrl());
	}
    }


    //--------------------------------------------------------------
    // Grab all headers...and Cookie stuff...
    //-------------------------------------------------------------- 
    protected void doGetHeaders(HttpServletRequest  req, 
				HttpServletResponse res, QMessage argv)
    { 
	CookieGen cg    = new CookieGen(req, res);

	Enumeration enm2 = req.getHeaderNames();
	while(enm2.hasMoreElements())
	{
	    String name = (String)enm2.nextElement();
	    String value=  req.getHeader(name);

	    if(name!= null && value != null) 
	        argv.put(name, value);
	}

	String userId =  cg.getCookieValue(CookieId, getCookieExpires());
	argv.put("UserId", userId);
	argv.put("SessionId", userId);
	argv.put("REMOTE_ADDR", req.getRemoteAddr());
    }

    /*
    public QmlServlet()
    {

    }
    */
}
