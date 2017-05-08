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

import javax.xml.rpc.Stub;

import smilehouse.gui.html.fieldbased.FieldInfo;
import smilehouse.gui.html.fieldbased.GUIContext;
import smilehouse.gui.html.fieldbased.editor.PasswordEditor;
import smilehouse.gui.html.fieldbased.editor.TextEditor;
import smilehouse.gui.html.fieldbased.model.DefaultModelModifier;
import smilehouse.gui.html.fieldbased.model.ModelModifier;
import smilehouse.openinterface.AccessDeniedException;
import smilehouse.openinterface.ExportResult;
import smilehouse.openinterface.ImportResult;
import smilehouse.openinterface.LoginInfo;
import smilehouse.openinterface.OpenInterfaceException;
import smilehouse.openinterface.OpenInterfaceIF;
import smilehouse.openinterface.OpenInterface_Impl;
import smilehouse.openinterface.OrderCriteria;
import smilehouse.opensyncro.pipes.component.FailTransferException;
import smilehouse.opensyncro.pipes.component.PipeComponentUtils;
import smilehouse.opensyncro.pipes.gui.GUIConfigurationIF;
import smilehouse.opensyncro.pipes.log.MessageLogger;
import smilehouse.opensyncro.pipes.metadata.SourceInfo;
import smilehouse.opensyncro.system.Environment;
import smilehouse.workspace.operator.web.OpenInterfaceAddress_Impl;

/**
 * RemoteOrderSource.java
 * 
 * Created: Mon Apr 5 10:21:04 2004
 */

public class RemoteOrderSource extends OrderSource implements GUIConfigurationIF {

    private static final String OPEN_INTERFACE_HOST_ATTR = "openinterfaceHost";
    private static final String DATABASE_ATTR = "database";

    private static final String USERNAME_ATTR = "userName";
    private static final String PASSWORD_ATTR = "password";

    private static OrderSourceGUIContextContainer guiContextContainer = new RemoteOrderSourceGUIContextContainer();

    private OpenInterfaceIF openInterface = null;
    
    private static class RemoteOrderSourceGUIContextContainer extends
            OrderSourceGUIContextContainer {

        public RemoteOrderSourceGUIContextContainer() {
            super();
            try {
                {
                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            return ((RemoteOrderSource) model).getOpenInterfaceHost();
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            ((RemoteOrderSource) model).setOpenInterfaceHost((String) value);
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
                    context.addFieldInfo(fieldInfo);
                }
                {
                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            return ((RemoteOrderSource) model).getDatabase();
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            ((RemoteOrderSource) model).setDatabase((String) value);
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
                    context.addFieldInfo(fieldInfo);
                }
                {
                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            return ((RemoteOrderSource) model).getUsername();
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            ((RemoteOrderSource) model).setUsername((String) value);
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
                    context.addFieldInfo(fieldInfo);
                }
                {
                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            return ((RemoteOrderSource) model).getPassword();
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            ((RemoteOrderSource) model).setPassword((String) value);
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
                    context.addFieldInfo(fieldInfo);
                }
            } catch(Exception e) {
                Environment.getInstance().log(
                    "Couldn't create GUIContext for RemoteOrderSource", e);

            }
        }
    }

    public RemoteOrderSource( Object pipeComponentData ) {
        super( pipeComponentData );
    }

    public String getName() {
        return "RemoteOrderSource";
    }

