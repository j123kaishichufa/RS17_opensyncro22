/* OpenSyncro - A web-based enterprise application integration tool
 * Copyright (C) 2008 Smilehouse Oy, support@opensyncro.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

/*
 * Created on 04.01.2005
 * 
 * 
 */
package smilehouse.opensyncro.defaultcomponents.http;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import smilehouse.util.Utils;


public class HTTPUtils {
	
	
	private static final String SOAPMESSAGE = "SOAPMessage";
	private static final String SOAP_ACTION_HEADER = "SOAPAction";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String DEFAULT_CONTENT_TYPE = "text/xml";

	/**
	 * Makes a HTTP request to the specified url with a set of parameters,
     * request method, user name and password
	 * 
	 * @param url the <code>URL</code> to make a request to 
	 * @param method either "GET", "POST", "PUT" or "SOAP"
	 * @param parameters two dimensional string array containing parameters to send in the request
	 * @param user user name to submit in the request 
	 * @param password password to submit in the request
	 * @param charsetName charset name used for message content encoding
     * @param responseCharsetName charset name used to decode HTTP responses,
     *                            or null to use default ("ISO-8859-1")
     * @param contentType Content-Type header value (without charset information)
     *                    for POST (and SOAP) type requests. If null, defaults to
     *                    "text/xml".
	 * @return a string array containing the body of the response, the headers of
     *         the response and possible error message
	 * @throws Exception 
	 */
	public static HTTPResponse makeRequest(URL url,
										   String method,
										   String[][] parameters,
										   String user,
										   String password,
										   String charsetName,
										   String responseCharsetName,
                                           String contentType) throws Exception  {
		
		HttpClient httpclient=new HttpClient();
       
		HttpMethod request_method=null;
		HTTPResponse responseData=new HTTPResponse();
		NameValuePair[] names_values=null;
        
        String requestContentType;
        if( contentType != null && contentType.length() > 0 ) {
            requestContentType = contentType;
        } else {
            requestContentType = DEFAULT_CONTENT_TYPE;
        }
		
		if(parameters!=null&&method.equals("PUT")==false){
			names_values=new NameValuePair[parameters.length];
			
			for(int i=0;i<parameters.length;i++){
				names_values[i]=new NameValuePair(parameters[i][0],parameters[i][1]);
			}
	
		}
		if (method.equalsIgnoreCase("POST")){
			request_method=new PostMethod(url.toString());
			if(names_values!=null) ((PostMethod)request_method).setRequestBody(names_values);
		}
		else if(method.equalsIgnoreCase("PUT")){
			if(parameters==null) throw new Exception("No data to use in PUT request");
			request_method=new PutMethod(url.toString());
			StringRequestEntity sre=new StringRequestEntity(parameters[0][0]);
			((PutMethod)request_method).setRequestEntity(sre);
		}
		else if(method.equalsIgnoreCase("SOAP")){
			String urlString=url.toString()+"?";
			String message=null;
			String action=null;
			for(int i=0;i<parameters.length;i++){

				if(parameters[i][0].equals(SOAPMESSAGE))
					message=parameters[i][1];
				else if(parameters[i][0].equals(SOAP_ACTION_HEADER))
					action=parameters[i][1];
				else
					urlString+=parameters[i][0]+"="+parameters[i][1]+"&";
			}
			urlString=urlString.substring(0, urlString.length()-1);
			request_method=new PostMethod(urlString);
            // Encoding content with requested charset 
			StringRequestEntity sre=new StringRequestEntity(message,
                                                            requestContentType,
                                                            charsetName);
			((PostMethod)request_method).setRequestEntity(sre);
			if(action!=null) {
				request_method.setRequestHeader(SOAP_ACTION_HEADER, action);
            }
            // Adding charset also into header's Content-Type
			request_method.addRequestHeader(CONTENT_TYPE_HEADER,
                                            requestContentType + "; charset=" + charsetName);
		}
		else{
			request_method=new GetMethod(url.toString());
			if(names_values!=null) ((GetMethod)request_method).setQueryString(names_values);
		}
		
		user=(user==null||user.length()<1)?null:user;
		password=(password==null||password.length()<1)?null:password;
		
		if((user!=null&password==null)||(user==null&password!=null)){
			throw new Exception("Invalid username or password");
			
		}
		if(user!=null&&password!=null){
			httpclient.getParams().setAuthenticationPreemptive(true);
			Credentials defaultcreds = new UsernamePasswordCredentials(user, password);
			httpclient.getState().setCredentials(new AuthScope(url.getHost(), 
					url.getPort(), AuthScope.ANY_REALM), defaultcreds);
			request_method.setDoAuthentication(true);
		}
		
		try{
			httpclient.executeMethod(request_method);
			
			if(request_method.getStatusCode()!=HttpStatus.SC_OK){
				responseData.setResponseError(request_method.getStatusCode()+" "+request_method.getStatusText());
			}
			
			//Write response header to the out string array
			Header[] headers=request_method.getResponseHeaders();
			responseData.appendToHeaders("\nHTTP status "+request_method.getStatusCode()+"\n");
			for(int i=0;i<headers.length;i++){
				responseData.appendToHeaders(headers[i].getName()+": "+headers[i].getValue()+"\n");
			}
			
			/*
			 * TODO: By default, the response charset should be read from the Content-Type header of 
			 * the response and that should be used in the InputStreamReader constructor: 
			 * 
			 * <code>new InputStreamReader(request_method.getResponseBodyAsStream(), 
			 * 						((HttpMethodBase)request_method).getResponseCharSet());</code>
			 * 
			 * But for backwards compatibility, the charset used by default is now the one that the 
			 * HttpClient library chooses (ISO-8859-1). An alternative charset can be chosen, but in 
			 * no situation is the charset read from the response's Content-Type header.
			 */
			BufferedReader br = null;
			if (responseCharsetName != null) {
				br = new BufferedReader(new InputStreamReader(request_method.getResponseBodyAsStream(), responseCharsetName));
			} else {
				br = new BufferedReader(new InputStreamReader(request_method.getResponseBodyAsStream()));
			}
			
			String responseline;
			//Write response body to the out string array
			while((responseline=br.readLine())!=null){
				responseData.appendToBody(responseline+"\n");
			}
		}
		finally{
			request_method.releaseConnection();
		}
		return responseData;
	}
	/**
	 * Makes a HTTP request to the specified url using the specified method, user name and password
	 * 
	 * @param url the <code>URL</code> to make a request to 
	 * @param method either "GET", "POST, "PUT" or "SOAP"
	 * @param user user name to submit in the request 
	 * @param pass password to submit in the request
     * @param charsetName charset name used for message content encoding
	 * @return a string array containing the body of the response, the headers of the response and possible error message
	 */
	public static HTTPResponse makeRequest(URL url,String method,String user,String pass,String charsetName) throws Exception {
		return makeRequest(url, method, null, user, pass, charsetName, null, null);
	}
	/**
	 * Makes a HTTP request to the specified url using the specified method.
	 * 
	 * @param url the <code>URL</code> to make a request to 
	 * @param method either "GET", "POST", "PUT" or "SOAP"
	 * @return a string array containing the body of the response, the headers of the response and possible error message
	 */
	public static HTTPResponse makeRequest(URL url,String method) throws Exception {
		return makeRequest(url, method, null, null, null, null, null, null);
	}

	/**
	 * Splits the input string, first at each occurence of <b>delimiter</b> then at the first 
	 * occurence of <b>inline_delimiter</b>. 
	 * @param namevaluepairs the <code>URL</code> to make a request to
	 * @param delimiter
	 * @param inline_delimiter 
	 * @return a two dimesional string containing names and values
	 */
	public static String[][] splitParameters(String namevaluepairs,
			 String delimiter, String inline_delimiter){
		//return null if no input data or if inline_delimiter is not present in the input string
		if(namevaluepairs==null||namevaluepairs.length()<1||
				namevaluepairs.indexOf(inline_delimiter)==-1) return null;
		
		String[] splitparameters;
		
		splitparameters=Utils.split(namevaluepairs, delimiter);
		String[] single;
		String[][] parameters=new String[splitparameters.length][2];
		try{
			for (int i=0;i<splitparameters.length;i++){
				single=splitparameters[i].split(inline_delimiter,2);
				parameters[i][0]=single[0];
				parameters[i][1]=single[1];
			}
		}
		catch(ArrayIndexOutOfBoundsException ex){
			return null;
		}
		return parameters;
	}

	
}
