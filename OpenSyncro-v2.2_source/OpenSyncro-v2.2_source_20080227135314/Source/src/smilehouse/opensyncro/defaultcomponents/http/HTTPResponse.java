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
 * Created on 28.06.2006
 * 
 * 
 */
package smilehouse.opensyncro.defaultcomponents.http;


public class HTTPResponse {
	
	private String responseBody;
	private String responseHeaders;
	private String responseError;
	
	/**Full constructor
	 *
	 * @param body Body of the response
	 * @param headers Headers of the response
	 * @param error Possible errors
	 */
	public HTTPResponse(String body, String headers,String error){
		this.responseBody=body;
		this.responseHeaders=headers;
		this.responseError=error;
	}
	
	/**
	 * Empty constructor
	 */
	public HTTPResponse(){}
	
	/**
	 * @return Body of the response
	 */
	public String getResponseBody() {
		return responseBody;
	}
	
	/**
	 * @param responseBody Body of the response
	 */
	public void setResponseBody(String responseBody) {
		this.responseBody = responseBody;
	}
	
	/**
	 * @return Error code in the response
	 */
	public String getResponseError() {
		return responseError;
	}
	
	/**
	 * @param responseError Error in the response
	 */
	public void setResponseError(String responseError) {
		this.responseError = responseError;
	}
	
	/**
	 * @return Headers of the response
	 */
	public String getResponseHeaders() {
		return responseHeaders;
	}
	
	/**
	 * @param responseHeaders Headers of the response
	 */
	public void setResponseHeaders(String responseHeaders) {
		this.responseHeaders = responseHeaders;
	}
	
	/**
	 * @param header Header to append to response headers
	 */
	public void appendToHeaders(String header){
		if(this.responseHeaders==null)
			responseHeaders=header;
		else
			responseHeaders+=header;
	}
	/**
	 * @param row Row to append to response body
	 */
	public void appendToBody(String row){
		if(this.responseBody==null)
			responseBody=row;
		else
			responseBody+=row;
	}
}
