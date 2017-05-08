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
import smilehouse.openinterface.CustomerCriteria;
import smilehouse.openinterface.ExportResult;
import smilehouse.opensyncro.pipes.component.AbortTransferException;
import smilehouse.opensyncro.pipes.component.FailTransferException;
import smilehouse.opensyncro.pipes.component.PipeComponentData;
import smilehouse.opensyncro.pipes.component.PipeComponentUtils;
import smilehouse.opensyncro.pipes.component.SourceIF;
import smilehouse.opensyncro.pipes.log.MessageLogger;
import smilehouse.opensyncro.pipes.metadata.SourceInfo;
import smilehouse.util.Utils;

/**
 * CustomerSource.java
 * 
 * Created: Thu Nov 23 10:25:14 2006
 */

public abstract class CustomerSource implements SourceIF {
    
    // TODO: Replace these with proper Status classes
    final int PIPE_EXECUTION_OK = 1;
    final int PIPE_EXECUTION_FAILED = 0;
    //final int PIPE_EXECUTION_ABORTED = -1;
    
    private static final Double NAN_DOUBLE = new Double(Double.NaN);

    protected static final String DATE_FORMAT = "dd.MM.yyyy HH:mm:ss";
    protected static DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
    protected static final String CUSTOMER_ID_ATTR = "customerId";
    protected static final String ID_GREATER_THAN_ATTR = "idGreaterThan";
    protected static final String ID_LESS_THAN_ATTR = "idLessThan";
    protected static final String ID_IN_ATTR = "idIn";
    protected static final String PRIMARY_CUSTOMER_GROUP_ATTR = "primaryCustomerGroup";
    protected static final String CUSTOMER_GROUP_ATTR = "customerGroup";
    protected static final String DATE_CREATED_BEFORE_ATTR = "dateCreatedBefore";
    protected static final String DATE_CREATED_AFTER_ATTR = "dateCreatedAfter";
    protected static final String DATE_LAST_VISIT_BEFORE_ATTR = "dateLastVisitBefore";
    protected static final String DATE_LAST_VISIT_AFTER_ATTR = "dateLastVisitAfter";
    
    protected static final String DATE_CUST_MODIFIED_BEFORE_ATTR = "customerModifiedBefore";
    protected static final String DATE_CUST_MODIFIED_AFTER_ATTR = "customerModifiedAfter";
    protected static final String DATE_ADMIN_MODIFIED_BEFORE_ATTR = "adminModifiedBefore";
    protected static final String DATE_ADMIN_MODIFIED_AFTER_ATTR = "adminModifiedAfter";
    protected static final String MODIFIED_OPERATION_TYPE_ATTR = "modifyOperation";

    public CustomerSource( Object pipeComponentData ) {
        setData((PipeComponentData) pipeComponentData);
    }

