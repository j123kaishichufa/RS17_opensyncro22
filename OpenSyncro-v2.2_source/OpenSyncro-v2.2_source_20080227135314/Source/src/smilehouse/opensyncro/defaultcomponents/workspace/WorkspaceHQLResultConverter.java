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

package smilehouse.opensyncro.defaultcomponents.workspace;

import java.io.StringReader;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xerces.util.XMLChar;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import smilehouse.gui.html.fieldbased.FieldInfo;
import smilehouse.gui.html.fieldbased.GUIContext;
import smilehouse.gui.html.fieldbased.editor.PasswordEditor;
import smilehouse.gui.html.fieldbased.model.DefaultModelModifier;
import smilehouse.gui.html.fieldbased.model.ModelModifier;
import smilehouse.openinterface.AccessDeniedException;
import smilehouse.openinterface.ExportResult;
import smilehouse.openinterface.IteratorClosedException;
import smilehouse.openinterface.LoginInfo;
import smilehouse.openinterface.OpenInterfaceException;
import smilehouse.openinterface.OpenInterfaceIF;
import smilehouse.opensyncro.pipes.component.AbortTransferException;
import smilehouse.opensyncro.pipes.component.ConverterIF;
import smilehouse.opensyncro.pipes.component.FailTransferException;
import smilehouse.opensyncro.pipes.component.PipeComponentData;
import smilehouse.opensyncro.pipes.component.PipeComponentUtils;
import smilehouse.opensyncro.pipes.gui.GUIConfigurationIF;
import smilehouse.opensyncro.pipes.gui.GUIDefinition;
import smilehouse.opensyncro.pipes.log.MessageLogger;
import smilehouse.opensyncro.pipes.metadata.ConversionInfo;
import smilehouse.opensyncro.system.Environment;
import smilehouse.util.Utils;


/**
 * WorkspaceHQLResultConverter.java
 * 
 * Performs an HQL (Hibernate) query to Workspace database via OpenInterface and
 * appends the result XML to the current data.
 * 
 * ToDo:
 * 	- check that the encodings match between the input XML and the HQL query result (UTF-8)
 *  - test the validity of HQL object and property names (valid Java class names)
 *  - free HQL query mode
 */

public class WorkspaceHQLResultConverter implements ConverterIF, GUIConfigurationIF {

    
    private static final String OPEN_INTERFACE_HOST_ATTR = "openinterfaceHost";
    private static final String DATABASE_ATTR = "database";
    private static final String USERNAME_ATTR = "userName";
    private static final String PASSWORD_ATTR = "password";

    private static final String RESULTS_PER_ITERATION_ATTR = "resultsPerIteration";
    private static final String SESSION_TIMEOUT_ATTR = "sessionTimeout";

    private static final String XPATH_SELECT_ATTRIBUTE = "xpath_select";
    private static final String XMLROOTELEMENTNAME_ATTR = "xmlRootElementName";
    private static final String HQLOBJECTNAME_ATTR = "hqlObjectName";
    private static final String HQLPROPERTYNAME_ATTR = "hqlPropertyName";

    private static final String OI_HQL_resultTagName = "result";
    
    // Variables to hold user specified parameter values during an iteration session
    private String databaseName;
    private String userName;
    private String password;
    private String openInterfaceHost;
    private String rootXMLElementName;
    private String hqlObjectName;
    private String hqlPropertyName;
    private int sessionTimeout;
    private int resultsPerIteration;
    
    // --------------
    // GUI definition
    // --------------
    private static WorkspaceHQLResultGUI gui = new WorkspaceHQLResultGUI();

    private static class WorkspaceHQLResultGUI extends GUIDefinition {


