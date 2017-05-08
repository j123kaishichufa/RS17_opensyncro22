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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import smilehouse.openinterface.AccessDeniedException;
import smilehouse.openinterface.ExportResult;
import smilehouse.openinterface.ImportResult;
import smilehouse.openinterface.OrderCriteria;
import smilehouse.opensyncro.pipes.component.AbortTransferException;
import smilehouse.opensyncro.pipes.component.FailTransferException;
import smilehouse.opensyncro.pipes.component.PipeComponentData;
import smilehouse.opensyncro.pipes.component.PipeComponentUtils;
import smilehouse.opensyncro.pipes.component.SourceIF;
import smilehouse.opensyncro.pipes.log.MessageLogger;
import smilehouse.opensyncro.pipes.metadata.SourceInfo;
import smilehouse.util.Utils;

/**
 * OrderSource.java
 * 
 * Created: Mon Apr 5 10:25:14 2004
 */

public abstract class OrderSource implements SourceIF {

    // Set to true to compile a version of RemoteOrderSource which is backward compatible
    // with Workspace 1.5, but performs Order status update disregard of the success of the
    // Pipe execution. 
    private static final boolean ws15CompatibilityMode = false;
    
    // TODO: Replace these with proper Status classes
    final int PIPE_EXECUTION_OK = 1;
    final int PIPE_EXECUTION_FAILED = 0;
    //final int PIPE_EXECUTION_ABORTED = -1;
    
    private static final Double NAN_DOUBLE = new Double(Double.NaN);

    protected static final String DATE_FORMAT = "dd.MM.yyyy HH:mm:ss";
    protected static DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

    protected static final String DATE_AFTER_ATTR = "dateAfter";
    protected static final String DATE_BEFORE_ATTR = "dateBefore";
    protected static final String CUSTOMER_ID_IN_ATTR = "customerIdIn";
    protected static final String ID_GREATER_THAN_ATTR = "idGreaterThan";
    protected static final String ID_LESS_THAN_ATTR = "idLessThan";
    protected static final String ID_IN_ATTR = "idIn";
    protected static final String HANDLING_STATUS_NAME_IN_ATTR = "handlingStatusNameIn";
    protected static final String HANDLING_STATUS_NAME_NOT_IN_ATTR = "handlingStatusNameNotIn";
    protected static final String PAYMENT_STATUS_NAME_IN_ATTR = "paymentStatusNameIn";
    protected static final String PAYMENT_STATUS_NAME_NOT_IN_ATTR = "paymentStatusNameNotIn";
    protected static final String SUM_GREATER_THAN_ATTR = "sumGreaterThan";
    protected static final String SUM_LESS_THAN_ATTR = "sumLessThan";
    protected static final String NEW_STATUS_NAME_ATTR = "newStatusName";

    public OrderSource( Object pipeComponentData ) {
        setData((PipeComponentData) pipeComponentData);
    }
    
    private boolean allDataOutput;
    
    // New update log Id to store to component attributes after a successful Pipe execution
    private Long newUpdateLogId = null;
    
    // New order handling status name to set after a successful Pipe execution
    private String newHandlingStatusName = null;
    
    // Id array of order whose handling status should be changed to newStatusName 
    private Long[] orderIds = null;
    
    // MessageLogger reference
    private MessageLogger logger = null;
    
    protected PipeComponentData data;
    
    public void setData(PipeComponentData data) {
        this.data = data;
    }

    public PipeComponentData getData() {
        return data;
    }
    
    public final int getType() {
        return TYPE_SOURCE;
    }
    
    public String getName() {
        return "OrderSource";
    }

    public String getID() {
        return this.getClass().getName();
    }

    public String getDescription(Locale locale) {
        return PipeComponentUtils.getDescription(locale, this.getClass());
    }

    public String getNewHandlingStatusName() {
        return newHandlingStatusName;
    }

    public void setNewHandlingStatusName(String newHandlingStatusName) {
        this.newHandlingStatusName = newHandlingStatusName;
    }

    public Long getNewUpdateLogId() {
        return newUpdateLogId;
    }

    public void setNewUpdateLogId(Long newUpdateLogId) {
        this.newUpdateLogId = newUpdateLogId;
    }

    public Long[] getOrderIds() {
        return orderIds;
    }

    public void setOrderIds(Long[] orderIds) {
        this.orderIds = orderIds;
    }

    // Dummy methods due to no iteration supported
    public int open(SourceInfo info, MessageLogger logger) throws FailTransferException {
        
        // Store MessageLogger for later use
        this.logger = logger;
        
        this.allDataOutput = false;
        return ITERATION_OPEN_STATUS_OK;
    }

    public int close(SourceInfo info, MessageLogger logger) throws FailTransferException {
        return ITERATION_CLOSE_STATUS_OK;
    }

