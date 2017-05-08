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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.SSLProtocolSocketFactory;

import smilehouse.gui.html.fieldbased.FieldInfo;
import smilehouse.gui.html.fieldbased.GUIContext;
import smilehouse.gui.html.fieldbased.editor.BooleanEditor;
import smilehouse.gui.html.fieldbased.editor.DefaultSelectOption;
import smilehouse.gui.html.fieldbased.editor.PasswordEditor;
import smilehouse.gui.html.fieldbased.editor.SelectEditor;
import smilehouse.gui.html.fieldbased.editor.TextAreaEditor;
import smilehouse.gui.html.fieldbased.formatter.IntegerFormatter;
import smilehouse.gui.html.fieldbased.model.DefaultModelModifier;
import smilehouse.gui.html.fieldbased.model.ModelModifier;
import smilehouse.opensyncro.pipes.component.AbortTransferException;
import smilehouse.opensyncro.pipes.component.DestinationIF;
import smilehouse.opensyncro.pipes.component.FailTransferException;
import smilehouse.opensyncro.pipes.component.PipeComponentData;
import smilehouse.opensyncro.pipes.component.PipeComponentUtils;
import smilehouse.opensyncro.pipes.gui.GUIConfigurationIF;
import smilehouse.opensyncro.pipes.gui.GUIDefinition;
import smilehouse.opensyncro.pipes.log.MessageLogger;
import smilehouse.opensyncro.pipes.metadata.DestinationInfo;
import smilehouse.opensyncro.system.Environment;

public class HTTPDestination implements DestinationIF, GUIConfigurationIF {

    protected static final String HOST_ATTR = "host";
    protected static final String PORT_ATTR = "port";
    protected static final String USER_ATTR = "user";
    protected static final String PASSWORD_ATTR = "password";
    protected static final String PATH_ATTR = "path";
    protected static final String NAME_ATTR= "name";
    protected static final String REQUEST_METHOD_ATTR="method";
    protected static final String PARAMETERS_ATTRIBUTE = "param";
    protected static final String[] REQUEST_METHOD_LABELS={"GET","POST","PUT","SOAP"};
    protected static final String[] REQUEST_PROTOCOL_LABELS={"HTTP","HTTPS"};
    protected static final String REQUEST_PROTOCOL_ATTR="protocol";
    protected static final int REQUEST_PROTOCOL_HTTP = 0;
	protected static final int REQUEST_METHOD_GET = 0;
	protected static final String ACCEPTSELFSIGNEDCERTIFICATES_ATTR = "acceptselfsignedcertificates";
	protected static final String SOAPACTION_ATTR = "soapaction";
	protected static final String LOG_ATTR = "logenabled";
	protected static final String CHOOSE_RESPONSE_CHARSET_ATTR = "chooseresponsecharset";
    protected static final String DISABLE_RESPONSE_CODE_CHECK_ATTR = "disableresponsecodecheck";
    protected static final String CHARSET_ATTR = "charset";
    protected static final String RESPONSE_CHARSET_ATTR = "responsecharset";
    protected static final String CONTENT_TYPE_ATTR = "contenttype";
    
    protected static final String DEFAULT_CHARSET = "ISO-8859-1";

    protected static final String[] CHARSETS = PipeComponentUtils.getCharacterSetArray();
    
    
    //  --------------
    // GUI definition
    // --------------
    protected static HTTPDestinationGUI gui = new HTTPDestinationGUI();

    protected static class HTTPDestinationGUI extends GUIDefinition {

