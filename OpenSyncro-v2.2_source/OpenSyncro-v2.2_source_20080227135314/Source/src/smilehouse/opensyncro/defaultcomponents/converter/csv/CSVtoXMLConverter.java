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
 * Created on Dec 14, 2004
 * 
 * Note: if you use CSV column headers as XML element names, remember to also skip the first CSV row
 *  
 */
package smilehouse.opensyncro.defaultcomponents.converter.csv;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.xerces.util.XMLChar;

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
import smilehouse.util.Utils;
import smilehouse.util.csv.CSVFormatException;
import smilehouse.util.csv.CSVReader;
import smilehouse.xml.XMLEscape;

public class CSVtoXMLConverter implements ConverterIF, GUIConfigurationIF {

    private static final String FIELDNAMES_ATTRIBUTE = "fieldnames";
    private static final String ROOTELEMENTNAME_ATTRIBUTE = "rootelementname";
    private static final String RECORDELEMENTNAME_ATTRIBUTE = "recordelementname";
    private static final String XMLHEADERLINE_ATTRIBUTE = "xmlheaderline";
    private static final String XMLFOOTERLINE_ATTRIBUTE = "xmlfooterline";

    
    private static final String UNKNOWNFIELDNAMEPREFIX_ATTRIBUTE = "unknownfieldnameprefix";

    private static final String CSVRECORDSEPARATOR_ATTRIBUTE = "csvrecordseparator";
    //private static final String CSVLINESEPARATOR_ATTRIBUTE = "csvlineseparator";
    private static final String CSVQUOTE_ATTRIBUTE = "csvquote";

    private static final String READFIELDNAMESFROMCSV_ATTRIBUTE = "readfieldnamesfromcsv";
    private static final String SKIPCSVHEADERLINE_ATTRIBUTE = "skipcsvheaderline";

    private static final String defaultRootElementName = "file";
    private static final String defaultRecordElementName = "row";
    private static final String defaultUnknownFieldNamePrefix = "column";

    private static final String defaultCSVRecordSeparatorChar = ";";
    //private static final String defaultCSVLineSeparatorChar = "\n";
    private static final String defaultCSVQuoteChar = "\"";

    private static final char fieldNameSeparatorChar = ',';
    
    private static final String skipColumnPrefix = "-";

    // --------------
    // GUI definition
    // --------------
    //private static GUIContextContainer guiContextContainer = new GUIContextContainer();

    private static GUIDefinition gui = new CSVtoXMLGUI();

    private static class CSVtoXMLGUI extends GUIDefinition {
        public CSVtoXMLGUI() {
            try {

                addSimpleTextFieldForComponent(
                    ROOTELEMENTNAME_ATTRIBUTE,
                    ROOTELEMENTNAME_ATTRIBUTE,
                    40);
                addSimpleTextFieldForComponent(
                    RECORDELEMENTNAME_ATTRIBUTE,
                    RECORDELEMENTNAME_ATTRIBUTE,
                    40);
                addSimpleTextFieldForComponent(FIELDNAMES_ATTRIBUTE, FIELDNAMES_ATTRIBUTE, 100);
                addSimpleTextFieldForComponent(
                    UNKNOWNFIELDNAMEPREFIX_ATTRIBUTE,
                    UNKNOWNFIELDNAMEPREFIX_ATTRIBUTE,
                    20);
                addSimpleTextFieldForComponent(
                    XMLHEADERLINE_ATTRIBUTE,
                    XMLHEADERLINE_ATTRIBUTE,
                    100);
                addSimpleTextFieldForComponent(
                    XMLFOOTERLINE_ATTRIBUTE,
                    XMLFOOTERLINE_ATTRIBUTE,
                    100);
                addSimpleTextFieldForComponent(
                    CSVRECORDSEPARATOR_ATTRIBUTE,
                    CSVRECORDSEPARATOR_ATTRIBUTE,
                    3);

                //			Line separator character cannot be set in the current version of CSVParser
                //    		addSimpleTextFieldForComponent(CSVLINESEPARATOR_ATTRIBUTE,
                // CSVLINESEPARATOR_ATTRIBUTE, 3);

                addSimpleTextFieldForComponent(CSVQUOTE_ATTRIBUTE, CSVQUOTE_ATTRIBUTE, 3);

                {
                    //set unique id and description labelkey
                    String id = "readfieldnamesfromcsv";
                    String label = "readfieldnamesfromcsv";

                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            return ((CSVtoXMLConverter) model).getReadFieldNamesFromCSV();
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            ((CSVtoXMLConverter) model).setReadFieldNamesFromCSV((Boolean) value);
                        }
                    };

                    BooleanEditor editor = new BooleanEditor();

                    //and finally create the configurationObject
                    FieldInfo fieldInfo = new FieldInfo(id, label, modifier, editor);

                    //add the configuration to the context for usage in the http-requests.
                    addField(id, fieldInfo);
                }

