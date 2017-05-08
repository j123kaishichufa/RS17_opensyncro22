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

package smilehouse.opensyncro.defaultcomponents.filesystem;

import java.io.File;
import java.util.Locale;

import smilehouse.gui.html.fieldbased.FieldInfo;
import smilehouse.gui.html.fieldbased.GUIContext;
import smilehouse.gui.html.fieldbased.editor.BooleanEditor;
import smilehouse.gui.html.fieldbased.editor.DefaultSelectOption;
import smilehouse.gui.html.fieldbased.editor.SelectEditor;
import smilehouse.gui.html.fieldbased.editor.TextAreaEditor;
import smilehouse.gui.html.fieldbased.editor.TextEditor;
import smilehouse.gui.html.fieldbased.model.DefaultModelModifier;
import smilehouse.gui.html.fieldbased.model.ModelModifier;
import smilehouse.opensyncro.pipes.component.AbortTransferException;
import smilehouse.opensyncro.pipes.component.DestinationIF;
import smilehouse.opensyncro.pipes.component.FailTransferException;
import smilehouse.opensyncro.pipes.component.PipeComponentData;
import smilehouse.opensyncro.pipes.component.PipeComponentUtils;
import smilehouse.opensyncro.pipes.gui.GUIConfigurationIF;
import smilehouse.opensyncro.pipes.gui.GUIDefinition;
import smilehouse.opensyncro.pipes.log.MessageLogger;
import smilehouse.opensyncro.pipes.metadata.DestinationInfo;
import smilehouse.opensyncro.system.Environment;

/**
 * LocalFileDestination.java
 * 
 * A Destination that writes it input to a local file. File name is given as a component attribute.
 * 
 * Created: Tue Mar 16 10:07:30 2004
 */

public class LocalFileDestination extends FileWriter implements DestinationIF, GUIConfigurationIF {

    protected static final String DIRECTORY_ATTR = "directory";
    protected static final String FILENAME_ATTR = "filename";
    protected static final String APPEND_ATTR = "append";
    protected static final String CHARSET_ATTR = "charset";
    protected static final String STARTSTRING_ATTR = "startstring";
    protected static final String ENDSTRING_ATTR = "endstring";

    protected static final String DEFAULT_CHARSET = "UTF-8";

    private static final String[] CHARSETS = PipeComponentUtils.getCharacterSetArray();

    protected File directory = null;
    protected File file = null;
    protected boolean singleFileMode;
    protected String startString;
    protected String endString;

    private boolean firstDataBlock = true;
    
    // --------------
    // GUI definition
    // --------------
    protected static LocalFileDestinationGUI gui = new LocalFileDestinationGUI();

    protected static class LocalFileDestinationGUI extends GUIDefinition {

        public LocalFileDestinationGUI() {
            try {
                {

                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            return ((LocalFileDestination) model).getFileName();
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            ((LocalFileDestination) model).setFileName((String) value);
                        }
                    };

                    TextEditor editor = new TextEditor();
                    editor.setSize(40);

                    //and finally create the configurationObject
                    FieldInfo fieldInfo = new FieldInfo(
                        FILENAME_ATTR,
                        FILENAME_ATTR,
                        modifier,
                        editor);

                    //add the configuration to the context for usage in the http-requests.
                    addField(FILENAME_ATTR, fieldInfo);
                }
                {

                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            return ((LocalFileDestination) model).getAppend();
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            ((LocalFileDestination) model).setAppend((Boolean) value);
                        }
                    };

                    BooleanEditor editor = new BooleanEditor();

                    //and finally create the configurationObject
                    FieldInfo fieldInfo = new FieldInfo(APPEND_ATTR, APPEND_ATTR, modifier, editor);