    public void lastBlockStatus(int statusCode) throws FailTransferException, AbortTransferException {

        if(ws15CompatibilityMode == false) {

            if(statusCode == PIPE_EXECUTION_OK) {
                
                if((getOrderIds() != null) && (getNewHandlingStatusName() != null) ) {
                    
                    // Update all order statuses
                    setOrderStatus(getOrderIds(), getNewHandlingStatusName(), null, this.logger);
                }
                
            }
        }    
        
    }

    private Date getDate(String attrName) {
        try {
            String dateStr = this.data.getAttribute(attrName);
            if(dateStr != null && dateStr.length() > 0) {
                return dateFormat.parse(dateStr);
            }
        } catch(ParseException pe) {}
        return null;
    }

    private void setDate(String attrName, Date date) {
        this.data.setAttribute(attrName, date != null ? dateFormat.format(date) : "");
    }


    public Date getDateAfter() {
        return getDate(DATE_AFTER_ATTR);
    }

    public void setDateAfter(Date after) {
        setDate(DATE_AFTER_ATTR, after);
    }

    public Date getDateBefore() {
        return getDate(DATE_BEFORE_ATTR);
    }

    public void setDateBefore(Date before) {
        setDate(DATE_BEFORE_ATTR, before);
    }

    public void setSumGreaterThan(Double sumGreaterThan) {
        String strValue = sumGreaterThan != null && !sumGreaterThan.isNaN() ? sumGreaterThan
            .toString() : "";
        this.data.setAttribute(SUM_GREATER_THAN_ATTR, strValue);
    }

    public Double getSumGreaterThan() {
        String sumGTStr = this.data.getAttribute(SUM_GREATER_THAN_ATTR);
        if(sumGTStr != null && sumGTStr.length() > 0) {
            try {
                return new Double(sumGTStr);
            } catch(NumberFormatException nfe) {
                // Shouldn't occur (?)
            }
        }
        return NAN_DOUBLE;
    }

    public void setSumLessThan(Double sumLessThan) {
        String strValue = sumLessThan != null && !sumLessThan.isNaN() ? sumLessThan.toString() : "";
        this.data.setAttribute(SUM_LESS_THAN_ATTR, strValue);
    }

    public Double getSumLessThan() {
        String sumLTStr = this.data.getAttribute(SUM_LESS_THAN_ATTR);
        if(sumLTStr != null && sumLTStr.length() > 0) {
            try {
                return new Double(sumLTStr);
            } catch(NumberFormatException nfe) {
                // Shouldn't occur (?)
            }
        }
        return NAN_DOUBLE;
    }

    public abstract ExportResult getExportResult(OrderCriteria criteria,
                                                 Long updateLogId,
                                                 String newStatusName,
                                                 SourceInfo info,
                                                 MessageLogger logger) throws FailTransferException,
                                                 							  RemoteException,
                                                 							  AccessDeniedException;

    public abstract ImportResult setOrderStatus(Long orderId,
                                                String newHandlingStatusName,
                                                String newPaymentStatusName,
                                                MessageLogger logger) throws FailTransferException;

    public abstract ImportResult setOrderStatus(Long[] orderIds,
                                                String newHandlingStatusName,
                                                String newPaymentStatusName,
                                                MessageLogger logger) throws FailTransferException;


