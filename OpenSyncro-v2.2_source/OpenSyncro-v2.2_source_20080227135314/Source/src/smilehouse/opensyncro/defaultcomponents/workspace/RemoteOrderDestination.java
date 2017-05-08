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

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;

import smilehouse.gui.html.fieldbased.FieldInfo;
import smilehouse.gui.html.fieldbased.GUIContext;
import smilehouse.gui.html.fieldbased.editor.BooleanEditor;
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

public class RemoteOrderDestination implements DestinationIF, GUIConfigurationIF {

	protected PipeComponentData data;
    protected OpenInterfaceIF openInterface;
    
    protected String oiHost; 
    protected String oiDatabase;
    protected LoginInfo oiLoginInfo;
    protected int oiImportMode;
    protected boolean additiveAnswerUpdate;
    protected boolean additiveBasketUpdate;
    protected boolean invokeReceivedEvents;

    private static final String HOST_ATTR = "host";
    private static final String DATABASE_ATTR = "database";
    private static final String USER_ATTR = "user";
    private static final String PASSWORD_ATTR = "password";
    private static final String IMPORT_MODE_ATTR = "import_mode";
    private static final String ADDITIVE_ANSWER_UPDATE_ATTR = "additive_answer_update";
    private static final String ADDITIVE_BASKET_UPDATE_ATTR = "additive_basket_update";
    private static final String INVOKE_RECEIVED_EVENTS_ATTR = "invoke_received_events";
    
    

    public RemoteOrderDestination( Object pipeComponentData ) {
        setData((PipeComponentData) pipeComponentData);
    }
    
    
    /**
     * @see smilehouse.opensyncro.pipes.component.DestinationIF#take(java.lang.String, smilehouse.opensyncro.pipes.metadata.DestinationInfo, smilehouse.opensyncro.pipes.log.MessageLogger)
     */
    public void take(String data, DestinationInfo info, MessageLogger logger)
            throws FailTransferException, AbortTransferException {

        try {
            ImportResult result = openInterface.importOrders(oiLoginInfo, data, oiImportMode, additiveAnswerUpdate, additiveBasketUpdate, invokeReceivedEvents);
            WorkspaceOIUtils.logImportResult(result, logger, this, false);
        }
        catch(AccessDeniedException ade) {
            logger.logMessage("Access to Open Interface denied. Check your login information.", this, MessageLogger.ERROR);
            PipeComponentUtils.failTransfer();
        }
        catch(OpenInterfaceException oie) {
            logger.logMessage("Open Interface reported error: " + oie.toString(), this, MessageLogger.ERROR);
            Environment.getInstance().log("Open Interface reported error", oie);
            PipeComponentUtils.failTransfer();
        }
        catch(RemoteException re) {
            logger.logMessage("Error contacting Open Interface: " + re.toString(), this, MessageLogger.ERROR);
            Environment.getInstance().log("Error Contacting OpenInterface", re);
            PipeComponentUtils.failTransfer();
        }

    }

    /* (non-Javadoc)
     * @see smilehouse.opensyncro.pipes.component.DestinationIF#takeAll(java.lang.String[], smilehouse.opensyncro.pipes.metadata.DestinationInfo, smilehouse.opensyncro.pipes.log.MessageLogger)
     */
    public void takeAll(String[] data, DestinationInfo info, MessageLogger logger)
            throws FailTransferException, AbortTransferException {
        
        for(int i = 0; i < data.length; i++) {
                take(data[i], info, logger);
            }

    }

    /* (non-Javadoc)
     * @see smilehouse.opensyncro.pipes.component.DestinationIF#open(smilehouse.opensyncro.pipes.metadata.DestinationInfo, smilehouse.opensyncro.pipes.log.MessageLogger)
     */
    public int open(DestinationInfo info, MessageLogger logger) throws FailTransferException,
            AbortTransferException {
    	// ------------------
        // Get the parameters
        // ------------------
    	
        getParameters(logger);
        
        // ---------------------------
        // Get the open interface stub
        // ---------------------------
        openInterface = WorkspaceOIUtils.getOpenInterfaceIF(oiDatabase, oiHost, logger, this);
                
        return ITERATION_OPEN_STATUS_OK;
    }