        public HTTPDestinationGUI() {
            try {
                addSimpleTextFieldForComponent(HOST_ATTR, HOST_ATTR, 40);
                addSimpleTextFieldForComponent(PORT_ATTR, PORT_ATTR, 10);
                addSimpleTextFieldForComponent(USER_ATTR, USER_ATTR, 10);
                {
                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws FailTransferException, AbortTransferException {
                            return "";
                            //return ((HTTPDestination) model).getAttribute(PASSWORD_ATTR);
                        }

                        public void setModelValue(Object model, Object value) throws FailTransferException, AbortTransferException {
                            String valueStr = (String) value;
                            if(valueStr != null && valueStr.length() > 0)
                                ((HTTPDestination) model).data.setAttribute(PASSWORD_ATTR, valueStr);
                        }
                    };

                    PasswordEditor editor = new PasswordEditor();
                    editor.setSize(10);

                    FieldInfo fieldInfo = new FieldInfo(
                        PASSWORD_ATTR,
                        PASSWORD_ATTR,
                        modifier,
                        editor);

                    //add the configuration to the context for usage in the http-requests.
                    addField(PASSWORD_ATTR, fieldInfo);
                }
                {
                	  String id = "param";
                      String label = "param";
                	   ModelModifier modifier = new DefaultModelModifier() {

                           public Object getModelValue(Object model) throws Exception {
                               return ((HTTPDestination) model).getParameters();
                           }

                           public void setModelValue(Object model, Object value) throws Exception {
                               ((HTTPDestination) model).setParameters((String) value);
                           }

                       };

                       TextAreaEditor editor = new TextAreaEditor();
                       editor.setCols(70);
                       editor.setRows(20);
                       FieldInfo fieldInfo = new FieldInfo(id, label, modifier, editor);

                       //add the configuration to the context for usage in the http-requests.
                       addField(id, fieldInfo);

                }
                {
                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws FailTransferException, AbortTransferException {
                            return new Integer(((HTTPDestination) model).getRequestMethod());
                        }

                        public void setModelValue(Object model, Object value) throws FailTransferException, AbortTransferException {
                            ((HTTPDestination) model).setRequestMethod(((Integer) value).intValue());
                        }
                    };                    

                    SelectEditor editor = new SelectEditor();
                    for(int i = 0; i < REQUEST_METHOD_LABELS.length; i++)
                        editor.addOption(new DefaultSelectOption(
                            new Integer(i),
                            REQUEST_METHOD_LABELS[i]));                    

                    editor.setFormatter(new IntegerFormatter());
                    
                    //and finally create the configurationObject
                    FieldInfo fieldInfo = new FieldInfo(
                        REQUEST_METHOD_ATTR,
                        REQUEST_METHOD_ATTR,
                        modifier,
                        editor);                    

                    //add the configuration to the context for usage in the http-requests.
                    addField(REQUEST_METHOD_ATTR, fieldInfo);                    
                }
                
                {
                	ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws FailTransferException, AbortTransferException {
                            return new Integer(((HTTPDestination) model).getRequestProtocol());
                        }

                        public void setModelValue(Object model, Object value) throws FailTransferException, AbortTransferException {
                            ((HTTPDestination) model).setRequestProtocol(((Integer) value).intValue());
                        }
                    };
                    
                    SelectEditor editor = new SelectEditor();
                    for(int i = 0; i < REQUEST_PROTOCOL_LABELS.length; i++)
                        editor.addOption(new DefaultSelectOption(
                            new Integer(i),
                            REQUEST_PROTOCOL_LABELS[i]));
                    
                    editor.setFormatter(new IntegerFormatter());
                    
                    FieldInfo fieldInfo = new FieldInfo(
                            REQUEST_PROTOCOL_ATTR,
                            REQUEST_PROTOCOL_ATTR,
                            modifier,
                            editor);
                    
                    addField(REQUEST_PROTOCOL_ATTR, fieldInfo);
                }
                
