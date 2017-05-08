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
 * Created on 12.1.2005
 */
package smilehouse.opensyncro.defaultcomponents.converter.xml.groupexpander;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import smilehouse.gui.html.fieldbased.FieldInfo;
import smilehouse.gui.html.fieldbased.GUIContext;
import smilehouse.gui.html.fieldbased.editor.BooleanEditor;
import smilehouse.gui.html.fieldbased.model.DefaultModelModifier;
import smilehouse.gui.html.fieldbased.model.ModelModifier;
import smilehouse.opensyncro.pipes.component.AbortTransferException;
import smilehouse.opensyncro.pipes.component.ConverterIF;
import smilehouse.opensyncro.pipes.component.FailTransferException;
import smilehouse.opensyncro.pipes.component.PipeComponentData;
import smilehouse.opensyncro.pipes.component.PipeComponentUtils;
import smilehouse.opensyncro.pipes.gui.GUIConfigurationIF;
import smilehouse.opensyncro.pipes.gui.GUIDefinition;
import smilehouse.opensyncro.pipes.log.MessageLogger;
import smilehouse.opensyncro.pipes.metadata.ConversionInfo;
import smilehouse.opensyncro.system.Environment;
import smilehouse.xml.XMLEscape;

/**
 * A component that takes in XML which has a <i>grouping element </i> inside <i>parent elements
 * </i>. The <i>grouping element </i> contains a group of <i>child elements </i>. <br>
 * In the outputted XML there is one copy of <i>parent element </i> for every for every <i>child
 * element </i> it originally contained.
 */
public class XMLGroupExpander implements ConverterIF, GUIConfigurationIF {

    private static final String HELP_HTML = "<table width=\"50%\"><tr><td colspan=\"3\" align=\"center\"><b>Simple example</b></td></tr>"
            + "<tr><td><pre><tt>"
            + "&lt;parentElement name=\"George\"&gt;\n"
            + "  &lt;groupingElement&gt;\n"
            + "     &lt;childElement name=\"Frederik\"/&gt;\n"
            + "     &lt;childElement name=\"Bill\"/&gt;\n"
            + "  &lt;/groupingElement&gt;\n"
            + "&lt;/parentElement&gt;\n"
            + "</tt></pre></td>"
            + "<td valign=\"middle\" align=\"center\"><b>--></b></td>"
            + "<td><pre><tt>"
            + "&lt;parentElement name=\"George\"&gt;\n"
            + "  &lt;childElement name=\"Frederik\"/&gt;\n"
            + "&lt;/parentElement&gt;\n"
            + "\n"
            + "&lt;parentElement name=\"George\"&gt;\n"
            + "  &lt;childElement name=\"Bill\"/&gt;\n"
            + "&lt;/parentElement&gt;</tt></pre>" + "</td></tr></table>";

    private static final String PARENT_ELEMENT_ATTR = "parent_element";
    private static final String GROUPING_ELEMENT_ATTR = "grouping_element";
    private static final String CHILD_ELEMENT_ATTR = "group_element";
    private static final String RETAIN_GROUPING_ELEMENTS_ATTR = "retain_grouping_elements";

    private static GUIDefinition gui = new ExpanderGUI();