                {
                    //set unique id and description labelkey
                    String id = "skipcsvheaderline";
                    String label = "skipcsvheaderline";

                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            return ((CSVtoXMLConverter) model).getSkipCSVHeaderLine();
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            ((CSVtoXMLConverter) model).setSkipCSVHeaderLine((Boolean) value);
                        }
                    };

                    BooleanEditor editor = new BooleanEditor();

                    //and finally create the configurationObject
                    FieldInfo fieldInfo = new FieldInfo(id, label, modifier, editor);

                    //add the configuration to the context for usage in the http-requests.
                    addField(id, fieldInfo);
                }


            } catch(Exception e) {
                Environment.getInstance().log("Couldn't initialize CSVtoXMLConverter GUI", e);
            }
        }
    }

    public GUIContext getGUIContext() {
        return gui.getGUIContext();
    }

    public String getGUITemplate() {
        return "<table border=0 cellspacing=5><tr>" + "<td>$" + CSVRECORDSEPARATOR_ATTRIBUTE
                + "$</td>" +

                //			Line separator character cannot be set in the current version of CSVParser
                //          "<td>$" + CSVLINESEPARATOR_ATTRIBUTE + "$</td>" +

                "<td>$" + CSVQUOTE_ATTRIBUTE + "$</td>" + "</tr><tr><td>$"
                + ROOTELEMENTNAME_ATTRIBUTE + "$</td>" + "<td>$" + RECORDELEMENTNAME_ATTRIBUTE
                + "$</td>" + "<td>$" + UNKNOWNFIELDNAMEPREFIX_ATTRIBUTE + "$</td></tr>"
                + "<tr><td colspan=3>$" + FIELDNAMES_ATTRIBUTE + "$</td></tr>" + "<tr><td>$"
                + READFIELDNAMESFROMCSV_ATTRIBUTE + "$</td>" + "<td>$"
                + SKIPCSVHEADERLINE_ATTRIBUTE + "$</td>" +
                "<tr><td colspan=3>$"
                + XMLHEADERLINE_ATTRIBUTE + "$</td></tr>" +
                "<tr><td colspan=3>$"
                + XMLFOOTERLINE_ATTRIBUTE + "$</td></tr>" +
                "</table>";
    }

    public void setReadFieldNamesFromCSV(Boolean ReadFieldNamesFromCSV) {
        this.data.setAttribute(READFIELDNAMESFROMCSV_ATTRIBUTE, ReadFieldNamesFromCSV != null
                ? ReadFieldNamesFromCSV.toString() : "false");
    }

    public Boolean getReadFieldNamesFromCSV() {
        return new Boolean(this.data.getAttribute(READFIELDNAMESFROMCSV_ATTRIBUTE));
    }

    public void setSkipCSVHeaderLine(Boolean SkipCSVHeaderLine) {
        this.data.setAttribute(SKIPCSVHEADERLINE_ATTRIBUTE, SkipCSVHeaderLine != null ? SkipCSVHeaderLine
            .toString() : "false");
    }

    public Boolean getSkipCSVHeaderLine() {
        return new Boolean(this.data.getAttribute(SKIPCSVHEADERLINE_ATTRIBUTE));
    }

    /**
     * The getters below are used by the components convert method while configuration GUI
     * accesses the attribute values using ComponentAttributeModifier's generic getters
     * and setters
     */
       
    public String getFieldNames() {
        String fieldNames = this.data.getAttribute(FIELDNAMES_ATTRIBUTE);
        if((fieldNames != null) && (fieldNames.length() > 0) )
            return fieldNames;

        return "";
    }

    public String getRootElementName() {
        String rootElementName = this.data.getAttribute(ROOTELEMENTNAME_ATTRIBUTE);
        if((rootElementName != null) && (rootElementName.length() > 0) )
            return rootElementName;

        return defaultRootElementName;
    }

    public String getRecordElementName() {
        String recordElementName = this.data.getAttribute(RECORDELEMENTNAME_ATTRIBUTE);
        if((recordElementName != null) && (recordElementName.length() > 0) )
            return recordElementName;

        return defaultRecordElementName;
    }

    public String getXMLHeaderLine() {
        String xmlHeaderLine = this.data.getAttribute(XMLHEADERLINE_ATTRIBUTE);
        if(xmlHeaderLine != null)
            return xmlHeaderLine;

        return "";
    }

    public String getXMLFooterLine() {
        String xmlFooterLine = this.data.getAttribute(XMLFOOTERLINE_ATTRIBUTE);
        if(xmlFooterLine != null)
            return xmlFooterLine;

        return "";
    }

    public String getUnknownFieldNamePrefix() {
        String unknownFieldNamePrefix = this.data.getAttribute(UNKNOWNFIELDNAMEPREFIX_ATTRIBUTE);
        if((unknownFieldNamePrefix != null) && (unknownFieldNamePrefix.length() > 0))
            return unknownFieldNamePrefix;

        return defaultUnknownFieldNamePrefix;
    }

    public String getCSVRecordSeparatorChar() {
        String csvRecordSeparator = this.data.getAttribute(CSVRECORDSEPARATOR_ATTRIBUTE);
        if((csvRecordSeparator != null) && (csvRecordSeparator.length() > 0))
            return csvRecordSeparator;

        return defaultCSVRecordSeparatorChar;
    }

    public String getCSVQuoteChar() {
        String csvQuoteChar = this.data.getAttribute(CSVQUOTE_ATTRIBUTE);
        if((csvQuoteChar != null) && (csvQuoteChar.length() > 0))
            return csvQuoteChar;

        return defaultCSVQuoteChar;
    }

    // ---
    
    public CSVtoXMLConverter( Object pipeComponentData ) {
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
        return "CSVtoXMLConverter";
    }

    public String getID() {
        return "smilehouse.opensyncro.defaultcomponents.converter.csv.CSVtoXMLConverter";
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

    // Unescapes a single linefeed, tab and a few other special characters.
    // Used to parse CSV record separator and quote character attributes. 
    private char escapeSequenceToChar(String inputString) {
        char resultChar;
        if(inputString.equals("\\n"))
            resultChar = '\n';
        else if(inputString.equals("\\r"))
            resultChar = '\r';
        else if(inputString.equals("\\0"))
            resultChar = '\0';
        else if(inputString.equals("\\\""))
            resultChar = '"';
        else if(inputString.equals("\\'"))
            resultChar = '\'';
        else if(inputString.equals("\\b"))
            resultChar = '\b';
        else if(inputString.equals("\\t"))
            resultChar = '\t';
        else if(inputString.equals("\\f"))
            resultChar = '\f';
        else
            resultChar = inputString.charAt(0);
        return resultChar;
    }

    public String[] convert(String data, ConversionInfo info, MessageLogger logger)
    throws FailTransferException, AbortTransferException {

        String csvResult[][] = null;

        /** Field names list can be empty, in which case we'll use unknownFieldNamePrefix
            -based generated names */
        String fieldNames;
        
        boolean fieldNamesValid = true;
        char csvRecordSeparatorChar;
        char csvQuoteChar;

        int firstCSVDataRow;
        String[] fieldNameList;

        String rootElementName = getRootElementName();

        String recordElementName = getRecordElementName();

        // XML Header and Footer lines may be empty
        String XMLheader = getXMLHeaderLine();
        String XMLfooter = getXMLFooterLine();
        
        csvRecordSeparatorChar = escapeSequenceToChar(getCSVRecordSeparatorChar());

        csvQuoteChar = escapeSequenceToChar(getCSVQuoteChar());

        String unknownFieldNamePrefix = getUnknownFieldNamePrefix();

        if(csvRecordSeparatorChar == csvQuoteChar) {
            logger.logMessage("CSV column separator and CSV row separator characters can not be same, aborting",
                               this,
                               MessageLogger.ERROR);
            PipeComponentUtils.failTransfer();
        }
        
        if((unknownFieldNamePrefix.indexOf(skipColumnPrefix) != 0) &&
           (XMLChar.isValidName(unknownFieldNamePrefix) == false)) {
            logger.logMessage(
                "Invalid XML element name prefix for missing field names, aborting",
                this,
                MessageLogger.ERROR);
            PipeComponentUtils.failTransfer();
        }
        
        StringBuffer XMLresult = new StringBuffer("");
       

        // Current version of CSVParser, does not use csvLineSeparatorChar parameter
        CSVReader csvReader = new CSVReader(
            new StringReader(data),
            csvRecordSeparatorChar,
            csvQuoteChar);

        try {
            csvResult = csvReader.records();
        } catch(CSVFormatException e) {
            logger.logMessage("Invalid data detected while reading CSV table, aborting", this, MessageLogger.ERROR);
            PipeComponentUtils.failTransfer();
        } catch(IOException e) {
            logger.logMessage("IOException while reading CSV table, aborting", this, MessageLogger.ERROR);
            PipeComponentUtils.failTransfer();
        }

        if(csvResult == null) {
            logger.logMessage(
                "CSVParser unable to parse input with specified parameters, aborting",
                this,
                MessageLogger.ERROR);
            PipeComponentUtils.failTransfer();
        } else {

            logger.logMessage(
                ("CSV data parsed, total length " + csvResult.length + " lines"),
                this,
                MessageLogger.DEBUG);

            if((getReadFieldNamesFromCSV().booleanValue()) && (csvResult.length > 0)) {

                List tempFieldNameList = new LinkedList();
                for(int i = 0; i < csvResult[0].length; i++) {
                    tempFieldNameList.add(csvResult[0][i]);
                }
                fieldNameList = (String[]) tempFieldNameList.toArray(new String[tempFieldNameList
                    .size()]);

            } else {
                fieldNames = getFieldNames();
                if( (fieldNames == null) || (fieldNames.length() == 0)) {
                    fieldNameList = new String[0];
                } else {
                    fieldNameList = fieldNames.split(Character.toString(fieldNameSeparatorChar));
                }
            }

            if((getSkipCSVHeaderLine().booleanValue()) && (csvResult.length > 0))
                firstCSVDataRow = 1;
            else
                firstCSVDataRow = 0;

            //int fieldNameCount = fieldNameList.length;
            for(int i = 0; i < fieldNameList.length; i++) {

                // Test each XML element name for validity unless
                // the name begins with a "skip column" prefix
                if((fieldNameList[i].indexOf(skipColumnPrefix) != 0) &&
                   (XMLChar.isValidName(fieldNameList[i]) == false)) {
                    fieldNamesValid = false;
                    break;
                }
            }

            if(fieldNamesValid) {
                
                if(XMLheader.length() > 0) {
                    XMLresult.append((XMLheader + "\n"));
                }
                XMLresult.append(("<" + rootElementName + ">\n"));

                if(data.length() > 0) {
                
                boolean hideUnknownFields = false;
                
                if(unknownFieldNamePrefix.indexOf(skipColumnPrefix) == 0) {
                    hideUnknownFields = true;
                }
                    
                for(int j = firstCSVDataRow; j < csvResult.length; j++) {

                    XMLresult.append(("<" + recordElementName + ">"));

                    for(int i = 0; i < csvResult[j].length; i++) {

                        if(i < fieldNameList.length) {
                            
                            // Output the XML element if the column is not set to be skipped
                            if(fieldNameList[i].indexOf(skipColumnPrefix) != 0) {
                                XMLresult
                                	.append(("<" + fieldNameList[i] + ">"
                                	             + XMLEscape.escape(
                                	         Utils.filterInvalidXMLCharacters(csvResult[j][i])) +
                                	         "</" + fieldNameList[i] + ">"));
                            }

                        } else {
                            // Generate field names if input CSV records contain more fields than
                            // there are field names specified. Output the XML element unless all
                            // unknown fields are set to be hidden
                            if(!hideUnknownFields) {
                            XMLresult.append(("<" + unknownFieldNamePrefix + (i + 1) + ">"
                                                  + XMLEscape.escape(
                                              Utils.filterInvalidXMLCharacters(csvResult[j][i])) + 
                                              "</" + unknownFieldNamePrefix + (i + 1) + ">"));
                            }

                        }
                    }

                    XMLresult.append(("</" + recordElementName + ">\n"));

                }
                
                }
                XMLresult.append(("</" + rootElementName + ">\n"));

            } else {
                logger.logMessage(
                    "Invalid XML element name detected, aborting",
                    this,
                    MessageLogger.ERROR);
                PipeComponentUtils.failTransfer();
            }

        }

        if(XMLfooter.length() > 0) {
            XMLresult.append((XMLfooter + "\n"));
        }

        return new String[] {XMLresult.toString()};
    }




} // CSVtoXMLConverter
