package com.qkernel;
//
// MetaQueueMessage.java	metaQueue Message 
// ----------------------------------------------------------------------------
// History:
// --------
// 09/06/03 M. Gill	Add getSenderEmail() and setSenderEmail()
// 07/27/03 M. Gill     Initial creation.
// ----------------------------------------------------------------------------
//
import java.lang.*;
import java.util.*;
import java.io.*;
@SuppressWarnings("unchecked")
//
public class MetaQueueMessage extends CloneableObject implements SerialVersionId
{
    static final long serialVersionUID = SERIAL_VERSION_UID;

    private String topic	="";
    private String textbody	="";
    private String smstext	="";
    private String smsnumber	="";
    private String messagetime	="";
    private String smsapplication="";
    private String pubsource	="";
    private String firstname	="";
    private String lastname	="";
    private String username	="";
    private String email        ="";
    private FileObject attachment = null;

    //--------------------------------------------------------------------------------
    // METHOD   getTopic()
    //
    // PURPOSE:	Get Topic name 
    // RETURN:  String ==> Topic name
    //--------------------------------------------------------------------------------
    public String getTopic()
    {
	return(topic);
    }

    //--------------------------------------------------------------------------------
    // METHOD   setTopic()
    //
    // PURPOSE:	Set Topic name 
    //--------------------------------------------------------------------------------
    public void setTopic(String str)
    {
	topic = str;
    }

    //--------------------------------------------------------------------------------
    // METHOD   getText()
    //
    // PURPOSE:	Get Text body 
    // RETURN:  String ==> Text body
    //--------------------------------------------------------------------------------
    public String getText()
    {
	return(textbody);
    }

    //--------------------------------------------------------------------------------
    // METHOD   setTopic()
    //
    // PURPOSE:	Set text
    //--------------------------------------------------------------------------------
    public void setText(String str)
    {
	textbody = str;
    }

    //--------------------------------------------------------------------------------
    // METHOD   getSmsText()
    //
    // PURPOSE:	Get SMS Text body 
    // RETURN:  String ==> SMS text
    //--------------------------------------------------------------------------------
    public String getSmsText()
    {
	return(smstext);
    }


    //--------------------------------------------------------------------------------
    // METHOD   setSmstext()
    //
    // PURPOSE:	Set text
    //--------------------------------------------------------------------------------
    public void setSmsText(String str)
    {
	smstext = str;
	textbody= str;   // Might as well set this too :-)
    }

    //--------------------------------------------------------------------------------
    // METHOD   getSmsNumber()
    //
    // PURPOSE:	Get SMS phone number 
    // RETURN:  String ==> SMS phone number
    //--------------------------------------------------------------------------------
    public String getSmsNumber()
    {
	return(smsnumber);
    }

    //--------------------------------------------------------------------------------
    // METHOD   setSmsNumber()
    //
    // PURPOSE:	Set SMS number
    //--------------------------------------------------------------------------------
    public void setSmsNumber(String str)
    {
	smsnumber = str;
    }


    //--------------------------------------------------------------------------------
    // METHOD   getMessageTime()
    //
    // PURPOSE:	Get  Time/Date of message sent
    // RETURN:  String ==> Time/Date
    //--------------------------------------------------------------------------------
    public String getMessageTime()
    {
	return(messagetime);
    }


    //--------------------------------------------------------------------------------
    // METHOD   setMessageTime()
    //
    // PURPOSE:	Set text
    //--------------------------------------------------------------------------------
    public void setMessageTime(String str)
    {
	messagetime = str;
    }


    //--------------------------------------------------------------------------------
    // METHOD   getSmsApplication()
    //
    // PURPOSE:	Get SMS Broker carrier type
    // RETURN:  String ==> SMS Broker
    //--------------------------------------------------------------------------------
    public String getSmsApplication()
    {
	return(smsapplication);
    }

    //--------------------------------------------------------------------------------
    // METHOD   setSmsApplication()
    //
    // PURPOSE:	Set text
    //--------------------------------------------------------------------------------
    public void setSmsApplication(String str)
    {
	smsapplication = str;
    }

    //--------------------------------------------------------------------------------
    // METHOD   getPubSource()
    //
    // PURPOSE:	Get Publisher source application
    // RETURN:  String ==> Publisher source
    //--------------------------------------------------------------------------------
    public String getPubSource()
    {
	return(pubsource);
    }

    //--------------------------------------------------------------------------------
    // METHOD   setPubSource()
    //
    // PURPOSE:	Set text
    //--------------------------------------------------------------------------------
    public void setPubSource(String str)
    {
	pubsource = str;
    }

