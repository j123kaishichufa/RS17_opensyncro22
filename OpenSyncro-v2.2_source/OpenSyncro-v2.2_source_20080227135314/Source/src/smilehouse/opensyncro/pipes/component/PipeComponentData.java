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

package smilehouse.opensyncro.pipes.component;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import smilehouse.opensyncro.pipes.log.MessageLogger;

public class PipeComponentData implements Serializable {

    /** identifier field */
    private Long id;

    /** nullable persistent field */
    private String name;

    /** persistent field */
    private Map attributes;

    /** default constructor */
    public PipeComponentData() {
        setId(null);
        }

    /** minimal constructor */
    public PipeComponentData(Map attributes) {
        setId(null);
        this.attributes = attributes;
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

    public Map getAttributes() {
        return this.attributes;
    }

    public void setAttributes(Map attributes) {
        this.attributes = attributes;
    }

    public String toString() {
        return new ToStringBuilder(this).append("id", getId()).toString();
    }

    public boolean equals(Object other) {
        if(!(other instanceof PipeComponentData))
            return false;
        PipeComponentData castOther = (PipeComponentData) other;
        return new EqualsBuilder().append(this.getId(), castOther.getId()).isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder().append(getId()).toHashCode();
    }

    public String getAttribute(String name) {
        return (String) attributes.get(name);
    }

    public void setAttribute(String name, String value) {
        attributes.put(name, value);
    }
    
    /** The following attribute setters moved here from PipeComponent class */
    
    public void setAttribute(String name, int value) {
        this.setAttribute(name, String.valueOf(value));
    }

    public void setAttribute(String name, double value) {
        this.setAttribute(name, String.valueOf(value));
    }

    public void setAttribute(String name, long value) {
        this.setAttribute(name, String.valueOf(value));
    }

    public void setAttribute(String name, float value) {
        this.setAttribute(name, String.valueOf(value));
    }

    public void setAttribute(String name, boolean value) {
        this.setAttribute(name, String.valueOf(value));
    }
   
    /**
     * A convenience method for making sure that an attribute is not null. Logs an error message if
     * MessageLogger is given.
     * 
     * @param name Attribute name
     * @param logger Used to log error message if given and the attribute is null
     * @param errorMessage Error message to be logged when the attribute is null
     * @param severity How bad is it if the attribute is null. See the constants in MessageLogger
     *        interface. If set to ERROR FailTransferException will be thrown if the attribute is
     *        null.
     * @return Attribute string if it's no null and an empty string if it is (and severity != ERROR)
     * 
     * @throws FailTransferException If the attribute is null and severity is set to ERROR
     */
    public String getNonNullAttribute(String name,
                                      MessageLogger logger,
                                      String errorMessage,
                                      int severity) throws FailTransferException {
        String attr = getAttribute(name);
        if(attr != null)
            return attr;
        else {
            if(logger != null && errorMessage != null)
                logger.logMessage(errorMessage, this, severity);
            if(severity == MessageLogger.ERROR) {
                PipeComponentUtils.failTransfer();
                return null; // Never actually reached...
            } else
                return "";
        }
    }

    /**
     * A convenience method for integer attributes.
     */
    public Integer getIntegerAttribute(String name) throws NumberFormatException {
        String stringAttr = getAttribute(name);
        if(stringAttr == null)
            return null;
        else
            return new Integer(stringAttr);
    }

    /**
     * A convenience method with some errorhandling for integer attributes. Intended to be used
     * during transfer.
     * 
     * @param name Attribute name
     * @param logger MessageLogger in case of an error
     * @param errorMessage Error messge in case of an error
     * 
     * @return Integer representation of the attribute or null if not set
     * @throws FailTransferException If there was an error
     */
    public Integer getIntegerAttribute(String name, MessageLogger logger, String errorMessage)
            throws FailTransferException {
        try {
            return getIntegerAttribute(name);
        } catch(NumberFormatException nfe) {
            logger.logMessage(errorMessage, this, MessageLogger.ERROR);
            PipeComponentUtils.failTransfer();
            return null; // Never actually reached...
        }
    }

    /**
     * A convenience method for double attributes.
     */
    public Double getDoubleAttribute(String name) throws NumberFormatException {
        String stringAttr = getAttribute(name);
        if(stringAttr == null)
            return null;
        else
            return new Double(name);
    }

    /**
     * A convenience method with some errorhandling for double attributes. Intended to be used
     * during transfer.
     * 
     * @param name Attribute name
     * @param logger MessageLogger in case of an error
     * @param errorMessage Error messge in case of an error
     * 
     * @return Integer representation of the attribute or null if not set
     * @throws FailTransferException If there was an error
     */
    public Double getDoubleAttribute(String name, MessageLogger logger, String errorMessage)
            throws FailTransferException {
        try {
            return getDoubleAttribute(name);
        } catch(NumberFormatException nfe) {
            logger.logMessage(errorMessage, this, MessageLogger.ERROR);
            PipeComponentUtils.failTransfer();
            return null; // Never actually reached...
        }
    }

    public Boolean getBooleanAttribute(String name) {
        String strValue = getAttribute(name);
        if(strValue == null)
            return null;
        else
            return new Boolean(strValue);
    }
    
}