	protected void getParameters(MessageLogger logger) throws FailTransferException {
		
        oiHost = data.getNonNullAttribute(HOST_ATTR, logger, "OI host not set!", MessageLogger.ERROR);
        oiDatabase = data.getNonNullAttribute(DATABASE_ATTR, logger, "OI database not set!", MessageLogger.ERROR);
        String oiUser = data.getNonNullAttribute(USER_ATTR, logger, "OI user name not set!", MessageLogger.ERROR);
        String oiPassword = data.getNonNullAttribute(PASSWORD_ATTR, logger, "OI password not set!", MessageLogger.ERROR);
        oiLoginInfo = new LoginInfo(oiDatabase, oiPassword, oiUser);
        
        Integer i = data.getIntegerAttribute(IMPORT_MODE_ATTR, logger, "Invalid value for import mode!");
        if(i == null) {
            logger.logMessage("Import mode not set!", this, MessageLogger.ERROR);
            PipeComponentUtils.failTransfer();
        }
        oiImportMode = i.intValue();
        
        Boolean aau = data.getBooleanAttribute(ADDITIVE_ANSWER_UPDATE_ATTR);
        additiveAnswerUpdate = aau != null ? aau.booleanValue() : true;

        Boolean abu = data.getBooleanAttribute(ADDITIVE_BASKET_UPDATE_ATTR);
        additiveBasketUpdate = abu != null ? abu.booleanValue() : true;
        
        Boolean ire = data.getBooleanAttribute(INVOKE_RECEIVED_EVENTS_ATTR);
        invokeReceivedEvents = ire != null ? ire.booleanValue() : false;
	}

    /* (non-Javadoc)
     * @see smilehouse.opensyncro.pipes.component.DestinationIF#close(smilehouse.opensyncro.pipes.metadata.DestinationInfo, smilehouse.opensyncro.pipes.log.MessageLogger)
     */
    public int close(DestinationInfo info, MessageLogger logger) throws FailTransferException,
            AbortTransferException {
        return ITERATION_CLOSE_STATUS_OK;
    }

    /* (non-Javadoc)
     * @see smilehouse.opensyncro.pipes.gui.GUIConfigurationIF#getGUIContext()
     */
    public GUIContext getGUIContext() {
        return gui.getGUIContext();
    }

    /**
     * @see smilehouse.opensyncro.pipes.gui.GUIConfigurationIF#getGUITemplate()
     */
    public String getGUITemplate() {
        return
        	"<table>"+ 
        		"<tr><td>$"+HOST_ATTR +"$</td><td>$"+DATABASE_ATTR+"$</td></tr>" +
        		"<tr><td>$"+USER_ATTR+"$</td><td>$"+PASSWORD_ATTR+"$</td></tr>" + 
        		"<tr><td>$"+IMPORT_MODE_ATTR+"$</td><td>$"+INVOKE_RECEIVED_EVENTS_ATTR+"$</td></tr>" + 
        		"<tr><td>$"+ADDITIVE_ANSWER_UPDATE_ATTR+"$</td><td>$"+ADDITIVE_BASKET_UPDATE_ATTR+"$</td></tr>" + 
        	"</table>";
    }

    /* (non-Javadoc)
     * @see smilehouse.opensyncro.pipes.component.PipeComponentIF#setData(smilehouse.opensyncro.pipes.component.PipeComponentData)
     */
    public void setData(PipeComponentData data) {
    	this.data = data;
    }

    /* (non-Javadoc)
     * @see smilehouse.opensyncro.pipes.component.PipeComponentIF#getData()
     */
    public PipeComponentData getData() {
        return this.data;
    }

    /* (non-Javadoc)
     * @see smilehouse.opensyncro.pipes.component.PipeComponentIF#getID()
     */
    public String getID() {
        return this.getClass().getName();
    }

    /* (non-Javadoc)
     * @see smilehouse.opensyncro.pipes.component.PipeComponentIF#getDescription(java.util.Locale)
     */
    public String getDescription(Locale locale) {
        return PipeComponentUtils.getDescription(locale, this.getClass());
    }

