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

package smilehouse.opensyncro.pipes;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import smilehouse.opensyncro.pipes.component.AbortTransferException;
import smilehouse.opensyncro.pipes.component.ConverterIF;
import smilehouse.opensyncro.pipes.component.DestinationIF;
import smilehouse.opensyncro.pipes.component.FailTransferException;
import smilehouse.opensyncro.pipes.component.PipeComponentData;
import smilehouse.opensyncro.pipes.component.PipeComponentIF;
import smilehouse.opensyncro.pipes.component.SourceIF;
import smilehouse.opensyncro.pipes.log.LogEntry;
import smilehouse.opensyncro.pipes.log.LogMessageEntry;
import smilehouse.opensyncro.pipes.log.MessageLogger;
import smilehouse.opensyncro.pipes.metadata.TransferInfo;
import smilehouse.opensyncro.servlets.PipeComponentCreationException;
import smilehouse.opensyncro.system.Environment;
import smilehouse.opensyncro.system.Persister;
import smilehouse.util.Utils;

public class Pipe implements Serializable,Cloneable {

	private static final String MAIL_MESSAGE_NO_ENTRIES = "There are no log entries that match the notification level setting for this pipe";

    private static final String MAIL_SENDER = "OpenSyncro";

    private static final String[] MAIL_ADDRESS_DELIMITERS = {",", ";"};

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm";
    
    private static DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

    /** identifier field */
    private Long id;

    /** nullable persistent field */
    private String name;

    private String startPassword;
    // RPC Start is not currently supported
    //private boolean rpcStartEnabled;

    private boolean httpStartEnabled;

    private boolean abortMailEnabled;
    
    /** persistent field */
    private String sourceID;

    /** non-persistent properties */
    private SourceIF source;
    private PipeComponentData sourceData;

    /** persistent field */
    private String destinationID;

    /** non-persistent properties */
    private DestinationIF destination;
    private PipeComponentData destinationData;

    private List converterList;


    /** persistent field */
    private Set log;

    private int loggingVerbosityLevel;
    private int transferLogNotificationLevel;
    private String mailHost;
    private String recipientAddress;
    
    private Date startTime;
    private Date endTime;
    private String lastStatus;
    private Long duration;
    private String user;
    
    private String database;
    
    /** default constructor */
    public Pipe() {
        this.name = "Unnamed Pipe";
        this.startPassword = "";
        //this.rpcStartEnabled = false;
        this.httpStartEnabled = false;
        this.abortMailEnabled = true;
        loggingVerbosityLevel = MessageLogger.LOG_DYNAMIC;
        transferLogNotificationLevel = 1;
        mailHost = "";
        recipientAddress = "";
        this.converterList = new LinkedList();
        this.source = null;
        this.sourceData = null;
        this.destination = null;
        this.destinationData = null;
        this.startTime=null;
        this.endTime=null;
        this.duration=new Long("0");
        this.user="";
        this.lastStatus="";
        
    }

