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
 * Created on Oct 14, 2005
 */
package smilehouse.opensyncro.defaultcomponents.workspace;

import java.io.StringReader;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.Stub;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import smilehouse.openinterface.AccessDeniedException;
import smilehouse.openinterface.ImportResult;
import smilehouse.openinterface.LoginInfo;
import smilehouse.openinterface.OpenInterfaceException;
import smilehouse.openinterface.OpenInterfaceIF;
import smilehouse.openinterface.OpenInterface_Impl;
import smilehouse.opensyncro.pipes.component.FailTransferException;
import smilehouse.opensyncro.pipes.component.PipeComponentUtils;
import smilehouse.opensyncro.pipes.log.MessageLogger;
import smilehouse.opensyncro.system.Environment;
import smilehouse.util.Utils;
import smilehouse.workspace.operator.web.OpenInterfaceAddressIF;
import smilehouse.workspace.operator.web.OpenInterfaceAddress_Impl;

public class WorkspaceOIUtils {

    private static final SimpleDateFormat oiDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
    
    private static final String WORKSPACE_ADMIN_WEBAPP = "/workspace.admin";
    private static final String ADDRESS_SERVICE = "openinterfaceaddress";
    
   
    
    /**
     * openHQLIterator call to OpenInterface with exception logging
     * 
     * @param oi OpenInterface remote interface stub
     * @param login OpenInterface LoginInfo
     * @param hqlQuery HQL select query
     * @param sessionTimeout Session timeout in milliseconds. Iterator will be closed automatically after this
     *        period of inactivity.
     * @param logger OpenSyncro MessageLogger instance for writing error messages to Transfer log.
     * @param logClassRef Reference to this method's caller (this) used for writing the class name to Transfer log
     * @return New iterator Id 
     * @throws FailTransferException if any OpenInterface exceptions occur during opening of the iterator.
     */
    public static String openHQLIterator(OpenInterfaceIF oi,
                                  LoginInfo login,
                                  String hqlQuery,
                                  int sessionTimeout,
                                  MessageLogger logger,
                                  Object logClassRef)
    							 throws FailTransferException {
        
        String iteratorId = null;
        
        // Access OpenInterface to open a new HQL iteration session
        try {
            iteratorId = oi.openHQLIterator(login, hqlQuery, sessionTimeout);
        } catch (AccessDeniedException e) {
            logger.logMessage(
                "Access to OpenInterface was denied. Check your username and password settings.",
                logClassRef,
                MessageLogger.ERROR);
            PipeComponentUtils.failTransfer();
        } catch (OpenInterfaceException e) {
            logger.logMessage("OpenInterfaceException while opening HQL iterator or performing the HQL query",
                logClassRef, MessageLogger.ERROR);
            logger.logMessage(e.getMessage(), logClassRef, MessageLogger.ERROR);
            
            Environment.getInstance().log(
                "OpenInterfaceException while opening HQL iterator or performing the HQL query", e);
            PipeComponentUtils.failTransfer();
        } catch (RemoteException e) {
            logger.logMessage("RemoteException while opening HQL iterator or performing the HQL query",
                logClassRef, MessageLogger.ERROR);
            Environment.getInstance().log(
                "RemoteException while opening HQL iterator or performing the HQL query", e);
            PipeComponentUtils.failTransfer();
        }
        return iteratorId;
    }

