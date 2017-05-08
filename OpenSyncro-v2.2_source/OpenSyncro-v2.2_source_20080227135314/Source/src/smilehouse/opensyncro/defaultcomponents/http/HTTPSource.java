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
 * Created on Oct 5, 2005
 */

package smilehouse.opensyncro.defaultcomponents.http;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import org.apache.commons.httpclient.HttpException;

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
import smilehouse.opensyncro.pipes.component.FailTransferException;
import smilehouse.opensyncro.pipes.component.PipeComponentData;
import smilehouse.opensyncro.pipes.component.PipeComponentUtils;
import smilehouse.opensyncro.pipes.component.SourceIF;
import smilehouse.opensyncro.pipes.gui.GUIConfigurationIF;
import smilehouse.opensyncro.pipes.gui.GUIDefinition;
import smilehouse.opensyncro.pipes.log.MessageLogger;
import smilehouse.opensyncro.pipes.metadata.SourceInfo;
import smilehouse.opensyncro.system.Environment;


public class HTTPSource implements SourceIF, GUIConfigurationIF {

    private static final String HOST_ATTR = "host";
    private static final String PORT_ATTR = "port";
    private static final String FILENAME_ATTR = "file_name";
    private static final String DIRECTORY_NAME = "dir_name";
    private static final String USER_ATTR = "user";
    private static final String PASSWORD_ATTR = "password";
    private static final String PARAMETERS_ATTRIBUTE = "param";
    private static final String[] REQUEST_METHOD_LABELS={"GET","POST"};
	private static final int REQUEST_METHOD_GET = 0;
    private static final String REQUEST_METHOD_ATTR="method";
    private static final String[] REQUEST_PROTOCOL_LABELS={"HTTP","HTTPS"};
    private static final String REQUEST_PROTOCOL_ATTR="protocol";
    private static final int REQUEST_PROTOCOL_HTTP = 0;
    private static final String CHOOSE_RESPONSE_CHARSET_ATTR = "chooseresponsecharset";
    
    public static final String RESPONSE_CHARSET_ATTR = "responsecharset";
        
    private static final String DEFAULT_CHARSET = "ISO-8859-1";
    
    private static final String[] CHARSETS = PipeComponentUtils.getCharacterSetArray();
    
    // --------------
    // GUI definition
    // --------------
    protected static HTTPSourceGUI gui = new HTTPSourceGUI();

