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
 * WorkspaceHQLSource - Source component which performs user specified HQL queries to 
 * export Hibernate mapped objects (e.g. products and sales orders) in XML.
 * 
 */

package smilehouse.opensyncro.defaultcomponents.workspace;

import java.rmi.RemoteException;
import java.util.Locale;

import smilehouse.gui.html.fieldbased.FieldInfo;
import smilehouse.gui.html.fieldbased.GUIContext;
import smilehouse.gui.html.fieldbased.editor.BooleanEditor;
import smilehouse.gui.html.fieldbased.editor.PasswordEditor;
import smilehouse.gui.html.fieldbased.editor.TextEditor;
import smilehouse.gui.html.fieldbased.model.DefaultModelModifier;
import smilehouse.gui.html.fieldbased.model.ModelModifier;
import smilehouse.openinterface.AccessDeniedException;
import smilehouse.openinterface.ExportResult;
import smilehouse.openinterface.IteratorClosedException;
import smilehouse.openinterface.LoginInfo;
import smilehouse.openinterface.OpenInterfaceException;
import smilehouse.openinterface.OpenInterfaceIF;
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
import smilehouse.util.Utils;

public class WorkspaceHQLSource implements SourceIF, GUIConfigurationIF {

    protected static final String OPEN_INTERFACE_HOST_ATTR = "openinterfaceHost";
    protected static final String DATABASE_ATTR = "database";

    protected static final String USERNAME_ATTR = "userName";
    protected static final String PASSWORD_ATTR = "password";

    protected static final String HQL_SELECT_QUERY_ATTR = "hqlSelectQuery";
    protected static final String RESULTS_PER_ITERATION_ATTR = "resultsPerIteration";
    protected static final String SESSION_TIMEOUT_ATTR = "sessionTimeout";
    protected static final String XMLDECLARATION_FOR_EACH_BLOCK = "xmlDeclarationForEachBlock";
    
    protected PipeComponentData data;

    protected static WorkspaceHQLSourceGUI guiContextContainer = new WorkspaceHQLSourceGUI();

    protected static final String OI_HQL_resultTagName = "result";
    
    // OpenInterface address
    protected String oiEndpointAddress = null;
    
    // OpenInterface HQL iterator Id
    protected String oiIteratorId = null;
    
    // Login information for OpenInterface
    protected LoginInfo login;

    // Number of results to retrieve per iteration step
    protected int resultsPerIteration;

    // Iteration number, used to determine should the XML declaration be output
    protected int iterationNumber;
    
    // If set to true, all HQL results are output on the first iteration
    protected boolean allResultsAtOnce;
    
    // MessageLogger reference
    protected MessageLogger logger = null;

    // All data output flag used to prevent sending OpenInterface iterate messages
    // after all data has been output
    protected boolean allDataOutput;

    // A flag to keep track whether one or more results have been output. Used to
    // abort Pipe execution if there are no results to output.
    protected boolean noDataOutput;

    
    protected static class WorkspaceHQLSourceGUI extends GUIDefinition {

        public WorkspaceHQLSourceGUI() {

            try {
                {
                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            return ((WorkspaceHQLSource) model).getOpenInterfaceHost();
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            ((WorkspaceHQLSource) model).setOpenInterfaceHost((String) value);
                        }
                    };

                    TextEditor editor = new TextEditor();
                    editor.setSize(70);

                    //and finally create the configurationObject
                    FieldInfo fieldInfo = new FieldInfo(
                        OPEN_INTERFACE_HOST_ATTR,
                        OPEN_INTERFACE_HOST_ATTR,
                        modifier,
                        editor);

                    //add the configuration to the context for usage in the http-requests.
                    addField(OPEN_INTERFACE_HOST_ATTR, fieldInfo);
                }
                {
                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            return ((WorkspaceHQLSource) model).getDatabase();
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            ((WorkspaceHQLSource) model).setDatabase((String) value);
                        }
                    };

                    TextEditor editor = new TextEditor();
                    editor.setSize(30);

                    //and finally create the configurationObject
                    FieldInfo fieldInfo = new FieldInfo(
                        DATABASE_ATTR,
                        DATABASE_ATTR,
                        modifier,
                        editor);

