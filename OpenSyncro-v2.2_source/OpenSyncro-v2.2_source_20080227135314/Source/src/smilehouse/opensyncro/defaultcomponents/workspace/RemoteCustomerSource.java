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
import smilehouse.openinterface.CustomerCriteria;
import smilehouse.openinterface.ExportResult;
import smilehouse.openinterface.LoginInfo;
import smilehouse.openinterface.OpenInterfaceException;
import smilehouse.openinterface.OpenInterfaceIF;
import smilehouse.openinterface.OpenInterface_Impl;
import smilehouse.opensyncro.pipes.component.FailTransferException;
import smilehouse.opensyncro.pipes.component.PipeComponentUtils;
import smilehouse.opensyncro.pipes.gui.GUIConfigurationIF;
import smilehouse.opensyncro.pipes.log.MessageLogger;
import smilehouse.opensyncro.pipes.metadata.SourceInfo;
import smilehouse.opensyncro.system.Environment;
import smilehouse.workspace.operator.web.OpenInterfaceAddress_Impl;

/**
 * RemoteCustomerSource.java
 * 
 * Created: Thu Nov 23 10:21:04 2006
 */

public class RemoteCustomerSource extends CustomerSource implements GUIConfigurationIF {

    private static final String OPEN_INTERFACE_HOST_ATTR = "openinterfaceHost";
    private static final String DATABASE_ATTR = "database";

    private static final String USERNAME_ATTR = "userName";
    private static final String PASSWORD_ATTR = "password";

    private static CustomerSourceGUIContextContainer guiContextContainer = new RemoteCustomerSourceGUIContextContainer();

    private OpenInterfaceIF openInterface = null;
    
    private static class RemoteCustomerSourceGUIContextContainer extends
            CustomerSourceGUIContextContainer {

        public RemoteCustomerSourceGUIContextContainer() {
            super();
            try {
                {
                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            return ((RemoteCustomerSource) model).getOpenInterfaceHost();
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            ((RemoteCustomerSource) model).setOpenInterfaceHost((String) value);
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
                            return ((RemoteCustomerSource) model).getDatabase();
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            ((RemoteCustomerSource) model).setDatabase((String) value);
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
                            return ((RemoteCustomerSource) model).getUsername();
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            ((RemoteCustomerSource) model).setUsername((String) value);
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
                            return ((RemoteCustomerSource) model).getPassword();
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            ((RemoteCustomerSource) model).setPassword((String) value);
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
                    "Couldn't create GUIContext for RemoteCustomerSource", e);

            }
        }
    }

    public RemoteCustomerSource( Object pipeComponentData ) {
        super( pipeComponentData );
    }

    public String getName() {
        return "RemoteCustomerSource";
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
    
    public ExportResult getExportResult(CustomerCriteria criteria,MessageLogger logger) throws FailTransferException {
        OpenInterfaceIF oi = getOpenInterfaceIF(logger);
        LoginInfo login = getLoginInfo();

        try {
            return oi.exportCustomers(login, criteria);           
        } catch (RemoteException e) {
            logger.logMessage("RemoteException while querying customers from Open Interface: " + e.getMessage(),
                this, MessageLogger.ERROR);
            Environment.getInstance().log("RemoteException while querying customers from Open Interface", e);
            PipeComponentUtils.failTransfer();
        } catch (AccessDeniedException e) {
            logger.logMessage(
                "Access to OpenInterface was denied. Check your username and password settings.",
                this,
                MessageLogger.ERROR);
            PipeComponentUtils.failTransfer();
        } catch (OpenInterfaceException e) {
            logger.logMessage("OpenInterfaceException while querying customers from Open Interface: " + e.getMessage(),
                this, MessageLogger.ERROR);
            Environment.getInstance().log("OpenInterfaceException while querying customers from Open Interface", e);
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
    
    public GUIContext getGUIContext() {
        return guiContextContainer.getGUIContext();
    }

    public String getGUITemplate() {
        return "<table border=\"0\">" +
        		"<tr><td colspan=\"2\">$openinterfaceHost$</td></tr>" +
                "<tr><td>$database$</td></tr>" +
                "<tr><td>$userName$</td><td>$password$</td></tr>" +
                "<tr><td colspan=\"2\"><hr></td></tr>" +
                
                "<tr><td>$customerId$</td></tr>" +
                "<tr><td>$idGreaterThan$</td><td>$idLessThan$</td></tr>" +
                "<tr><td colspan=\"2\">$idIn$</td></tr>" +
                "<tr><td>$primaryCustomerGroup$</td><td>$customerGroup$</td></tr>" +
                "<tr><td>$dateCreatedAfter$<br>(Date format: " + DATE_FORMAT + ")</td><td>$dateCreatedBefore$<br>(Date format: " + DATE_FORMAT + ")</td></tr>" +
                "<tr><td>$dateLastVisitAfter$<br>(Date format: " + DATE_FORMAT + ")</td><td>$dateLastVisitBefore$<br>(Date format: " + DATE_FORMAT + ")</td></tr>" +
                "<tr><td>$customerModifiedAfter$<br>(Date format: "+ DATE_FORMAT+")</td><td>$customerModifiedBefore$<br>(Date format: "+ DATE_FORMAT+")</td></tr>"+
                "<tr><td>$adminModifiedAfter$<br>(Date format: "+ DATE_FORMAT+")</td><td>$adminModifiedBefore$<br>(Date format: "+ DATE_FORMAT+")</td></tr>"+
                "<tr><td colspan=\"2\">$modifyOperation$</td></tr>"+
                "</table>";
    }

} // RemoteCustomerSource

