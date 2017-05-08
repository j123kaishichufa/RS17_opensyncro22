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
 * Created on 27.12.2004
 */

// FIXME: 

package smilehouse.opensyncro.defaultcomponents.workspace;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import smilehouse.gui.html.fieldbased.FieldInfo;
import smilehouse.gui.html.fieldbased.GUIContext;
import smilehouse.gui.html.fieldbased.editor.DefaultSelectOption;
import smilehouse.gui.html.fieldbased.editor.PasswordEditor;
import smilehouse.gui.html.fieldbased.editor.SelectEditor;
import smilehouse.gui.html.fieldbased.formatter.IntegerFormatter;
import smilehouse.gui.html.fieldbased.model.DefaultModelModifier;
import smilehouse.gui.html.fieldbased.model.ModelModifier;
import smilehouse.openinterface.AccessDeniedException;
import smilehouse.openinterface.ImportResult;
import smilehouse.openinterface.LoginInfo;
import smilehouse.openinterface.OpenInterfaceException;
import smilehouse.openinterface.OpenInterfaceIF;
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

public class RemotePriceListDestination implements DestinationIF, GUIConfigurationIF {

    // ------------------------
    // Constants for attributes
    // ------------------------
    private static final String HOST_ATTR = "host";
    private static final String DATABASE_ATTR = "database";
    private static final String USER_ATTR = "user";
    private static final String PASSWORD_ATTR = "password";
    private static final String IMPORT_MODE_ATTR = "import_mode";
    private static final String PROTECTED_GROUPS_ATTR = "protected_groups";
    private static final String CREATE_GROUPS_ATTR = "create_groups";

    private static final String ADDITIVE_GROUP_UPDATE_ATTR = "additive_group_update";
    private static final String ADDITIVE_OPTION_UPDATE_ATTR = "additive_option_update";
    
    private static final LinkedHashMap IMPORT_MODES = new LinkedHashMap();
    static {
            IMPORT_MODES.put(new Integer(0), "insert_or_update");
            IMPORT_MODES.put(new Integer(1), "insert");
            IMPORT_MODES.put(new Integer(2), "update");
            // IMPORT_MODE 3 is reserved
            IMPORT_MODES.put(new Integer(4), "total_replace");
            }

    // OpenInterfaceIF reference
    private OpenInterfaceIF oi = null;
    
    private LoginInfo login = null;
    
    private Integer importMode;
    
    private static GUIDefinition gui = new PriceListDestinationGUI();

    private static class PriceListDestinationGUI extends GUIDefinition {