    /**
     * closeHQLIterator call to OpenInterface with exception logging
     * 
     * @param oi OpenInterface remote interface stub
     * @param login OpenInterface LoginInfo
     * @param oiIteratorId Id of the Iterator to be closed
     * @param logger OpenSyncro MessageLogger instance for writing error messages to Transfer log.
     * @param logClassRef Reference to this method's caller (this) used for writing the class name to Transfer log
     * @throws FailTransferException if any OpenInterface exceptions occur during closing of the iterator.
     */
    public static void closeHQLIterator(OpenInterfaceIF oi,
                                        LoginInfo login,
                                        String oiIteratorId,
                                        MessageLogger logger,
                                        Object logClassRef) throws FailTransferException {
        try {

            oi.closeIterator(login, oiIteratorId);
        
        } catch(AccessDeniedException e) {
            logger.logMessage(
                "Access to OpenInterface was denied. Check your username and password settings.",
                logClassRef,
                MessageLogger.ERROR);
            PipeComponentUtils.failTransfer();
        } catch(OpenInterfaceException e) {
            logger.logMessage(
                "OpenInterfaceException while closing HQL query iterator",
                logClassRef,
                MessageLogger.ERROR);
            logger.logMessage(e.getMessage(), logClassRef, MessageLogger.ERROR);

            Environment.getInstance().log(
                "OpenInterfaceException while closing HQL query iterator",
                e);
            PipeComponentUtils.failTransfer();
        } catch(RemoteException e) {
            logger.logMessage(
                "RemoteException from OpenInterface while closing HQL iterator",
                logClassRef,
                MessageLogger.ERROR);
            Environment.getInstance().log(
                "RemoteException from OpenInterface while closing HQL iterator",
                e);
            PipeComponentUtils.failTransfer();
        }

    }

    
    /**
     * Logs possible warnings and the number of inserted, updated and removed (see parameter logRemoved) records.
     * 
     * @param result
     * @param logger
     * @param importer
     * @param logRemoved Should the number of removed records be logged.
     */
    public static void logImportResult(ImportResult result, MessageLogger logger, Object importer, boolean logRemoved) {
        List warnings = result.getWarnings();
        if(warnings != null)
            for(Iterator it = warnings.iterator(); it.hasNext();) {
                logger.logMessage((String) it.next(), importer, MessageLogger.WARNING);
            }
        List insertedIds = result.getInsertedIds();        
        List updatedIds = result.getUpdatedIds();
        String message = "Inserted " + (insertedIds != null ? insertedIds.size() : 0) + ", updated "
            + (updatedIds != null ? updatedIds.size() : 0);
        if(logRemoved) {
            List removedIds = result.getRemovedIds();
            message += ", removed " + (removedIds != null ?removedIds.size() : 0); 
        }
        logger.logMessage(
            message,
            importer,
            MessageLogger.DEBUG);
    }
    
    
    /**
     * Retrieves OpenInterface Endpoint address from OpenInterfaceAddress webservice
     * 
     * @param organizationName Name of the webshop database
     * @param openInterfaceHost Base URL of the webshop
     * @param logger OpenSyncro MessageLogger instance for writing error messages to Transfer log.
     * @param logClassRef Reference to this method's caller (this) used for writing the class name to Transfer log
     * @return OpenInterface endpoint address
     * @throws FailTransferException if any errors occur during the endpoint address query 
     */
    public static String getOIEndpointAddress(String organizationName,
                                              String openInterfaceHost,
                                              MessageLogger logger,
                                              Object logClassRef)
    										  throws FailTransferException {
        
        String oiEndpointAddress = null;
        try {
            Stub addressStub = createOpenInterfaceAddressProxy();
            addressStub._setProperty(
                javax.xml.rpc.Stub.ENDPOINT_ADDRESS_PROPERTY,
                openInterfaceHost + WORKSPACE_ADMIN_WEBAPP + '/' + ADDRESS_SERVICE);
            OpenInterfaceAddressIF oiAddress = (OpenInterfaceAddressIF) addressStub;
            oiEndpointAddress = oiAddress.getOpenInterfaceAddress(organizationName);
        } catch(RemoteException e) {
            logger.logMessage(
                "Exception while querying OpenInterface endpoint address: " + e.getMessage(),
                logClassRef,
                MessageLogger.ERROR);

            Environment.getInstance().log(
                "Exception while querying OpenInterface endpoint address", e);

            PipeComponentUtils.failTransfer();
        }

        if(oiEndpointAddress == null) {
            logger.logMessage(
                "Failed to get OpenInterface endpoint address for organization \"" +
                organizationName + "\"",
                logClassRef,
                MessageLogger.ERROR);
            PipeComponentUtils.failTransfer();
        }
        
        return oiEndpointAddress;
    }

    
    /**
     * @param oiEndpointAddress Workspace OpenInterface endpoint address (URL suffix)
     * @param openInterfaceHost Workspace OpenInterface host address (URL)
     * @return OpenInterface remote interface stub
     */
    public static OpenInterfaceIF getOpenInterfaceIF(String oiEndpointAddress, String openInterfaceHost) {
        Stub stub = createOpenInterfaceProxy();

        /*
         * If host string contains one or more slash ('/') character(s), we need to remove
         * everything after and including the slash char. Otherwise OI problems (HTTP error 404)
         * will occur with installations not located in the document root path.
         */

        int hostNameStartIndex = 0;
        String fullAddress;
        if(openInterfaceHost.indexOf("://") != -1) {
            hostNameStartIndex = openInterfaceHost.indexOf("://") + 3; // Skip slash characters in protocol
                                                          // specification

        }
        if(openInterfaceHost.indexOf('/', hostNameStartIndex) != -1) {
            fullAddress = openInterfaceHost.substring(0, openInterfaceHost.indexOf('/', hostNameStartIndex))
                    + oiEndpointAddress;
        } else {
            fullAddress = openInterfaceHost + oiEndpointAddress;
        }
        
        stub._setProperty(javax.xml.rpc.Stub.ENDPOINT_ADDRESS_PROPERTY, fullAddress);
        OpenInterfaceIF oi = (OpenInterfaceIF) stub;
        return oi;
    }
    
