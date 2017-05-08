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
 * Created on Oct 15, 2007
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
import smilehouse.gui.html.fieldbased.editor.BooleanEditor;
import smilehouse.gui.html.fieldbased.editor.DefaultSelectOption;
import smilehouse.gui.html.fieldbased.editor.PasswordEditor;
import smilehouse.gui.html.fieldbased.editor.SelectEditor;
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
import smilehouse.util.Utils;

public class JDBCSource implements SourceIF, GUIConfigurationIF {
	private static final String JDBC_URL_ATTR = "jdbc_url";

	private static final String USER_NAME_ATTR = "user_name";

	private static final String PASSWORD_ATTR = "password";

	private static final String JDBC_DRIVER_NAME_ATTR = "jdbc_driver";

	private static final String CHARSET_ATTR = "charset";

	private static final String DEFAULT_CHARSET = "UTF-8";

	private static final String RESULTS_PER_ITERATION_ATTR = "resultsPerIteration";

	private static final String XMLDECLARATION_FOR_EACH_BLOCK_ATTR = "xmlDeclarationForEachBlock";

	private static final String SQL_QUERY_ATTR = "sql_query";

	private static final String[] CHARSETS = { "ISO-8859-1", "UTF-8", "UTF-16", "US-ASCII", "ISO-8859-15" };

	private Connection con;

	private ResultSet rs;
	
    // Number of results to retrieve per iteration step
    private int resultsPerIteration;

	// Iteration number, used to determine should the XML declaration be output
	private int iterationNumber;

	// Determines if all data have been output during "All results at once" mode
	private boolean allDataOutput;

	protected PipeComponentData data;

	// --------------
	// GUI definition
	// --------------
	protected static JDBCSourceGUI gui = new JDBCSourceGUI();

	protected static class JDBCSourceGUI extends GUIDefinition {

		public JDBCSourceGUI() {
			try {
				addSimpleTextFieldForComponent(JDBC_URL_ATTR, JDBC_URL_ATTR, 80);
				addSimpleTextFieldForComponent(USER_NAME_ATTR, USER_NAME_ATTR, 20);
				addSimpleTextFieldForComponent(JDBC_DRIVER_NAME_ATTR, JDBC_DRIVER_NAME_ATTR, 20);
				addSimpleTextFieldForComponent(RESULTS_PER_ITERATION_ATTR, RESULTS_PER_ITERATION_ATTR, 20);
				addSimpleTextAreaFieldForComponent(SQL_QUERY_ATTR, SQL_QUERY_ATTR, 80, 10);
				{
					ModelModifier modifier = new DefaultModelModifier() {
						public Object getModelValue(Object model) throws Exception {
							return "";

						}

						public void setModelValue(Object model, Object value) throws Exception {
							String valueStr = (String) value;
							if (valueStr != null && valueStr.length() > 0)
								((JDBCSource) model).setPassword(valueStr);
						}
					};

					PasswordEditor editor = new PasswordEditor();
					editor.setSize(10);

					FieldInfo fieldInfo = new FieldInfo(PASSWORD_ATTR, PASSWORD_ATTR, modifier, editor);
					addField(PASSWORD_ATTR, fieldInfo);
				}
				{
					ModelModifier modifier = new DefaultModelModifier() {
						public Object getModelValue(Object model) throws FailTransferException, AbortTransferException {
							String value = ((JDBCSource) model).getCharset();
							return value != null ? value : DEFAULT_CHARSET;
						}

						public void setModelValue(Object model, Object value) throws FailTransferException, AbortTransferException {
							((JDBCSource) model).setCharset((String) value);
						}
					};

					SelectEditor editor = new SelectEditor();
					for (int i = 0; i < CHARSETS.length; i++)
						editor.addOption(new DefaultSelectOption(CHARSETS[i], CHARSETS[i]));

					FieldInfo fieldInfo = new FieldInfo(CHARSET_ATTR, CHARSET_ATTR, modifier, editor);
					addField(CHARSET_ATTR, fieldInfo);
				}
				{
					ModelModifier modifier = new DefaultModelModifier() {
						public Object getModelValue(Object model) throws Exception {
							return ((JDBCSource) model).getXMLdeclarationForEachBlock();
						}

						public void setModelValue(Object model, Object value) throws Exception {
							((JDBCSource) model).setXMLdeclarationForEachBlock((Boolean) value);
						}
					};

					BooleanEditor editor = new BooleanEditor();

					FieldInfo fieldInfo = new FieldInfo(XMLDECLARATION_FOR_EACH_BLOCK_ATTR, XMLDECLARATION_FOR_EACH_BLOCK_ATTR, modifier, editor);
					addField(XMLDECLARATION_FOR_EACH_BLOCK_ATTR, fieldInfo);
				}
			} catch (Exception e) {
				Environment.getInstance().log("Couldn't create GUIContext for JDBCSource", e);
			}
		}

	}

