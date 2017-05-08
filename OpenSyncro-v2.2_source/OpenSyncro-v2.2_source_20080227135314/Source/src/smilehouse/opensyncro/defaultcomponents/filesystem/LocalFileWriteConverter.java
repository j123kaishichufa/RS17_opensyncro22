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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

import smilehouse.gui.html.fieldbased.FieldInfo;
import smilehouse.gui.html.fieldbased.GUIContext;
import smilehouse.gui.html.fieldbased.editor.DefaultSelectOption;
import smilehouse.gui.html.fieldbased.editor.SelectEditor;
import smilehouse.gui.html.fieldbased.editor.TextEditor;
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
 * LocalFileWriteConverter.java
 * 
 * A Destination that can be included into the Pipe to the position of Converter.
 * Writes its input to a local file. File name is given as a component attribute.
 * 
 * Created: Tue Oct 10 10:07:30 2006
 */

public class LocalFileWriteConverter implements ConverterIF, GUIConfigurationIF {

    protected static final String DIRECTORY_ATTR = "directory";
    protected static final String FILENAME_ATTR = "filename";
    protected static final String CHARSET_ATTR = "charset";
    protected static final String WRITE_TYPE_ATTR = "writetype";
    
    private static final String WRITE_TYPE_ALWAYS_OVERWRITE = "write_type_alwaysoverwrite";
    private static final String WRITE_TYPE_ITERATION_START_OVERWRITE = "write_type_iterationstartoverwrite";
    private static final String WRITE_TYPE_ALWAYS_APPEND = "write_type_alwaysappend";
    
    private static final String APPEND_ATTR = "append";
    
    

    protected static final String DEFAULT_CHARSET = "UTF-8";

    private static final String[] CHARSETS = PipeComponentUtils.getCharacterSetArray();

    protected File directory = null;
    protected File file = null;
    protected boolean singleFileMode;

    private boolean firstDataBlock = true;
    private boolean alwaysOverwrite = false;
    
    
    
    // --------------
    // GUI definition
    // --------------
    protected static LocalFileWriteConverterGUI gui = new LocalFileWriteConverterGUI();

    protected static class LocalFileWriteConverterGUI extends GUIDefinition {

        public LocalFileWriteConverterGUI() {
            try {
                {

                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            return ((LocalFileWriteConverter) model).getFileName();
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            ((LocalFileWriteConverter) model).setFileName((String) value);
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
                addSimpleTextFieldForComponent(DIRECTORY_ATTR, DIRECTORY_ATTR, 70);

                {
                    //set unique id and description labelkey
                    String id = CHARSET_ATTR;

                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            String value = ((LocalFileWriteConverter) model).getData().getAttribute(
                                CHARSET_ATTR);
                            return value != null ? value : DEFAULT_CHARSET;
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            ((LocalFileWriteConverter) model).getData().setAttribute(
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
                	//set unique id and description labelkey
                	String id = WRITE_TYPE_ATTR;
                	
                	ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            String value = ((LocalFileWriteConverter) model).getData().getAttribute(
                            		WRITE_TYPE_ATTR);
                            return value != null ? value : WRITE_TYPE_ALWAYS_OVERWRITE;
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            ((LocalFileWriteConverter) model).getData().setAttribute(
                            		WRITE_TYPE_ATTR,
                                (String) value);
                        }
                    };

                    SelectEditor editor = new SelectEditor();
                    editor.addOption(new DefaultSelectOption(WRITE_TYPE_ALWAYS_OVERWRITE, WRITE_TYPE_ALWAYS_OVERWRITE));
                    editor.addOption(new DefaultSelectOption(WRITE_TYPE_ALWAYS_APPEND, WRITE_TYPE_ALWAYS_APPEND));
                    editor.addOption(new DefaultSelectOption(WRITE_TYPE_ITERATION_START_OVERWRITE, WRITE_TYPE_ITERATION_START_OVERWRITE));
                    
                    //and finally create the configurationObject
                    FieldInfo fieldInfo = new FieldInfo(id, id, modifier, editor);

                    //add the configuration to the context for usage in the http-requests.
                    addField(id, fieldInfo);
                }

            } catch(Exception e) {
                Environment.getInstance().log(
                    "Couldn't create GUIContext for LocalFileWriteConverter", e);
            }
        }
    }

    public LocalFileWriteConverter(Object pipeComponentData) {
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
        return TYPE_CONVERTER;
    }

    public String getName() {
        return "LocalFileWriteConverter";
    }

    public String getID() {
        return "smilehouse.opensyncro.defaultcomponents.filesystem.LocalFileWriteConverter";
    }

    public String getDescription(Locale locale) {
        return PipeComponentUtils.getDescription(locale, this.getClass());
    }

    public int open(ConversionInfo info, MessageLogger logger) throws FailTransferException {
        this.directory = getDirectory(logger);
        this.file = getFile(directory, logger);
        this.singleFileMode = true;
        this.firstDataBlock = true;
        
        if(getData().getAttribute(WRITE_TYPE_ATTR).equals(WRITE_TYPE_ALWAYS_OVERWRITE)){
        	this.alwaysOverwrite = true;
        }
        
        return ITERATION_OPEN_STATUS_OK;
    }