    public String[] give(SourceInfo info, MessageLogger logger) throws FailTransferException, AbortTransferException {
        
        // This component does not support iteration, so we output all our data
        // once (and only once)
        if( this.allDataOutput == true ) return null;
        else this.allDataOutput = true;
        
        OrderCriteria criteria = new OrderCriteria();

        // Date after
        Date dateAfter = getDateAfter();
        if(dateAfter != null) {
            Calendar dateAfterCal = new GregorianCalendar();
            dateAfterCal.setTime(dateAfter);
            criteria.setDateAfter(dateAfterCal);
        }

        // Date before
        Date dateBefore = getDateBefore();
        if(dateBefore != null) {
            Calendar dateBeforeCal = new GregorianCalendar();
            dateBeforeCal.setTime(dateBefore);
            criteria.setDateBefore(dateBeforeCal);
        }

        // Customer id in
        String customerIdInStr = this.data.getAttribute(CUSTOMER_ID_IN_ATTR);
        if(customerIdInStr != null && customerIdInStr.length() > 0)
            criteria.setCustomerIdIn(Utils.split(customerIdInStr, ","));

        // Hanling status in
        String handlingStatusNameInStr = this.data.getAttribute(HANDLING_STATUS_NAME_IN_ATTR);
        if(handlingStatusNameInStr != null && handlingStatusNameInStr.length() > 0)
            criteria.setHandlingStatusNameIn(Utils.split(handlingStatusNameInStr, ","));

        // Handling status not in
        String handlingStatusNameNotInStr = this.data.getAttribute(HANDLING_STATUS_NAME_NOT_IN_ATTR);
        if(handlingStatusNameNotInStr != null && handlingStatusNameNotInStr.length() > 0)
            criteria.setHandlingStatusNameNotIn(Utils.split(handlingStatusNameNotInStr, ","));

        // Payment status in
        String paymentStatusNameInStr = this.data.getAttribute(PAYMENT_STATUS_NAME_IN_ATTR);
        if(paymentStatusNameInStr != null && paymentStatusNameInStr.length() > 0)
            criteria.setPaymentStatusNameIn(Utils.split(paymentStatusNameInStr, ","));

        // Payment status not in
        String paymentStatusNameNotInStr = this.data.getAttribute(PAYMENT_STATUS_NAME_NOT_IN_ATTR);
        if(paymentStatusNameNotInStr != null && paymentStatusNameNotInStr.length() > 0)
            criteria.setPaymentStatusNameNotIn(Utils.split(paymentStatusNameNotInStr, ","));

        // New status
        String newStatusName = this.data.getAttribute(NEW_STATUS_NAME_ATTR);
        if(newStatusName != null && newStatusName.length() == 0)
            newStatusName = null;

        // id greater than
        String idGTStr = this.data.getAttribute(ID_GREATER_THAN_ATTR);
        if(idGTStr != null && idGTStr.length() > 0) {
            try {
                criteria.setIdGreaterThan(new Long(idGTStr));
            } catch(NumberFormatException nfe) {
                logger.logMessage(
                    "Non-numeric value in 'id greater than' criteria",
                    this,
                    MessageLogger.ERROR);
                throw nfe;
            }
        }
        // id less than
        String idLTStr = this.data.getAttribute(ID_LESS_THAN_ATTR);
        if(idLTStr != null && idLTStr.length() > 0) {
            try {
                criteria.setIdLessThan(new Long(idLTStr));
            } catch(NumberFormatException nfe) {
                logger.logMessage(
                    "Non-numeric value in 'id greater than' criteria",
                    this,
                    MessageLogger.ERROR);
                throw nfe;
            }
        }
        // id in
        String idInStr = this.data.getAttribute(ID_IN_ATTR);
        if(idInStr != null && idInStr.length() > 0) {
            String[] idInStrArray = Utils.split(idInStr, ",");
            Long[] idInLongArray = new Long[idInStrArray.length];
            try {
                for(int i = 0; i < idInStrArray.length; i++)
                    idInLongArray[i] = new Long(idInStrArray[i]);
                criteria.setIdIn(idInLongArray);
            } catch(NumberFormatException nfe) {
                logger.logMessage(
                    "Non-numeric value in 'id-in' criteria",
                    this,
                    MessageLogger.ERROR);
                throw nfe;
            }
        }

        // Sum greater than
        Double sumGreaterThan = getSumGreaterThan();
        if(sumGreaterThan != null && !sumGreaterThan.isNaN())
            criteria.setSumGreaterThan(sumGreaterThan);
        // Sum less than
        Double sumLessThan = getSumLessThan();
        if(sumLessThan != null && !sumLessThan.isNaN())
            criteria.setSumLessThan(sumLessThan);

        logger.logMessage("Calling Open Interface", this, MessageLogger.DEBUG);
        try {
            ExportResult result;
            
            if(ws15CompatibilityMode == true) {
                result = getExportResult(
                    criteria,
                    null,
                    newStatusName,
                    info,
                    logger);
                
            } else {
                // For Workspace 1.6 and newer we don't update the orders' status while
                // exporting them, but after the Pipe execution is successfully completed 
                result = getExportResult(
                    criteria,
                    null,
                    null,
                    info,
                    logger);
                
                setNewHandlingStatusName(newStatusName);
                
                // Extract and save a list of order Ids so that we can update the orders'
                // handling statuses after successful execution of the Pipe
                setOrderIds(WorkspaceOIUtils.getOrderIdsFromXML(result.getXml()));
                
                // Also lastUpdateLogId will be stored only after a successful Pipe execution
                if(result.getLastUpdateLogId() != null) {
                    newUpdateLogId = result.getLastUpdateLogId();
                }
            }

            if(result.getResultSize() != 0) {
                logger.logMessage(
                    "Open Interface result size: " + result.getResultSize() + " orders, XML length: "
                            + result.getXml().length(),
                    this,
                    MessageLogger.DEBUG);
            } else {
                logger.logMessage("No matching orders.", this, MessageLogger.WARNING);
                PipeComponentUtils.abortTransfer();
            }
            return new String[] {result.getXml()};

        } catch(AccessDeniedException ade) {
            logger.logMessage(
                "Access to Open Interface was denied. Check your username and password settings.",
                this,
                MessageLogger.ERROR);
            //throw ade;
            PipeComponentUtils.failTransfer();
        } catch(RemoteException re) {
            logger.logMessage(
                "Couldn't access the remote Open Interface: " + re.getMessage(),
                this,
                MessageLogger.ERROR);
            //throw re;
            PipeComponentUtils.failTransfer();
        }
        
        // Unreachable code, should never return null String array
        return null;
    }

} // OrderSource