                    //add the configuration to the context for usage in the http-requests.
                    addField(DATABASE_ATTR, fieldInfo);
                }
                {
                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            return ((WorkspaceHQLSource) model).getUsername();
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            ((WorkspaceHQLSource) model).setUsername((String) value);
                        }
                    };

                    TextEditor editor = new TextEditor();
                    editor.setSize(30);

                    //and finally create the configurationObject
                    FieldInfo fieldInfo = new FieldInfo(
                        USERNAME_ATTR,
                        USERNAME_ATTR,
                        modifier,
                        editor);

                    //add the configuration to the context for usage in the http-requests.
                    addField(USERNAME_ATTR, fieldInfo);
                }
                {
                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            return ((WorkspaceHQLSource) model).getPassword();
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            ((WorkspaceHQLSource) model).setPassword((String) value);
                        }
                    };

                    PasswordEditor editor = new PasswordEditor();
                    editor.setSize(30);

                    //and finally create the configurationObject
                    FieldInfo fieldInfo = new FieldInfo(
                        PASSWORD_ATTR,
                        PASSWORD_ATTR,
                        modifier,
                        editor);

                    //add the configuration to the context for usage in the http-requests.
                    addField(PASSWORD_ATTR, fieldInfo);
                }
                
                {
                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            return ((WorkspaceHQLSource) model).getHQLQuery();
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            ((WorkspaceHQLSource) model).setHQLQuery((String) value);
                        }
                    };

                    TextEditor editor = new TextEditor();
                    editor.setSize(100);

                    //and finally create the configurationObject
                    FieldInfo fieldInfo = new FieldInfo(
                        HQL_SELECT_QUERY_ATTR,
                    	HQL_SELECT_QUERY_ATTR,
                        modifier,
                        editor);

                    //add the configuration to the context for usage in the http-requests.
                    addField(HQL_SELECT_QUERY_ATTR, fieldInfo);
                }

                {
                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            return ((WorkspaceHQLSource) model).getSessionTimeout();
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            ((WorkspaceHQLSource) model).setSessionTimeout((String) value);
                        }
                    };

                    TextEditor editor = new TextEditor();
                    editor.setSize(30);

                    //and finally create the configurationObject
                    FieldInfo fieldInfo = new FieldInfo(
                        SESSION_TIMEOUT_ATTR,
                        SESSION_TIMEOUT_ATTR,
                        modifier,
                        editor);

                    //add the configuration to the context for usage in the http-requests.
                    //context.addFieldInfo(fieldInfo);
                    addField(SESSION_TIMEOUT_ATTR, fieldInfo);
                }

                {
                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            return ((WorkspaceHQLSource) model).getResultsPerIteration();
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            ((WorkspaceHQLSource) model).setResultsPerIteration((String) value);
                        }
                    };

                    TextEditor editor = new TextEditor();
                    editor.setSize(30);

                    //and finally create the configurationObject
                    FieldInfo fieldInfo = new FieldInfo(
                        RESULTS_PER_ITERATION_ATTR,
                        RESULTS_PER_ITERATION_ATTR,
                        modifier,
                        editor);

                    //add the configuration to the context for usage in the http-requests.
                    addField(RESULTS_PER_ITERATION_ATTR, fieldInfo);
                }
                
                {
                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            return ((WorkspaceHQLSource) model).getXMLdeclarationForEachBlock();
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            ((WorkspaceHQLSource) model).setXMLdeclarationForEachBlock((Boolean) value);
                        }
                    };

                    BooleanEditor editor = new BooleanEditor();

                    //and finally create the configurationObject
                    FieldInfo fieldInfo = new FieldInfo(XMLDECLARATION_FOR_EACH_BLOCK,
                        								XMLDECLARATION_FOR_EACH_BLOCK,
                        								modifier,
                        								editor);

                    //add the configuration to the context for usage in the http-requests.
                    addField(XMLDECLARATION_FOR_EACH_BLOCK, fieldInfo);
                }

            } catch(Exception e) {
                Environment.getInstance().log(
                    "Couldn't create GUIContext in " + Utils.getClassName(this.getClass()), e);

            }
        }
    }

    public WorkspaceHQLSource( Object pipeComponentData ) {
        setData((PipeComponentData) pipeComponentData);
    }
    
    public final int getType() {
        return TYPE_SOURCE;
    }

    public void setData(PipeComponentData data) {
        this.data = data;
    }

    public PipeComponentData getData() {
        return data;
    }

    public String getName() {
        return "WorkspaceHQLSource";
    }

    public String getID() {
        return this.getClass().getName();
    }

    public String getDescription(Locale locale) {
        return PipeComponentUtils.getDescription(locale, this.getClass());
    }

    // ToDo: use better parameter value checking as in WorkspaceHQLResultConverter, add
    //       default value support.
    public int open(SourceInfo info, MessageLogger logger) throws FailTransferException {
    
        int sessionTimeout = 0;
        String iteratorId = null;
        
        // Reset allDataOutput and noDataOutput flags
        this.allDataOutput = false;
        this.noDataOutput = true;
                
        // Set MessageLogger reference
        this.logger = logger;
        
        // Reset iterationNumber
        this.iterationNumber = 0;
        
        // Retrieve and store OpenInterface endpoint address
        this.oiEndpointAddress = WorkspaceOIUtils.getOIEndpointAddress(getDatabase(),
            														   getOpenInterfaceHost(),
            														   logger,
            														   this);
        OpenInterfaceIF oi = WorkspaceOIUtils.getOpenInterfaceIF(this.oiEndpointAddress,
            													 getOpenInterfaceHost());
        this.login = WorkspaceOIUtils.getLoginInfo(getDatabase(),
            									   getUsername(),
            									   getPassword());

        // Test HQL query parameter
        if(getHQLQuery() == null || getHQLQuery().length() == 0) {
            logger.logMessage(
                "No HQL Select query specified",
                this,
                MessageLogger.ERROR);
            return ITERATION_OPEN_STATUS_ERROR;
        }
        
        // Validate session timeout parameter
        try {
            sessionTimeout = Integer.parseInt(getSessionTimeout());
        } catch(NumberFormatException e1) {
            logger.logMessage(
                "Invalid session timeout value (\"" + getSessionTimeout() + "\"), should be an integer",
                this,
                MessageLogger.ERROR);
            return ITERATION_OPEN_STATUS_ERROR;
        }
        
        // Validate results per iteration parameter
        try {
            this.resultsPerIteration = Integer.parseInt(getResultsPerIteration());
        } catch(NumberFormatException e1) {
            logger.logMessage(
                "Invalid value (\"" + getResultsPerIteration() + "\") for results per iteration, should be an integer",
                this,
                MessageLogger.ERROR);
            return ITERATION_OPEN_STATUS_ERROR;
        }
        if(this.resultsPerIteration < 0) {
            logger.logMessage(
                "Invalid value for results per iteration (\"" + this.resultsPerIteration
                + "\"), should be atleast 0 (which returns all results at first iteration)",
                this, MessageLogger.ERROR);
            return ITERATION_OPEN_STATUS_ERROR;
        }

        // If number of results per iteration is set to zero, switch to allResultsAtOnce mode
        if(this.resultsPerIteration == 0) {
            this.allResultsAtOnce = true;
            
            // Query one result at time
            this.resultsPerIteration = 1;
        } else {
            this.allResultsAtOnce = false;
        }

        // Send the HQL query to OpenInterface and get an iterator Id
        iteratorId = WorkspaceOIUtils.openHQLIterator(oi,
            										  this.login,
            										  getHQLQuery(),
            										  sessionTimeout,
            									      logger, this);
        
        if(iteratorId == null || iteratorId.length() == 0) {
            logger.logMessage(
                "Unknown error while performing HQL query, unable to get result iterator",
                this,
                MessageLogger.ERROR);
            return ITERATION_OPEN_STATUS_ERROR;
        }
        
        this.oiIteratorId = iteratorId;
        return ITERATION_OPEN_STATUS_OK;
    }


    public int close(SourceInfo info, MessageLogger logger) throws FailTransferException {

        // Close the OpenInterface iterator unless it has been closed (due to a timeout) earlier
        if(this.oiIteratorId != null) {
            OpenInterfaceIF oi = WorkspaceOIUtils.getOpenInterfaceIF(
                this.oiEndpointAddress,
                getOpenInterfaceHost()
            );
            WorkspaceOIUtils.closeHQLIterator(oi, this.login, this.oiIteratorId, logger, this);
            this.oiIteratorId = null;
        }
        
        // Reset MessageLogger reference
        this.logger = null;
        
        // Reset OpenInterface endpoint address
        this.oiEndpointAddress = null;
        
        return ITERATION_CLOSE_STATUS_OK;
    }

    public void lastBlockStatus(int statusCode) throws FailTransferException, AbortTransferException {
        // No actions (rollback etc) to be taken depending on the success of Pipe execution
    }

    
    public void setOpenInterfaceHost(String openInterfaceHost) {
        this.data.setAttribute(OPEN_INTERFACE_HOST_ATTR, openInterfaceHost);
    }

    public String getOpenInterfaceHost() {
        String openInterfaceHost = this.data.getAttribute(OPEN_INTERFACE_HOST_ATTR);
        return (openInterfaceHost != null ? openInterfaceHost : "");
    }

    public void setDatabase(String database) {
        this.data.setAttribute(DATABASE_ATTR, database);
    }

    public String getDatabase() {
        String database = this.data.getAttribute(DATABASE_ATTR);
        return (database != null ? database : "");
    }

    public void setUsername(String username) {
        this.data.setAttribute(USERNAME_ATTR, username);
    }

    public String getUsername() {
        String username = this.data.getAttribute(USERNAME_ATTR);
        return (username != null ? username : "");
    }

    public void setPassword(String password) {
        this.data.setAttribute(PASSWORD_ATTR, password);
    }

    public String getPassword() {
        String password = this.data.getAttribute(PASSWORD_ATTR);
        return (password != null ? password : "");
    }

    public void setHQLQuery(String hqlQuery) {
        this.data.setAttribute(HQL_SELECT_QUERY_ATTR, hqlQuery);
    }

    public String getHQLQuery() {
        String hqlQuery = this.data.getAttribute(HQL_SELECT_QUERY_ATTR);
        return (hqlQuery != null ? hqlQuery : "");
    }

    public void setSessionTimeout(String sessionTimeout) {
        this.data.setAttribute(SESSION_TIMEOUT_ATTR, sessionTimeout);
    }

    public String getSessionTimeout() {
        String sessionTimeout = this.data.getAttribute(SESSION_TIMEOUT_ATTR);
        return (sessionTimeout != null ? sessionTimeout : "");
    }

    public void setResultsPerIteration(String resultsPerIteration) {
        this.data.setAttribute(RESULTS_PER_ITERATION_ATTR, resultsPerIteration);
    }

    public String getResultsPerIteration() {
        String resultsPerIteration = this.data.getAttribute(RESULTS_PER_ITERATION_ATTR);
        return (resultsPerIteration != null ? resultsPerIteration : "");
    }

    public void setXMLdeclarationForEachBlock(Boolean xmlDeclarationForEachBlock) {
        this.data.setAttribute(XMLDECLARATION_FOR_EACH_BLOCK, xmlDeclarationForEachBlock != null
                ? xmlDeclarationForEachBlock.toString() : "false");
    }

    public Boolean getXMLdeclarationForEachBlock() {
        return new Boolean(this.data.getAttribute(XMLDECLARATION_FOR_EACH_BLOCK));
    }


    public String[] give(SourceInfo info, MessageLogger logger)
    				throws FailTransferException, AbortTransferException {
        ExportResult exportResult = null;
        StringBuffer hqlResult = new StringBuffer("");
        
        // If we have already output all data, no need for further communication
        // with OpenInterface. Simply return null as an EOF signal to the Pipe framework.
        if( this.allDataOutput == true ) return null;
        
        // Counter used in the all results at once mode to determine whether
        // a single HQL result should have the XML declaration line or not
        int allResultsAtOnceIndex = 0;
        
        // Increment iteration number
        this.iterationNumber++;
        
        Boolean xmlDeclarationForEachBlock = getXMLdeclarationForEachBlock(); 
        
        OpenInterfaceIF oi = WorkspaceOIUtils.getOpenInterfaceIF(
			this.oiEndpointAddress,
			getOpenInterfaceHost()
			);
        
        do {
            try {
                exportResult = oi.iterate(this.login, oiIteratorId, resultsPerIteration);
                
            } catch (AccessDeniedException e) {
                logger.logMessage(
                    "Access to OpenInterface was denied. Check your username and password settings.",
                    this,
                    MessageLogger.ERROR);
                PipeComponentUtils.failTransfer();
            } catch (IteratorClosedException e) {

                // Invalidate OpenInterface iterator's number so that we don't attempt to close it later
                this.oiIteratorId = null;

                logger.logMessage(
	                "OpenInterface HQL iterator had been closed due to inactivity. Try increasing session timeout value.",
	                this,
	                MessageLogger.ERROR);
	            PipeComponentUtils.failTransfer();
	        } catch (OpenInterfaceException e) {
	            logger.logMessage("OpenInterfaceException while iterating over the HQL query's result set",
	                this, MessageLogger.ERROR);
	            logger.logMessage(e.getMessage(), this, MessageLogger.ERROR);
	            Environment.getInstance().log(
	                "OpenInterfaceException while iterating over the HQL query's result set", e);
	            PipeComponentUtils.failTransfer();
	        } catch (RemoteException e) {
	            logger.logMessage("RemoteException from OpenInterface while iterating over the HQL query's result set",
	                this, MessageLogger.ERROR);
	            Environment.getInstance().log(
	                "RemoteException from OpenInterface while iterating over the HQL query's result set", e);
	            PipeComponentUtils.failTransfer();
	        }
	
	        if(exportResult != null) {
	            
	            if(this.allResultsAtOnce == false) {
	                
	                // Normal iteration mode
		            if((this.iterationNumber > 1) &&
		               (xmlDeclarationForEachBlock.booleanValue() == false)) {
		                   hqlResult.append(Utils.stripXMLdeclaration(exportResult.getXml()));
		               } else {
		                   hqlResult.append(exportResult.getXml());
		               }
	            } else {
	                
	                // All results at once mode
	                allResultsAtOnceIndex++;
	                // Only the first HQL result will keep its XML declaration line
	                if(allResultsAtOnceIndex == 1) {
		                hqlResult.append(stripResultCloseTag(exportResult.getXml()));
	                } else if(allResultsAtOnceIndex > 1) {
		                hqlResult.append(stripResultTag(Utils.stripXMLdeclaration(exportResult.getXml())));
	                }
	            }
	        }
	        
        } while((this.allResultsAtOnce == true) && (exportResult != null));
        
        if(this.allResultsAtOnce == false) {
            // Normal iteration mode
            if(exportResult != null) {

                this.noDataOutput = false;

                return new String[] { hqlResult.toString() };
            } else {
                if(this.noDataOutput == true) {
                    // No results have been output earlier and the current result is
                    // null, so we abort the Pipe and log "no results" warning message.
                    logger.logMessage("HQL query returned no results.", this, MessageLogger.WARNING);
                    PipeComponentUtils.abortTransfer();
                    
                    // Unreachable code
                    return null;
                } else {
                    // Some results have been output earlier
                    return null;    
                }
                
            }
        } else {
            // All results at once mode

            // Set allDataOutput flag to true so that we know we don't need to send another
            // OpenInterface iterate message when the Pipe frameworks calls the give() method again
            this.allDataOutput = true;

            if ( hqlResult.length() > 0 ) {

                this.noDataOutput = false;

                // Append the end tag of HQL result
                return new String[] { hqlResult.toString() + "</" +
                        			  OI_HQL_resultTagName + ">" };    
            } else {
                logger.logMessage("HQL query returned no results.", this, MessageLogger.WARNING);
                PipeComponentUtils.abortTransfer();

                // Unreachable code
                return null;
            }
        }
            
    }

    // Quick hacks to remove the XML root tag
    // used by the OpenInterface to contain HQL query results.
    // WARNING: does NOT check whether the start/end tags
    // actually belog to the root element!
    
    // TODO: a generic, more XML syntax aware removeXMLRootTag utility method
    protected String stripResultTag(String xmlData) {
        return stripResultCloseTag(xmlData.replaceFirst("<"
                                   + OI_HQL_resultTagName + ">", ""));
}

    protected String stripResultCloseTag(String xmlData) {
        int j = xmlData.lastIndexOf("</" + OI_HQL_resultTagName + ">");
		if (j == -1) {
		    // Error: the end tag was not found -> return the original data instead
			return xmlData;
		}
		return xmlData.substring(0, j);
    }
    
    
    public GUIContext getGUIContext() {
        return guiContextContainer.getGUIContext();
    }

    public String getGUITemplate() {
        return "<table border=\"0\"><tr><td>$" + OPEN_INTERFACE_HOST_ATTR + "$</td></tr>"
                + "<tr><td>$" + DATABASE_ATTR + "$</td>"
                + "<td>$" + USERNAME_ATTR + "$</td><td>$" + PASSWORD_ATTR + "$</td></tr>" +

                "<tr><td colspan=\"3\"><hr></td></tr>" +

                "<tr><td colspan=\"2\">$" + HQL_SELECT_QUERY_ATTR + "$</td></tr>" +
                "<tr><td>$" + SESSION_TIMEOUT_ATTR + "$</td></tr>" +
                "<tr><td>$" + RESULTS_PER_ITERATION_ATTR + "$</td>" +
                "<td colspan=\"2\">$" + XMLDECLARATION_FOR_EACH_BLOCK + "$</td>" +
                "</tr></table>";
    }

} // WorkspaceHQLSource
