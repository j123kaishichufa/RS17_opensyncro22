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
 * Created on Oct 5, 2005 Inserts the supplied data to a JDBC database. Data can be a SQL
 * statements.
 * 
 * TODO: Test parameter values: e.g. JDBC driver name can not be empty, otherwise nullpointer exception
 */
package smilehouse.opensyncro.defaultcomponents.jdbc;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

import smilehouse.gui.html.fieldbased.FieldInfo;
import smilehouse.gui.html.fieldbased.GUIContext;
import smilehouse.gui.html.fieldbased.editor.PasswordEditor;
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


public class JDBCDestination implements DestinationIF, GUIConfigurationIF {

    private static final String JDBC_URL = "jdbc_url";
    private static final String USER_NAME = "jdbc_user";
    private static final String PASSWORD_ATTR = "jdbc_password";
    private static final String CLASS_NAME = "jdbc_driver";


    protected PipeComponentData data;
    private Connection con;

    //  --------------
    // GUI definition
    // --------------
    protected static JDBCDestinationGUI gui = new JDBCDestinationGUI();

    protected static class JDBCDestinationGUI extends GUIDefinition {

        public JDBCDestinationGUI() {
            try {
                addSimpleTextFieldForComponent(JDBC_URL, JDBC_URL, 80);
                addSimpleTextFieldForComponent(USER_NAME, USER_NAME, 20);
                addSimpleTextFieldForComponent(CLASS_NAME, CLASS_NAME, 20);

                {
                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            return "";

                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            String valueStr = (String) value;
                            if(valueStr != null && valueStr.length() > 0)
                                ((JDBCDestination) model).data
                                    .setAttribute(PASSWORD_ATTR, valueStr);
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

            } catch(Exception e) {
                Environment.getInstance().log(
                    "Couldn't create GUIContext for JDBCDestination", e);
            }
        }


    }

    public JDBCDestination(Object pipeComponentData) {
        setData((PipeComponentData) pipeComponentData);
    }

    public void setData(PipeComponentData pipeComponentData) {
        this.data = pipeComponentData;
    }

    public PipeComponentData getData() {
        return data;
    }

    public final int getType() {
        return TYPE_DESTINATION;
    }

    public String getName() {
        return "JDBCDestination";
    }

    public String getID() {
        return "smilehouse.opensyncro.defaultcomponents.jdbc.JDBCDestination";
    }

    public String getDescription(Locale locale) {
        return PipeComponentUtils.getDescription(locale, this.getClass());
    }

    public GUIContext getGUIContext() {
        return gui.getGUIContext();
    }

    public String getGUITemplate() {
        return "<table border=0 cellspacing=5><tr><td colspan=\"2\">$" + JDBC_URL + "$</td></tr>"
                + "<tr><td>$" + USER_NAME + "$</td>" + "<td>$" + PASSWORD_ATTR + "$</td></tr>"
                + "<tr><td>$" + CLASS_NAME + "$</td></tr>" + "</table>";
    }

    
    public void take(String data, DestinationInfo info, MessageLogger logger)
            throws FailTransferException, AbortTransferException {
        Statement stmt = null;
        String line = null;
        try {
            stmt = con.createStatement();
            //separate statements because some jdbc driver can't do batch (like mysql connector/j)
            StatementReader sr = new StatementReader(data);
            line = sr.nextStatement();
            int i=1;
            while(line!=null) {
                if(line.length()>0) {
                    int rowCount = stmt.executeUpdate(line);
                    logger.logMessage("Statement " + i + " executed ok, "
                        + rowCount + " rows affected", this, MessageLogger.DEBUG);
                }
                i++;
                line=sr.nextStatement();
            }
            /*
            stmt.addBatch(data);
            stmt.executeBatch();
            */
            
        } catch(SQLException e) {
            String msg = "The SQL resulted in an SQLException. Errorcode: " + e.getErrorCode();
            if(e.getMessage() != null) {
                msg += " Exception message: " + e.getMessage();
            }
            if(e.getSQLState() != null) {
                msg += " SQLState: " + e.getSQLState();
            }
            logger.logMessage(msg+ " See server log for details.", this, MessageLogger.WARNING);

            if(line!=null && line.length() < 3000) {
                msg += " Executed SQL was: \n--------------\n" + line + "\n--------------\n";
            }
            Environment.getInstance().log(msg, e);
            PipeComponentUtils.failTransfer();

        } finally {
            //JDBC resource handling is ugly
            if(stmt != null) {
                try {
                    stmt.close();
                } catch(SQLException e) {
                    logger.logMessage("Failed to close statement", this, MessageLogger.WARNING);
                }
            }
        }

    }

    public void takeAll(String[] data, DestinationInfo info, MessageLogger logger)
            throws FailTransferException, AbortTransferException {
        for(int i = 0; i < data.length; i++) {
            take(data[i], info, logger);
        }

    }

    public int open(DestinationInfo info, MessageLogger logger) throws FailTransferException,
            AbortTransferException {

        String className = this.getData().getAttribute(CLASS_NAME);
        String url = this.getData().getAttribute(JDBC_URL);
        String user = this.getData().getAttribute(USER_NAME);
        String pass = this.getData().getAttribute(PASSWORD_ATTR);

        try {
            Class.forName(className);
            this.con = DriverManager.getConnection(url, user, pass);
        } catch(SQLException e) {
            String msg = "Failed to open a database connection to url '" + url
                    + "' with the given username and password. See server log for details.";
            logger.logMessage(msg, this, MessageLogger.WARNING);
            Environment.getInstance().log(msg, e);
            PipeComponentUtils.failTransfer();
        } catch(ClassNotFoundException e) {
            String msg = "Couldn't find jdbc driver class with classname '" + className
                    + "'.  See server log for details.";
            logger.logMessage(msg, this, MessageLogger.WARNING);
            Environment.getInstance().log(msg, e);
            PipeComponentUtils.failTransfer();
        }
        return ITERATION_OPEN_STATUS_OK;
    }

    public int close(DestinationInfo info, MessageLogger logger) throws FailTransferException,
            AbortTransferException {
        if(con != null)
            try {
                con.close();
            } catch(SQLException e) {
                logger.logMessage(
                    "Failed to close database connection",
                    this,
                    MessageLogger.WARNING);
            }
        return ITERATION_CLOSE_STATUS_OK;
    }

}