    /* (non-Javadoc)
     * @see smilehouse.opensyncro.pipes.component.PipeComponentIF#getName()
     */
    public String getName() {
        return "RemoteOrderDestination";
    }

    /* (non-Javadoc)
     * @see smilehouse.opensyncro.pipes.component.PipeComponentIF#getType()
     */
    public int getType() {
        return TYPE_DESTINATION;
    }
    
    private static GUIDefinition gui = new OrderDestinationGUI();
    
    private static class OrderDestinationGUI extends GUIDefinition {

        private static final LinkedHashMap IMPORT_MODES = new LinkedHashMap();
        static {
            IMPORT_MODES.put(new Integer(0), "insert_or_update");
            IMPORT_MODES.put(new Integer(1), "insert");
            IMPORT_MODES.put(new Integer(2), "update");
        }
        
        public OrderDestinationGUI() {
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
                // Additive answer update checkbox
                {
                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            Boolean value = ((DestinationIF) model)
                                .getData().getBooleanAttribute(ADDITIVE_ANSWER_UPDATE_ATTR);
                            if(value == null)
                                value = Boolean.TRUE;
                            return value;
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            boolean booleanValue = value != null ? ((Boolean) value).booleanValue()
                                    : true;
                            ((DestinationIF) model).getData().setAttribute(ADDITIVE_ANSWER_UPDATE_ATTR, booleanValue);
                        }
                    };

                    BooleanEditor editor = new BooleanEditor();

                    FieldInfo fieldInfo = new FieldInfo(
                        ADDITIVE_ANSWER_UPDATE_ATTR,
                        ADDITIVE_ANSWER_UPDATE_ATTR,
                        modifier,
                        editor);

                    //add the configuration to the context for usage in the http-requests.
                    addField(ADDITIVE_ANSWER_UPDATE_ATTR, fieldInfo);
                }
                // Additive basket update checkbox
                {
                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            Boolean value = ((DestinationIF) model)
                                .getData().getBooleanAttribute(ADDITIVE_BASKET_UPDATE_ATTR);
                            if(value == null)
                                value = Boolean.FALSE;
                            return value;
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            boolean booleanValue = value != null ? ((Boolean) value).booleanValue()
                                    : false;
                            ((DestinationIF) model).getData().setAttribute(
                                ADDITIVE_BASKET_UPDATE_ATTR,
                                booleanValue);
                        }
                    };

                    BooleanEditor editor = new BooleanEditor();

                    FieldInfo fieldInfo = new FieldInfo(
                        ADDITIVE_BASKET_UPDATE_ATTR,
                        ADDITIVE_BASKET_UPDATE_ATTR,
                        modifier,
                        editor);

                    //add the configuration to the context for usage in the http-requests.
                    addField(ADDITIVE_BASKET_UPDATE_ATTR, fieldInfo);
                }
                
                // Invoke received events checkbox
                {
                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            Boolean value = ((DestinationIF) model)
                                .getData().getBooleanAttribute(INVOKE_RECEIVED_EVENTS_ATTR);
                            if(value == null)
                                value = Boolean.FALSE;
                            return value;
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            boolean booleanValue = value != null ? ((Boolean) value).booleanValue()
                                    : false;
                            ((DestinationIF) model).getData().setAttribute(
                                INVOKE_RECEIVED_EVENTS_ATTR,
                                booleanValue);
                        }
                    };

                    BooleanEditor editor = new BooleanEditor();

                    FieldInfo fieldInfo = new FieldInfo(
                        INVOKE_RECEIVED_EVENTS_ATTR,
                        INVOKE_RECEIVED_EVENTS_ATTR,
                        modifier,
                        editor);

                    //add the configuration to the context for usage in the http-requests.
                    addField(INVOKE_RECEIVED_EVENTS_ATTR, fieldInfo);
                }

            } catch(Exception e) {
                Environment.getInstance().log(
                    "Could not create GUI for RemoteOrderDestination",
                    e);
            }
        }
    }

}