        public WorkspaceHQLResultGUI() {
            try {
                addSimpleTextFieldForComponent(XPATH_SELECT_ATTRIBUTE, XPATH_SELECT_ATTRIBUTE, 120);
                addSimpleTextFieldForComponent(HQLOBJECTNAME_ATTR, HQLOBJECTNAME_ATTR, 20);
                addSimpleTextFieldForComponent(HQLPROPERTYNAME_ATTR, HQLPROPERTYNAME_ATTR, 20);
                addSimpleTextFieldForComponent(XMLROOTELEMENTNAME_ATTR, XMLROOTELEMENTNAME_ATTR, 20);
                
                addSimpleTextFieldForComponent(OPEN_INTERFACE_HOST_ATTR, OPEN_INTERFACE_HOST_ATTR, 50);
                addSimpleTextFieldForComponent(DATABASE_ATTR, DATABASE_ATTR, 30);
                addSimpleTextFieldForComponent(USERNAME_ATTR, USERNAME_ATTR, 30);

                {
                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            return ((WorkspaceHQLResultConverter) model).getPassword();
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            ((WorkspaceHQLResultConverter) model).setPassword((String) value);
                        }
                    };

                    PasswordEditor editor = new PasswordEditor();
                    editor.setSize(30);

                    FieldInfo fieldInfo = new FieldInfo(
                        PASSWORD_ATTR,
                        PASSWORD_ATTR,
                        modifier,
                        editor);

                    addField(PASSWORD_ATTR, fieldInfo);
                }