        public PriceListDestinationGUI() {
            try {
                // Host
                addSimpleTextFieldForComponent(HOST_ATTR, HOST_ATTR, 40);
                // Database
                addSimpleTextFieldForComponent(DATABASE_ATTR, DATABASE_ATTR, 20);
                // User
                addSimpleTextFieldForComponent(USER_ATTR, USER_ATTR, 20);
                // Password
                {
                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            return "";
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            String valueStr = (String) value;
                            if(valueStr != null && valueStr.length() > 0)
                                ((DestinationIF) model).getData().setAttribute(PASSWORD_ATTR, valueStr);
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
                // Import type
                {
                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            Integer value = ((DestinationIF) model)
                                .getData().getIntegerAttribute(IMPORT_MODE_ATTR);
                            return value != null ? value : new Integer(0);
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            int intValue = value != null ? ((Integer) value).intValue() : 0;
                            ((DestinationIF) model).getData().setAttribute(IMPORT_MODE_ATTR, intValue);
                        }
                    };

                    SelectEditor editor = new SelectEditor();

                    for(Iterator i = IMPORT_MODES.keySet().iterator(); i.hasNext();) {
                        
                        Integer optionNumber = (Integer) i.next();
                        String optionName = (String) IMPORT_MODES.get(optionNumber);
                        
                        editor.addOption(new DefaultSelectOption(optionNumber, optionName));
                        
                    }
                    
                    editor.setFormatter(new IntegerFormatter());

                    //and finally create the configurationObject
                    FieldInfo fieldInfo = new FieldInfo(
                        IMPORT_MODE_ATTR,
                        IMPORT_MODE_ATTR,
                        modifier,
                        editor);

                    //add the configuration to the context for usage in the http-requests.
                    addField(IMPORT_MODE_ATTR, fieldInfo);
                }
            } catch(Exception e) {
                Environment.getInstance().log(
                    "Could not create GUI for RemotePriceListDestination",
                    e);
            }
        }
    }

    public GUIContext getGUIContext() {
        return gui.getGUIContext();
    }

    public String getGUITemplate() {
        return "<table border=\"0\">" + "<tr><td>$" + HOST_ATTR + "$</td><td>$" + DATABASE_ATTR
                + "$</td></tr>" + "<tr><td>$" + USER_ATTR + "$</td><td>$" + PASSWORD_ATTR
                + "$</td></tr>" + "<tr><td colspan=\"2\"><hr></td></tr>" + "<tr><td>$"
                + IMPORT_MODE_ATTR + "$</td><td>$" + CREATE_GROUPS_ATTR + "$<br>$"
                + ADDITIVE_GROUP_UPDATE_ATTR + "$<br>$" + ADDITIVE_OPTION_UPDATE_ATTR + "$</td>"
                + "</tr>" + "<tr><td colspan=\"2\">$"
                + PROTECTED_GROUPS_ATTR + "$</td></tr>" + "</table>";

    }
    
    public RemotePriceListDestination( Object pipeComponentData ) {
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
        return TYPE_DESTINATION;
    }
    
    public String getName() {
        return "RemotePriceListDestination";
    }

    public String getID() {
        return this.getClass().getName();
    }

    public String getDescription(Locale locale) {
        return PipeComponentUtils.getDescription(locale, this.getClass());
    }
    
    // Dummy methods due to no iteration supported
    public int open(DestinationInfo info, MessageLogger logger) throws FailTransferException {
        
        // --------------------------------
        // Check that the attributes are OK
        // --------------------------------

        // Host
        String host = this.data.getAttribute(HOST_ATTR);
        if(host == null || host.length() == 0) {
            logger.logMessage("Host not set", this, MessageLogger.ERROR);
            return ITERATION_OPEN_STATUS_ERROR;
        }

        // Database
        String database = this.data.getAttribute(DATABASE_ATTR);
        if(database == null || database.length() == 0) {
            logger.logMessage("Database not set", this, MessageLogger.ERROR);
            return ITERATION_OPEN_STATUS_ERROR;
        }

        // User
        String user = this.data.getAttribute(USER_ATTR);
        if(user == null || user.length() == 0) {
            logger.logMessage("User not set", this, MessageLogger.ERROR);
            return ITERATION_OPEN_STATUS_ERROR;
        }

        // Password
        String password = this.data.getAttribute(PASSWORD_ATTR);
        if(password == null || password.length() == 0) {
            logger.logMessage("Password not set", this, MessageLogger.ERROR);
            return ITERATION_OPEN_STATUS_ERROR;
        }

        // Import mode
        Integer importMode = this.data.getIntegerAttribute(IMPORT_MODE_ATTR);
        if(importMode == null) {
            logger.logMessage("Import mode not set", this, MessageLogger.ERROR);
            return ITERATION_OPEN_STATUS_ERROR;
        }

        // Store import mode
        this.importMode = importMode;
        
        // Prepare OpenInterface login parameters
        LoginInfo login = new LoginInfo();
        login.setDatabase(database);
        login.setUserName(user);
        login.setPassword(password);
        this.login = login;
        
        // ----------------------------
        // Connect to the OpenInterface
        // ----------------------------
        
        String oiEndpointAddress;
        try {
            oiEndpointAddress = WorkspaceOIUtils.getOIEndpointAddress(database, host, logger, this);
        } catch(FailTransferException e) {
            return ITERATION_OPEN_STATUS_ERROR;
        }
        // Store the OpenInterfaceIF reference for the iteration session
        this.oi = WorkspaceOIUtils.getOpenInterfaceIF(oiEndpointAddress, host);
        
        return ITERATION_OPEN_STATUS_OK;
    }

    public int close(DestinationInfo info, MessageLogger logger) throws FailTransferException {

        // Reset the OpenInterfaceIF reference and LoginInfo
        this.oi = null;
        this.login = null;
        
        return ITERATION_CLOSE_STATUS_OK;
    }

    public void lastBlockStatus(int statusCode) { }

    /**
     * The method actually called by pipe during the conversion. This default implementation uses
     * the take-method to handle all the input records separately, So in most cases you only need to
     * implement it. If however you for some reason need access to all the data you can overwrite
     * this.
     */
    public void takeAll(String[] data, DestinationInfo info, MessageLogger logger) throws FailTransferException,
    																					  AbortTransferException {
        for(int i = 0; i < data.length; i++) {
            take(data[i], info, logger);
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see smilehouse.opensyncro.user.pipes.Destination#take(java.lang.String,
     *      smilehouse.opensyncro.user.pipes.DestinationInfo, smilehouse.opensyncro.user.pipes.log.MessageLogger)
     */
    public void take(String data, DestinationInfo info, MessageLogger logger) throws FailTransferException,
    																				 AbortTransferException {
        // -------------
        // Do the import
        // -------------
        try {
            
            ImportResult result = oi.importPricelist(login, data, this.importMode.intValue());
            
            if(result != null) {
                List warnings = result.getWarnings();
                if(warnings != null)
                    for(Iterator it = warnings.iterator(); it.hasNext();) {
                        logger.logMessage((String) it.next(), this, MessageLogger.WARNING);
                    }
                List insertedIds = result.getInsertedIds();
                List updatedIds = result.getUpdatedIds();
                logger.logMessage(
                    "Inserted " + (insertedIds != null ? insertedIds.size() : 0) + ", updated "
                            + (updatedIds != null ? updatedIds.size() : 0),
                    this,
                    MessageLogger.DEBUG);
            } else {
                logger.logMessage(
                    "Internal error: OpenInterface returned null result after importing pricelist data",
                    this,
                    MessageLogger.ERROR);
                PipeComponentUtils.failTransfer();
            }

        } catch(AccessDeniedException ade) {
            logger.logMessage(
                "Access to Open Interface was denied. Check your username and password settings.",
                this,
                MessageLogger.ERROR);
            PipeComponentUtils.failTransfer();

        } catch(RemoteException re) {
            logger.logMessage(
                "Couldn't access the remote Open Interface while importing pricelist data: " + re.getMessage(),
                this,
                MessageLogger.ERROR);

            Environment.getInstance().log(
                "Couldn't access the remote Open Interface while importing pricelist data", re);
            PipeComponentUtils.failTransfer();

        } catch(OpenInterfaceException oie) {
            logger.logMessage(
                "OpenInterfaceException while importing pricelist data: " + oie.getMessage(),
                this,
                MessageLogger.ERROR);

            Environment.getInstance().log(
                "OpenInterfaceException while importing pricelist data", oie);
            PipeComponentUtils.failTransfer();
        } catch(FailTransferException e) {
            // This error should be already logged, thus we simply rethrow the Exception
            throw(e);
        }
    }

}