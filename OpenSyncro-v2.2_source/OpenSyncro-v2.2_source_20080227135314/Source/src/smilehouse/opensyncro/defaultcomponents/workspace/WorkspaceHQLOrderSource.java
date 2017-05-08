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

import smilehouse.gui.html.fieldbased.FieldInfo;
import smilehouse.gui.html.fieldbased.GUIContext;
import smilehouse.gui.html.fieldbased.editor.TextEditor;
import smilehouse.gui.html.fieldbased.model.DefaultModelModifier;
import smilehouse.gui.html.fieldbased.model.ModelModifier;
import smilehouse.openinterface.LoginInfo;
import smilehouse.openinterface.OpenInterfaceIF;
import smilehouse.opensyncro.pipes.component.AbortTransferException;
import smilehouse.opensyncro.pipes.component.FailTransferException;
import smilehouse.opensyncro.pipes.log.MessageLogger;
import smilehouse.opensyncro.pipes.metadata.SourceInfo;
import smilehouse.opensyncro.system.Environment;
import smilehouse.util.Utils;

public class WorkspaceHQLOrderSource extends WorkspaceHQLSource {

    protected static final String NEW_ORDER_HANDLING_STATUS = "newOrderHandlingStatus";
    protected static final String NEW_ORDER_PAYMENT_STATUS = "newOrderPaymentStatus";
    
    // TODO: Replace these with proper Status classes
    private final int PIPE_EXECUTION_OK = 1;
    private final int PIPE_EXECUTION_FAILED = 0;
    //private final int PIPE_EXECUTION_ABORTED = -1;
    
    protected static WorkspaceHQLOrderSourceGUI guiContextContainerLocal = new WorkspaceHQLOrderSourceGUI();

    // Id array of order whose handling statuses are to be changed in case of successful Pipe execution 
    private Long[] orderIds = null;
    
    public WorkspaceHQLOrderSource(Object pipeComponentData) {
        super(pipeComponentData);
    }

    protected static class WorkspaceHQLOrderSourceGUI extends WorkspaceHQLSourceGUI {

        public WorkspaceHQLOrderSourceGUI() {
            // FIXME: If an Exception occurs during initialization of the GUI, we log one
            // error message at WorkspaceHQLSourceGUI and another one here at
            // WorkspaceHQLOrderSourceGUI 
            
            // Get the GUI fields from WorkspaceHQLSourceGUI
            super();
            
            // Add WorkspaceHQLOrderSource specific fields to GUI
            try {
                {
                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            return ((WorkspaceHQLOrderSource) model).getNewOrderHandlingStatus();
                        }
    
                        public void setModelValue(Object model, Object value) throws Exception {
                            ((WorkspaceHQLOrderSource) model).setNewOrderHandlingStatus((String) value);
                        }
                    };
    
                    TextEditor editor = new TextEditor();
                    editor.setSize(40);
    
                    //and finally create the configurationObject
                    FieldInfo fieldInfo = new FieldInfo(
                        NEW_ORDER_HANDLING_STATUS,
                        NEW_ORDER_HANDLING_STATUS,
                        modifier,
                        editor);
    
                    //add the configuration to the context for usage in the http-requests.
                    addField(NEW_ORDER_HANDLING_STATUS, fieldInfo);
                }
                {
                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            return ((WorkspaceHQLOrderSource) model).getNewOrderPaymentStatus();
                        }
    
                        public void setModelValue(Object model, Object value) throws Exception {
                            ((WorkspaceHQLOrderSource) model).setNewOrderPaymentStatus((String) value);
                        }
                    };
    
                    TextEditor editor = new TextEditor();
                    editor.setSize(40);
    
                    //and finally create the configurationObject
                    FieldInfo fieldInfo = new FieldInfo(
                        NEW_ORDER_PAYMENT_STATUS,
                        NEW_ORDER_PAYMENT_STATUS,
                        modifier,
                        editor);
    