    /**
     * Convenience method for getting the OI address directly.
     * 
     * @param organizationName
     * @param openInterfaceHost
     * @param logger
     * @param logClassRef
     * @return
     * @throws FailTransferException
     */
    public static OpenInterfaceIF getOpenInterfaceIF(String organizationName,
                                              String openInterfaceHost,
                                              MessageLogger logger,
                                              Object logClassRef)
    										  throws FailTransferException {
        
        return getOpenInterfaceIF(getOIEndpointAddress(organizationName, openInterfaceHost, logger, logClassRef), openInterfaceHost);
    }

    /**
     * @param organizationName Name of the Workspace database (organization)
     * @param userName User name to log in with. This user must have access enabled to OpenInterface.
     * @param password Password of the user
     * @return LoginInfo object for use in OpenInterface method calls
     */
    public static LoginInfo getLoginInfo(String organizationName, String userName, String password) {
        LoginInfo login = new LoginInfo();
        login.setDatabase(organizationName);
        login.setUserName(userName);
        login.setPassword(password);
        return login;
    }

    
    private static Stub createOpenInterfaceProxy() {
        return (Stub) (new OpenInterface_Impl().getOpenInterfaceIFPort());
    }

    private static Stub createOpenInterfaceAddressProxy() {
        return (Stub) (new OpenInterfaceAddress_Impl().getOpenInterfaceAddressIFPort());
    }
    
    /**
     * Get an array of Order Ids from an OpenInterface Order XML message.
     * Does not remove duplicate Ids.
     * 
     * @param oiOrderXMLMessage OpenInterface Order XML message
     * @return Array of Order Id numbers
     * @throws FailTransferException in case of XML parsing errors
     */
    public static Long[] getOrderIdsFromXML( String oiOrderXMLMessage ) throws FailTransferException {
        return getOrderIdsFromXML(new String[] { oiOrderXMLMessage });
    }
    
    /**
     * Get an array of Order Ids from an OpenInterface Order XML message.
     * Does not remove duplicate Ids.
     * 
     * @param oiOrderXMLMessages Array of OpenInterface Order XML messages
     * @return Array of Order Id numbers
     * @throws FailTransferException in case of XML parsing errors
     */
    public static Long[] getOrderIdsFromXML( String[] oiOrderXMLMessages ) throws FailTransferException {
        DocumentBuilder builder = null;
        LinkedList orderIds = new LinkedList();

        // Initialize DOM parser for an XPath query
        try {
            DocumentBuilderFactory docBFactory = DocumentBuilderFactory.newInstance();
            builder = docBFactory.newDocumentBuilder();
        } catch(ParserConfigurationException e) {
            PipeComponentUtils.failTransfer("Error initializing XML DOM parser for retrieving order Id list", e);
        }

        try {

            // Iterate through the order XML messages and add order Ids 
            for( int j = 0; j < oiOrderXMLMessages.length; j++ ) {
                
                InputSource resultReader = new InputSource(new StringReader(oiOrderXMLMessages[j]));
        
                    // Parse XML document to DOM
                    Document document = builder.parse(resultReader);
                    
                    // Perform XPath query against DOM to get order Ids
                    NodeList idList = XPathAPI.selectNodeList(document.getDocumentElement(), "//order/@id");
        
                    for(int i = 0; i < idList.getLength(); i++) {
                        orderIds.add(Long.valueOf(idList.item(i).getNodeValue()));
                    }
    
            }

        } catch(Exception ex) {
            PipeComponentUtils.failTransfer("Exception while parsing order XML to retrieve Id list", ex);
        }

        return (Long[]) orderIds.toArray(new Long[orderIds.size()]);
    }
    
    /**
     * Set handling and/or payment status of an order in Workspace.
     * 
     * @param oi OpenInterfaceIF
     * @param login LoginInfo
     * @param orderId Order Id
     * @param newHandlingStatusName Name of new handling status (null value means no change)
     * @param newPaymentStatusName Name of new payment status (null value means no change)
     * @param logger MessageLogger instance
     * @return OpenInterface ImportResult containing a list of successfully updated order Ids
     * @throws FailTransferException
     */
    public static ImportResult setOrderStatus(OpenInterfaceIF oi,
                                              LoginInfo login,
                                              Long orderId,
                                              String newHandlingStatusName,
                                              String newPaymentStatusName,
                                              MessageLogger logger,
                                              Object logClassRef) throws FailTransferException {
        return setOrderStatus(oi,
                              login,
                              new Long[] { orderId },
                              newHandlingStatusName,
                              newPaymentStatusName,
                              logger,
                              logClassRef);
    }