    public PipeComponentData getSourceData() {
        return this.sourceData;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSourceID() {
        return this.sourceID;
    }

    public void setSourceID(String sourceID) {
        this.sourceID = sourceID;
        this.source = null;
    }

    public String getDestinationID() {
        return this.destinationID;
    }


    public void setDestinationID(String destinationID) {
        this.destinationID = destinationID;
        this.destination = null;
    }

    public String getStartPassword() {
        return this.startPassword;
    }

    public void setStartPassword(String startPassword) {
        this.startPassword = startPassword;
    }

    /*
     * public boolean isRpcStartEnabled() { return this.rpcStartEnabled; }
     * 
     * public void setRpcStartEnabled(boolean rpcStartEnabled) { this.rpcStartEnabled =
     * rpcStartEnabled; }
     */

    public boolean isHttpStartEnabled() {
        return this.httpStartEnabled;
    }

    public void setHttpStartEnabled(boolean httpStartEnabled) {
        this.httpStartEnabled = httpStartEnabled;
    }
    
    public boolean isAbortMailEnabled() {
        return this.abortMailEnabled;
    }

    public void setAbortMailEnabled(boolean emptyMailEnabled) {
        this.abortMailEnabled = emptyMailEnabled;
    }
    
    public SourceIF getSource() {
        try {
            if((this.source == null) && (this.sourceID != null)) {
                this.source = (SourceIF) Persister.getInstance(getSourceID());
            }
        } catch(PipeComponentCreationException e) {
            Environment.getInstance().log("Error loading Source component for Pipe \"" +
                getName() + "\"", e);
            this.source = null;
        }
        return this.source;
    }

    public SourceIF getCurrentSource() {
        return this.source;
    }


    public void setSourceData(PipeComponentData sourceData) {
        this.sourceData = sourceData;
    }

    public void setSource(SourceIF source) {
        this.source = source;
    }

    public DestinationIF getDestination() {
        try {
            if((this.destination == null) && (this.destinationID != null)) {
                this.destination = (DestinationIF) Persister.getInstance(getDestinationID());
            }
        } catch(PipeComponentCreationException e) {
            Environment.getInstance().log("Error loading Destination component for Pipe \""
                + getName() + "\"", e);
            this.destination = null;
        }
        return this.destination;
    }

    public DestinationIF getCurrentDestination() {
        return this.destination;
    }

    public void setDestinationData(PipeComponentData destinationData) {
        this.destinationData = destinationData;
    }

    public void setDestination(DestinationIF destination) {
        this.destination = destination;
    }

    public List getConverterList() {
        return this.converterList;
    }

    public void setConverterList(List converters) {
        Iterator iter = converters.iterator();
        while(iter.hasNext()) {
            ConverterListItem convListItem = (ConverterListItem) iter.next();
            convListItem.setParent(this);
        }
        this.converterList = converters;
    }

    public ConverterListItem addConverter(String converterID, PipeComponentData pcdata) {
        ConverterListItem convListItem = new ConverterListItem(converterID, pcdata);
        convListItem.setParent(this);
        this.converterList.add(convListItem);
        return convListItem;
    }

    public ConverterListItem addConverter(ConverterIF converter, PipeComponentData pcdata) {
        ConverterListItem convListItem = new ConverterListItem(converter, pcdata);
        convListItem.setParent(this);
        this.converterList.add(convListItem);
        return convListItem;
    }

    public PipeComponentData getDestinationData() {
        return this.destinationData;
    }

    public Set getLog() {
        return this.log;
    }

    public void setLog(Set log) {
        this.log = log;
    }

    public void setLoggingVerbosityLevel(int level) {
        this.loggingVerbosityLevel = level;
    }

    public int getLoggingVerbosityLevel() {
        return this.loggingVerbosityLevel;
    }

    public void setTransferLogNotificationLevel(int level) {
        this.transferLogNotificationLevel = level;
    }

    public int getTransferLogNotificationLevel() {
        return this.transferLogNotificationLevel;
    }

    public String getMailHost() {
        return this.mailHost;
    }

    public void setMailHost(String host) {
        this.mailHost = host;
    }

    public String getRecipientAddress() {
        return this.recipientAddress;
    }

    public void setRecipientAddress(String address) {
        this.recipientAddress = address;
    }

    public String toString() {
        return new ToStringBuilder(this).append("id", getId()).toString();
    }

    public boolean equals(Object other) {
        if(!(other instanceof Pipe))
            return false;
        Pipe castOther = (Pipe) other;
        return new EqualsBuilder().append(this.getId(), castOther.getId()).isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder().append(getId()).toHashCode();
    }

    public synchronized void transfer(TransferInfo info){
    	transfer(info,null);
    }
    
    /** Execute the Pipe */
    public synchronized void transfer(TransferInfo info, Date requestTime) {
        LogEntry logEntry = new LogEntry(this, info);
        database = info.getDatabaseName();
        
        //Set start time so that duration can be calculated when pipe finishes
        long start=System.currentTimeMillis();
        Date startTime=new Date();
        setExecutionStartInfo(info.getUserName(), startTime);
        logEntry.setUserName(info.getUserName());
        // TODO: Replace these with proper Status classes
        
        final int PIPE_EXECUTION_OK = 1;
        final int PIPE_EXECUTION_FAILED = 0;
        //final int PIPE_EXECUTION_ABORTED = -1;        

        // currentTask information will be used for error/exception logging
        String currentTask = "Pipe transfer initialization";

        // Initialize Pipe execution status to STATUS_OK
        int statusCode = LogEntry.STATUS_OK;

        // This status flag indicates whether calling closeComponentSessions is required
        // at the end of Pipe execution
        boolean componentSessionsOpen = false;
        
        if(requestTime != null) {
            logEntry.logMessage("Received Pipe execution request", this, MessageLogger.DEBUG, requestTime);
        }
        
        // Get Source component and initialize its parameters
        {
            SourceIF source = getSource();
            if(source == null) {
                if(getSourceID() == null) {
                    logEntry.logMessage(
                        "Pipe does not have a Source component, aborting",
                        this,
                        MessageLogger.ERROR);
                } else {
                    logEntry.logMessage(
                        "Pipe Source component cannot be loaded, aborting",
                        this,
                        MessageLogger.ERROR);
                }
                setExecutionEndInfo(new Date(), System.currentTimeMillis()-start, LogEntry.STATUS_SOURCE_ERROR);
                addLogEntry(logEntry, LogEntry.STATUS_SOURCE_ERROR);
                return;
            }
            source.setData(getSourceData());
        }

        // Get Converter components and initialize their parameters
        {
            int converterIndex = 0;
            for(Iterator it = converterList.iterator(); it.hasNext();) {
                ConverterListItem converterItem = (ConverterListItem) it.next();
                converterIndex++;

                try {
                    ConverterIF converter = converterItem.getConverter();
                    converter.setData(converterItem.getConverterData());
                } catch(PipeComponentCreationException e) {
                    logEntry.logMessage("Pipe Converter component #" + converterIndex
                            + " cannot be loaded, aborting", this, MessageLogger.ERROR);
                    Environment.getInstance().log("Error loading Converter component #" + converterIndex +
                        " for Pipe \"" + getName() + "\"", e);
                    setExecutionEndInfo(new Date(), System.currentTimeMillis()-start, LogEntry.STATUS_CONVERSION_ERROR);
                    addLogEntry(logEntry, LogEntry.STATUS_CONVERSION_ERROR);
                    return;

                }
            }
        }

        // Get Destination component and initialize its parameters
        {
            DestinationIF destination = getDestination();
            if(destination == null) {
                if(getDestinationID() == null) {
                    logEntry.logMessage(
                        "Pipe does not have a Destination component, aborting",
                        this,
                        MessageLogger.ERROR);
                } else {
                    logEntry.logMessage(
                        "Pipe Destination component cannot be loaded, aborting",
                        this,
                        MessageLogger.ERROR);
                }
                setExecutionEndInfo(new Date(), System.currentTimeMillis()-start, LogEntry.STATUS_DESTINATION_ERROR);
                addLogEntry(logEntry, LogEntry.STATUS_DESTINATION_ERROR);
                return;
            }
            destination.setData(getDestinationData());
        }
        
        logEntry.logMessage("Starting Pipe execution", this, MessageLogger.DEBUG);
        
        try {

            /** Open Source, Converter and Destination components' sessions */
            currentTask = "Pipe Component initialization";
            statusCode = openComponentSessions(
                source,
                destination,
                getConverterList(),
                info,
                logEntry);
            if(statusCode != LogEntry.STATUS_OK) {
                // Initialization failed, LogEntry's statusCode is used at the end of this method
                // for addLogEntry() call
                throw new FailTransferException();
            }
            componentSessionsOpen = true;

            /** Pipe iteration loop starts here */
            int i = 0;
            while(true) {

                try {
                    logEntry.logMessage(
                        "Requesting data block #" + ++i + " from Source component",
                        this,
                        MessageLogger.DEBUG);

                    currentTask = "Source component " + this.source.getName();
                    statusCode = LogEntry.STATUS_SOURCE_ERROR;
                    String sourceResults[] = source.give(info, logEntry);

                    // Test if Source component returned no (more) data
                    if(sourceResults == null || sourceResults[0] == null) {
                        logEntry.logMessage(
                            "Source component returned no data",
                            this,
                            MessageLogger.DEBUG);

                        /*
                         * Exit iteration loop when Source component returns null instead of data
                         * String
                         */
                        break;
                    }

                    if(sourceResults.length > 1) {
                        logEntry.logMessage("Source component returned " + sourceResults.length
                                + " data blocks", this, MessageLogger.DEBUG);
                    }

                    // Iterate over Strings returned by Source component
                    for(int j = 0; j < sourceResults.length; j++) {

                        // Proceed with conversions
                        statusCode = LogEntry.STATUS_CONVERSION_ERROR;
                        currentTask = "Preparing to call first Converter component";

                        getConverterList();
                        Iterator it = this.converterList.iterator();

                        // Check if we have any Converters on the list
                        if(it.hasNext() == true) {

                            /*
                             * Converters (such as SplitConverter) may return multiple data parts
                             * based on a single input String
                             */

                            String[] processedData = new String[1];

                            /*
                             * For first Converter on the list we call the regular 'convert' method,
                             * which takes String as input and returns String array
                             */

                            ConverterListItem converterItem = (ConverterListItem) it.next();
                            ConverterIF converter = converterItem.getConverter();
                            currentTask = "Converter component " + converter.getName();
                            processedData = converter.convert(sourceResults[j], info, logEntry);
                            /*
                             * Test if any of the first Converter's result Strings is null, if yes ->
                             * report an error and abort Pipe execution
                             */
                            if(arrayContainsNull(processedData)) {
                                logEntry.logMessage(
                                    "Error: Converter " + converter.getName()
                                            + " returned null result, aborting",
                                    this,
                                    MessageLogger.ERROR);
                                throw new FailTransferException();
                            }

                            /*
                             * Repeat over the rest of the Converters list and call 'convertAll'
                             * method for processing every String in a String array
                             */

                            while(it.hasNext()) {

                                converterItem = (ConverterListItem) it.next();
                                converter = converterItem.getConverter();
                                currentTask = "Converter component " + converter.getName();
                                processedData = converter.convertAll(processedData, info, logEntry);
                                /*
                                 * Test if any of the Converter's result Strings is null, if yes ->
                                 * report an error and abort Pipe execution
                                 */
                                if(arrayContainsNull(processedData)) {
                                    logEntry.logMessage(
                                        "Error: Converter " + converter.getName()
                                                + " returned null result, aborting",
                                        this,
                                        MessageLogger.ERROR);
                                    throw new FailTransferException();
                                }
                            }

                            statusCode = LogEntry.STATUS_DESTINATION_ERROR;
                            currentTask = "Destination component " + this.destination.getName();
                            destination.takeAll(processedData, info, logEntry);

                        } else {

                            /*
                             * There were no Converters in this Pipe, so we pass the data from
                             * Source component directly to Destination component
                             */

                            statusCode = LogEntry.STATUS_DESTINATION_ERROR;
                            currentTask = "Destination component " + this.destination.getName();
                            destination.take(sourceResults[j], info, logEntry);

                        }

                        statusCode = LogEntry.STATUS_OK;

                    }

                    /*
                     * Notify the Source component that last data block(s) were processed
                     * successfully
                     */
                    statusCode = LogEntry.STATUS_SOURCE_ERROR;
                    currentTask = "while notifying Source component " + this.source.getName() + " of "
                            + " successful processing of last data block through the Pipe";
                    
                    source.lastBlockStatus(PIPE_EXECUTION_OK);

                    currentTask = "End of Pipe iteration loop";
                    statusCode = LogEntry.STATUS_OK;

                } catch(AbortTransferException ate) {
                    /*
                     * Pass on AbortTransferException to the outside-loop catcher, since we want to
                     * abort the entire transfer.
                     */
                    throw ate;

                } catch(Throwable t) {
                    
                    if(t instanceof Exception || t instanceof VirtualMachineError) {
                        logPipeExecutionError(t, currentTask, logEntry);

                        /**
                         * Notify the Source component of last data block failing to process, unless
                         * an unknown exception has occured in the Source component itself
                         */
                        if(statusCode != LogEntry.STATUS_SOURCE_ERROR) {
                            currentTask = "Notifying Source component " + this.source.getName()
                                    + " of last data block failing to process through the Pipe";
                            // TODO: test support for FailTransferException and AbortTransferException during lastBlockStatus!
                            source.lastBlockStatus(PIPE_EXECUTION_FAILED);
                        }

                        logEntry.logMessage(
                            "Data block processing failed, aborting Pipe execution",
                            this,
                            MessageLogger.ERROR);

                        if(t instanceof Exception) {
                            // Pass Exception for the outer Exception handler
                            throw (Exception) t;
                        } else {
                            // Convert VirtualMachineErrors to FailTransferExceptions as we have
                            // already written the detailed error message to Transfer log
                            throw new FailTransferException();
                        }
                    }

                }

            } // Iteration loop ends here

            logEntry.logMessage("Data block processing complete", this, MessageLogger.DEBUG);
            
            statusCode = LogEntry.STATUS_OK;

        } catch(AbortTransferException ate) {

            statusCode = LogEntry.STATUS_ABORTED;

        } catch(Exception e) {

            // -----------------------------------------------------------
            // If some component did not deliberately fail the transfer by
            // throwing AbortTransferException, log the exception
            // -----------------------------------------------------------
            if(!(e instanceof FailTransferException)) {
                Environment.getInstance().log(Utils.getThrowableName(e) + " during transfer, " + currentTask, e);
                logEntry.logMessage(
                    Utils.getThrowableName(e) + " during transfer, " + currentTask + ": " + e.getMessage()
                            + ". See OpenSyncro log file for details.",
                    this,
                    MessageLogger.ERROR);
            }

        } finally {

            // Close iteration session (if open).
            // Discard return code since an error has already happened.
            // Preserve the earlier statusCode for addLogEntry() at the end of this method.
            if(componentSessionsOpen) {
                currentTask = "Closing Pipe component sessions";

                // Preserve the Pipe's original statusCode unless
                // something goes wrong during closing the component iteration sessions.
                int closeSessionStatusCode = closeComponentSessions(info, logEntry);
                if(closeSessionStatusCode != LogEntry.STATUS_OK) {
                    statusCode = closeSessionStatusCode;
                }
                componentSessionsOpen = false;
            }

            switch(statusCode) {
            
                case LogEntry.STATUS_OK:
                    break;
                    
                case LogEntry.STATUS_ABORTED:
                    logEntry.logMessage("TRANSFER ABORTED", this, MessageLogger.WARNING);
                    break;
                    
                // The rest of the statusCodes are errors    
                default:
                    logEntry.logMessage("TRANSFER FAILED!", this, MessageLogger.ERROR);
                    break;
            }
            
            logEntry.logMessage("Pipe execution finished", this, MessageLogger.DEBUG);
            setExecutionEndInfo( new Date(), System.currentTimeMillis()-start, statusCode);
            addLogEntry(logEntry, statusCode);
            
            // Add "--" log message entry when there are no other entries.
            // Needed to display log entries that don't have log message entries.
            Persister persister = new Persister(database);
            if (persister.getLogMessageEntries(logEntry.getId(), MessageLogger.LOG_DYNAMIC).size() == 0){
            	logEntry.setIndex(1);
            	logEntry.logMessage("--", MessageLogger.ERROR);
            }

        }

    }


	private int openComponentSessions(SourceIF source,
                                      DestinationIF destination,
                                      List converterList,
                                      TransferInfo info,
                                      LogEntry logEntry) {
        int statusCode = LogEntry.STATUS_OK;

        /** Open iteration session at Source component */
        if(openComponentSession(getSource(), info, logEntry) == PipeComponentIF.ITERATION_OPEN_STATUS_ERROR) {
            statusCode = LogEntry.STATUS_SOURCE_ERROR;
            return statusCode;
        }

        /** Open iteration session at Converter components */
            // Initialize converters
        int converterIndex = 0;
        for(Iterator it = converterList.iterator(); it.hasNext();) {
            ConverterListItem converterItem = (ConverterListItem) it.next();
            ConverterIF converter = converterItem.getConverter();
            converterIndex++;

            // Set PipeComponentData to Converter components before opening iteration session
            converter.setData(converterItem.getConverterData());

            // Open Converter's iteration session
            if(openComponentSession(converter, converterIndex, info, logEntry) == PipeComponentIF.ITERATION_OPEN_STATUS_ERROR) {
                // Close preceding Converter and Source component sessions

                // Warning: make sure (converterIndex - 1) here is at least 0, because
                // -1 means that all Converter sessions are to be closed
                closeConverterComponentSessions(converterIndex - 1, info, logEntry);
                closeComponentSession(source, info, logEntry);

                statusCode = LogEntry.STATUS_CONVERSION_ERROR;
                return statusCode;
            }
        }

        /** Open iteration session at Destination component */
        if(openComponentSession(getDestination(), info, logEntry) == PipeComponentIF.ITERATION_OPEN_STATUS_ERROR) {

            // Close Converter and Source component sessions
            closeConverterComponentSessions(info, logEntry);
            closeComponentSession(source, info, logEntry);

            statusCode = LogEntry.STATUS_DESTINATION_ERROR;
            return statusCode;
        }

        return statusCode;
    }

    private int openComponentSession(DestinationIF destination, TransferInfo info, LogEntry logEntry) {
        // Open Destination component session
        try {
            int retCode;
            // Initialize Destination component for iteration mode
            logEntry.logMessage(
                "Opening iteration session at Destination component",
                this,
                MessageLogger.DEBUG);
            retCode = destination.open(info, logEntry);
            if(retCode != PipeComponentIF.ITERATION_OPEN_STATUS_OK) {
                logEntry.logMessage("Error opening session at Destination component "
                        + this.destination.getName(), this, MessageLogger.ERROR);
                return PipeComponentIF.ITERATION_OPEN_STATUS_ERROR;
            }

        } catch(Exception e) {
        
            logSessionOpenError(e, "destination", destination, logEntry);
            return PipeComponentIF.ITERATION_OPEN_STATUS_ERROR;

        } catch(VirtualMachineError e) {

            logSessionOpenError(e, "destination", destination, logEntry);
            return PipeComponentIF.ITERATION_OPEN_STATUS_ERROR;
        
        }

        return PipeComponentIF.ITERATION_OPEN_STATUS_OK;
    }

    private int openComponentSession(ConverterIF converter,
                                     int converterIndex,
                                     TransferInfo info,
                                     LogEntry logEntry) {
        // Open Converter component session
        try {
            int retCode;
            logEntry.logMessage("Opening iteration session at Converter component #"
                    + converterIndex + " (" + converter.getName() + ")", this, MessageLogger.DEBUG);

            retCode = converter.open(info, logEntry);
            if(retCode != PipeComponentIF.ITERATION_OPEN_STATUS_OK) {

                logEntry.logMessage(
                    "Error opening iteration session at Converter component #" + converterIndex
                            + " (" + converter.getName() + ")",
                    this,
                    MessageLogger.ERROR);
                return PipeComponentIF.ITERATION_OPEN_STATUS_ERROR;

            }
        } catch(Exception e) {

            logSessionOpenError(e, "converter", converter, logEntry);
            return PipeComponentIF.ITERATION_OPEN_STATUS_ERROR;
            
        } catch(VirtualMachineError e) {

            logSessionOpenError(e, "converter", converter, logEntry);
            return PipeComponentIF.ITERATION_OPEN_STATUS_ERROR;
            
        }

        return PipeComponentIF.ITERATION_OPEN_STATUS_OK;
    }

    private int openComponentSession(SourceIF source, TransferInfo info, LogEntry logEntry) {
        // Open Source component session
        try {
            int retCode;

            // Initialize Source component for iteration mode
            logEntry.logMessage(
                "Opening iteration session at Source component",
                this,
                MessageLogger.DEBUG);
            retCode = source.open(info, logEntry);
            if(retCode != PipeComponentIF.ITERATION_OPEN_STATUS_OK) {
                logEntry.logMessage(
                    "Error opening session at Source component " + source.getName(),
                    this,
                    MessageLogger.ERROR);
                return PipeComponentIF.ITERATION_OPEN_STATUS_ERROR;
            }

        } catch(Exception e) {
            
            logSessionOpenError(e, "source", source, logEntry);
            return PipeComponentIF.ITERATION_OPEN_STATUS_ERROR;
            
        } catch(VirtualMachineError e) {

            logSessionOpenError(e, "source", source, logEntry);
            return PipeComponentIF.ITERATION_OPEN_STATUS_ERROR;
            
        }
        
        return PipeComponentIF.ITERATION_OPEN_STATUS_OK;
    }

    public int closeComponentSessions(TransferInfo info, LogEntry logEntry) {

        int statusCode = LogEntry.STATUS_OK;

        /** Close iteration session at Source component */
        if(closeComponentSession(getSource(), info, logEntry) == PipeComponentIF.ITERATION_CLOSE_STATUS_ERROR) {
            statusCode = LogEntry.STATUS_SOURCE_ERROR;
        }

        /** Close iteration session at Converter component(s) */
        if(closeConverterComponentSessions(info, logEntry) == PipeComponentIF.ITERATION_CLOSE_STATUS_ERROR) {
            statusCode = LogEntry.STATUS_CONVERSION_ERROR;
        }

        /** Close iteration session at Destination component */
        if(closeComponentSession(getDestination(), info, logEntry) == PipeComponentIF.ITERATION_CLOSE_STATUS_ERROR) {
            statusCode = LogEntry.STATUS_DESTINATION_ERROR;
        }

        return statusCode;
    }

    private int closeConverterComponentSessions(TransferInfo info, LogEntry logEntry) {
        return closeConverterComponentSessions(-1, info, logEntry);
    }

    /**
     * @param stopIndex Number of Converters to process. -1 closes all Converters' sessions.
     * @return
     * @see smilehouse.opensyncro.pipes.log.LogEntry#STATUS_*
     */
    private int closeConverterComponentSessions(int stopIndex, TransferInfo info, LogEntry logEntry) {
        int converterIndex = 0;
        for(Iterator it = getConverterList().iterator(); it.hasNext();) {

            // If stopIndex is specified, break out of the loop when stopIndex
            // is reached
            if((stopIndex != -1) && (converterIndex >= stopIndex))
                break;

            ConverterListItem converterItem = (ConverterListItem) it.next();
            converterIndex++;

            if(closeComponentSession(converterItem.getConverter(), converterIndex, info, logEntry) == PipeComponentIF.ITERATION_CLOSE_STATUS_ERROR) {
                return PipeComponentIF.ITERATION_CLOSE_STATUS_ERROR;
            }
        }
        return PipeComponentIF.ITERATION_CLOSE_STATUS_OK;
    }

    private int closeComponentSession(DestinationIF destination,
                                      TransferInfo info,
                                      LogEntry logEntry) {
        int retCode;
        try {
            logEntry.logMessage(
                "Closing iteration session at Destination component",
                this,
                MessageLogger.DEBUG);

            retCode = destination.close(info, logEntry);
            if(retCode != PipeComponentIF.ITERATION_CLOSE_STATUS_OK) {

                logEntry.logMessage(
                    "Error closing iteration session at Destination component",
                    this,
                    MessageLogger.ERROR);
                return PipeComponentIF.ITERATION_CLOSE_STATUS_ERROR;
            }
        } catch(Exception e) {

            logSessionCloseError(e, "destination", destination, logEntry);
            return PipeComponentIF.ITERATION_CLOSE_STATUS_ERROR;

        } catch(VirtualMachineError e) {

            logSessionCloseError(e, "destination", destination, logEntry);
            return PipeComponentIF.ITERATION_CLOSE_STATUS_ERROR;

        }
        
        return PipeComponentIF.ITERATION_CLOSE_STATUS_OK;
    }

    private int closeComponentSession(ConverterIF converter,
                                      int converterIndex,
                                      TransferInfo info,
                                      LogEntry logEntry) {

        try {
            int retCode;
            logEntry.logMessage("Closing iteration session at Converter component #"
                    + converterIndex + " (" + converter.getName() + ")", this, MessageLogger.DEBUG);

            retCode = converter.close(info, logEntry);

            if(retCode != PipeComponentIF.ITERATION_CLOSE_STATUS_OK) {

                logEntry.logMessage(
                    "Error closing iteration session at Converter component #" + converterIndex
                            + " (" + converter.getName() + ")",
                    this,
                    MessageLogger.ERROR);
                return PipeComponentIF.ITERATION_CLOSE_STATUS_ERROR;

            }

        } catch(Exception e) {

            logSessionCloseError(e, "converter", converter, logEntry);
            return PipeComponentIF.ITERATION_CLOSE_STATUS_ERROR;

        } catch(VirtualMachineError e) {

            logSessionCloseError(e, "converter", converter, logEntry);
            return PipeComponentIF.ITERATION_CLOSE_STATUS_ERROR;

        }

        return PipeComponentIF.ITERATION_CLOSE_STATUS_OK;
    }

    private int closeComponentSession(SourceIF source, TransferInfo info, LogEntry logEntry) {

        try {
            int retCode;
            logEntry.logMessage(
                "Closing iteration session at Source component",
                this,
                MessageLogger.DEBUG);

            retCode = source.close(info, logEntry);
            if(retCode != PipeComponentIF.ITERATION_CLOSE_STATUS_OK) {

                logEntry.logMessage(
                    "Error closing iteration session at Source component",
                    this,
                    MessageLogger.ERROR);
                return PipeComponentIF.ITERATION_CLOSE_STATUS_ERROR;
            }

        } catch(Exception e) {

            logSessionCloseError(e, "source", source, logEntry);
            return PipeComponentIF.ITERATION_CLOSE_STATUS_ERROR;

        } catch(VirtualMachineError e) {

            logSessionCloseError(e, "source", source, logEntry);
            return PipeComponentIF.ITERATION_CLOSE_STATUS_ERROR;

        }
            
        return PipeComponentIF.ITERATION_CLOSE_STATUS_OK;
    }

    private void logSessionError(Throwable t,
                                 String componentType,
                                 PipeComponentIF component,
                                 String taskDescription,
                                 LogEntry logEntry) {
        if(t instanceof FailTransferException || t instanceof AbortTransferException) {
            logEntry.logMessage(
                "Error while " + taskDescription + " at " + componentType + " component " + component.getName(),
                this,
                MessageLogger.ERROR);
        } else {
            // Log unknown Throwable
            Environment.getInstance().log(
                Utils.getThrowableName(t) + " while " + taskDescription + " at " + componentType + " component "
                + component.getName(), t);
            logEntry.logMessage(
                Utils.getThrowableName(t) + " while " + taskDescription + " at " + componentType + " component "
                + component.getName() + ", see OpenSyncro application log file for details",
                this,
                MessageLogger.ERROR);
        }
        
    }
    
    private void logSessionOpenError(Throwable t,
                                     String componentType,
                                     PipeComponentIF component,
                                     LogEntry logEntry) {
        logSessionError(t, componentType, component, "opening session", logEntry);
    }

    private void logSessionCloseError(Throwable t,
                                      String componentType,
                                      PipeComponentIF component,
                                      LogEntry logEntry) {
        logSessionError(t, componentType, component, "closing session", logEntry);
    }

    private void logPipeExecutionError(Throwable t, String currentTask, LogEntry logEntry) {
        if(!(t instanceof FailTransferException)) {
            Environment.getInstance().log("Exception while executing Pipe, " + currentTask, t);
            logEntry.logMessage(
                Utils.getThrowableName(t) + " while executing Pipe, " + currentTask + ": " + t.getMessage()
                        + ", see OpenSyncro log file for details",
                this,
                MessageLogger.ERROR);
        } else {
            // FailTransferException
            logEntry.logMessage(
                "Error while executing Pipe, " + currentTask + ".",
                this,
                MessageLogger.ERROR);
        }
    }

    private void addLogEntry(LogEntry logEntry, int statusCode) {
        if(this.log == null)
            this.log = new HashSet();
        
        logEntry.setStatusCode(statusCode);
        logEntry.setTime(new Date());
        
                
        // The LogEntry is not explicitly added to the Pipe's log Set, since it causes
        // Hibernate to query all LogEntries from the database and thus consume
        // ever-increasing amount of server resources.

        //this.log.add(logEntry);
        String status = getStatusString(statusCode);
        
        Persister persister = new Persister(database);

        try {
            persister.update(logEntry);

            List messages;
            
            boolean mailAddressNotSet =((getMailHost() == null || getRecipientAddress() == null) ||
                    (getMailHost().length() == 0 || getRecipientAddress().length() == 0));
            
            // Notification via email only if host and email address are present
            if(!mailAddressNotSet&&!(getTransferLogNotificationLevel()==MessageLogger.MAIL_NONE)) {
                

                String date = dateFormat.format(new Date());
                String subject = this.getName() + " " + status + " " + date + " (" + database + ")";
                String message = "";

                // Get number of log messages at or below transferLogNotificationLevel.  
                
                int entries=persister.getLogMessageEntries(logEntry.getId(), getTransferLogNotificationLevel()).size();
                
                //Generate mail message
                if(entries > 0) {
                	 
                	messages = persister.getLogMessageEntries(
                             logEntry.getId(),
                             getLoggingVerbosityLevel());
                    for(Iterator m = messages.iterator(); m.hasNext();) {
                        LogMessageEntry messageEntry = (LogMessageEntry) m.next();
                        message += (messageEntry.getMessage()) + "\n";
                    }
                } else {
                    message += MAIL_MESSAGE_NO_ENTRIES;
                }
                
                // Send notification email except when the message is
				// MAIL_MESSAGE_NO_ENTRIES or the pipe has aborted and 
                // isAbortMailEnabled()==true 
				if (!message.equals(MAIL_MESSAGE_NO_ENTRIES)
						|| (statusCode==LogEntry.STATUS_ABORTED && isAbortMailEnabled())) {
					try {

						Properties props = new Properties();
						props.put("mail.host", getMailHost());

						Session mailConnection = Session.getInstance(props,
								null);
						Message msg = new MimeMessage(mailConnection);
						Address sender = new InternetAddress(MAIL_SENDER + "@"
								+ getMailHost(), MAIL_SENDER);
						Address[] receivers = receiverAddresses(getRecipientAddress());

						// Set mail content and subject
						msg.setContent(message, "text/plain");
						msg.setFrom(sender);
						msg.setRecipients(Message.RecipientType.TO, receivers);
						msg.setSubject(subject);

						// Send the mail
						Transport.send(msg);

					} catch (MessagingException e) {
						String error = "An error occurred when sending mail report from "
								+ MAIL_SENDER
								+ "@"
								+ getMailHost()
								+ " to "
								+ getRecipientAddress()
								+ ":\n"
								+ e.getMessage();
						Environment.getInstance().log(error);
						logEntry.logMessage(error, this, MessageLogger.ERROR);
						persister.update(logEntry);
					} catch (RuntimeException ex) {
						Environment.getInstance().log(
								"A RuntimeException has occurred: "
										+ ex.getMessage()
										+ ex.getStackTrace().toString());
					}
				}
			}
            // Remove unnecessary (debug level) messages from the LogEntry if Transfer log
            // verbosity level is set to DYNAMIC and the current LogEntry's status is either
            // OK or ABORTED
            if(getLoggingVerbosityLevel() == MessageLogger.LOG_DYNAMIC
                    && (statusCode == LogEntry.STATUS_OK || statusCode == LogEntry.STATUS_ABORTED) ) {
                messages = persister.getLogMessageEntries(logEntry.getId(), MessageLogger.DEBUG);
                if(messages.size() > 0) {
                    for(Iterator m = messages.iterator(); m.hasNext();) {
                        LogMessageEntry messageEntry = (LogMessageEntry) m.next();
                        if(messageEntry.getMessageType() == MessageLogger.DEBUG) {
                            persister.delete(messageEntry);
                            
                        }
                    }
                }
            }
        } catch(Exception e) {
            Environment.getInstance().log(e.getMessage());
        } finally {
            persister.close();
        }
    }

    // Utility method for splitting a String of email addresses to an array of JavaMail Addresses
    private Address[] receiverAddresses(String recipientAddress) throws AddressException {
        Address[] addresses = null;

        for(int i = 0; i < MAIL_ADDRESS_DELIMITERS.length; i++) {
            if(recipientAddress.indexOf(MAIL_ADDRESS_DELIMITERS[i]) != -1) {
                String[] separatedAddresses = Utils.split(
                    recipientAddress,
                    MAIL_ADDRESS_DELIMITERS[i]);
                addresses = new Address[separatedAddresses.length];
                for(int j = 0; j < separatedAddresses.length; j++) {

                    addresses[j] = new InternetAddress(separatedAddresses[j]);
                }
                return (addresses);
            }

        }
        addresses = new Address[1];
        addresses[0] = new InternetAddress(recipientAddress);

        return addresses;
    }

    // Returns true if a String array contains a null entry
    private boolean arrayContainsNull(String[] array) {
        for(int k = 0; k < array.length; k++) {
            if(array[k] == null) {
                return true;
            }
        }
        return false;
    }
    
	

	/**Get end time of last pipe execution
	 * @return End time of last pipe execution
	 */
	public Date getEndTime() {
		return endTime;
	}

	/**Set end time of pipe execution
	 * @param endTime
	 */
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	/**Get last pipe execution status
	 * @return last pipe execution status
	 */
	public String getLastStatus() {
		return lastStatus;
	}

	/**Set last execution status
	 * @param lastStatus
	 */
	public void setLastStatus(String lastStatus) {
		this.lastStatus = lastStatus;
	}

	/**Get start time of last execution
	 * @return Start time of last execution
	 */
	public Date getStartTime() {
		return startTime;
	}

	/**Set the start time of execution
	 * @param startTime
	 */
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	
	/**Get the duration of last pipe execution
	 * @return Duration of last pipe execution
	 */
	public Long getDuration() {
		return duration;
	}

	/**Set duration of pipe execution
	 * @param duration 
	 */
	public void setDuration(Long duration) {
		this.duration = duration;
	}

	/**Get the user the pipe was started by 
	 * @return user Name of the user who started the pipe last
	 */
	public String getUser() {
		return user;
	}

	/**Set the user name who started the pipe
	 * @param user Name of the user
	 */
	public void setUser(String user) {
		this.user = user;
	}
	
	/**
	 * Sets some parameters of the pipe to same values as of the given pipe. 
	 * All parameters, except those relating to last execution info are copied
	 * 
	 * @param pipe The pipe to get the settings from
	 */
	public Pipe clone() throws ClassCastException{
		Pipe pipe=new Pipe();
		pipe.setAbortMailEnabled(this.isAbortMailEnabled());
		
		Iterator it=this.getConverterList().iterator();
		List newClist=new LinkedList();
		while(it.hasNext()){
			ConverterListItem cvi=(ConverterListItem)it.next();
			ConverterListItem ci=new ConverterListItem();
			ci.setConverter(cvi.getConverter());
			HashMap cmap=new HashMap(cvi.getConverterData().getAttributes());
			ci.setConverterData(new PipeComponentData(cmap));
			ci.setConverterID(cvi.getConverterID());
			ci.setParent(this);
			newClist.add(ci);
		}
		pipe.setConverterList(newClist);
		
		HashMap dmap=new HashMap();
		if(this.getDestinationData()!=null){
			dmap.putAll(this.getDestinationData().getAttributes());
		}
		pipe.setDestinationData(new PipeComponentData(dmap));
		pipe.setDestinationID(this.getDestinationID());
		
		pipe.setHttpStartEnabled(this.isHttpStartEnabled());
		pipe.setMailHost(this.getMailHost());
		pipe.setName("Copy of "+this.getName());
		pipe.setRecipientAddress(this.getRecipientAddress());
		
		HashMap smap=new HashMap();
		if(this.getSourceData()!=null){
			smap.putAll(this.getSourceData().getAttributes());
		}
		pipe.setSourceData(new PipeComponentData(smap));
		pipe.setSourceID(this.getSourceID());
		
		pipe.setStartPassword(this.getStartPassword());
		pipe.setTransferLogNotificationLevel(this.getTransferLogNotificationLevel());
		pipe.setLoggingVerbosityLevel(this.getLoggingVerbosityLevel());
		return pipe;
		
	}
	
	private void setExecutionEndInfo(Date finish, long duration, int statusCode){
		this.setDuration(duration);
		this.setEndTime(finish);
		this.setLastStatus(getStatusString(statusCode));
	}
	
    private void setExecutionStartInfo(String userName, Date startTime) {
	   	this.setUser(userName);
    	this.setStartTime(startTime);
    }

	private String getStatusString(int statusCode) {
		String status = "";
        switch(statusCode) {
        case LogEntry.STATUS_OK:
            status = LogEntry.STAT_OK;
            break;
        case LogEntry.STATUS_SOURCE_ERROR:
            status = LogEntry.STAT_SOURCE_ERROR;
            break;
        case LogEntry.STATUS_CONVERSION_ERROR:
            status = LogEntry.STAT_CONVERSION_ERROR;
            break;
        case LogEntry.STATUS_DESTINATION_ERROR:
            status = LogEntry.STAT_DESTINATION_ERROR;
            break;
        case LogEntry.STATUS_ABORTED:
            status = LogEntry.STAT_ABORTED;
            break;
        default:
            status = LogEntry.STAT_UNKNOWN;
        }
        return status;
	}
	

}