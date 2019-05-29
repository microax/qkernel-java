package com.qkernel;
//
// QmlErrorHandler.java
// ----------------------------------------------------------------------------
// History:
// --------
// 07/15/00 Natasha	Initial creation.	
// ----------------------------------------------------------------------------
//
import java.io.*;
import java.util.*;
import java.text.*;
import java.lang.*;
import java.lang.reflect.*;

import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import org.xml.sax.helpers.AttributeListImpl;
import org.xml.sax.helpers.DefaultHandler;

public class QmlErrorHandler extends DefaultHandler
{

    public void error (SAXParseException e)
    {

    }

    public void warning (SAXParseException err)
    {

    }

}