    public String getID() {
        return this.getClass().getName();
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

    private static Stub createOpenInterfaceProxy() {
        return (Stub) (new OpenInterface_Impl().getOpenInterfaceIFPort());
    }

    private static Stub createOpenInterfaceAddressProxy() {
        return (Stub) (new OpenInterfaceAddress_Impl().getOpenInterfaceAddressIFPort());
    }

    private LoginInfo getLoginInfo() {
        LoginInfo login = new LoginInfo();
        login.setDatabase(getDatabase());
        login.setUserName(getUsername());
        login.setPassword(getPassword());
        return login;
    }
    
    public ExportResult getExportResult(OrderCriteria criteria,
                                        Long updateLogId,
                                        String newStatusName,
                                        SourceInfo info,
                                        MessageLogger logger) throws FailTransferException {
        OpenInterfaceIF oi = getOpenInterfaceIF(logger);
        LoginInfo login = getLoginInfo();

        try {
            return oi.exportOrders(login, criteria, updateLogId, newStatusName);
        } catch (RemoteException e) {
            logger.logMessage("RemoteException while querying orders from Open Interface: " + e.getMessage(),
                this, MessageLogger.ERROR);
            Environment.getInstance().log("RemoteException while querying orders from Open Interface", e);
            PipeComponentUtils.failTransfer();
        } catch (AccessDeniedException e) {
            logger.logMessage(
                "Access to OpenInterface was denied. Check your username and password settings.",
                this,
                MessageLogger.ERROR);
            PipeComponentUtils.failTransfer();
        } catch (OpenInterfaceException e) {
            logger.logMessage("OpenInterfaceException while querying orders from Open Interface: " + e.getMessage(),
                this, MessageLogger.ERROR);
            Environment.getInstance().log("OpenInterfaceException while querying orders from Open Interface", e);
            PipeComponentUtils.failTransfer();
        } 
        
        // Unreachable code, should never return null ExportResult
        return null;

    }
    
   public int open(SourceInfo info, MessageLogger logger) throws FailTransferException {

       // Reset OpenInterfaceIF
       this.openInterface = null;
       
       return super.open(info, logger);
     
    }
   
   public int close(SourceInfo info, MessageLogger logger) throws FailTransferException {
       int returnCode = super.close(info, logger);
       
       // Reset OpenInterfaceIF
       this.openInterface = null;

       return returnCode;
   }

    private OpenInterfaceIF getOpenInterfaceIF(MessageLogger logger) throws FailTransferException {

        if( this.openInterface != null ) {
            return this.openInterface;
        } else {
            // ----------------------------
            // Connect to the OpenInterface
            // ----------------------------
            String oiEndpointAddress = WorkspaceOIUtils.getOIEndpointAddress(getDatabase(),
                getOpenInterfaceHost(),
                logger,
                this);
            OpenInterfaceIF oi = WorkspaceOIUtils.getOpenInterfaceIF(oiEndpointAddress, getOpenInterfaceHost());
            this.openInterface = oi;
            return oi;
        }
    }
    
    /**
     * Set handling and payment status of an order in Workspace.
     * 
     * @param orderId Order Id
     * @param newHandlingStatusName Name of new handling status (null value means no change)
     * @param newPaymentStatusName Name of new payment status (null value means no change)
     * @param logger MessageLogger instance
     * @return OpenInterface ImportResult containing a list of successfully updated order Ids
     * @throws FailTransferException
     * @see smilehouse.opensyncro.defaultcomponents.workspace.WorkspaceOIUtils#setOrderStatus(OpenInterfaceIF, LoginInfo, Long, String, String, MessageLogger, Object)
     */
    public ImportResult setOrderStatus(Long orderId,
                                       String newHandlingStatusName,
                                       String newPaymentStatusName,
                                       MessageLogger logger) throws FailTransferException {
        // Pass the request along with OpenInterfaceIF and LoginInfo parameters to WorkspaceOIUtils
        return WorkspaceOIUtils.setOrderStatus(getOpenInterfaceIF(logger),
                                               getLoginInfo(),
                                               orderId,
                                               newHandlingStatusName,
                                               newPaymentStatusName,
                                               logger,
                                               this);
    }

    /**
     * Set handling and payment status of multiple orders in Workspace.
     * 
     * @param orderIds Array of Order Ids
     * @param newHandlingStatusName Name of new handling status (null value means no change)
     * @param newPaymentStatusName Name of new payment status (null value means no change)
     * @param logger MessageLogger instance
     * @return OpenInterface ImportResult containing a list of successfully updated order Ids
     * @throws FailTransferException
     * @see smilehouse.opensyncro.defaultcomponents.workspace.WorkspaceOIUtils#setOrderStatus(OpenInterfaceIF, LoginInfo, Long[], String, String, MessageLogger, Object)
     */
    public ImportResult setOrderStatus(Long[] orderIds,
                                       String newHandlingStatusName,
                                       String newPaymentStatusName,
                                       MessageLogger logger) throws FailTransferException {
        // Pass the request along with OpenInterfaceIF and LoginInfo parameters to WorkspaceOIUtils
        return WorkspaceOIUtils.setOrderStatus(getOpenInterfaceIF(logger),
            getLoginInfo(),
            orderIds,
            newHandlingStatusName,
            newPaymentStatusName,
            logger,
            this);
    }

    public GUIContext getGUIContext() {
        return guiContextContainer.getGUIContext();
    }

    public String getGUITemplate() {
        return "<table border=\"0\"><tr><td colspan=\"2\">$openinterfaceHost$</td></tr>"
                + "<tr><td>$database$</td></tr>"
                + "<tr><td>$userName$</td><td>$password$</td></tr>" +

                "<tr><td colspan=\"2\"><hr></td></tr>" +

                "<tr><td>$idGreaterThan$</td><td>$idLessThan$</td></tr>"
                + "<tr><td colspan=\"2\">$idIn$</td></tr>" +

                "<tr><td>$sumGreaterThan$</td><td>$sumLessThan$</td></tr>" +

                "<tr><td colspan=\"2\">$handlingStatusNameIn$</td></tr>"
                + "<tr><td colspan=\"2\">$handlingStatusNameNotIn$</td></tr>"
                + "<tr><td colspan=\"2\">$paymentStatusNameIn$</td></tr>"
                + "<tr><td colspan=\"2\">$paymentStatusNameNotIn$</td></tr>"
                + "<tr><td colspan=\"2\">$newStatusName$</td></tr>" +

                "<tr><td colspan=\"2\">$customerIdIn$</td></tr>" +

                "<tr><td>$dateAfter$<br>(Date format: " + DATE_FORMAT
                + ")</td><td>$dateBefore$<br>(Date format: " + DATE_FORMAT + ")</td></tr>" +

                "</table>";
    }

} // RemoteOrderSource
