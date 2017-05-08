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
 * Created on Oct 26, 2005
 */

package smilehouse.opensyncro.defaultcomponents.jdbc;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import smilehouse.gui.html.fieldbased.FieldInfo;
import smilehouse.gui.html.fieldbased.GUIContext;
import smilehouse.gui.html.fieldbased.editor.DefaultSelectOption;
import smilehouse.gui.html.fieldbased.editor.PasswordEditor;
import smilehouse.gui.html.fieldbased.editor.SelectEditor;
import smilehouse.gui.html.fieldbased.model.DefaultModelModifier;
import smilehouse.gui.html.fieldbased.model.ModelModifier;
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

public class JDBCConverter implements ConverterIF, GUIConfigurationIF {

    private static final String USER_NAME = "user_name";
    private static final String PASSWORD_ATTR="password";
    private static final String DATABASE_NAME="db_name";
    private static final String DATABASE_TYPE_ATTR="db_type";
    private static final String CLASS_NAME="class_name";
    private static final String HOST_ATTR = "host";
    private static final String PORT_ATTR = "port";
    private static final String CHARSET_ATTR = "charset";
    private static final String DEFAULT_CHARSET = "UTF-8";

    private static final String[] CHARSETS = {
            "ISO-8859-1",
            "UTF-8",
            "UTF-16",
            "US-ASCII",
            "ISO-8859-15"};

    private Connection con;
    private String port;
    
    //  --------------
    // GUI definition
    // --------------
    protected static JDBCConverterGUI gui = new JDBCConverterGUI();

    protected static class JDBCConverterGUI extends GUIDefinition {

        public JDBCConverterGUI() {
            try {

                addSimpleTextFieldForComponent(USER_NAME, USER_NAME, 20);
                addSimpleTextFieldForComponent(DATABASE_NAME, DATABASE_NAME, 20);
                addSimpleTextFieldForComponent(DATABASE_TYPE_ATTR, DATABASE_TYPE_ATTR, 20);
                addSimpleTextFieldForComponent(CLASS_NAME, CLASS_NAME, 40);
                addSimpleTextFieldForComponent(HOST_ATTR, HOST_ATTR, 40);
                addSimpleTextFieldForComponent(PORT_ATTR, PORT_ATTR, 5);
                {
                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            return "";

                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            String valueStr = (String) value;
                            if(valueStr != null && valueStr.length() > 0)
                                ((JDBCConverter) model).data.setAttribute(PASSWORD_ATTR, valueStr);
                        }
                    };

                    PasswordEditor editor = new PasswordEditor();
                    editor.setSize(10);

                    FieldInfo fieldInfo = new FieldInfo(
                        PASSWORD_ATTR,
                        PASSWORD_ATTR,
                        modifier,
                        editor);

                    addField(PASSWORD_ATTR, fieldInfo);
                }
                {
                    //set unique id and description labelkey
                    String id = CHARSET_ATTR;

                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws FailTransferException,
                                AbortTransferException {
                            String value = ((JDBCConverter) model).getData().getAttribute(
                                CHARSET_ATTR);
                            return value != null ? value : DEFAULT_CHARSET;
                        }

                        public void setModelValue(Object model, Object value)
                                throws FailTransferException, AbortTransferException {
                            ((JDBCConverter) model).getData().setAttribute(
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
                
            } catch(Exception e) {
                Environment.getInstance().log(
                    "Couldn't create GUIContext for JDBCConverter", e);
            }
        }


    }
   
    // ---

    public JDBCConverter(Object pipeComponentData) {
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
        return TYPE_CONVERTER;
    }

    public String getName() {
        return "JDBCConverter";
    }

    public String getID() {
        return "smilehouse.opensyncro.defaultcomponents.jdbc.JDBCConverter";
    }

    public String getDescription(Locale locale) {
        return PipeComponentUtils.getDescription(locale, this.getClass());
    }