    //--------------------------------------------------------------------------------
    // METHOD   getAttachment()
    //
    // PURPOSE:	Get  FileObject attachment
    // RETURN:  FileObject ==> attachment
    //--------------------------------------------------------------------------------
    public FileObject getAttachment()
    {
	return(attachment);
    }

    //--------------------------------------------------------------------------------
    // METHOD   setAttachment()
    //
    // PURPOSE:	Set FileObject Attachment
    //--------------------------------------------------------------------------------
    public void setAttachment(FileObject fo)
    {
	attachment = fo;
    }



    //--------------------------------------------------------------------------------
    // METHOD   getSenderFirstName()
    //
    // PURPOSE:	Get sender first name
    // RETURN:  String ==> first name
    //--------------------------------------------------------------------------------
    public String getSenderFirstName()
    {
	return(firstname);
    }

    //--------------------------------------------------------------------------------
    // METHOD   setSenderFirstName()
    //
    // PURPOSE:	Set text
    //--------------------------------------------------------------------------------
    public void setSenderFirstName(String str)
    {
	firstname = str;
    }

    //--------------------------------------------------------------------------------
    // METHOD   getSenderLastName()
    //
    // PURPOSE:	Get sender last name
    // RETURN:  String ==> last name
    //--------------------------------------------------------------------------------
    public String getSenderLastName()
    {
	return(lastname);
    }

    //--------------------------------------------------------------------------------
    // METHOD   setSenderLastName()
    //
    // PURPOSE:	Set text
    //--------------------------------------------------------------------------------
    public void setSenderLastName(String str)
    {
	lastname = str;
    }

    //--------------------------------------------------------------------------------
    // METHOD   getSenderUserName()
    //
    // PURPOSE:	Get sender username
    // RETURN:  String ==> username
    //--------------------------------------------------------------------------------
    public String getSenderUserName()
    {
	return(username);
    }

    //--------------------------------------------------------------------------------
    // METHOD   setSenderUserName()
    //
    // PURPOSE:	Set text
    //--------------------------------------------------------------------------------
    public void setSenderUserName(String str)
    {
	username = str;
    }

    //--------------------------------------------------------------------------------
    // METHOD   getSenderEmail()
    //
    // PURPOSE:	Get sender email address
    // RETURN:  String ==> email address
    //--------------------------------------------------------------------------------
    public String getSenderEmail()
    {
	return(email);
    }

    //--------------------------------------------------------------------------------
    // METHOD   setSenderEmail()
    //
    // PURPOSE:	Set text
    //--------------------------------------------------------------------------------
    public void setSenderEmail(String str)
    {
	email = str;
    }


    //--------------------------------------------------------------------------------
    // METHOD   setQMessage()
    //
    // PURPOSE:	Creates an Attachment and place QMessage into it
    //--------------------------------------------------------------------------------
    public void setQMessage(QMessage m, String filename)
    {
	try
	{
	    //-----------------------------------------
	    // I've always found this code bizzare...
	    // But, I think it's the only way :-)
	    //------------------------------------------
	    ByteArrayOutputStream bostream = new ByteArrayOutputStream();
	    ObjectOutputStream    output   = new ObjectOutputStream(bostream);
	    output.writeObject(m);
            output.flush();

	    byte[] image = bostream.toByteArray();
	    long   size  = image.length;
	    FileObject fo= new FileObject(filename, 
					  "application/octet-stream",
					  "/",
					  image,
					  size);
	    //-----------------------------------
	    // Set FileObject
	    //-----------------------------------
	    setAttachment(fo);
	}
	catch(Exception e)
	{
	}
    }


    //--------------------------------------------------------------------------------
    // METHOD   getQMessage()
    //
    // PURPOSE:	Get QMessage from Attachment 
    //--------------------------------------------------------------------------------
    public QMessage getQMessage()
    {
	QMessage qmessage = null;
	FileObject fo = getAttachment();

	if(fo != null)
	{
	    try
	    {
	    byte[] buff = fo.getImage();
	    ByteArrayInputStream bistream = new ByteArrayInputStream(buff);
            ObjectInputStream    input	  = new ObjectInputStream(bistream);

	    Object o = input.readObject();

	    if(o instanceof QMessage)
	    {
		qmessage = (QMessage)o;
	    }
	    else
	    {
		qmessage = new QMessage();
	    }
	    }
	    catch(Exception e)
	    {
		qmessage = new QMessage();
	    } 
	}

	return(qmessage);
    }

    //--------------------------------------------------------------------------------
    // METHOD   MetaQueueMessage()
    //
    // PURPOSE: Constructor
    // RETURN:  N/A
    //--------------------------------------------------------------------------------
    public MetaQueueMessage()
    {

    }
}