    private static class ExpanderGUI extends GUIDefinition {
        public ExpanderGUI() {
            try {
                addSimpleTextFieldForComponent(PARENT_ELEMENT_ATTR, PARENT_ELEMENT_ATTR, 50);
                addSimpleTextFieldForComponent(GROUPING_ELEMENT_ATTR, GROUPING_ELEMENT_ATTR, 50);
                addSimpleTextFieldForComponent(CHILD_ELEMENT_ATTR, CHILD_ELEMENT_ATTR, 50);
                {
                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws FailTransferException, AbortTransferException {
                            Boolean b = ((ConverterIF) model).getData()
                                .getBooleanAttribute(RETAIN_GROUPING_ELEMENTS_ATTR);
                            return b != null ? b : Boolean.FALSE;
                        }

                        public void setModelValue(Object model, Object value) throws FailTransferException, AbortTransferException {
                            ((ConverterIF) model).getData().setAttribute(
                                RETAIN_GROUPING_ELEMENTS_ATTR,
                                ((Boolean) value).booleanValue());
                        }
                    };

                    BooleanEditor editor = new BooleanEditor();

                    //and finally create the configurationObject
                    FieldInfo fieldInfo = new FieldInfo(
                        RETAIN_GROUPING_ELEMENTS_ATTR,
                        RETAIN_GROUPING_ELEMENTS_ATTR,
                        modifier,
                        editor);

                    //add the configuration to the context for usage in the http-requests.
                    addField(RETAIN_GROUPING_ELEMENTS_ATTR, fieldInfo);
                }
            } catch(Exception e) {
                Environment.getInstance().log("Couldn't initialize GUI", e);
            }
        }
    }

    public String getGUITemplate() {
        return "<table>" + "<tr><td colspan=\"2\">$" + PARENT_ELEMENT_ATTR + "$</td></tr>"
                + "<tr><td>$" + GROUPING_ELEMENT_ATTR + "$</td><td>$"
                + RETAIN_GROUPING_ELEMENTS_ATTR + "$</td></tr>" + "<tr><td colspan=\"2\">$"
                + CHILD_ELEMENT_ATTR + "$</td></tr></table>" + "<hr>" + HELP_HTML;
    }



    public GUIContext getGUIContext() {
        return gui.getGUIContext();
    }

    public XMLGroupExpander( Object pipeComponentData ) {
        setData((PipeComponentData) pipeComponentData);
    }
    
    protected PipeComponentData data;
    
    public void setData(PipeComponentData data) {
        this.data = data;
    }

    public PipeComponentData getData() {
        return data;
    }
    
    public final int getType() {
        return TYPE_CONVERTER;
    }
    
    public String getName() {
        return "XMLGroupExpander";
    }

    public String getID() {
        return "smilehouse.opensyncro.defaultcomponents.converter.xml.groupexpander.XMLGroupExpander";
    }

    public String getDescription(Locale locale) {
        return PipeComponentUtils.getDescription(locale, this.getClass());
    }
    
    // Dummy methods due to no iteration supported
    public int open(ConversionInfo info, MessageLogger logger) throws FailTransferException {
        return ITERATION_OPEN_STATUS_OK;
    }

    public int close(ConversionInfo info, MessageLogger logger) throws FailTransferException {
        return ITERATION_CLOSE_STATUS_OK;
    }

    public void lastBlockStatus(int statusCode) { }

    
    /**
     * The method actually called by pipe during the conversion. This default implementation uses
     * the convert-method to convert all the input records separately and is usually sufficient so
     * you only have to implement it. If you however need access to all the input when converting
     * (foer example Join-converter) you need to override this.
     */
    public String[] convertAll(String[] data, ConversionInfo info, MessageLogger logger)
            throws FailTransferException, AbortTransferException {
        String[][] allResults = new String[data.length][];
        int resultCount = 0;
        for(int i = 0; i < data.length; i++) {
            allResults[i] = convert(data[i], info, logger);
            resultCount += allResults[i].length;
        }
        if(data.length == 1)
            return allResults[0];
        else {
            String[] combinedResult = new String[resultCount];
            int c = 0;
            for(int i = 0; i < allResults.length; i++) {
                for(int j = 0; j < allResults[i].length; j++, c++) {
                    combinedResult[c] = allResults[i][j];
                }
            }
            return combinedResult;
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see smilehouse.opensyncro.user.pipes.Converter#convert(java.lang.String,
     *      smilehouse.opensyncro.user.pipes.ConversionInfo, smilehouse.opensyncro.user.pipes.log.MessageLogger)
     */
    public String[] convert(String data, ConversionInfo info, MessageLogger logger)
            throws FailTransferException, AbortTransferException {


        ExpanderHandler handler = new ExpanderHandler();

        String parentElement = this.data.getAttribute(PARENT_ELEMENT_ATTR);
        if(parentElement == null || parentElement.length() == 0) {
            logger.logMessage("Parent element name is not set!", this, MessageLogger.ERROR);
            PipeComponentUtils.failTransfer();
        }
        handler.setParentElementName(parentElement);

        String groupingElement = this.data.getAttribute(GROUPING_ELEMENT_ATTR);
        if(groupingElement == null || groupingElement.length() == 0) {
            logger.logMessage("Grouping element name is not set!", this, MessageLogger.ERROR);
            PipeComponentUtils.failTransfer();
        }
        handler.setGroupingElementName(groupingElement);

        String childElement = this.data.getAttribute(CHILD_ELEMENT_ATTR);
        if(childElement == null || childElement.length() == 0) {
            logger.logMessage("Child element name is not set!", this, MessageLogger.ERROR);
            PipeComponentUtils.failTransfer();
        }
        handler.setChildElementName(childElement);

        Boolean retainGroupingElements = this.data.getBooleanAttribute(RETAIN_GROUPING_ELEMENTS_ATTR);
        handler.setRetainGroupingElements(retainGroupingElements != null ? retainGroupingElements
            .booleanValue() : false);

        SAXParserFactory factory = SAXParserFactory.newInstance();

        try {
            SAXParser saxParser = factory.newSAXParser();
            InputSource source = new InputSource(new StringReader(data));
            saxParser.parse(source, handler);
            
        } catch(ParserConfigurationException e) {
            logger.logMessage("SAX Parser configuration exception, aborting", this, MessageLogger.ERROR);
            PipeComponentUtils.failTransfer();
        } catch(IOException e) {
            logger.logMessage("IOException while parsing XML data, aborting", this, MessageLogger.ERROR);
            PipeComponentUtils.failTransfer();
        } catch(SAXException se) {
            logger.logMessage("SAXException while parsing XML data, aborting", this, MessageLogger.ERROR);
            PipeComponentUtils.failTransfer();
        }

        return new String[] {handler.getExpandedXML()};
    }



    /**
     * The SAX handler doing the actual work
     */
    public static class ExpanderHandler extends DefaultHandler {


        private boolean startTagOpen = false;
        private String parentElementName;
        private String groupingElementName;
        private String childElementName;
        private boolean retainGroupingElements;


        public String getChildElementName() {
            return childElementName;
        }

        public void setChildElementName(String childElementName) {
            this.childElementName = childElementName;
        }

        public String getGroupingElementName() {
            return groupingElementName;
        }

        public void setGroupingElementName(String groupingElementName) {
            this.groupingElementName = groupingElementName;
        }

        public String getParentElementName() {
            return parentElementName;
        }

        public void setParentElementName(String parentElementName) {
            this.parentElementName = parentElementName;
        }

        public boolean isRetainGroupingElements() {
            return retainGroupingElements;
        }

        public void setRetainGroupingElements(boolean retainGroupingElements) {
            this.retainGroupingElements = retainGroupingElements;
        }

        /** StringBuffer to hold the beginning part of the parent element before the grouping-element */
        private StringBuffer parentStartBuffer;
        /** StringBuffer to hold the end part of the parent element after the grouping-element */
        private StringBuffer parentEndBuffer;
        /** List to hold thechild-elements */
        private List groupBuffers;
        /** The buffer where the handler writes */
        private StringBuffer currentBuffer;
        /** The final output buffer */
        private StringBuffer destinationBuffer;

        public ExpanderHandler() {
            this("parentElement", "groupingElement", "childElement");
        }

        public ExpanderHandler(String parentElementName,
                               String groupingElementName,
                               String childElementName) {
            this.parentElementName = parentElementName;
            this.groupingElementName = groupingElementName;
            this.childElementName = childElementName;
            destinationBuffer = new StringBuffer();
            parentStartBuffer = new StringBuffer();
            parentEndBuffer = new StringBuffer();
            currentBuffer = destinationBuffer;
            groupBuffers = new LinkedList();
            retainGroupingElements = false;
        }

        /**
         * The element contains something, close the tag which was left open not knowing should it
         * be ">" or "/>".
         *  
         */
        private void closeOpenStartTag() {
            if(startTagOpen) {
                currentBuffer.append('>');
                startTagOpen = false;
            }
        }

        public void characters(char[] ch, int start, int length)
        // throws SAXException
        {
            closeOpenStartTag();
            currentBuffer.append(XMLEscape.escape(new String(ch, start, length)));
        }

        public void startDocument()
        // throws SAXException
        {
            currentBuffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes)
        // throws SAXException
        {
            closeOpenStartTag();

            String eName = localName;
            if(eName == null || eName.length() == 0)
                eName = qName;

            if(eName.equals(parentElementName)) {
                currentBuffer = parentStartBuffer;
            } else if(eName.equals(groupingElementName)) {
                if(!retainGroupingElements)
                    return; // Ignore the grouping tag
            } else if(eName.equals(childElementName)) {
                // Start a new child buffer
                currentBuffer = new StringBuffer();
            }

            currentBuffer.append('<');
            currentBuffer.append(eName);
            for(int i = 0; i < attributes.getLength(); i++) {
                currentBuffer.append(' ');
                String aName = attributes.getLocalName(i);
                if(aName == null || aName.length() == 0)
                    aName = attributes.getQName(i);
                currentBuffer.append(aName);
                currentBuffer.append("=\"");
                currentBuffer.append(attributes.getValue(i));
                currentBuffer.append('"');
            }

            startTagOpen = true;
        }

        //        public void endDocument() throws SAXException {
        //            // TODO Auto-generated method stub
        //            super.endDocument();
        //        }


        public void endElement(String uri, String localName, String qName)
        // throws SAXException
        {
            String eName = localName;
            if(eName == null || eName.length() == 0)
                eName = qName;
            if(eName.equals(groupingElementName)) {
                currentBuffer = parentEndBuffer;
                if(!retainGroupingElements)
                    return; // ignore the grouping tag
            }

            if(startTagOpen) {
                // If the element didn't contain anything (the start tag is still left open)
                // just cose it.
                currentBuffer.append("/>");
                startTagOpen = false;
            } else {
                currentBuffer.append("</");
                currentBuffer.append(eName);
                currentBuffer.append('>');
            }
            if(eName.equals(childElementName)) {
                groupBuffers.add(currentBuffer);
            } else if(eName.equals(parentElementName)) {
                expandElement();
                currentBuffer = destinationBuffer;
            }
        }


        // public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        //try {
        //                currentBuffer.append(ch, start, length);
        //       currentBuffer.append("HIP");
        //}
        //catch(IOException ioe) { throw new SAXException(ioe); }
        //}

        private void expandElement() {
            for(Iterator it = groupBuffers.iterator(); it.hasNext();) {
                destinationBuffer.append(parentStartBuffer);
                destinationBuffer.append((StringBuffer) it.next());
                destinationBuffer.append(parentEndBuffer);
            }
            groupBuffers.clear();
            parentStartBuffer = new StringBuffer();
            parentEndBuffer = new StringBuffer();
        }

        public String getExpandedXML() {
            return destinationBuffer.toString();
        }
    }
}