    private boolean allDataOutput;
    
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
        return "CustomerSource";
    }

    public String getID() {
        return this.getClass().getName();
    }

    public String getDescription(Locale locale) {
        return PipeComponentUtils.getDescription(locale, this.getClass());
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
    
	public Date getDateCreatedAfter() {
		return getDate(DATE_CREATED_AFTER_ATTR);
	}
	
	public void setDateCreatedAfter(Date after){
		setDate(DATE_CREATED_AFTER_ATTR, after);
	}

	public Date getDateCreatedBefore() {
		return getDate(DATE_CREATED_BEFORE_ATTR);
	}
	
	public void setDateCreatedBefore(Date before){
		setDate(DATE_CREATED_BEFORE_ATTR, before);
	}

	public Date getDateLastVisitAfter() {
		return getDate(DATE_LAST_VISIT_AFTER_ATTR);
	}
	
	public void setDateLastVisitAfter(Date after){
		setDate(DATE_LAST_VISIT_AFTER_ATTR, after);
	}

	public Date getDateLastVisitBefore() {
		return getDate(DATE_LAST_VISIT_BEFORE_ATTR);
	}
	
	public void setDateLastVisitBefore(Date before){
		setDate(DATE_LAST_VISIT_BEFORE_ATTR, before);
	}
	
	public void setDateCustModifiedBefore(Date before){
		setDate(DATE_CUST_MODIFIED_BEFORE_ATTR, before);
	}
	
	public Date getDateCustModifiedBefore(){
		return getDate(DATE_CUST_MODIFIED_BEFORE_ATTR);
	}
	
	public void setDateCustModifiedAfter(Date after){
		setDate(DATE_CUST_MODIFIED_AFTER_ATTR, after);
	}
	
	public Date getDateCustModifiedAfter(){
		return getDate(DATE_CUST_MODIFIED_AFTER_ATTR);
	}
	
	public void setDateAdminModifiedBefore(Date before){
		setDate(DATE_ADMIN_MODIFIED_BEFORE_ATTR, before);
	}
	
	public Date getDateAdminModifiedBefore(){
		return getDate(DATE_ADMIN_MODIFIED_BEFORE_ATTR);
	}
	
	public void setDateAdminModifiedAfter(Date after){
		setDate(DATE_ADMIN_MODIFIED_AFTER_ATTR, after);
	}
	
	public Date getDateAdminModifiedAfter(){
		return getDate(DATE_ADMIN_MODIFIED_AFTER_ATTR);
	}
	public void setModifyOperation(String operation){
		this.data.setAttribute(MODIFIED_OPERATION_TYPE_ATTR, operation != null ? operation : "OR");
	}
	public String getModifyOperation(){
		return this.data.getAttribute(MODIFIED_OPERATION_TYPE_ATTR);
	}

    public abstract ExportResult getExportResult(CustomerCriteria criteria,
                                                 MessageLogger logger) throws FailTransferException,
                                                 							  RemoteException,
                                                 							  AccessDeniedException;

    public String[] give(SourceInfo info, MessageLogger logger) throws FailTransferException, AbortTransferException {
        
        // This component does not support iteration, so we output all our data
        // once (and only once)
        if( this.allDataOutput == true ) return null;
        else this.allDataOutput = true;
        
        CustomerCriteria criteria = new CustomerCriteria();
        
        // Customer id in
        String customerIdStr = this.data.getAttribute(CUSTOMER_ID_ATTR);
        if(customerIdStr != null && customerIdStr.length() > 0)
            criteria.setCustomerId(customerIdStr);
        
        // id greater than
        String idGTStr = this.data.getAttribute(ID_GREATER_THAN_ATTR);
        if(idGTStr != null && idGTStr.length() > 0) 
                criteria.setIdGreaterThan(new String(idGTStr));

        // id less than
        String idLTStr = this.data.getAttribute(ID_LESS_THAN_ATTR);
        if(idLTStr != null && idLTStr.length() > 0)
                criteria.setIdLessThan(new String(idLTStr));

        // id in
        String idInStr = this.data.getAttribute(ID_IN_ATTR);
        if(idInStr != null && idInStr.length() > 0) {
            String[] idInStrArray = Utils.split(idInStr, ",");
            Long[] idInLongArray = new Long[idInStrArray.length];
                criteria.setIdIn(idInStrArray);
        }
        
        // Primary customer group
        String primaryCustomerGroupStr = this.data.getAttribute(PRIMARY_CUSTOMER_GROUP_ATTR);
        if(primaryCustomerGroupStr != null && primaryCustomerGroupStr.length() > 0)
            criteria.setPrimaryCustomerGroup(primaryCustomerGroupStr);
        
        // Customer group
        String customerGroupStr = this.data.getAttribute(CUSTOMER_GROUP_ATTR);
        if(customerGroupStr != null && customerGroupStr.length() > 0)
            criteria.setCustomerGroup(customerGroupStr);
        
        // Date created before
        Date dateCreatedBefore = getDateCreatedBefore();
        if(dateCreatedBefore != null) {
            Calendar dateCreatedBeforeCal = new GregorianCalendar();
            dateCreatedBeforeCal.setTime(dateCreatedBefore);
            criteria.setFirstVisitDateBefore(dateCreatedBeforeCal);
        }
        
        // Date created after
        Date dateCreatedAfter = getDateCreatedAfter();
        if(dateCreatedAfter != null) {
            Calendar dateCreatedAfterCal = new GregorianCalendar();
            dateCreatedAfterCal.setTime(dateCreatedAfter);
            criteria.setFirstVisitDateAfter(dateCreatedAfterCal);
        }
       
        // Date before last visited 
        Date dateLastVisitBefore = getDateLastVisitBefore();
        if(dateLastVisitBefore != null) {
            Calendar dateLastVisitBeforeCal = new GregorianCalendar();
            dateLastVisitBeforeCal.setTime(dateLastVisitBefore);
            criteria.setLastVisitDateBefore(dateLastVisitBeforeCal);
        }

        // Date after last visited 
        Date dateLastVisitAfter = getDateLastVisitAfter();
        if(dateLastVisitAfter != null) {
            Calendar dateLastVisitAfterCal = new GregorianCalendar();
            dateLastVisitAfterCal.setTime(dateLastVisitAfter);
            criteria.setLastVisitDateAfter(dateLastVisitAfterCal);
        }
        
        //Date before customer modified
        Date dateCustomerModifiedBefore = getDateCustModifiedBefore();
        if(dateCustomerModifiedBefore != null) {
            Calendar dateCustomerModifiedBeforeCal = new GregorianCalendar();
            dateCustomerModifiedBeforeCal.setTime(dateCustomerModifiedBefore);
            criteria.setCustomerModifiedBefore(dateCustomerModifiedBeforeCal);
        }
        
        //Date after customer modified
        Date dateCustomerModifiedAfter = getDateCustModifiedAfter();
        if(dateCustomerModifiedAfter != null) {
            Calendar dateCustomerModifiedAfterCal = new GregorianCalendar();
            dateCustomerModifiedAfterCal.setTime(dateCustomerModifiedAfter);
            criteria.setCustomerModifiedAfter(dateCustomerModifiedAfterCal);
        }
        
        //Date before admin modified
        Date dateAdminModifiedBefore = getDateAdminModifiedBefore();
        if(dateAdminModifiedBefore != null) {
            Calendar dateAdminModifiedBeforeCal = new GregorianCalendar();
            dateAdminModifiedBeforeCal.setTime(dateAdminModifiedBefore);
            criteria.setAdminModifiedBefore(dateAdminModifiedBeforeCal);
        }
        
        //Date after admin modified
        Date dateAdminModifiedAfter = getDateAdminModifiedAfter();
        if(dateAdminModifiedAfter != null) {
            Calendar dateAdminModifiedAfterCal = new GregorianCalendar();
            dateAdminModifiedAfterCal.setTime(dateAdminModifiedAfter);
            criteria.setAdminModifiedAfter(dateAdminModifiedAfterCal);
        }
        
        //Define the operation type.
        String customerModifyOperationType = getModifyOperation();
        if(customerModifyOperationType != null){
        	criteria.setModifyOperation(customerModifyOperationType);
        }
        
        logger.logMessage("Calling Open Interface", this, MessageLogger.DEBUG);
        try {
            ExportResult result;
                result = getExportResult(
                    criteria,
                    logger);
                
            if(result.getResultSize() != 0) {
                logger.logMessage(
                    "Open Interface result size: " + result.getResultSize() + " customers, XML length: "
                            + result.getXml().length(),
                    this,
                    MessageLogger.DEBUG);
            } else {
                logger.logMessage("No matching customers.", this, MessageLogger.WARNING);
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

} // CustomerSource
