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
 * Created on Feb 1, 2005
 * 
 * ASCIItoXMLConverter converts text data to XML by performing a series of
 * hierarchical regular expression matches. It executes a script consisting of
 * lines specifying:
 * 
 * 1. name of the source data (XML element) to pass to the matcher
 * 2. names of new XML elements to contain regular expression group matches
 * 3. the actual regular expression with match groups (parts of pattern
 *    in parenthesis)
 * 
 * For more information on the component use, see OpenSyncro component
 * reference manual.
 * 
 * ---
 * 
 * TODO: Data debug mode, which outputs regexp match offset as an attribute to
 *       each XML element.
 * 
 * TODO: BUGFIX: allow multiple root level entries to make it possible to write
 *       the following type of scripts without the first dummy line...
 *  
 *       temp row (.+)
 *       row messagetype1 m1(.*)
 *       row messagetype2 m2(.*)
 *       row messagetype3 m3(.*)
 * 
 * TODO: BUGFIX: order of elements on the same nested level is determined by
 *               their order of appearance in the script instead of the input data?
 */
package smilehouse.opensyncro.defaultcomponents.converter.ascii;

import java.util.LinkedList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.xerces.util.XMLChar;

import smilehouse.gui.html.fieldbased.FieldInfo;
import smilehouse.gui.html.fieldbased.GUIContext;
import smilehouse.gui.html.fieldbased.editor.TextAreaEditor;
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
import smilehouse.xml.XMLEscape;

public class ASCIItoXMLConverter implements ConverterIF, GUIConfigurationIF {

    private static final String defaultRootElementName = "file";

    private static final String SCRIPT_ATTRIBUTE = "script";
    private static final String XMLHEADERLINE_ATTRIBUTE = "xmlheaderline";
    private static final String ROOTELEMENTNAME_ATTRIBUTE = "rootelementname";

    private static final String lineSeparatorPattern = "\r\n|\r|\n";
    private static final char fieldNameSeparatorChar = ',';

    
    private static boolean regexpGroupAmountWarningOutput;

    private static GUIDefinition gui = new ASCIItoXMLGUI();

    private static class ASCIItoXMLGUI extends GUIDefinition {
        public ASCIItoXMLGUI() {
            try {

                //set unique id and description labelkey
                String id = "script";
                String label = "script";

                ModelModifier modifier = new DefaultModelModifier() {

                    public Object getModelValue(Object model) throws Exception {
                        return ((ASCIItoXMLConverter) model).getScript();
                    }

                    public void setModelValue(Object model, Object value) throws Exception {
                        ((ASCIItoXMLConverter) model).setScript((String) value);
                    }

                };

                TextAreaEditor editor = new TextAreaEditor();
                editor.setCols(120);
                editor.setRows(20);

                //and finally create the configurationObject
                FieldInfo fieldInfo = new FieldInfo(id, label, modifier, editor);

                //add the configuration to the context for usage in the http-requests.
                addField(id, fieldInfo);

                //add edit field for XML root element name
                addSimpleTextFieldForComponent(
                    ROOTELEMENTNAME_ATTRIBUTE,
                    ROOTELEMENTNAME_ATTRIBUTE,
                    40);

                //add XML header line edit field
                addSimpleTextFieldForComponent(
                    XMLHEADERLINE_ATTRIBUTE,
                    XMLHEADERLINE_ATTRIBUTE,
                    100);

            }

            catch(Exception e) {
                Environment.getInstance().log("Couldn't initialize ASCIItoXMLConverter GUI", e);
            }
        }
    }

    public GUIContext getGUIContext() {
        return gui.getGUIContext();
    }

    public String getGUITemplate() {
        return "<table border=0>" + "<tr><td>$" + SCRIPT_ATTRIBUTE + "$</td></tr>" + "<tr><td>$"
                + ROOTELEMENTNAME_ATTRIBUTE + "$</td></tr>" + "<tr><td>$" + XMLHEADERLINE_ATTRIBUTE
                + "$</td></tr></table>";
    }

    public ASCIItoXMLConverter() {}

    // ---
    
    public ASCIItoXMLConverter( Object pipeComponentData ) {
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
        return "ASCIItoXMLConverter";
    }