    protected static class HTTPSourceGUI extends GUIDefinition {

    
        public HTTPSourceGUI() {
            try {
                addSimpleTextFieldForComponent(HOST_ATTR, HOST_ATTR, 40);
                addSimpleTextFieldForComponent(PORT_ATTR, PORT_ATTR, 10);
                addSimpleTextFieldForComponent(USER_ATTR, USER_ATTR, 10);
                {
                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws FailTransferException, AbortTransferException {
                            return "";
                            //return ((HTTPSource) model).getAttribute(PASSWORD_ATTR);
                        }

                        public void setModelValue(Object model, Object value) throws FailTransferException, AbortTransferException {
                            String valueStr = (String) value;
                            if(valueStr != null && valueStr.length() > 0)
                                ((HTTPSource) model).data.setAttribute(PASSWORD_ATTR, valueStr);
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
                               return ((HTTPSource) model).getParameters();
                           }

                           public void setModelValue(Object model, Object value) throws Exception {
                               ((HTTPSource) model).setParameters((String) value);
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
                            return new Integer(((HTTPSource) model).getRequestMethod());
                        }

                        public void setModelValue(Object model, Object value) throws FailTransferException, AbortTransferException {
                            ((HTTPSource) model).setRequestMethod(((Integer) value).intValue());
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
                            return new Integer(((HTTPSource) model).getRequestProtocol());
                        }

                        public void setModelValue(Object model, Object value) throws FailTransferException, AbortTransferException {
                            ((HTTPSource) model).setRequestProtocol(((Integer) value).intValue());
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
                
                addSimpleTextFieldForComponent(DIRECTORY_NAME, DIRECTORY_NAME, 40);
                addSimpleTextFieldForComponent(FILENAME_ATTR, FILENAME_ATTR, 20);
               
                
                {
                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            return ((HTTPSource) model).getChosenResponseCharset();
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            ((HTTPSource) model).setChosenResponseCharset((Boolean)value);
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
                            String value = ((HTTPSource) model).getData().getAttribute(
                                RESPONSE_CHARSET_ATTR);
                            return value != null ? value : DEFAULT_CHARSET;
                        }

                        public void setModelValue(Object model, Object value)
                                throws FailTransferException, AbortTransferException {
                            ((HTTPSource) model).getData().setAttribute(
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
                    "Couldn't create GUIContext for HTTPSource", e);
            }
        }

    }

    // ---
    
    protected Boolean getChosenResponseCharset() {
        return new Boolean(this.data.getAttribute(CHOOSE_RESPONSE_CHARSET_ATTR));
    }

    protected void setChosenResponseCharset(boolean ignore) {
        this.data.setAttribute(CHOOSE_RESPONSE_CHARSET_ATTR, String.valueOf(ignore));
    }

    public HTTPSource(Object pipeComponentData) {
        setData((PipeComponentData) pipeComponentData);
    }

    private boolean allDataOutput;
    protected PipeComponentData data;

    public void setData(PipeComponentData pipeComponentData) {
        this.data = pipeComponentData;
    }

    public PipeComponentData getData() {
        return data;
    }

    public final int getType() {
        return TYPE_SOURCE;
    }

    public String getName() {
        return "HTTPSource";
    }

    public String getID() {
        return this.getClass().getName();
    }

    public String getDescription(Locale locale) {
        return PipeComponentUtils.getDescription(locale, this.getClass());
    }
    
    public String getParameters() {
        String params = this.data.getAttribute(PARAMETERS_ATTRIBUTE);
        if(params != null)
            return params;

        return "";
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
    
    public void setParameters(String params) {
        this.data.setAttribute(PARAMETERS_ATTRIBUTE, params);
    }
    // Dummy methods due to no iteration supported
    public int open(SourceInfo info, MessageLogger logger) throws FailTransferException {
        this.allDataOutput = false;
        return ITERATION_OPEN_STATUS_OK;
    }

    public int close(SourceInfo info, MessageLogger logger) throws FailTransferException {
        return ITERATION_CLOSE_STATUS_OK;
    }

    public void lastBlockStatus(int statusCode) {}


    // ---


    public GUIContext getGUIContext() {
        return gui.getGUIContext();
    }

    public String getGUITemplate() {
        return "<table border=0 cellspacing=5><tr><td>$" + HOST_ATTR + "$</td><td>$"
        + PORT_ATTR + "$</td></tr>" + "<tr><td>$" + DIRECTORY_NAME + "$</td><td colspan=\"2\">$"
        + FILENAME_ATTR + "$</td></tr>" + "<tr><td>$" + USER_ATTR + "$</td><td colspan=\"2\">$"
        + PASSWORD_ATTR + "$</td></tr>"
        + "<tr><td colspan=\"1\">$" + REQUEST_METHOD_ATTR + "$</td><td colspan=\"1\">$" + REQUEST_PROTOCOL_ATTR + "$</td></tr>"
        + "<tr><td colspan=\"3\">$" + PARAMETERS_ATTRIBUTE + "$</td></tr>"
        + "<tr><td>$"+ CHOOSE_RESPONSE_CHARSET_ATTR +"$</td></tr>"
        + "<tr><td colspan=\"2\">$" + RESPONSE_CHARSET_ATTR + "$</td></tr>"
        +"</table>";
    }

    public String[] give(SourceInfo info, MessageLogger logger) throws FailTransferException,
            AbortTransferException {

        // This component does not support iteration, so we output all our data
        // once (and only once)  	
        if(this.allDataOutput == true)
            return null;
        else
            this.allDataOutput = true;


        HTTPResponse data=null;

        int port=0;
    	try{
    		port = Integer.parseInt(this.getData().getAttribute(PORT_ATTR));
    		if(port>65535||port<1) throw new IllegalArgumentException();
    	} catch(Exception e){
    		logger.logMessage("Invalid port number", this, MessageLogger.WARNING);
    		PipeComponentUtils.failTransfer();
    	}
       
        URL url=null;
    	try{
    		String protocol = null;
            String path = this.getData().getAttribute(DIRECTORY_NAME);
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
        			port, path + "/" + this.getData().getAttribute(FILENAME_ATTR));
        			
    	} catch (MalformedURLException e){
    		logger.logMessage(e.getMessage(), this, MessageLogger.ERROR);
    		PipeComponentUtils.failTransfer();
    	}
    	
		String[][] parameters = HTTPUtils.splitParameters(getParameters(), "\r\n", "=");
		String method=REQUEST_METHOD_LABELS[getRequestMethod()];
    	
    	try{
    		//use character set defined by user if applicable
            String responseCharsetName = null;
            if (getChosenResponseCharset()) {
                responseCharsetName = this.data.getAttribute(RESPONSE_CHARSET_ATTR);
                if(responseCharsetName == null || responseCharsetName.length() == 0)
                    responseCharsetName = null;
            }
            
  			data = HTTPUtils.makeRequest(url, method,parameters,
  			                            this.getData().getAttribute(USER_ATTR),
                                        this.getData().getAttribute(PASSWORD_ATTR),
                                        DEFAULT_CHARSET, responseCharsetName, null);

            if(data.getResponseError()!=null)
    			throw new HttpException(data.getResponseError());
    	} catch(Exception e){
    		logger.logMessage(e.toString(), this, MessageLogger.ERROR);
    		PipeComponentUtils.failTransfer();
    	}
    	logger.logMessage("Request complete", this, MessageLogger.DEBUG);
        
        if( data.getResponseBody() != null ) {
             return new String[] { data.getResponseBody() };
        } else {
            logger.logMessage("Empty response body detected, aborting.",
                              this, MessageLogger.WARNING);
            PipeComponentUtils.abortTransfer();
            
            // This code shouldn't be reached
            return null;
        }
    }
}