                    //add the configuration to the context for usage in the http-requests.
                    addField(NEW_ORDER_PAYMENT_STATUS, fieldInfo);
                    
                }
            } catch(Exception e) {
                Environment.getInstance().log(
                    "Couldn't create GUIContext in " + Utils.getClassName(this.getClass()), e);

            }

        }
        
    }
    
    
    public void setNewOrderHandlingStatus(String orderHandlingStatus) {
        this.data.setAttribute(NEW_ORDER_HANDLING_STATUS, orderHandlingStatus);
    }

    public String getNewOrderHandlingStatus() {
        String orderHandlingStatus = this.data.getAttribute(NEW_ORDER_HANDLING_STATUS);
        return (orderHandlingStatus != null ? orderHandlingStatus : "");
    }

    public void setNewOrderPaymentStatus(String orderPaymentStatus) {
        this.data.setAttribute(NEW_ORDER_PAYMENT_STATUS, orderPaymentStatus);
    }

    public String getNewOrderPaymentStatus() {
        String orderPaymentStatus = this.data.getAttribute(NEW_ORDER_PAYMENT_STATUS);
        return (orderPaymentStatus != null ? orderPaymentStatus : "");
    }

    public Long[] getOrderIds() {
        return orderIds;
    }

    public void setOrderIds(Long[] orderIds) {
        this.orderIds = orderIds;
    }

    
    public String getName() {
        return "WorkspaceHQLOrderSource";
    }
    
    private LoginInfo getLoginInfo() {
        LoginInfo login = new LoginInfo();
        login.setDatabase(getDatabase());
        login.setUserName(getUsername());
        login.setPassword(getPassword());
        return login;
    }
    
    public void lastBlockStatus(int statusCode) throws FailTransferException, AbortTransferException {

        if( statusCode == PIPE_EXECUTION_OK ) {
            
            Long[] orderIds = getOrderIds();
            String newHandlingStatusName = getNewOrderHandlingStatus();
            String newPaymentStatusName = getNewOrderPaymentStatus();

            // Convert empty status names to null so that the order statuses won't be changed (to empty)
            if( newHandlingStatusName.length() == 0 ) {
                newHandlingStatusName = null;
            }
            if( newPaymentStatusName.length() == 0 ) {
                newPaymentStatusName = null;
            }

            // Proceed with updating order statuses if the list of Order Ids is not empty and either
            // a new handling or payment status is specified
            if((orderIds != null) && ( (newHandlingStatusName != null) || (newPaymentStatusName != null) )) {
                
                // -----------------
                // Get OpenInterface
                // -----------------
                
                OpenInterfaceIF oi = WorkspaceOIUtils.getOpenInterfaceIF(this.oiEndpointAddress,
                                                                         getOpenInterfaceHost());
                LoginInfo login = getLoginInfo();
                
                // Update all order statuses
                WorkspaceOIUtils.setOrderStatus(oi,
                    login,
                    getOrderIds(),
                    newHandlingStatusName,
                    newPaymentStatusName,
                    this.logger,
                    this);
            }
        }
    }
    
    public String[] give(SourceInfo info, MessageLogger logger)
       throws FailTransferException, AbortTransferException {
        
        String[] exportedXML = super.give(info, logger);

        // TODO: Log the number of Orders exported if more than 1
        
        if(exportedXML != null) {
            // Extract and save a list of order Ids so that we can update the orders'
            // handling statuses after successful execution of the Pipe
            setOrderIds(WorkspaceOIUtils.getOrderIdsFromXML(exportedXML));
        }

        // Pass through the result from parent component
        return exportedXML;
    }
    
    public GUIContext getGUIContext() {
        return guiContextContainerLocal.getGUIContext();
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
                
                "<tr><td colspan=\"3\"><hr></td></tr>" +
                
                "<tr><td>$" + NEW_ORDER_HANDLING_STATUS + "$</td>" +
                "<td>$" + NEW_ORDER_PAYMENT_STATUS + "$</td>" +
                "</tr></table>";
    }

    
}