    public String getID() {
        return "smilehouse.opensyncro.defaultcomponents.converter.ascii.ASCIItoXMLConverter";
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
    
    
    
    // ---

    
    
    public String getScript() {
        String script = this.data.getAttribute(SCRIPT_ATTRIBUTE);
        if(script != null)
            return script;

        return "";
    }

    public void setScript(String script) {
        this.data.setAttribute(SCRIPT_ATTRIBUTE, script);
    }

    public String getRootElementName() {
        String rootElementName = this.data.getAttribute(ROOTELEMENTNAME_ATTRIBUTE);
        if(rootElementName != null)
            return rootElementName;

        return defaultRootElementName;
    }

    public void setRootElementName(String rootElementName) {
        this.data.setAttribute(ROOTELEMENTNAME_ATTRIBUTE, rootElementName);
    }

    public String getXMLHeaderLine() {
        String XMLHeaderLine = this.data.getAttribute(XMLHEADERLINE_ATTRIBUTE);
        if(XMLHeaderLine != null)
            return XMLHeaderLine;

        return "";
    }

    public void setXMLHeaderLine(String XMLHeaderLine) {
        this.data.setAttribute(XMLHEADERLINE_ATTRIBUTE, XMLHeaderLine);
    }

    private class scriptLine {

        /*
         * Store original script line numbers (1-n) here for use in error messages, since the script
         * parser skips all comment lines
         */
        public int lineNumber;

        public String sourceFieldName;
        public String[] destinationFieldNames;
        public String regExp;

        public scriptLine(int lineNumber,
                          String sourceFieldName,
                          String[] destinationFieldNames,
                          String regExp) {
            this.lineNumber = lineNumber;
            this.sourceFieldName = sourceFieldName;
            this.destinationFieldNames = destinationFieldNames;
            this.regExp = regExp;
        }

    }

    private boolean testValidXMLNamesInArray(String[] stringArray, MessageLogger logger) {

        for(int i = 0; i < stringArray.length; i++) {
            if(XMLChar.isValidName(stringArray[i]) == false) {
                logger.logMessage("Invalid XML destination element name \"" + stringArray[i]
                        + "\" detected, aborting", this, MessageLogger.ERROR);
                return false;
            }
        }
        return true;
    }

    private boolean testDestinationFieldNameArray(String[] stringArray, MessageLogger logger) {
        // There should at least one destination element on the list
        if(stringArray.length == 0)
            return false;

        return testValidXMLNamesInArray(stringArray, logger);
    }

    public void processElement(int scriptIndex,
                               int currentBufferStartOffset,
                               String currentBuffer,
                               StringBuffer resultBuffer,
                               scriptLine[] scriptLineArray,
                               MessageLogger logger) throws FailTransferException {



        Pattern pattern = Pattern.compile(scriptLineArray[scriptIndex].regExp);
        Matcher matcher = pattern.matcher(currentBuffer);

        while(matcher.find()) {

            // resultBuffer.append("Debug: Match start: " + matcher.start() + ", Match end: " +
            // matcher.end() + "\n");

            resultBuffer.append("<" + scriptLineArray[scriptIndex].sourceFieldName + ">\n");
            int gCount = matcher.groupCount();

            if(gCount > 0) {

                if(gCount > scriptLineArray[scriptIndex].destinationFieldNames.length) {
                    logger.logMessage(
                        "Regular expression matched " + gCount + " groups at input data offset "
                                + currentBufferStartOffset + matcher.start() + ", but only "
                                + scriptLineArray[scriptIndex].destinationFieldNames.length
                                + " destination element name(s) were specified for "
                                + scriptLineArray[scriptIndex].sourceFieldName + " on line "
                                + scriptLineArray[scriptIndex].lineNumber + ". Aborting.",
                        this,
                        MessageLogger.ERROR);
                    PipeComponentUtils.failTransfer();
                }

                /*
                 * Warn if there are more destination elements than regular expression match groups.
                 * Output warning only once per component execution
                 */
                if(!regexpGroupAmountWarningOutput
                        && gCount < scriptLineArray[scriptIndex].destinationFieldNames.length) {

                    logger.logMessage(
                        "Regular expression matched " + gCount + " group(s) at input data offset "
                                + currentBufferStartOffset + matcher.start() + ", but "
                                + scriptLineArray[scriptIndex].destinationFieldNames.length
                                + " destination element names were specified for "
                                + scriptLineArray[scriptIndex].sourceFieldName + " on line "
                                + scriptLineArray[scriptIndex].lineNumber
                                + ". Suppressing further warnings of this type.",
                        this,
                        MessageLogger.WARNING);

                    regexpGroupAmountWarningOutput = true;
                }


                for(int j = 0; j < gCount; j++) {

                    String fieldValue;
                    int scriptIndexOfNewRegExp = -1;

                    fieldValue = matcher.group(j + 1);
                    String currDestFieldName = scriptLineArray[scriptIndex].destinationFieldNames[j];

                    for(int k = 0; k < scriptLineArray.length; k++) {

                        if((k != scriptIndex)
                                && (scriptLineArray[k].sourceFieldName.compareTo(currDestFieldName) == 0)) {
                            scriptIndexOfNewRegExp = k;

                            processElement(
                                scriptIndexOfNewRegExp,
                                currentBufferStartOffset + matcher.start(j + 1),
                                fieldValue,
                                resultBuffer,
                                scriptLineArray,
                                logger);

                        }

                    }

                    // There weren't any further regexps to process, just output the content as is
                    if(scriptIndexOfNewRegExp == -1)

                    {
                        String outputFieldValue = Utils.filterInvalidXMLCharacters(fieldValue);
                        if(outputFieldValue.length() > 0) {
                            resultBuffer.append("<" + currDestFieldName + ">"
                                + XMLEscape.escape(outputFieldValue) + "</" + currDestFieldName + ">");
                        } else {
                            resultBuffer.append("<" + currDestFieldName + "/>");
                        }
                    }
                }
            }

            resultBuffer.append("</" + scriptLineArray[scriptIndex].sourceFieldName + ">\n");

        }

    }

    public String[] convert(String data, ConversionInfo info, MessageLogger logger)
            throws FailTransferException, AbortTransferException {

        String xmlDeclarationString = getXMLHeaderLine();
        // boolean trimFieldValues = true;

        // Reset warning output status
        regexpGroupAmountWarningOutput = false;

        scriptLine[] scriptLineArray;
        String[] scriptLineStrings = getScript().split(lineSeparatorPattern);

        // Initialize result buffer with XML declaration
        StringBuffer resultBuffer = new StringBuffer(xmlDeclarationString + "\n");

        LinkedList scriptLineList = new LinkedList();


        /* Parse script */

        /*
         * Extract 3 columns separated by white-space character, allow the 3rd column ("regExp") to
         * contain also spaces
         */
        Pattern p = Pattern.compile("(\\S+)?\\s+(\\S+)?\\s+(.+)?");

        for(int i = 0; i < scriptLineStrings.length; i++) {

            String sourceFieldName = "", destinationFieldNameString = "", regExp = "";
            
            // Skip empty lines and comment lines beginning with '#' character
            if(scriptLineStrings[i].length() > 0 &&
                    !(scriptLineStrings[i].startsWith("#") || scriptLineStrings[i].startsWith("\n"))) {
                Matcher m = p.matcher(scriptLineStrings[i]);
                if(m.find()) {
                    int gCount = m.groupCount();

                    if(gCount < 3) {

                        /*
                         * Script line column amount check does not seem to work currently, this
                         * code is never reached.
                         */
                        logger.logMessage(
                            ("Syntax error, line " + (i + 1) + ": less than 3 columns, aborting."),
                            this,
                            MessageLogger.ERROR);
                        PipeComponentUtils.failTransfer();

                    } else {
                        sourceFieldName = m.group(1);
                        if(XMLChar.isValidName(sourceFieldName) == false) {
                            logger.logMessage(
                                "Invalid XML source element name \"" + sourceFieldName
                                        + "\" detected, aborting",
                                this,
                                MessageLogger.ERROR);
                            PipeComponentUtils.failTransfer();
                        }

                        destinationFieldNameString = m.group(2);

                        /*
                         * Check that the destination element names are valid names in XML and that
                         * the element list is not empty
                         */

                        // ToDo: remove duplicate destinationFieldNameString split operation
                        if(testDestinationFieldNameArray(destinationFieldNameString.split(Character
                            .toString(fieldNameSeparatorChar)), logger) == false) {
                            logger.logMessage(
                                "Invalid or missing destination element name(s) on line " + (i + 1)
                                        + ", aborting.",
                                this,
                                MessageLogger.ERROR);
                            PipeComponentUtils.failTransfer();
                        }

                        regExp = m.group(3);
                        scriptLineList.add(new scriptLine(
                            i + 1,
                            sourceFieldName,
                            destinationFieldNameString.split(Character
                                .toString(fieldNameSeparatorChar)),
                            regExp));
                    }
                } else {
                    logger.logMessage(
                        "Invalid syntax at script line " + (i + 1) + ", aborting.",
                        this,
                        MessageLogger.ERROR);
                    PipeComponentUtils.failTransfer();
                }
            }

        }

        scriptLineArray = (scriptLine[]) scriptLineList.toArray(new scriptLine[scriptLineList
            .size()]);

        String tempMessage = "Successfully parsed " + scriptLineArray.length
                + " script command lines";
        if(scriptLineStrings.length > scriptLineArray.length)
            tempMessage = tempMessage + ", skipped "
                    + (scriptLineStrings.length - scriptLineArray.length)
                    + " comment or empty lines";
        logger.logMessage(tempMessage, this, MessageLogger.DEBUG);

        if(XMLChar.isValidName(getRootElementName()) == true) {
            resultBuffer.append("<" + getRootElementName() + ">\n");
        } else {
            logger.logMessage("Invalid XML root element name \"" + getRootElementName()
                    + "\" detected, aborting", this, MessageLogger.ERROR);
            PipeComponentUtils.failTransfer();
        }

        // Start script execution from line 1
        processElement(0, 0, data, resultBuffer, scriptLineArray, logger); // First column on the
                                                                           // first script line
                                                                           // contains root element
                                                                           // name

        resultBuffer.append("</" + getRootElementName() + ">\n");

        return new String[] {resultBuffer.toString()};
    }


} // ASCIItoXMLConverter