    public int close(ConversionInfo info, MessageLogger logger) throws FailTransferException {
        return ITERATION_CLOSE_STATUS_OK;
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

    public void setWriteType(String writeType){
    	this.getData().setAttribute(WRITE_TYPE_ATTR, writeType);
    }
    
    public String getWriteType(){
    	
    	if(this.getData().getAttribute(WRITE_TYPE_ATTR) == null){
    		if(this.getData().getAttribute(APPEND_ATTR) == null){
    			this.setWriteType(WRITE_TYPE_ALWAYS_OVERWRITE);
    		}
    		else{
    			Boolean oldCheck = new Boolean(this.getData().getAttribute(APPEND_ATTR));
    			if(oldCheck){
    				this.setWriteType(WRITE_TYPE_ALWAYS_APPEND);
    			}
    			else
    				this.setWriteType(WRITE_TYPE_ITERATION_START_OVERWRITE);
    		}
    	}
    	return new String(this.getData().getAttribute(WRITE_TYPE_ATTR));    	
    }
    
    public File getFile(File directory, MessageLogger logger) throws FailTransferException {
        String fileWriteMode = null;
        String fileName = getFileName();
        if(fileName == null || fileName.length() == 0) {
            logger.logMessage("File name is not set!", this, MessageLogger.ERROR);
            PipeComponentUtils.failTransfer();
        }  
        
        if(getWriteType().equals(WRITE_TYPE_ALWAYS_APPEND)) {
            fileWriteMode = "always append mode";
        } 
        else if(getWriteType().equals(WRITE_TYPE_ALWAYS_OVERWRITE)) {
            fileWriteMode = "always overwrite mode";
        }
        else{
        	fileWriteMode = "overwrite at iteration start, otherwise append mode";
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

  
    /**
     * The method actually called by pipe during the conversion. 
     */
    
    public String[] convertAll(String[] data, ConversionInfo info, MessageLogger logger)
            throws FailTransferException, AbortTransferException {
    	
        for(int i = 0; i < data.length; i++) {
            convert(data[i], info, logger);
        }
        return data;
    }
    
   public String[] convert(String data, ConversionInfo info, MessageLogger logger)
            throws FailTransferException, AbortTransferException {
        String charsetName = this.getData().getAttribute(CHARSET_ATTR);
        //PrintWriter out = null;
        OutputStreamWriter out = null;
        if(charsetName == null || charsetName.length() == 0)
            charsetName = DEFAULT_CHARSET;
        
        try {

            logger.logMessage("Writing " + data.length() + " characters to file \""
                    + file.getPath() + "\"", this, MessageLogger.DEBUG);

            if(this.singleFileMode == true) {

                // In single file mode we append all data blocks to one file 
                if(this.firstDataBlock == true || this.alwaysOverwrite == true) {
                    if (this.firstDataBlock == true) {  
                        this.firstDataBlock = false;
                    }
                    // If it's the first data block, we follow the component's file append setting 
                    out = new OutputStreamWriter(new FileOutputStream(file, getWriteType().equals(WRITE_TYPE_ALWAYS_APPEND)), charsetName);
                }
             	else {
                    // If it's not the first data block, we always append to the file 
                    out = new OutputStreamWriter(new FileOutputStream(file, true), charsetName);
                }

            } else {
                 // As we are not in single file mode, we follow the component's file append
                 //setting for every data block
                 
              
            	
            	out = new OutputStreamWriter(
                            new FileOutputStream(file, getWriteType().equals(WRITE_TYPE_ALWAYS_APPEND)),
                            charsetName);
            }

            out.write(data);

            //logger.logMessage("Writing complete", this, MessageLogger.DEBUG);

        } catch(UnsupportedEncodingException e) {
            logger.logMessage(
                "Unsupported encoding: " + charsetName + ", aborting",
                this,
                MessageLogger.ERROR);
            PipeComponentUtils.failTransfer();

        } catch(IOException e) {
            logger.logMessage("IOException while writing to destination file \"" + getFileName()
                    + "\", aborting", this, MessageLogger.ERROR);
            PipeComponentUtils.failTransfer();

        } finally {

            if(out != null) {
                try {
                    out.flush();
                    out.close();
                } catch(IOException e) {
                    logger.logMessage("IOException while closing destination file \""
                            + getFileName() + "\", aborting", this, MessageLogger.ERROR);
                    PipeComponentUtils.failTransfer();
                }
            }
        }

        String[] dataArr = new String[1];
        dataArr [0] = data;
        return dataArr;
    }
    
    public GUIContext getGUIContext() {
        return gui.getGUIContext();
    }

    public String getGUITemplate() {
        return "<table border=0 cellspacing=5><tr><td colspan=\"2\">$" + DIRECTORY_ATTR
                + "$</td></tr>" + "<tr><td colspan=\"2\">$" + FILENAME_ATTR + "$</td></tr>" 
                + "<tr><td colspan=\"2\">$" + CHARSET_ATTR + "$</td></tr>" 
                + "<tr><td colspan=\"2\">$"+ WRITE_TYPE_ATTR +"$</td></tr></table>";
    }
} // LocalFileWriteConverter

