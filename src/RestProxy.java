package com.qkernel;
/**
 * Rest object performs all http/rest functions 
 * for qkernel API's 
 *
 * History:
 * --------
 * 19-06-04 mgill  Initial Creation based on TPJ Rest.java 
 *
 */
import java.lang.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Base64;

/**
 * {@code RestProxy} class performs all http/rest functions for qkernel API's 
 * <p>
 * Supported methods are get(), post() and getDataBytes() for 
 * raw datas such as MP3's and photo images 
 *
 */
public class RestProxy extends Object
{
    private String url    ="";
    private String auth   ="";
    private Daemon daemon = null;
    
   /**
    * Set url string 
    *
    * @param  url
    */
    public void setUrl(String u)
    {
        url = u;
    }

   /**
    * Get url string 
    *
    * @return  url
    */
    public String getUrl()
    {
	return(url);
    }
    
   /**
    * Set AUTH string 
    *
    * @param  AUTH string
    */
    public void setAuth(String a)
    {
	auth = a;
    }
    
   /**
    * Get AUTH string 
    *
    * @return  AUTH
    */
    public String getAuth()
    {
	return(auth);
    }

    
   /**
    * post request 
    *
    * @param  api_function
    * @param  params
    */
    public String post(String api_function, QMessage params)
    {
	log("POST request function:"+api_function);
        try
        {
            String url = this.getUrl()+api_function;
            URL obj    = new URL(url);
	    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setDoOutput(true);
            /*if(!this.getAuth().equals(""))
	    {
		String encoding = Base64.getEncoder().encodeToString((this.getAuth()).getBytes("UTF-8"));
	        con.setRequestProperty("Authorization", "Basic "+encoding);
		log("Basic "+encoding);
		}*/
            if(!this.getAuth().equals(""))
	    {
		String encoding = this.getAuth();
	        con.setRequestProperty("Authorization", "Basic "+encoding);
		log("Basic "+encoding);
	    }
            con.setRequestMethod("POST");
	    con.setRequestProperty("User-Agent","Java/1.8");
	    con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
	    con.setRequestProperty("Content-Type", "application/json");
            String urlParameters = "";
            String rawData = params.getString("RAWDATA");
	    DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            if(rawData.equals(""))
	    {
	        Set set = params.entrySet();
	        Iterator it = set.iterator();
	        while (it.hasNext())
	        {
	            Map.Entry entry = (Map.Entry) it.next();
                    urlParameters = urlParameters+entry.getKey()+"="+entry.getValue()+"&";
	        }
	        log("URL parameters: "+urlParameters);	    
	        wr.writeBytes(urlParameters);
	    }
	    else
	    {
	        log("Body data: "+rawData);
		wr.writeBytes(rawData);
	    }
	    wr.flush();
	    wr.close();

	    int responseCode  = con.getResponseCode();
	    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	    String inputLine;
	    StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null)
	    {
	        response.append(inputLine);
	    }
	    in.close();
	    log("Response OK");
            return(response.toString());
        }
	catch(Exception e)
	{
	    //log("errorCode:500 Server Error");
	    //daemon.event_log.sendMessage(e);
	    daemon.event_log.sendMessage(e.getMessage());
	    return("{\"errorCode\":\"500\", \"errorMessage\":\"REST Server Error\"}");
	}
    }

    /**
    * put request 
    *
    * @param  api_function
    * @param  params
    */
    public String put(String api_function, QMessage params)
    {
	log("PUT request function:"+api_function);
        try
        {
            String url = this.getUrl()+api_function;
            URL obj    = new URL(url);
	    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            if(!this.getAuth().equals(""))
	    {
		String encoding = Base64.getEncoder().encodeToString((this.getAuth()).getBytes("UTF-8"));
	        con.setRequestProperty("Authorization", "Basic "+encoding);
	    }
            con.setRequestMethod("PUT");
	    con.setRequestProperty("User-Agent","Java/1.8");
	    con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
	    con.setRequestProperty("Content-Type", "application/json");
            String urlParameters = "";
	    Set set = params.entrySet();
	    Iterator it = set.iterator();
	    while (it.hasNext())
	    {
	        Map.Entry entry = (Map.Entry) it.next();
                urlParameters = urlParameters+entry.getKey()+"="+entry.getValue()+"&";
	    }
	    log("URL parameters: "+urlParameters);
	    con.setDoOutput(true);
	    DataOutputStream wr = new DataOutputStream(con.getOutputStream());
	    wr.writeBytes(urlParameters);
	    wr.flush();
	    wr.close();

	    int responseCode  = con.getResponseCode();
	    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	    String inputLine;
	    StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null)
	    {
	        response.append(inputLine);
	    }
	    in.close();
	    log("Response OK");
            return(response.toString());
        }
	catch(Exception e)
	{
	    log("errorCode:500 Server Error");
	    return("{\"errorCode\":\"500\", \"errorMessage\":\"REST Server Error\"}");
	}	
    }
    
    
   /**
    * get request 
    *
    * @param  api_function
    * @param  params
    */
    public String get(String api_function, QMessage params)
    {
        log("GET request function: "+api_function);
        try
        {
	    String url =getUrl()+api_function;
	    String urlParameters;
	    if(!params.isEmpty())
		urlParameters ="?";
	    else
		urlParameters ="";
	    Set set = params.entrySet();
	    Iterator it = set.iterator();
	    while (it.hasNext())
	    {
	        Map.Entry entry = (Map.Entry) it.next();
	        urlParameters = urlParameters+entry.getKey()+"="+entry.getValue()+"&";
	    }
	    url = url+urlParameters;
            URL obj = new URL(url);
	    log("URL parameters: "+urlParameters);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            if(!this.getAuth().equals(""))
	    {
		String encoding = Base64.getEncoder().encodeToString((this.getAuth()).getBytes("UTF-8"));
	        con.setRequestProperty("Authorization", "Basic "+encoding);
	    }
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Java/1.8");
            StringBuffer response = new StringBuffer();
            int responseCode = con.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK)
	    {	
                BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()));
                String inputLine;

                while ((inputLine = in.readLine()) != null)
                {
                    response.append(inputLine);
                }
                in.close();
	        log("Response OK");
                return(response.toString());
	    }
	    else
	    {
	        log("Response FAIL");
                return(response.toString());		
	    }
	}
	catch(Exception e)
	{
	    //log("errorCode:500 Server Error");
	    daemon.event_log.sendMessage(e);
	    return("{\"errorCode\":\"500\", \"errorMessage\":\"REST Server Error\"}");
	}
    }

   /**
    * delete request 
    *
    * @param  api_function
    * @param  params
    */
    public String delete(String api_function, QMessage params)
    {
        log("DELETE request function: "+api_function);
        try
        {
	    String url =getUrl()+api_function;
	    String urlParameters;
	    if(!params.isEmpty())
		urlParameters ="?";
	    else
		urlParameters ="";
	    Set set = params.entrySet();
	    Iterator it = set.iterator();
	    while (it.hasNext())
	    {
	        Map.Entry entry = (Map.Entry) it.next();
	        urlParameters = urlParameters+entry.getKey()+"="+entry.getValue()+"&";
	    }
	    url = url+urlParameters;
            URL obj = new URL(url);
	    log("URL parameters: "+urlParameters);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            if(!this.getAuth().equals(""))
	    {
		String encoding = Base64.getEncoder().encodeToString((this.getAuth()).getBytes("UTF-8"));
	        con.setRequestProperty("Authorization", "Basic "+encoding);
	    }
            con.setRequestMethod("DELETE");
            con.setRequestProperty("User-Agent", "Java/1.8");
            int responseCode = con.getResponseCode();
            BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null)
            {
                response.append(inputLine);
            }
            in.close();
	    log("Response OK");
            return(response.toString());
	}
	catch(Exception e)
	{
	    log("errorCode:500 Server Error");
	    return("{\"errorCode\":\"500\", \"errorMessage\":\"REST Server Error\"}");
	}
    }
    
   /**
    * getDatabytes -- called to to get non-REST functions that return raw data
    *
    * @param  api_function
    * @param  params
    */
    public byte[] getDataBytes(String api_function, QMessage params)
    {
        log("RAW DATA request function: "+api_function);
        try
        {
            String url =getUrl()+api_function;
            String urlParameters ="?";
            Set set = params.entrySet();
            Iterator it = set.iterator();
            while (it.hasNext())
            {
                Map.Entry entry = (Map.Entry) it.next();
                urlParameters = urlParameters+entry.getKey()+"="+entry.getValue()+"&";
            }
            url = url+urlParameters;
	    log("URL parameters: "+url);
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Java/1.8");
            if(!this.getAuth().equals(""))
                con.setRequestProperty("Authorization", this.getAuth());

            int responseCode = con.getResponseCode();

            InputStream in = con.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int reads = in.read();
            while(reads != -1){
                baos.write(reads);
                reads = in.read();
            }
	    log("Response OK");
            return(baos.toByteArray());
        }
        catch(Exception e)
        {
	    log("errorCode:500 Server Error");
            return(null);
        }
    }

   /**
    * RestProxy logger 
    *
    * @param  event string
    */
    protected void log(String e)
    {
	if(daemon != null)
	    daemon.eventLog.sendMessage(e);	    
	else
            System.out.println(e);
    }
    
   /**
    * RestProxy constructor 
    *
    * @param  remote service url
    * @param  reference to Daemon object
    *
    */
    public RestProxy(String u, Daemon d)
    {
	daemon = d;
        url    = u;
	auth   = "";
    }

   /**
    * RestProxy constructor 
    *
    * @param  remote service url
    * @param  AUTH string with username:password
    * @param  reference to Daemon object
    *
    */
    public RestProxy(String u, String a, Daemon d)
    {
	daemon = d;
	url    = u;
	auth   = a;
    }

   /**
    * RestProxy constructor 
    *
    * @param  reference to Daemon object
    *
    */
    public RestProxy(Daemon d)
    {
	daemon = d;
        url ="";
	auth="";
    }

   /**
    * RestProxy constructor 
    *
    * @param  remote service url
    *
    */
    public RestProxy(String u)
    {
	daemon = null;
        url    = u;
	auth   = "";
    }

   /**
    * RestProxy constructor 
    *
    * @param  remote service url
    * @param  AUTH string with username:password
    *
    */
    public RestProxy(String u, String a)
    {
	daemon = null;
	url    = u;
	auth   = a;
    }

   /**
    * RestProxy constructor 
    *
    */
    public RestProxy()
    {
	daemon = null;
        url ="";
	auth="";
    }
}