                    //add the configuration to the context for usage in the http-requests.
                    addField(APPEND_ATTR, fieldInfo);
                }
                addSimpleTextFieldForComponent(DIRECTORY_ATTR, DIRECTORY_ATTR, 70);

                {
                    //set unique id and description labelkey
                    String id = CHARSET_ATTR;

                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            String value = ((LocalFileDestination) model).getData().getAttribute(
                                CHARSET_ATTR);
                            return value != null ? value : DEFAULT_CHARSET;
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            ((LocalFileDestination) model).getData().setAttribute(
                                CHARSET_ATTR,
                                (String) value);
                        }
                    };

                    SelectEditor editor = new SelectEditor();
                    for(int i = 0; i < CHARSETS.length; i++)
                        editor.addOption(new DefaultSelectOption(CHARSETS[i], CHARSETS[i]));

                    //and finally create the configurationObject
                    FieldInfo fieldInfo = new FieldInfo(id, id, modifier, editor);

                    //add the configuration to the context for usage in the http-requests.
                    addField(id, fieldInfo);
                }
                {
                    String id = STARTSTRING_ATTR;
                    
                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            String value = ((LocalFileDestination) model).getData().getAttribute(
                                STARTSTRING_ATTR);
                            return value != null ? value : "";
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            ((LocalFileDestination) model).getData().setAttribute(
                                STARTSTRING_ATTR,
                                (String) value);
                        }  
                    };
                    TextAreaEditor editor = new TextAreaEditor();
                    editor.setCols(40);
                    editor.setRows(2);

                    //and finally create the configurationObject
                    FieldInfo fieldInfo = new FieldInfo(id, id, modifier, editor);

                    //add the configuration to the context for usage in the http-requests.
                    addField(id, fieldInfo);
                }
                {
                    String id = ENDSTRING_ATTR;
                    
                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            String value = ((LocalFileDestination) model).getData().getAttribute(
                                ENDSTRING_ATTR);
                            return value != null ? value : "";
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            ((LocalFileDestination) model).getData().setAttribute(
                                ENDSTRING_ATTR,
                                (String) value);
                        }  
                    };
                    TextAreaEditor editor = new TextAreaEditor();
                    editor.setCols(40);
                    editor.setRows(2);

                    //and finally create the configurationObject
                    FieldInfo fieldInfo = new FieldInfo(id, id, modifier, editor);

                    //add the configuration to the context for usage in the http-requests.
                    addField(id, fieldInfo);
                }
            } catch(Exception e) {
                Environment.getInstance().log(
                    "Couldn't create GUIContext for LocalFileDestination", e);
            }
        }
    }

    public LocalFileDestination(Object pipeComponentData) {
        setData((PipeComponentData) pipeComponentData);
    }

    protected PipeComponentData data;

    public void setData(PipeComponentData pipeComponentData) {
        this.data = pipeComponentData;
    }

    public PipeComponentData getData() {
        return data;
    }

    public final int getType() {
        return TYPE_DESTINATION;
    }

    public String getName() {
        return "LocalFileDestination";
    }

    public String getID() {
        return this.getClass().getName();
    }

    public String getDescription(Locale locale) {
        return PipeComponentUtils.getDescription(locale, this.getClass());
    }

    public int open(DestinationInfo info, MessageLogger logger) throws FailTransferException {
        this.directory = getDirectory(logger);
        this.file = getFile(directory, logger);
        this.singleFileMode = true;
        this.firstDataBlock = true;
        //write start string
        if (getStartString().length() > 0 && this.firstDataBlock == true) {
            String charsetName = this.getData().getAttribute(CHARSET_ATTR);
            if (charsetName == null) { 
                charsetName = DEFAULT_CHARSET;
            }
            write(file, getStartString(), getAppend().booleanValue(), logger, charsetName);
            this.firstDataBlock = false;
        }
        return ITERATION_OPEN_STATUS_OK;
    }

    public int close(DestinationInfo info, MessageLogger logger) throws FailTransferException {
        //write end string
        if (getEndString().length() > 0) {
            String charsetName = this.getData().getAttribute(CHARSET_ATTR);
            if (charsetName == null) { 
                charsetName = DEFAULT_CHARSET;
            }
            write(file, getEndString(), true, logger, charsetName);
        }
        return ITERATION_CLOSE_STATUS_OK;
    }
    
    public void lastBlockStatus(int statusCode) {}

    /**
     * The method actually called by pipe during the conversion. This default implementation uses
     * the take-method to handle all the input records separately, So in most cases you only need to
     * implement it. If however you for some reason need access to all the data you can overwrite
     * this.
     */
    public void takeAll(String[] data, DestinationInfo info, MessageLogger logger)
            throws FailTransferException, AbortTransferException {
        for(int i = 0; i < data.length; i++) {
            take(data[i], info, logger);
        }
    }

    public void setFileName(String fileName) {
        this.getData().setAttribute(FILENAME_ATTR, fileName);
    }

    public String getFileName() {
        String fileName = this.getData().getAttribute(FILENAME_ATTR);
        if(fileName != null)
            return fileName;
        else
            return "";
    }

    public void setAppend(Boolean append) {
        this.getData().setAttribute(APPEND_ATTR, append != null ? append.toString() : "false");
    }

    public Boolean getAppend() {
        return new Boolean(this.getData().getAttribute(APPEND_ATTR));
    }

    public File getFile(File directory, MessageLogger logger) throws FailTransferException {
        String fileWriteMode = null;
        String fileName = getFileName();
        if(fileName == null || fileName.length() == 0) {
            logger.logMessage("File name is not set!", this, MessageLogger.ERROR);
            PipeComponentUtils.failTransfer();
        }

        if(getAppend().booleanValue()) {
            fileWriteMode = "append mode";
        } else {
            fileWriteMode = "overwrite mode";
        }

        if(directory != null) {
            logger.logMessage("Opening file \"" + directory + File.separatorChar + fileName
                    + "\" in " + fileWriteMode, this, MessageLogger.DEBUG);
            return new File(directory, fileName);
        } else {
            logger.logMessage(
                "Opening file \"" + fileName + "\" in " + fileWriteMode,
                this,
                MessageLogger.DEBUG);
            return new File(fileName);
        }
    }

    public File getDirectory(MessageLogger logger) {
        String directoryName = this.getData().getAttribute(DIRECTORY_ATTR);
        if(directoryName == null || directoryName.length() == 0) {
            return null;
        } else {
            return new File(directoryName);
        }
    }
    
    private String getStartString() {
        String start = this.getData().getAttribute(STARTSTRING_ATTR);
        if (start != null) {
            return start;
        } else {
            return "";
        }
    }
    
    private String getEndString() {
        String end = this.getData().getAttribute(ENDSTRING_ATTR);
        if (end != null) {
            return end;
        } else {
            return "";
        }
    }

    public void take(String data, DestinationInfo info, MessageLogger logger)
            throws FailTransferException, AbortTransferException {

        String charsetName = this.getData().getAttribute(CHARSET_ATTR);
        if(charsetName == null || charsetName.length() == 0)
            charsetName = DEFAULT_CHARSET;

        if(this.singleFileMode == true) {
            /** In single file mode we append all data blocks to one file */
            if(this.firstDataBlock == true) {
                this.firstDataBlock = false;
                /** If it's the first data block, we follow the component's file append setting */
                write(file, data, getAppend().booleanValue(), logger, charsetName);
            } else {
                /** If it's not the first data block, we always append to the file */
                write(file, data, true, logger, charsetName);
            }

        } else {
            /**
             * As we are not in single file mode, we follow the component's file append
             * setting for every data block
             */
            write(file, data, getAppend().booleanValue(), logger, charsetName);            
        }
    }
    
    public GUIContext getGUIContext() {
        return gui.getGUIContext();
    }

    public String getGUITemplate() {
        return "<table border=0 cellspacing=5><tr><td colspan=\"2\">$" + DIRECTORY_ATTR
                + "$</td></tr>" + "<tr><td>$"+ FILENAME_ATTR +"$</td><td>$"+ APPEND_ATTR +"$</td></tr>"
                + "<tr><td>$"+ STARTSTRING_ATTR +"$</td></tr><tr><td>$"+ ENDSTRING_ATTR +"$</td></tr>"
                + "<tr><td colspan=\"2\">$" + CHARSET_ATTR + "$</td></tr></table>";
    }
} // LocalFileDestination