                {
                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            return ((HTTPDestination) model).getAcceptSelfSignedCertificates();
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            ((HTTPDestination) model).setAcceptSelfSignedCertificates((Boolean) value);
                        }
                    };

                    BooleanEditor editor = new BooleanEditor();

                    FieldInfo fieldInfo = new FieldInfo(ACCEPTSELFSIGNEDCERTIFICATES_ATTR, 
                    		ACCEPTSELFSIGNEDCERTIFICATES_ATTR, modifier, editor);

                    addField(ACCEPTSELFSIGNEDCERTIFICATES_ATTR, fieldInfo);
                }
                {
                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            return ((HTTPDestination) model).getLogEnabled();
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            ((HTTPDestination) model).setLogEnabled((Boolean)value);
                        }
                    };

                    BooleanEditor editor = new BooleanEditor();

                    FieldInfo fieldInfo = new FieldInfo(LOG_ATTR, 
                    		LOG_ATTR, modifier, editor);

                    addField(LOG_ATTR, fieldInfo);
                }
                
                {
                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            return ((HTTPDestination) model).getResponseCodeCheckDisabled();
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            ((HTTPDestination) model).setResponseCodeCheckDisabled((Boolean)value);
                        }
                    };

                    BooleanEditor editor = new BooleanEditor();

                    FieldInfo fieldInfo = new FieldInfo(DISABLE_RESPONSE_CODE_CHECK_ATTR, 
                            DISABLE_RESPONSE_CODE_CHECK_ATTR, modifier, editor);

                    addField(DISABLE_RESPONSE_CODE_CHECK_ATTR, fieldInfo);
                }

                
                
                addSimpleTextFieldForComponent(PATH_ATTR, PATH_ATTR, 40);
                addSimpleTextFieldForComponent(NAME_ATTR, NAME_ATTR, 20);
                addSimpleTextFieldForComponent(SOAPACTION_ATTR,SOAPACTION_ATTR, 40);
                addSimpleTextFieldForComponent(CONTENT_TYPE_ATTR,CONTENT_TYPE_ATTR, 40);
                
                
                {
                    //set unique id and description labelkey
                    String id = CHARSET_ATTR;

                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws FailTransferException,
                                AbortTransferException {
                            String value = ((HTTPDestination) model).getData().getAttribute(
                                CHARSET_ATTR);
                            return value != null ? value : DEFAULT_CHARSET;
                        }

                        public void setModelValue(Object model, Object value)
                                throws FailTransferException, AbortTransferException {
                            ((HTTPDestination) model).getData().setAttribute(
                                CHARSET_ATTR,
                                (String) value);
                        }
                    };

                    SelectEditor editor = new SelectEditor();
                    for(int i = 0; i < CHARSETS.length; i++)
                        editor.addOption(new DefaultSelectOption(CHARSETS[i], CHARSETS[i]));

                    //and finally create the configurationObject
                    FieldInfo fieldInfo = new FieldInfo(id, id, modifier, editor);

                    //add the configuration to the context for usage in the http-requests.
                    addField(id, fieldInfo);
                }
                
                {
                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            return ((HTTPDestination) model).getChosenResponseCharset();
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            ((HTTPDestination) model).setChosenResponseCharset((Boolean)value);
                        }
                    };

                    BooleanEditor editor = new BooleanEditor();

                    FieldInfo fieldInfo = new FieldInfo(CHOOSE_RESPONSE_CHARSET_ATTR, 
                    		CHOOSE_RESPONSE_CHARSET_ATTR, modifier, editor);

                    addField(CHOOSE_RESPONSE_CHARSET_ATTR, fieldInfo);
                }
                
                {
                    //set unique id and description labelkey
                    String id = RESPONSE_CHARSET_ATTR;

                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws FailTransferException,
                                AbortTransferException {
                            String value = ((HTTPDestination) model).getData().getAttribute(
                                RESPONSE_CHARSET_ATTR);
                            return value != null ? value : DEFAULT_CHARSET;
                        }

                        public void setModelValue(Object model, Object value)
                                throws FailTransferException, AbortTransferException {
                            ((HTTPDestination) model).getData().setAttribute(
                                RESPONSE_CHARSET_ATTR,
                                (String) value);
                        }
                    };

                    SelectEditor editor = new SelectEditor();
                    for(int i = 0; i < CHARSETS.length; i++)
                        editor.addOption(new DefaultSelectOption(CHARSETS[i], CHARSETS[i]));

                    //and finally create the configurationObject
                    FieldInfo fieldInfo = new FieldInfo(id, id, modifier, editor);

                    //add the configuration to the context for usage in the http-requests.
                    addField(id, fieldInfo);
                }
                
                
               
            } catch(Exception e) {
                Environment.getInstance().log(
                    "Couldn't create GUIContext for HTTPDestination", e);
            }
        }

    }

    public GUIContext getGUIContext() {
        return gui.getGUIContext();
    }

    protected Boolean getResponseCodeCheckDisabled() {
        return new Boolean(this.data.getAttribute(DISABLE_RESPONSE_CODE_CHECK_ATTR));
    }

    protected void setResponseCodeCheckDisabled(boolean responseCodeCheckDisabled) {
        this.data.setAttribute(DISABLE_RESPONSE_CODE_CHECK_ATTR, String.valueOf(responseCodeCheckDisabled));
    }
    
    protected Boolean getLogEnabled() {
		return new Boolean(this.data.getAttribute(LOG_ATTR));
	}

	protected void setLogEnabled(boolean logEnabled) {
		this.data.setAttribute(LOG_ATTR, String.valueOf(logEnabled));
	}
	
	protected Boolean getChosenResponseCharset() {
		return new Boolean(this.data.getAttribute(CHOOSE_RESPONSE_CHARSET_ATTR));
	}

	protected void setChosenResponseCharset(boolean ignore) {
		this.data.setAttribute(CHOOSE_RESPONSE_CHARSET_ATTR, String.valueOf(ignore));
	}

	public int getRequestMethod() {
        String fileTypeAttr = this.data.getAttribute(REQUEST_METHOD_ATTR);
        if(fileTypeAttr != null) {
            try {
                return Integer.parseInt(fileTypeAttr);
            } catch(NumberFormatException nfe) {
                // ignored...
            }
        }
        return REQUEST_METHOD_GET;
    }

    public void setRequestMethod(int method) {
        this.data.setAttribute(REQUEST_METHOD_ATTR, String.valueOf(method));
    }
    
    public int getRequestProtocol() {
        String fileTypeAttr = this.data.getAttribute(REQUEST_PROTOCOL_ATTR);
        if(fileTypeAttr != null) {
            try {
                return Integer.parseInt(fileTypeAttr);
            } catch(NumberFormatException nfe) {
                // ignored...
            }
        }
        return REQUEST_PROTOCOL_HTTP;
    }

    public void setRequestProtocol(int protocol) {
        this.data.setAttribute(REQUEST_PROTOCOL_ATTR, String.valueOf(protocol));
    }
    
    public void setAcceptSelfSignedCertificates(Boolean AcceptSelfSignedCertificates) {
    	this.data.setAttribute(ACCEPTSELFSIGNEDCERTIFICATES_ATTR, AcceptSelfSignedCertificates != null
                ? AcceptSelfSignedCertificates.toString() : "false");
    }
    
    public Boolean getAcceptSelfSignedCertificates() {
        return new Boolean(this.data.getAttribute(ACCEPTSELFSIGNEDCERTIFICATES_ATTR));
    }
    
	public String getGUITemplate() {
        return "<table border=0 cellspacing=5><tr><td>$" + HOST_ATTR + "$</td><td>$"
                + PORT_ATTR + "$</td></tr>" + "<tr><td>$" + PATH_ATTR + "$</td><td colspan=\"2\">$"
                + NAME_ATTR + "$</td></tr>" + "<tr><td>$" + USER_ATTR + "$</td><td colspan=\"2\">$"
                + PASSWORD_ATTR + "$</td></tr>"
                + "<tr><td>$" + REQUEST_METHOD_ATTR + "$</td><td>$" + REQUEST_PROTOCOL_ATTR + "$</td></tr>"
                + "<tr><td>$" + SOAPACTION_ATTR + "$</td><td>$" + ACCEPTSELFSIGNEDCERTIFICATES_ATTR + "$</td></tr>"
                + "<tr><td>$" + CONTENT_TYPE_ATTR + "$</td></tr>"
                + "<tr><td colspan=\"3\">$" + PARAMETERS_ATTRIBUTE + "$</td></tr>"
                + "<tr><td>$" + DISABLE_RESPONSE_CODE_CHECK_ATTR + "$</td><td>$" + LOG_ATTR + "$</td></tr>"
                + "<tr><td colspan=\"2\">$" + CHARSET_ATTR + "$</td></tr>"
                + "<tr></tr>"
                + "<tr><td>$"+ CHOOSE_RESPONSE_CHARSET_ATTR +"$</td></tr>"
                + "<tr><td colspan=\"2\">$" + RESPONSE_CHARSET_ATTR + "$</td></tr>"
                + "</table>";
    }


    public HTTPDestination( Object pipeComponentData ) {
        setData((PipeComponentData) pipeComponentData);
    }
    
    protected PipeComponentData data;
    
    public void setData(PipeComponentData data) {
        this.data = data;
    }

    public PipeComponentData getData() {
        return data;
    }
    
    public int getType() {
        return TYPE_DESTINATION;
    }
    public String getParameters() {
        String params = this.data.getAttribute(PARAMETERS_ATTRIBUTE);
        if(params != null&&params.trim().length()>1){
        	return params;
        }
        return "";
    }

    public void setParameters(String params) {
        this.data.setAttribute(PARAMETERS_ATTRIBUTE, params);
    }
    public String getName() {
        return "HTTPDestination";
    }

    public String getID() {
        return this.getClass().getName();
    }

    public String getDescription(Locale locale) {
        return PipeComponentUtils.getDescription(locale, this.getClass());
    }
    
    // Dummy methods due to no iteration supported
    public int open(DestinationInfo info, MessageLogger logger) throws FailTransferException {
        return ITERATION_OPEN_STATUS_OK;
    }

    public int close(DestinationInfo info, MessageLogger logger) throws FailTransferException {
        return ITERATION_CLOSE_STATUS_OK;
    }

    public void lastBlockStatus(int statusCode) { }

    /**
     * The method actually called by pipe during the conversion. This default implementation uses
     * the take-method to handle all the input records separately, So in most cases you only need to
     * implement it. If however you for some reason need access to all the data you can overwrite
     * this.
     */
    public void takeAll(String[] data, DestinationInfo info, MessageLogger logger) throws FailTransferException, AbortTransferException {
        for(int i = 0; i < data.length; i++) {
            take(data[i], info, logger);
        }
    }
    
  
    public void take(String data, DestinationInfo info, MessageLogger logger) throws FailTransferException, AbortTransferException {    	
    			   	
    	int port = getPort(logger);
    	
    	initializeProtocol(port);
    	
    	URL url = getURL(logger, port);
        
        String method = getValidatedMethod(data, logger); 
   	    
   	    String[][] parameters = getParameters(method, data, logger);
        
        String charsetName = this.data.getAttribute(CHARSET_ATTR);
        if(charsetName == null || charsetName.length() == 0)
            charsetName = DEFAULT_CHARSET;
        
        String responseCharsetName = null;
        if (getChosenResponseCharset()) {
        	responseCharsetName = this.data.getAttribute(RESPONSE_CHARSET_ATTR);
            if(responseCharsetName == null || responseCharsetName.length() == 0)
                responseCharsetName = null;
    	}
        
        String contentType = this.data.getAttribute(CONTENT_TYPE_ATTR);
        if(contentType == null || contentType.length() == 0)
            contentType = null;

        HTTPResponse response = null;
    	try{
    		response = sendHTTPRequest(url, method, parameters, data,
                                       charsetName, responseCharsetName, contentType);
            
    		if(getLogEnabled()){
    			Environment.getInstance().log(this.getName()+", server response:\n"
    					+response.getResponseHeaders()+"\n"+response.getResponseBody());
    		}
            
            if(response.getResponseError() != null) {
                if(getResponseCodeCheckDisabled() == false) {
                    throw new HttpException(response.getResponseError());
                } else {
                    logger.logMessage("Ignoring HTTP error response: " +
                        response.getResponseError(), this, MessageLogger.DEBUG);
                }
            }
            
    	} catch(Exception e){
    		logger.logMessage(e.toString(), this, MessageLogger.ERROR);
    		PipeComponentUtils.failTransfer();
    	}
    	logger.logMessage("Request complete", this, MessageLogger.DEBUG);

        if( response.getResponseBody() == null ) {
            logger.logMessage("Empty response body detected",
                              this, MessageLogger.DEBUG);
            // We do not abort the Pipe execution since we don't
            // need to return any data to the Pipe framework.
        }
        
    }

    protected HTTPResponse sendHTTPRequest(URL url, 
    									  String method, 
    									  String[][] parameters, 
    									  String data, 
    									  String charsetName, 
    									  String responseCharsetName,
                                          String contentType) throws Exception {
        HTTPResponse response;
        if(method.equals("PUT")){
        	String[][] put_data=new String[1][1];
        	put_data[0][0]=data;
            
        	response=HTTPUtils.makeRequest(url, method, put_data,
        			this.getData().getAttribute(USER_ATTR),
                    this.getData().getAttribute(PASSWORD_ATTR),
                    charsetName,
                    responseCharsetName,
                    contentType);
        }
        else {
        	response=HTTPUtils.makeRequest(url, method, parameters,
        			this.getData().getAttribute(USER_ATTR),
                    this.getData().getAttribute(PASSWORD_ATTR),
                    charsetName, 
                    responseCharsetName,
                    contentType);
        }
        return response;
    }

    protected String[][] getParameters(String method, String data, MessageLogger logger) throws FailTransferException {
        String[][] parameters=null;
   	    if(method.equals("PUT")==false){
   	    	//Parameters in "parameters" field are separated by "\r\n" (lines) and "=" (names,values) 
		    String[][] extra_parameters = HTTPUtils.splitParameters(getParameters(), "\r\n", "=");
		    //Parameter pairs from components should be separated by "&" and name, values by "="
		    String[][] data_in_parameters;
		    if(!method.equals("SOAP")){
		    	data_in_parameters=HTTPUtils.splitParameters(data, "&","=");
		    }
		    else{
		    	if(data==null || data.length()<1){
		    		logger.logMessage("No SOAP message given!",
			    			this, MessageLogger.ERROR);
			    	PipeComponentUtils.failTransfer();
		    	}
		    	data_in_parameters=new String[2][2];
		    	data_in_parameters[0][0]="SOAPMessage";
		    	data_in_parameters[0][1]=data;
		    	if(this.getData().getAttribute(SOAPACTION_ATTR) != null){
		    		data_in_parameters[1][0]="SOAPAction";
		    		data_in_parameters[1][1]=this.getData().getAttribute(SOAPACTION_ATTR);
		    	}
		    }
		    if(data_in_parameters==null){
		    	logger.logMessage("Supplied parameters are not name value pairs",
		    			this, MessageLogger.ERROR);
		    	PipeComponentUtils.failTransfer();
		    }
		    if(extra_parameters==null&&(getParameters()==null||getParameters().length()<1)==false){
		    	logger.logMessage("Supplied parameters are not name value pairs",
		    			this, MessageLogger.ERROR);
		    	PipeComponentUtils.failTransfer();
		    }
		    if(extra_parameters!=null){
		    	parameters=new String[extra_parameters.length+data_in_parameters.length][2];
		    	for(int k=0;k<parameters.length;k++){
		    		if(k<extra_parameters.length){
		    			parameters[k][0]=extra_parameters[k][0];
		    			parameters[k][1]=extra_parameters[k][1];
		    		}
		    		else{
		    			parameters[k][0]=data_in_parameters[k-extra_parameters.length][0];
		    			parameters[k][1]=data_in_parameters[k-extra_parameters.length][1];
		    		}
		    	}
		    }
		    else{
		    	parameters=data_in_parameters;
		    }
   	    }
        return parameters;
    }

    /**
     * @param data Reference to component's input data for deciding whether the method can be used
     * @param logger MessageLogger
     * @return HTTP request method name
     * @throws FailTransferException
     */
    protected String getValidatedMethod(String data, MessageLogger logger) throws FailTransferException {
        String method=REQUEST_METHOD_LABELS[getRequestMethod()];
   	    
   	    if(method.equals("PUT")&&(data==null||data.length()<1)){
   	    	logger.logMessage("When using PUT method data must be provided by previous components",
   	    			this, MessageLogger.ERROR);
   	    	PipeComponentUtils.failTransfer();
   	    }
   	    if(method.equals("PUT")&&(getParameters()==null||getParameters().length()<1)==false){
	    	logger.logMessage("When using PUT method parameters field should be left empty",
	    			this, MessageLogger.ERROR);
	    	PipeComponentUtils.failTransfer();
	    }
        return method;
    }

    protected URL getURL(MessageLogger logger, int port) throws FailTransferException {
        URL url=null;
    	try{
    		String protocol = null;
            String path = this.getData().getAttribute(PATH_ATTR);

            String label = REQUEST_PROTOCOL_LABELS[getRequestProtocol()];

            if(label.equals("HTTPS"))
    			protocol = "https";
    		else
    			protocol = "http";
            
            // Insert a leading slash character to directory path if missing
            if((path.length() > 0) && (path.charAt(0) != '/')) {
                path = "/" + path;
            }
            
    		url=new URL(protocol,this.getData().getAttribute(HOST_ATTR),
        			port, path + "/" + this.getData().getAttribute(NAME_ATTR));
        			
    	} catch (MalformedURLException e){
    		logger.logMessage(e.getMessage(), this, MessageLogger.ERROR);
    		PipeComponentUtils.failTransfer();
    	}
        return url;
    }

    protected void initializeProtocol(int port) {
        ProtocolSocketFactory factory;
    	if((getAcceptSelfSignedCertificates().booleanValue())) {
    		factory = new  EasySSLProtocolSocketFactory();
    		Protocol easyhttps = new Protocol("https", factory, port);
    		Protocol.registerProtocol("https", easyhttps);
    	} else {
    		factory = new  SSLProtocolSocketFactory();
    		Protocol stricthttps = new Protocol("https", factory, port);
    		Protocol.registerProtocol("https", stricthttps);
    	}
    }

    protected int getPort(MessageLogger logger) throws FailTransferException {
        int port=0;
    	try{
    		port = Integer.parseInt(this.getData().getAttribute(PORT_ATTR));
    		if(port>65535||port<1) throw new IllegalArgumentException();
    	} catch(Exception e){
    		logger.logMessage("Invalid port number", this, MessageLogger.ERROR);
    		PipeComponentUtils.failTransfer();
    	}
        return port;
    }
}