    // No iteration supported, but session the open() is used to create the database connection
    public int open(ConversionInfo info, MessageLogger logger) throws FailTransferException {

        try {
            if(this.getData().getAttribute(PORT_ATTR).length() == 0)
                port = "";
            else
                port = ":" + this.getData().getAttribute(PORT_ATTR);
            Class.forName(this.getData().getAttribute(CLASS_NAME)).newInstance();
            con = DriverManager.getConnection("jdbc:" + this.getData().getAttribute(DATABASE_TYPE_ATTR)
                    + "://" + this.getData().getAttribute(HOST_ATTR) + port + "/"
                    + this.getData().getAttribute(DATABASE_NAME), this.getData().getAttribute(
                USER_NAME), this.getData().getAttribute(PASSWORD_ATTR));
        } catch(Exception e) {
            logger.logMessage("Error: " + e.getMessage(), this, MessageLogger.ERROR);
            PipeComponentUtils.failTransfer();
        }

        return ITERATION_OPEN_STATUS_OK;

    }

	
    public int close(ConversionInfo info, MessageLogger logger) throws FailTransferException {
        if(con != null)
			try {
				con.close();

			} catch (SQLException e) {
				logger.logMessage("Failed to close database connection", this, MessageLogger.WARNING);
			}
    	
    	return ITERATION_CLOSE_STATUS_OK;
    }

    public void lastBlockStatus(int statusCode) {}

    // ---

    public GUIContext getGUIContext() {
        return gui.getGUIContext();
    }
    public String getGUITemplate() {
        return "<table border=\"0\">" +
        		"<tr><td>$" + HOST_ATTR + "$</td>" +
        		"<td>$" + PORT_ATTR + "$</td></tr>" +
        		"<tr><td>$" + USER_NAME + "$</td>" +
                "<td>$" + PASSWORD_ATTR + "$</td></tr>" +
                "<tr><td>$" + DATABASE_NAME + "$</td></tr>" +
                "<tr><td>$" + CLASS_NAME + "$</td>" +
                "<td>$" + DATABASE_TYPE_ATTR + "$</td></tr>" +
                "<tr><td colspan=\"2\">$" + CHARSET_ATTR + "$</td></tr>" +
                "</table>";
    }

    
    private ResultSet makeQuery(String db_query) 
    throws SQLException
    
    {
       Statement stmt = con.createStatement();
       return stmt.executeQuery(db_query);
 
    }
    
    private Document makeDocument(ResultSet results,MessageLogger log) throws ParserConfigurationException, 
    SQLException {
    	
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    	DocumentBuilder builder =factory.newDocumentBuilder();
    	Document doc = builder.newDocument();
        Element el = doc.createElement("Results");
        doc.appendChild(el);
        ResultSetMetaData rsmd=results.getMetaData();
        int cols=rsmd.getColumnCount();
        int rows=0;
        while (results.next()) {
        	rows++;
            Element row = doc.createElement("Row");
            el.appendChild(row);
            for (int ii = 1; ii <= cols; ii++) {
               String columnName = rsmd.getColumnName(ii);
               Object value = results.getObject(ii);
               Element node = doc.createElement(columnName);
               if(value==null) continue;
               node.appendChild(doc.createTextNode(value.toString()));
               row.appendChild(node);
             
            }
          }
        log.logMessage(rows + " rows returned from database", this, MessageLogger.DEBUG);
        return doc;
    }
    
    private String getDocumentAsXml(Document d) throws TransformerConfigurationException, TransformerException {
        DOMSource domSource = new DOMSource(d);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.ENCODING,this.getData().getAttribute(CHARSET_ATTR));

        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        java.io.StringWriter sw = new java.io.StringWriter();
        StreamResult sr = new StreamResult(sw);
        transformer.transform(domSource, sr);
        return sw.toString();
    	
    }


	public String[] convert(String data, ConversionInfo info, MessageLogger logger)
            throws FailTransferException, AbortTransferException {

        String[] dataArray = new String[1];

        try {

            ResultSet rs = makeQuery(data);

            Document docout = makeDocument(rs, logger);

            dataArray[0] = getDocumentAsXml(docout);
        }

        catch(SQLException e) {
            logger.logMessage("Database error: " + e.getMessage(), this, MessageLogger.ERROR);
            PipeComponentUtils.failTransfer();

        } catch(Exception e) {
            logger.logMessage("Error: " + e.getMessage(), this, MessageLogger.ERROR);
            PipeComponentUtils.failTransfer();
        }

        return dataArray;

    }

	public String[] convertAll(String[] data, ConversionInfo info, MessageLogger logger) throws FailTransferException, AbortTransferException {
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

}