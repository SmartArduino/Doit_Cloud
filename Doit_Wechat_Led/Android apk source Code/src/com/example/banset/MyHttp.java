package com.example.banset;

import java.io.IOException;
 
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost; 
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
 

public class MyHttp {
	private String uri;
	
	public MyHttp(String uri){
		this.uri=uri; 
	}
	
	public String httpGet(){ 
		String strResult = new String(); 
		HttpParams params=new BasicHttpParams();
		params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 50000).
			setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 50000).
			setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, true);

	    DefaultHttpClient httpclient=new DefaultHttpClient(); 
	    HttpGet httpRequest = new HttpGet(uri);
	    httpRequest.setHeader("Referer", uri);
	    httpclient.setParams(params); 
	    try {          
	        HttpResponse httpResponse = httpclient.execute(httpRequest);  
			if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK)	        	 
	        	strResult = EntityUtils.toString(httpResponse.getEntity()); 	 
				//strResult=Uri.decode(strResult);
	    }  
	    catch (ClientProtocolException e) { 
	    	strResult = null;
	    } 
	    catch (IOException e) {              
	    	strResult = null;
	    } 
	    catch (Exception e) {   
	    	strResult = null;
	    }
	    return strResult;
	}
	
	
	public String httpPost(byte[] post){  
	    String strResult=new String(); 
		HttpParams params=new BasicHttpParams();
		
//		 HttpConnectionParams.setConnectionTimeout(params,  20000);
//		 HttpConnectionParams.setSoTimeout(params, 20000);   
		
		params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 50000).
			setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 50000).
			setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, true);

	    DefaultHttpClient httpclient=new DefaultHttpClient();  
	    HttpPost httpRequest = new HttpPost(uri);
	    httpclient.setParams(params);
	    try {          
	    	if(post.length<=0){
	    		post="a=a".getBytes();
	    	}
	    	ByteArrayEntity reqEntity=new ByteArrayEntity(post); 
	    	httpRequest.setHeader( "Content-Type", "application/x-www-form-urlencoded");
	        httpRequest.setEntity(reqEntity); 
	        HttpResponse httpResponse = httpclient.execute(httpRequest); 
	        if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
	            strResult = EntityUtils.toString(httpResponse.getEntity()); 	
	            //strResult=Uri.decode(strResult);  
	        }
	    } 
	    catch (ClientProtocolException e) { 
	    	strResult = null;
	    } 
	    catch (IOException e) {                 
	    	strResult = null;
	    } 
	    catch (Exception e) {                   
	    	strResult = null;
	    }
	    
	    return strResult;
	}
	
	
	public String httpPost(){  
	    String strResult=new String(); 
		HttpParams params=new BasicHttpParams();
		
//		 HttpConnectionParams.setConnectionTimeout(params,  20000);
//		 HttpConnectionParams.setSoTimeout(params, 20000);
		 
		params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 50000).
			setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 50000).
			setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, true);

	    DefaultHttpClient httpclient=new DefaultHttpClient();  
	    HttpPost httpRequest = new HttpPost(uri);
	    httpclient.setParams(params);
	    try {          
	    	byte[] post="a=a".getBytes();
	    	ByteArrayEntity reqEntity=new ByteArrayEntity(post); 
	    	httpRequest.setHeader( "Content-Type", "application/x-www-form-urlencoded");
	        httpRequest.setEntity(reqEntity); 
	        HttpResponse httpResponse = httpclient.execute(httpRequest); 
	        if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
	            strResult = EntityUtils.toString(httpResponse.getEntity()); 	
	            //strResult=Uri.decode(strResult);  
	        }

	    } 
	    catch (ClientProtocolException e) { 
	    	strResult = null;
	    } 
	    catch (IOException e) {                 
	    	strResult = null;
	    } 
	    catch (Exception e) {                   
	    	strResult = null;
	    }
	    
	    return strResult;
	}
}
