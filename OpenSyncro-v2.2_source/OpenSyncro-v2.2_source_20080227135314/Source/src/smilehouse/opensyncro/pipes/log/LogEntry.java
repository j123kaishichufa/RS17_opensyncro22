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

package smilehouse.opensyncro.pipes.log;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import smilehouse.opensyncro.pipes.Pipe;
import smilehouse.opensyncro.pipes.metadata.TransferInfo;
import smilehouse.opensyncro.system.Environment;
import smilehouse.opensyncro.system.Persister;

public class LogEntry implements MessageLogger, Serializable {

    private static final String DATE_FORMAT = "dd.MM.yyyy HH:mm:ss:SSS";
    private static DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

    public static final int STATUS_OK = 0;
    public static final int STATUS_SOURCE_ERROR = 1;
    public static final int STATUS_CONVERSION_ERROR = 2;
    public static final int STATUS_DESTINATION_ERROR = 3;
    public static final int STATUS_ABORTED = 4;
    
    public static final String STAT_OK="OK";
    public static final String STAT_SOURCE_ERROR="Source error";
    public static final String STAT_CONVERSION_ERROR="Conversion error";
    public static final String STAT_DESTINATION_ERROR="Destination error";
    public static final String STAT_ABORTED="Aborted";
    public static final String STAT_UNKNOWN="Unknown";
    /** identifier field */
    private Long id;

    /** nullable persistent field */
    private Date time;

    /** nullable persistent field */
    private int statusCode;

    /** nullable persistent field */
    private Set messages;

    /** persistent field */
    private Pipe pipe;
    
    private String userName;
    
    private TransferInfo info;
    private Persister persister;
    /** full constructor */
    public LogEntry(Date time, int statusCode, Set messages, Pipe pipe) {
        this.time = time;
        this.statusCode = statusCode;
        this.messages = messages;
        this.pipe = pipe;
    }

    /** default constructor */
    public LogEntry() {}

    /** minimal constructor */
    public LogEntry(Pipe pipe) {
        this.pipe = pipe;
    }
    public LogEntry(Pipe pipe,TransferInfo info) {
        this.pipe = pipe;
        this.info=info;
    }
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getTime() {
        return this.time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Set getMessages() {
        return this.messages;
    }

    // Setter method used by Hibernate
    protected void setMessages(Set messages) {
        this.messages = messages;
    }


    int index=1;
    boolean saved=false;
    
    public void setIndex(int index){
    	this.index = index;
    }
    
    public int getIndex(){
    	return this.index;
    }
    /**
     * Adds a message to this entry's message list.
     * 
     * @param message The message text
     * @param creator The creator of this message. Class name of this object will be included in the
     *        message.
     * @param verbosityLevel The verbosity level of this message. If the coresponding pipe's
     *        verbosity level is less than this, message is ignored.
     * @param timestamp If not null, it is used as the date for the log message.
     *                  If null, current time is used.
     */
    public void logMessage(String message, Object creator, int verbosityLevel, Date timestamp) {
        
    	if(messages == null){
            messages = new HashSet();
        }
    	
    	persister=new Persister(info.getDatabaseName());
    	
    	try{
    	if(!saved){
        	persister.save(this);
        	saved=true;
    	}
        
    	// Ignore messages with higher verbosity level than allowed by the pipe.
    	if(pipe != null && pipe.getLoggingVerbosityLevel() < verbosityLevel)
            return;
        
    	String logm="";
        if(creator!=null){	
        	
        	String date;
	        if(timestamp==null)
	        	date=dateFormat.format(new Date());
	        else
	        	date=dateFormat.format(timestamp);
	        
	        logm="[" + date + "] " + creator.getClass().getName()
	            + ": ";
        
        } 	
            
        logm=logm + message;
      
        LogMessageEntry lme=new LogMessageEntry(logm,index,this,verbosityLevel);
        persister.save(lme);
        index++;
    	}
    	catch(Exception e){
    		Environment.getInstance().log(e.getMessage());
    	}
    	finally{
    		persister.close();
    	}
    }

    public void logMessage(String message, int verbosityLevel){
    	logMessage(message,null,verbosityLevel,null);
    }
    
    public void logMessage(String message, Object creator, int verbosityLevel){
    	logMessage(message,creator,verbosityLevel,null);
    }
    
    public Pipe getPipe() {
        return this.pipe;
    }

    public void setPipe(Pipe pipe) {
        this.pipe = pipe;
    }

    public String toString() {
        return new ToStringBuilder(this).append("id", getId()).toString();
    }

    public boolean equals(Object other) {
        if(!(other instanceof LogEntry))
            return false;
        LogEntry castOther = (LogEntry) other;
        return new EqualsBuilder().append(this.getId(), castOther.getId()).isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder().append(getId()).toHashCode();
    }

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

}