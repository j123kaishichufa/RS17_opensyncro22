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

package smilehouse.opensyncro.defaultcomponents.converter.xml.xslt;

import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.ReceiverOptions;
import net.sf.saxon.trans.XPathException;
import smilehouse.opensyncro.pipes.log.MessageLogger;

/**
 * A class used to redirect output from xsl:message elements to OpenSyncro's
 * Transfer log.
 * 
 * Regular xsl:message's are written as debug (info) level messages while
 * xsl:message's with terminate="yes" attribute are written as error level
 * messages.
 */
public class XSLTMessageReceiver implements net.sf.saxon.event.Receiver {
	
	private MessageLogger logger;
	private Object caller;
    
    /** 
     * Contains information on whether the xsl:message element had attribute
     * terminate="yes" (ReceiverOptions.TERMINATE). Given by Saxon only at
     * the startDocument() call.
     */
	private int startElementProperties;
    
    // Specified as required by getter/setter methods in the Receiver interface
    private PipelineConfiguration pipelineConfig;
    private String systemId;

	/**
	 * Create new XSLTMessageReceiver that will output xsl:messages to Transfer log. 
	 * 
	 * @param logger <code>MessageLogger</code> used.
	 * @param caller <code>Object</code> reference to be used in the Transfer log.
	 */
	public XSLTMessageReceiver (MessageLogger logger, Object caller){
		this.logger = logger;
		this.caller = caller;
        this.startElementProperties = 0;
	}

    public void startDocument(int properties) throws XPathException {
        this.startElementProperties = properties;
    }

    public void characters(CharSequence chars, int locationId, int properties) throws XPathException {
        int logMessageLevel;
  
        if((this.startElementProperties & ReceiverOptions.TERMINATE) != 0) {
            logMessageLevel = MessageLogger.ERROR;
        } else {
            logMessageLevel = MessageLogger.DEBUG;
        }
        
        logger.logMessage("xsl:message: " + chars, caller, logMessageLevel);

    }


    // Obligatory PipelineConfiguration and SystemId getter/setter methods implemented
    
    public void setPipelineConfiguration(PipelineConfiguration pipe) {
        this.pipelineConfig = pipe;
    }

    public PipelineConfiguration getPipelineConfiguration() {
        return pipelineConfig;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public String getSystemId() {
        return systemId;
    }


    // The following methods are intentionally left no-op
    
    public void attribute(int nameCode, int typeCode, CharSequence value, int locationId, int properties) throws XPathException {}

    public void comment(CharSequence content, int locationId, int properties) throws XPathException {}

    public void namespace(int namespaceCode, int properties) throws XPathException {}

    public void processingInstruction(String name, CharSequence data, int locationId, int properties) throws XPathException {}

    public void setUnparsedEntity(String name, String systemID, String publicID) throws XPathException {}

    public void startContent() throws XPathException {}

    public void startElement(int nameCode, int typeCode, int locationId, int properties) throws XPathException {}

    public void endElement() throws XPathException {}

    public void endDocument() throws XPathException {}

    public void open() throws XPathException {}

    public void close() {}

}