	public JDBCSource(Object pipeComponentData) {
		setData((PipeComponentData) pipeComponentData);
	}

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
		return "JDBCSource";
	}

	public String getID() {
		return "smilehouse.opensyncro.defaultcomponents.jdbc.JDBCSource";
	}

	public String getDescription(Locale locale) {
		return PipeComponentUtils.getDescription(locale, this.getClass());
	}

	public int open(SourceInfo info, MessageLogger logger) throws FailTransferException {
		// Reset iterationNumber
		this.iterationNumber = 0;
		
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
                + "\"), should be at least 0 (which returns all results at first iteration)",
                this, MessageLogger.ERROR);
            return ITERATION_OPEN_STATUS_ERROR;
        }
		
		StatementReader str = new StatementReader(getSqlQuery());
		String query = str.nextStatement();

		if (query == null || str.nextStatement() != null) {
			logger.logMessage("Enter just one sql Select-query", this, MessageLogger.ERROR);
			throw new FailTransferException();
		}

		try {
			Class.forName(getJdbcDriverName()).newInstance();

			con = DriverManager.getConnection(getJdbcUrl(), getUsername(), getPassword());

		} catch (Exception e) {
			logger.logMessage("Error: " + e.getMessage(), this, MessageLogger.ERROR);
			PipeComponentUtils.failTransfer();
		}

		return ITERATION_OPEN_STATUS_OK;

	}

	public String[] give(SourceInfo info, MessageLogger logger) throws FailTransferException, AbortTransferException {

		// all data have been output in the first iteration
		if (allDataOutput)
			return null;

		String[] dataArray = new String[1];

		try {
			// make the query only once
			if (iterationNumber == 0){
				rs = makeQuery(getSqlQuery());
			}
			
			// Increment iteration number
			this.iterationNumber++;
			
			Document docout = makeDocument(resultsPerIteration, rs, logger);

			// no data was retrieved
			if (docout == null)
				return null;
			
			// if "results per iteration" is 0, all data are output at once
			if (resultsPerIteration != 0) {
				// Normal iteration mode
				if ((this.iterationNumber > 1) && (getXMLdeclarationForEachBlock() == false)) {
					dataArray[0] = Utils.stripXMLdeclaration(getDocumentAsXml(docout));
				} else {
					dataArray[0] = getDocumentAsXml(docout);
				}
			} else {
				// All results at once mode
				dataArray[0] = getDocumentAsXml(docout);
				allDataOutput = true;
			}

		}

		catch (SQLException e) {
			logger.logMessage("Database error: " + e.getMessage(), this, MessageLogger.ERROR);
			PipeComponentUtils.failTransfer();

		} catch (Exception e) {
			logger.logMessage("Error: " + e.getMessage(), this, MessageLogger.ERROR);
			PipeComponentUtils.failTransfer();
		}
		
		return dataArray;

	}

	public int close(SourceInfo info, MessageLogger logger) throws FailTransferException {
		if (rs != null)
			try {
				rs.close();

			} catch (SQLException e) {
				logger.logMessage("Failed to close resultset", this, MessageLogger.WARNING);
			}
		if (con != null)
			try {
				con.close();

			} catch (SQLException e) {
				logger.logMessage("Failed to close database connection", this, MessageLogger.WARNING);
			}

		return ITERATION_CLOSE_STATUS_OK;
	}

	public Document makeDocument(int resultsPerIteration, ResultSet results, MessageLogger logger) throws ParserConfigurationException, SQLException {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.newDocument();
		Element el = doc.createElement("Results");
		doc.appendChild(el);
		ResultSetMetaData rsmd = results.getMetaData();
		int cols = rsmd.getColumnCount();

		// counter of returned rows per block
		int rowsOfBlock = 0;
		int endOfBlock = results.getRow() + resultsPerIteration;
		
		// All results have been returned at once or resultset has reached its
		// end
		if (allDataOutput || results.isAfterLast())
			return null;

		while ((resultsPerIteration == 0 || results.getRow() < endOfBlock) && results.next()) {
			rowsOfBlock++;
			Element row = doc.createElement("Row");
			el.appendChild(row);
			for (int ii = 1; ii <= cols; ii++) {
				String columnName = rsmd.getColumnName(ii);
				Object value = results.getObject(ii);
				Element node = doc.createElement(columnName);
				if (value == null)
					continue;
				node.appendChild(doc.createTextNode(value.toString()));
				row.appendChild(node);
			}
		}
		// An empty resultset was returned
		if (rowsOfBlock == 0)
			return null;
		logger.logMessage(rowsOfBlock + " rows returned from database", this, MessageLogger.DEBUG);
		return doc;
	}

	private String getDocumentAsXml(Document d) throws TransformerConfigurationException, TransformerException {
		DOMSource domSource = new DOMSource(d);
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.ENCODING, getCharset());

		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");

		java.io.StringWriter sw = new java.io.StringWriter();
		StreamResult sr = new StreamResult(sw);
		transformer.transform(domSource, sr);
		return sw.toString();

	}

	private ResultSet makeQuery(String db_query) throws SQLException {
		Statement stmt = con.createStatement();
		return stmt.executeQuery(db_query);
	}

	public void lastBlockStatus(int statusCode) {
	}

	public GUIContext getGUIContext() {
		return gui.getGUIContext();
	}

	public String getGUITemplate() {
		return 
		"<table border=\"0\">" + 
		"<tr><td>$" + JDBC_URL_ATTR + "$</td></tr>" + 
		"<tr><td>$" + USER_NAME_ATTR + "$</td>" + "<td>$" + PASSWORD_ATTR + "$</td></tr>" + 
		"<tr><td>$" + JDBC_DRIVER_NAME_ATTR + "$</td>" + 
		"<tr><td colspan=\"2\">$" + CHARSET_ATTR + "$</td></tr>" + 
		"<tr><td>$" + RESULTS_PER_ITERATION_ATTR + "$</td>" + "<td>$" + XMLDECLARATION_FOR_EACH_BLOCK_ATTR + "$</td></tr>" + 
		"<tr><td>$" + SQL_QUERY_ATTR + "$</td></tr>" + 
		"</table>";
	}

	public void setJdbcUrl(String jdbc_url) {
		this.data.setAttribute(JDBC_URL_ATTR, jdbc_url);
	}

	public String getJdbcUrl() {
		String jdbc_url = this.data.getAttribute(JDBC_URL_ATTR);
		return (jdbc_url != null ? jdbc_url : "");
	}

	public void setUsername(String username) {
		this.data.setAttribute(USER_NAME_ATTR, username);
	}

	public String getUsername() {
		String username = this.data.getAttribute(USER_NAME_ATTR);
		return (username != null ? username : "");
	}

	public void setPassword(String password) {
		this.data.setAttribute(PASSWORD_ATTR, password);
	}

	public String getPassword() {
		String password = this.data.getAttribute(PASSWORD_ATTR);
		return (password != null ? password : "");
	}

	public void setJdbcDriverName(String jdbc_driver) {
		this.data.setAttribute(JDBC_DRIVER_NAME_ATTR, jdbc_driver);
	}

	public String getJdbcDriverName() {
		String jdbc_driver = this.data.getAttribute(JDBC_DRIVER_NAME_ATTR);
		return (jdbc_driver != null ? jdbc_driver : "");
	}

	public void setCharset(String charset) {
		this.data.setAttribute(CHARSET_ATTR, charset);
	}

	public String getCharset() {
		String charset = this.data.getAttribute(CHARSET_ATTR);
		return (charset != null ? charset : "");
	}

	public void setSqlQuery(String sql_query) {
		this.data.setAttribute(SQL_QUERY_ATTR, sql_query);
	}

	public String getSqlQuery() {
		String sql_query = this.data.getAttribute(SQL_QUERY_ATTR);
		return (sql_query != null ? sql_query : "");
	}

	public void setResultsPerIteration(String resultsPerIteration) {
		this.data.setAttribute(RESULTS_PER_ITERATION_ATTR, resultsPerIteration);
	}

	public String getResultsPerIteration() {
		String resultsPerIteration = this.data.getAttribute(RESULTS_PER_ITERATION_ATTR);
		return (resultsPerIteration != null ? resultsPerIteration : "");
	}

	public void setXMLdeclarationForEachBlock(Boolean xmlDeclarationForEachBlock) {
		this.data.setAttribute(XMLDECLARATION_FOR_EACH_BLOCK_ATTR, xmlDeclarationForEachBlock != null ? xmlDeclarationForEachBlock.toString() : "false");
	}

	public Boolean getXMLdeclarationForEachBlock() {
		return new Boolean(this.data.getAttribute(XMLDECLARATION_FOR_EACH_BLOCK_ATTR));
	}

}