    /**
     * Set handling and/or payment status of multiple orders in Workspace.
     * 
     * @param oi OpenInterfaceIF
     * @param login LoginInfo
     * @param orderIds Array of Order Ids
     * @param newHandlingStatusName Name of new handling status (null value means no change)
     * @param newPaymentStatusName Name of new payment status (null value means no change)
     * @param logger MessageLogger instance
     * @return OpenInterface ImportResult containing a list of successfully updated order Ids
     * @throws FailTransferException
     */
    public static ImportResult setOrderStatus(OpenInterfaceIF oi,
                                              LoginInfo login,
                                              Long[] orderIds,
                                              String newHandlingStatusName,
                                              String newPaymentStatusName,
                                              MessageLogger logger,
                                              Object logClassRef) throws FailTransferException {

        if(newHandlingStatusName != null) {
            logger.logMessage("Updating order handling status to \"" + newHandlingStatusName + "\"", logClassRef,
                MessageLogger.DEBUG);
        }
        if(newPaymentStatusName != null) {
            logger.logMessage("Updating order payment status to \"" + newPaymentStatusName + "\"", logClassRef,
                MessageLogger.DEBUG);
        }
        
        // Get an import XML message to change orders' handling and payment statuses
        String xmlMessage = getStatusChangeOrderXML(orderIds, newHandlingStatusName, newPaymentStatusName);
        
        // IMPORT_MODE_UPDATE == 2
        int importMode = 2;

        try {
            oi.importOrders(login, xmlMessage, importMode, false, false, false);
        } catch(RemoteException e) {
            logger.logMessage("RemoteException while setting order status in Open Interface: " + e.getMessage(),
                logClassRef, MessageLogger.ERROR);
            Environment.getInstance().log("RemoteException while setting order status in Open Interface", e);
            PipeComponentUtils.failTransfer();
        } catch (AccessDeniedException e) {
            logger.logMessage(
                "Access to OpenInterface was denied. Check your username and password settings.",
                logClassRef,
                MessageLogger.ERROR);
            PipeComponentUtils.failTransfer();
        } catch (OpenInterfaceException e) {
            logger.logMessage("OpenInterfaceException while setting order status in Open Interface: " +
                e.getMessage(), logClassRef, MessageLogger.ERROR);
            Environment.getInstance().log("OpenInterfaceException while setting order status in Open Interface", e);
            PipeComponentUtils.failTransfer();
        } 
        return null;
    }

    /**
     * Constructs an order import XML message that updates the order's handling & payment status.
     * 
     * @param orderIds Array of order Ids
     * @param newHandlingStatusName Handling status for the order. Null value means the status is not updated.
     * @param newPaymentStatusName Payment status for the order. Null value means the status is not updated.
     * @return OpenInterface Order XML message
     */
    public static String getStatusChangeOrderXML( Long[] orderIds,
                                                  String newHandlingStatusName,
                                                  String newPaymentStatusName ) {

        final String xmlMessageHead = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                      "<orders version=\"0.94\">\n";
        final String xmlMessageTail = "</orders>\n";

        StringBuffer xmlMessageBody = new StringBuffer("");
        
        Date currentDate = new Date();

        String orderHistoryEntryHead = "<entry time=\"" + oiDateFormat.format(currentDate) +
                                       "\">\n<status name=\"";
        String orderHistoryEntryTail = "\"/>\n</entry>\n";
        
        for(int i = 0; i < orderIds.length; i++) {
            
            xmlMessageBody.append("<order id=\"" + orderIds[i] + "\">\n");

            // Write handling status and payment status elements
            if(newHandlingStatusName != null) {
                xmlMessageBody.append("<handlingStatus name=\"" +
                    Utils.htmlentities(newHandlingStatusName) + "\"/>\n");
            }
            if(newPaymentStatusName != null) {
                xmlMessageBody.append("<paymentStatus name=\"" +
                    Utils.htmlentities(newPaymentStatusName) + "\"/>\n");
            }
            
            // Create order history entries if handling status or payment status was output
            if(newHandlingStatusName != null || newPaymentStatusName != null) {
                xmlMessageBody.append("<orderHistory>\n");
                if(newHandlingStatusName != null) {
                    xmlMessageBody.append(orderHistoryEntryHead +
                        Utils.htmlentities(newHandlingStatusName) +
                        orderHistoryEntryTail);
                }
                if(newPaymentStatusName != null) {
                    xmlMessageBody.append(orderHistoryEntryHead +
                        Utils.htmlentities(newPaymentStatusName) +
                        orderHistoryEntryTail);
                }
                xmlMessageBody.append("</orderHistory>\n");
            }

            xmlMessageBody.append("</order>\n");
        }
                            
        return xmlMessageHead + xmlMessageBody.toString() + xmlMessageTail;
        
    }

}