                addSimpleTextFieldForComponent(RESULTS_PER_ITERATION_ATTR, RESULTS_PER_ITERATION_ATTR, 5);
                addSimpleTextFieldForComponent(SESSION_TIMEOUT_ATTR, SESSION_TIMEOUT_ATTR, 8);
                
            } catch(Exception e) {
                Environment.getInstance().log(
                    "Couldn't create GUIContext for WorkspaceHQLResultConverter", e);
            }
        }

    }

    public WorkspaceHQLResultConverter() {}

    public String getXPath() {
        String xpath = this.data.getAttribute(XPATH_SELECT_ATTRIBUTE);
        if(xpath != null)
            return xpath;
        else
            return "";
    }

    public void setXPath(String xpath) {
        this.data.setAttribute(XPATH_SELECT_ATTRIBUTE, xpath);
    }

    public String getUserName() {
        String userName = this.data.getAttribute(USERNAME_ATTR);
        if(userName != null)
            return userName;
        else
            return "";
    }

    public void setUserName(String userName) {
        this.data.setAttribute(USERNAME_ATTR, userName);
    }

    public String getPassword() {
        String password = this.data.getAttribute(PASSWORD_ATTR);
        return (password != null ? password : "");
    }

    public void setPassword(String password) {
        this.data.setAttribute(PASSWORD_ATTR, password);
    }
    
    public void setDatabase(String database) {
        this.data.setAttribute(DATABASE_ATTR, database);
    }

    public String getDatabase() {
        String database = this.data.getAttribute(DATABASE_ATTR);
        return (database != null ? database : "");
    }
    
    public void setOpenInterfaceHost(String openInterfaceHost) {
        this.data.setAttribute(OPEN_INTERFACE_HOST_ATTR, openInterfaceHost);
    }

    public String getOpenInterfaceHost() {
        String openInterfaceHost = this.data.getAttribute(OPEN_INTERFACE_HOST_ATTR);
        return (openInterfaceHost != null ? openInterfaceHost : "");
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
    
    public String getXMLRootElementName() {
        String rootElementName = this.data.getAttribute(XMLROOTELEMENTNAME_ATTR);
        return (rootElementName != null ? rootElementName : "");
    }

    public void setXMLRootElementName(String rootElementName) {
        this.data.setAttribute(XMLROOTELEMENTNAME_ATTR, rootElementName);
    }

    public String getHQLObjectName() {
        String hqlObjectName = this.data.getAttribute(HQLOBJECTNAME_ATTR);
        return (hqlObjectName != null ? hqlObjectName : "");
    }

    public void setHQLObjectName(String hqlObjectName) {
        this.data.setAttribute(HQLOBJECTNAME_ATTR, hqlObjectName);
    }

    public String getHQLPropertyName() {
        String hqlPropertyName = this.data.getAttribute(HQLPROPERTYNAME_ATTR);
        return (hqlPropertyName != null ? hqlPropertyName : "");
    }

    public void setHQLPropertyName(String hqlPropertyName) {
        this.data.setAttribute(HQLPROPERTYNAME_ATTR, hqlPropertyName);
    }
    
    public WorkspaceHQLResultConverter(Object pipeComponentData) {
        setData((PipeComponentData) pipeComponentData);
    }

    protected PipeComponentData data;

    public void setData(PipeComponentData data) {
        this.data = data;
    }

    public PipeComponentData getData() {
        return data;
    }

    public final int getType() {
        return TYPE_CONVERTER;
    }

    public String getName() {
        return "WorkspaceHQLResultConverter";
    }

    public String getID() {
        return this.getClass().getName();
    }

    public String getDescription(Locale locale) {
        return PipeComponentUtils.getDescription(locale, this.getClass());
    }

    // No iteration supported, but we test parameters for validity in the iteration session
    // open method.
    public int open(ConversionInfo info, MessageLogger logger) throws FailTransferException {
        
        //boolean removeXMLDeclarationFromResult = true;
        try {
            this.databaseName = getDatabase();
            this.userName = getUserName();
            this.password = getPassword();
            this.openInterfaceHost = getOpenInterfaceHost();
            this.rootXMLElementName = getXMLRootElementName();
            this.hqlObjectName = getHQLObjectName();
            this.hqlPropertyName = getHQLPropertyName();
        
            // Fail if one of the integer parameter values is invalid
            this.sessionTimeout = getIntParameterValue(getSessionTimeout(), "session timeout");
            this.resultsPerIteration = getIntParameterValue(
                getResultsPerIteration(),
                "results per iteration");

            // Fail if results per iteration value is less than one
            if(this.resultsPerIteration < 1) {
                throw new IllegalArgumentException(
                    "invalid value for results per iteration (\"" + this.resultsPerIteration
                    + "\"), should be atleast 1");
            }

            // Fail if any of the required String parameters is empty or null
            testStringParameterValue(this.openInterfaceHost, "OpenInterface URL");
            testStringParameterValue(this.databaseName, "database name");
            testStringParameterValue(this.userName, "user name");
            testStringParameterValue(this.password, "password");
            if(this.rootXMLElementName != null && this.rootXMLElementName.length() > 0) {
                testXMLNameParameterValue(this.rootXMLElementName, "XML root element name");
            }
            testStringParameterValue(this.hqlObjectName, "HQL object name");
            testStringParameterValue(this.hqlPropertyName, "HQL property name");
            
        } catch (IllegalArgumentException e) {
            logger.logMessage("Error: " + e.getMessage(), this, MessageLogger.ERROR);
            return ITERATION_OPEN_STATUS_ERROR;
        }
        
        return ITERATION_OPEN_STATUS_OK;
    }

    public int close(ConversionInfo info, MessageLogger logger) throws FailTransferException {
        return ITERATION_CLOSE_STATUS_OK;
    }

    public void lastBlockStatus(int statusCode) {}


    /**
     * The method actually called by pipe during the conversion. This default implementation uses
     * the convert-method to convert all the input records separately and is usually sufficient so
     * you only have to implement it. If you however need access to all the input when converting
     * (for example Join-converter) you need to override this.
     */
    public String[] convertAll(String[] data, ConversionInfo info, MessageLogger logger)
            throws FailTransferException, AbortTransferException {
        String[][] allResults = new String[data.length][];
        int resultCount = 0;
        for(int i = 0; i < data.length; i++) {
            allResults[i] = convert(data[i], info, logger);
            resultCount += allResults[i].length;
        }
        if(data.length == 1)
            return allResults[0];
        else {
            String[] combinedResult = new String[resultCount];
            int c = 0;
            for(int i = 0; i < allResults.length; i++) {
                for(int j = 0; j < allResults[i].length; j++, c++) {
                    combinedResult[c] = allResults[i][j];
                }
            }
            return combinedResult;
        }
    }

    private String xmlNodeValueToString(org.w3c.dom.Node node) {
        StringBuffer sb = new StringBuffer("");
        int nodeCount = node.getChildNodes().getLength();
        NodeList nodeList = node.getChildNodes();
        for(int j = 0; j < nodeCount; j++) {
            String nodeValue = nodeList.item(j).getNodeValue();
            if(nodeValue != null) {
                sb.append(nodeValue);
            }
        }
        return sb.toString();
    }

    private String createHQLQueryFromXPathResult(String inputData,
                                                 String xpath,
                                                 String hqlObjectName,
                                                 String hqlPropertyName,
                                                 MessageLogger logger) throws FailTransferException {

        StringBuffer idListBuffer = new StringBuffer("");
        StringBuffer hqlQuery = new StringBuffer("");
        
        // queryValues holds a list of unique string results from XPath query.
        // Order of values is preserved.
        LinkedList<String> queryValues = new LinkedList<String>(); 
        
        InputSource inputReader = new InputSource(new StringReader(inputData));
        try {

            DocumentBuilderFactory docBFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = docBFactory.newDocumentBuilder();
            Document document = builder.parse(inputReader);

            NodeList propertyValues = XPathAPI.selectNodeList(document.getDocumentElement(), xpath);

            
            for(int i = 0; i < propertyValues.getLength(); i++) {
                String propertyValue = (
                        Utils.escapeChar(
                            Utils.escapeChar(
                                Utils.escapeChar(
                                    xmlNodeValueToString(propertyValues.item(i)),
                                        '\''),
                                      '\\'),
                                     ':'))
                                .trim();
                
                // Output unique, non-empty property values
                if((propertyValue.length() > 0) && (!queryValues.contains(propertyValue))) {
                    idListBuffer.append("'" + propertyValue + "',");
                    queryValues.add(propertyValue);
                }
                
            }
            if(propertyValues.getLength() > 0 && idListBuffer.length() > 1) {
                // Strip the trailing comma
                String idList = idListBuffer.substring(0, idListBuffer.length() - 1);
                if(idList.length() > 0) {
                    
                    int propertyIndex = 1;
                    if(hqlPropertyName.indexOf(':') != -1) {
                        hqlQuery.append("select s" + propertyIndex + " ");
                    }
                    hqlQuery.append("from " + hqlObjectName + " s" + propertyIndex + " ");

                    String compareProperty = hqlPropertyName;
                    
                    // If there are property names prefixed with colon character (':'),
                    // we need to create "inner join" statements for each
                    if(hqlPropertyName.indexOf(':') != -1) {
                        StringBuffer hqlPropertyJoinSeq = new StringBuffer("");
                        String[] propertiesToJoin = hqlPropertyName.split("\\:");
                        
                        // Set up initial reference to non-joinable properties
                        String previousProperty = propertiesToJoin[0];
                        
                        // Note: the loop starts from propertyIndex 1 (instead of 0)
                        while( propertyIndex < propertiesToJoin.length ) {
                            String currentProperty = propertiesToJoin[propertyIndex];
                            String currentPropertyFirst = null;
                            if(currentProperty.length() != 0) {
                                propertyIndex++;
                                
                                // Extract the first property name if we have multiple properties
                                // separated by period characters
                                if(currentProperty.indexOf('.') == -1) {
                                    currentPropertyFirst = currentProperty;
                                } else {
                                    currentPropertyFirst = currentProperty.substring(0, currentProperty.indexOf('.'));
                                }
                                
                                // Create inner join statement by concatenating previous non-joinable
                                // property names and the current property to join
                                hqlPropertyJoinSeq.append("inner join s" + (propertyIndex - 1) + ".");
                                if(previousProperty.length() > 0) {
                                    hqlPropertyJoinSeq.append(previousProperty + ".");
                                }
                                hqlPropertyJoinSeq.append(currentPropertyFirst + " as s" + propertyIndex + " ");
                                
                                // Update the previous, non-joinable properties reference
                                if(currentProperty.indexOf('.') == -1) {
                                       previousProperty = "";
                                } else {
                                       previousProperty = currentProperty.substring(currentProperty.indexOf('.') + 1);
                                }
                                   
                            }
                            
                            // The 'where' condition should now refer to the last non-joinable properties
                            compareProperty = previousProperty;
                        }

                        hqlQuery.append(hqlPropertyJoinSeq);
                    }
                    
                    hqlQuery.append("where s" + propertyIndex);
                    if(compareProperty.length() > 0) {
                        hqlQuery.append("." + compareProperty);
                    }
                    hqlQuery.append(" in (" + idList + ")");
                    
                    // Warn if the XPath query results had values with colon characters, which can not be
                    // properly escaped
                    if(idList.indexOf(':') != -1) {
                        logger.logMessage("WARNING: HQL query values contain colon (':') character(s)."
                            + " This may cause the query processing to fail or to produce unexpected"
                            + " results in older Workspace webshop versions",
                            this, MessageLogger.WARNING);
                    }
                }
            }

        } catch(Exception ex) {
            logger.logMessage(
                ("Exception while running XPath query against the input data"),
                this,
                MessageLogger.ERROR);
            logger.logMessage(ex.getMessage(), this, MessageLogger.ERROR);
            Environment.getInstance().log(
                "Exception while running XPath query against the input data", ex);
            PipeComponentUtils.failTransfer();
        }

        return hqlQuery.toString();
    }


    private void testStringParameterValue(String paramValue, String paramDesc)
            throws IllegalArgumentException {

        if((paramValue == null) || (paramValue.length() == 0)) {
            throw new IllegalArgumentException(paramDesc + " not specified");
        }
    }


    private void testXMLNameParameterValue(String paramValue, String paramDesc)
    		throws IllegalArgumentException {

        if((paramValue == null) || (paramValue.length() == 0) ||
                (XMLChar.isValidName(paramValue) == false)
                ) {
            throw new IllegalArgumentException(paramDesc + " is not a valid XML name");
        }
    }

    
    private int getIntParameterValue(String paramValue, String paramDesc)
            throws IllegalArgumentException {

        int result = 0;
        try {
            result = Integer.parseInt(paramValue);
        } catch(NumberFormatException e) {
            throw new IllegalArgumentException("invalid parameter value for " + paramDesc +
                							   ", should be an integer");
        }
        return result;
    }


    public String[] convert(String data, ConversionInfo info, MessageLogger logger)
            throws FailTransferException, AbortTransferException {

        String oiEndpointAddress;
        StringBuffer hqlResult = new StringBuffer("");

        // Run XPath query against the input data
        logger.logMessage(
            "Running XPath query \"" + getXPath() + "\" against the input data",
            this,
            MessageLogger.DEBUG);

        String hqlQuery = createHQLQueryFromXPathResult(
            data,
            getXPath(),
            hqlObjectName,
            hqlPropertyName,
            logger);

        if(hqlQuery == null) {
            logger.logMessage(
                "XPath query returned empty result, nothing to query from Workspace",
                this,
                MessageLogger.WARNING);

        } else {

            logger.logMessage("Created HQL query \"" + hqlQuery
                    + "\", sending it to Workspace OpenInterface", this, MessageLogger.DEBUG);

            // OpenInterface connection preparations
            LoginInfo login = WorkspaceOIUtils.getLoginInfo(databaseName, userName, password);

            oiEndpointAddress = WorkspaceOIUtils.getOIEndpointAddress(
                databaseName,
                openInterfaceHost,
                logger,
                this);

            OpenInterfaceIF oi = WorkspaceOIUtils.getOpenInterfaceIF(
                oiEndpointAddress,
                openInterfaceHost);

            // Send HQL query to OpenInterface and get a result iterator
            String iteratorId = WorkspaceOIUtils.openHQLIterator(
                oi,
                login,
                hqlQuery,
                sessionTimeout,
                logger,
                this);

            ExportResult exportResult = null;

            // Retrieve all results appending them to a StringBuffer
            try {
                do {
                    exportResult = oi.iterate(login, iteratorId, resultsPerIteration);
                    if(exportResult != null) {
                        hqlResult.append(stripResultTag(Utils.stripXMLdeclaration(exportResult
                            .getXml())));
                    }
                } while(exportResult != null);

            } catch(AccessDeniedException e) {
                logger.logMessage(
                        "Access to OpenInterface was denied. Check your username and password settings.",
                        this,
                        MessageLogger.ERROR);
                PipeComponentUtils.failTransfer();

            } catch(IteratorClosedException e) {
                logger.logMessage(
                        "OpenInterface HQL iterator had been closed due to inactivity. Try increasing session timeout value.",
                        this,
                        MessageLogger.ERROR);
                PipeComponentUtils.failTransfer();
            
            } catch(OpenInterfaceException e) {
                logger.logMessage(
                    "OpenInterfaceException while iterating over the HQL query's result set",
                    this,
                    MessageLogger.ERROR);
                logger.logMessage(e.getMessage(), this, MessageLogger.ERROR);
                Environment.getInstance().log(
                    "OpenInterfaceException while iterating over the HQL query's result set",
                    e);
                PipeComponentUtils.failTransfer();
            
            } catch(RemoteException e) {
                logger.logMessage("RemoteException from OpenInterface", this, MessageLogger.ERROR);
                Environment.getInstance().log("RemoteException from OpenInterface", e);
                PipeComponentUtils.failTransfer();
            
            } finally {
                // Close iterator
                WorkspaceOIUtils.closeHQLIterator(oi, login, iteratorId, logger, this);
                }
            }
            
        // Concatenate input XML with HQL query result XML, using the XML declaration
        // (=encoding information) from the input XML.
        String resultXMLcontent;
        String xmlDeclaration = Utils.getXMLdeclaration(data);
        if(xmlDeclaration == null) {
            resultXMLcontent = "";
        } else {
            resultXMLcontent = xmlDeclaration + "\n";
        }

        if(rootXMLElementName != null && rootXMLElementName.length() > 0) {
            // Write output contained in root XML element
            resultXMLcontent += "<" + rootXMLElementName + ">"
                    + Utils.stripXMLdeclaration(data) + "<" + OI_HQL_resultTagName + ">"
                    + hqlResult + "</" + OI_HQL_resultTagName + ">" + "</" + rootXMLElementName
                    + ">";
        } else {
            // Write output without root XML element
            resultXMLcontent += Utils.stripXMLdeclaration(data) + hqlResult;
        }

        return new String[] {resultXMLcontent};
        
    }

    // A quick hacks to remove the XML root tag
    // used by the OpenInterface to contain HQL query results.
    // WARNING: does NOT check whether the start/end tags
    // actually belong to the root element!
    
    // ToDo: a generic, reliable removeXMLRootTag utility method
    private String stripResultTag(String xmlData) {
        return stripResultCloseTag(xmlData.replaceFirst("<"
                                   + OI_HQL_resultTagName + ">", ""));
}

    private String stripResultCloseTag(String xmlData) {
        int j = xmlData.lastIndexOf("</" + OI_HQL_resultTagName + ">");
		if (j == -1) {
		    // Error: the end tag was not found -> return the original data instead
			return xmlData;
		}
		return xmlData.substring(0, j);
    }
    
    

    public GUIContext getGUIContext() {
        return gui.getGUIContext();
    }

    public String getGUITemplate() {
        return "<table border=\"0\"><tr><td>$" + OPEN_INTERFACE_HOST_ATTR + "$</td>"
                + "<td>$" + DATABASE_ATTR + "$</td></tr><tr><td>$" + USERNAME_ATTR + "$</td><td>$"
                + PASSWORD_ATTR + "$</td></tr>" +
                "<tr><td>$" + SESSION_TIMEOUT_ATTR + "$</td><td>$" + RESULTS_PER_ITERATION_ATTR
                + "$</td></tr>" +
                "<tr><td colspan=\"2\"><hr></td></tr>" +
                "<tr><td colspan=\"2\">$" + XPATH_SELECT_ATTRIBUTE + "$</td></tr>" +
                "<tr><td>$" + HQLOBJECTNAME_ATTR + "$</td><td>$" + HQLPROPERTYNAME_ATTR + "$</td></tr>" +
                "<tr><td>$" + XMLROOTELEMENTNAME_ATTR + "$</td></tr>" +
                "</table>";
    }

}// WorkspaceHQLResultConverter
