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

package smilehouse.opensyncro.defaultcomponents.converter.split;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

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

/**
 * SplitConverter.java
 * 
 * Created: Thu Jun 3 15:19:19 2004
 * 
 */

public class SplitConverter implements ConverterIF, GUIConfigurationIF {

    private static final String REGEX_ATTR = "regex";
    private static final String PREFIXSTRING_ATTR = "prefixString";
    private static final String SUFFIXSTRING_ATTR = "suffixString";
    private static final String PSSKIPFIRST_ATTR = "PSSkipFirst";
    private static final String REMOVEEMPTY_ATTR = "removeEmpty";

    private static GUIDefinition gui = new SplitGUI();

    private static class SplitGUI extends GUIDefinition {
        public SplitGUI() {
            try {
                addSimpleTextFieldForComponent(REGEX_ATTR, REGEX_ATTR, 50);

                
                {
                    //set unique id and description labelkey
                    String id = "removeEmpty";
                    String label = "removeEmpty";

                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws FailTransferException, AbortTransferException {
                            return ((SplitConverter) model).getRemoveEmpty();
                        }

                        public void setModelValue(Object model, Object value) throws FailTransferException, AbortTransferException {
                            ((SplitConverter) model).setRemoveEmpty((Boolean) value);
                        }
                    };

                    BooleanEditor editor = new BooleanEditor();

                    //and finally create the configurationObject
                    FieldInfo fieldInfo = new FieldInfo(id, label, modifier, editor);

                    //add the configuration to the context for usage in the http-requests.
                    addField(id, fieldInfo);
                }


                addSimpleTextFieldForComponent(PREFIXSTRING_ATTR, PREFIXSTRING_ATTR, 50);

                {
                    //set unique id and description labelkey
                    String id = "PSSkipFirst";
                    String label = "PSSkipFirst";

                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws FailTransferException, AbortTransferException {
                            return ((SplitConverter) model).getPSSkipFirst();
                        }

                        public void setModelValue(Object model, Object value) throws FailTransferException, AbortTransferException {
                            ((SplitConverter) model).setPSSkipFirst((Boolean) value);
                        }
                    };

                    BooleanEditor editor = new BooleanEditor();

                    //and finally create the configurationObject
                    FieldInfo fieldInfo = new FieldInfo(id, label, modifier, editor);

                    //add the configuration to the context for usage in the http-requests.
                    addField(id, fieldInfo);
                }

                addSimpleTextFieldForComponent(SUFFIXSTRING_ATTR, SUFFIXSTRING_ATTR, 50);
            } catch(Exception e) {
                Environment.getInstance().log("Couldn't initialize SplitConverter GUI", e);
            }
        }
    }

    public GUIContext getGUIContext() {
        return gui.getGUIContext();
    }

    public String getGUITemplate() {
        return "<table border=0 cellspacing=5><tr><td>$" + REGEX_ATTR + "$</td><td>$"
                + REMOVEEMPTY_ATTR + "$</td></tr>" + "<tr><td>$" + PREFIXSTRING_ATTR
                + "$</td><td>$" + PSSKIPFIRST_ATTR + "$</td></tr>" + "<tr><td>$"
                + SUFFIXSTRING_ATTR + "$</td></tr></table>";
    }

    public void setPSSkipFirst(Boolean PSSkipFirst) {
        this.data.setAttribute(PSSKIPFIRST_ATTR, PSSkipFirst != null ? PSSkipFirst.toString() : "false");
    }

    public Boolean getRemoveEmpty() {
        return new Boolean(this.data.getAttribute(REMOVEEMPTY_ATTR));
    }

    public void setRemoveEmpty(Boolean removeEmpty) {
        this.data.setAttribute(REMOVEEMPTY_ATTR, removeEmpty != null ? removeEmpty.toString() : "false");
    }

    public Boolean getPSSkipFirst() {
        return new Boolean(this.data.getAttribute(PSSKIPFIRST_ATTR));
    }

    public SplitConverter( Object pipeComponentData ) {
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
        return "SplitConverter";
    }

    public String getID() {
        return "smilehouse.opensyncro.defaultcomponents.converter.split.SplitConverter";
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
    
    private String[] addPrefixAndSuffixToArray(String[] stringArray,
                                               String prefix,
                                               String suffix,
                                               boolean skipFirstPrefix) {
        for(int i = 0; i < stringArray.length; i++) {
            if((i == 0) && skipFirstPrefix)
                stringArray[i] = stringArray[i] + suffix;
            else
                stringArray[i] = prefix + stringArray[i] + suffix;
        }
        return stringArray;
    }

    private String[] removeEmptyStringsFromArray(String[] stringArray) {
        List list = new LinkedList();
        for(int i = 0; i < stringArray.length; i++) {
            if(stringArray[i].length() > 0)
                list.add(stringArray[i]);
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

    public String[] convert(String data, ConversionInfo info, MessageLogger logger)
            throws FailTransferException, AbortTransferException {
        String regex = this.data.getAttribute(REGEX_ATTR);

        String prefixString = this.data.getAttribute(PREFIXSTRING_ATTR);
        if(prefixString == null)
            prefixString = "";

        String suffixString = this.data.getAttribute(SUFFIXSTRING_ATTR);
        if(suffixString == null)
            suffixString = "";

        if(regex == null || regex.length() == 0) {
            logger.logMessage(
                "Delimiting regular expression is not set, returning data unchanged.",
                this,
                MessageLogger.WARNING);
            return new String[] {data};
        } else {
            String[] splitted = data.split(regex, -1);
            if(getRemoveEmpty().booleanValue()) {
                logger.logMessage("Splitted parts before removing empty strings: "
                        + splitted.length, this, MessageLogger.DEBUG);
                splitted = removeEmptyStringsFromArray(splitted);
                logger.logMessage(
                    "Splitted parts after removing empty strings: " + splitted.length,
                    this,
                    MessageLogger.DEBUG);
            }
            // Insert prefix and append suffix string for each splitted component
            splitted = addPrefixAndSuffixToArray(
                splitted,
                prefixString,
                suffixString,
                getPSSkipFirst().booleanValue());
            logger.logMessage(
                "Splitted input into " + splitted.length + " components.",
                this,
                MessageLogger.DEBUG);
            return splitted;
        }
    }

} // SplitConverter
