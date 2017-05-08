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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import smilehouse.gui.html.fieldbased.FieldInfo;
import smilehouse.gui.html.fieldbased.GUIContext;
import smilehouse.gui.html.fieldbased.editor.DefaultSelectOption;
import smilehouse.gui.html.fieldbased.editor.SelectEditor;
import smilehouse.gui.html.fieldbased.editor.TextEditor;
import smilehouse.gui.html.fieldbased.formatter.IntegerFormatter;
import smilehouse.gui.html.fieldbased.model.DefaultModelModifier;
import smilehouse.gui.html.fieldbased.model.ModelModifier;
import smilehouse.opensyncro.pipes.component.AbortTransferException;
import smilehouse.opensyncro.pipes.component.FailTransferException;
import smilehouse.opensyncro.pipes.component.PipeComponentData;
import smilehouse.opensyncro.pipes.component.PipeComponentUtils;
import smilehouse.opensyncro.pipes.component.SourceIF;
import smilehouse.opensyncro.pipes.gui.GUIConfigurationIF;
import smilehouse.opensyncro.pipes.gui.GUIDefinition;
import smilehouse.opensyncro.pipes.log.MessageLogger;
import smilehouse.opensyncro.pipes.metadata.SourceInfo;
import smilehouse.opensyncro.system.Environment;
import smilehouse.xml.chopper.XMLChopper;


public class IteratingXMLFileSource implements SourceIF, GUIConfigurationIF {
    

    protected static final String PREFIX_ATTR = "prefix";
    protected static final String DATEFORMAT_ATTR = "dateformat";
    protected static final String EXTENSION_ATTR = "extension";
    
    private static final String CHARSET_ATTR = "charset";
    private static final String DIRECTORY_ATTR = "dir";
    
    private static final String BLOCK_SIZE_ATTR = "blocksize";
    private static final String CHOP_DEPTH_ATTR = "chopDepth";
    
    private static final String DEFAULT_CHARSET = "UTF-8";
    //private static final String DEFAULT_DATEFORMAT = "yyyy-MM-dd";
    
    private static final String[] CHARSETS = PipeComponentUtils.getCharacterSetArray();
    
    private PipeComponentData data;
    private InputStream in;
    private XMLChopper chopper;
    private int blockSize;

    
    // --------------
    // GUI definition
    // --------------
    protected static IteratingXmlSourceGUI gui = new IteratingXmlSourceGUI();

    protected static class IteratingXmlSourceGUI extends GUIDefinition {

        public IteratingXmlSourceGUI() {
            try {
                addSimpleTextFieldForComponent(DIRECTORY_ATTR, DIRECTORY_ATTR, 70);
                addSimpleTextFieldForComponent(PREFIX_ATTR, PREFIX_ATTR, 30);
                addSimpleTextFieldForComponent(DATEFORMAT_ATTR, DATEFORMAT_ATTR, 20);
                addSimpleTextFieldForComponent(EXTENSION_ATTR, EXTENSION_ATTR, 10);
                {
                    // ----------
                    // Block size
                    // ----------
                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            return new Integer( ((IteratingXMLFileSource) model).getBlockSize() );
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            ((IteratingXMLFileSource) model).setBlockSize( ((Integer) value).intValue() );
                        }
                    };

                    TextEditor editor = new TextEditor();
                    IntegerFormatter formatter = new IntegerFormatter();
                    formatter.acceptOnlyStrictlyPositive("invalid_block_size");
                    editor.setFormatter(formatter);
                    editor.setSize(5);
                    
                    FieldInfo fieldInfo = new FieldInfo(
                        BLOCK_SIZE_ATTR,
                        BLOCK_SIZE_ATTR,
                        modifier,
                        editor);

                    addField(BLOCK_SIZE_ATTR, fieldInfo);
                }
                {
                    // ----------
                    // Chop depth
                    // ----------
                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            return new Integer( ((IteratingXMLFileSource) model).getChopDepth() );
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            ((IteratingXMLFileSource) model).setChopDepth( ((Integer) value).intValue() );
                        }
                    };

                    TextEditor editor = new TextEditor();
                    IntegerFormatter formatter = new IntegerFormatter();
                    formatter.acceptOnlyPositive("invalid_chop_depth");
                    editor.setFormatter(formatter);
                    editor.setSize(5);
                    
                    FieldInfo fieldInfo = new FieldInfo(
                        CHOP_DEPTH_ATTR,
                        CHOP_DEPTH_ATTR,
                        modifier,
                        editor);

                    addField(CHOP_DEPTH_ATTR, fieldInfo);
                }
                {
                    //set unique id and description labelkey
                    String id = CHARSET_ATTR;

                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws FailTransferException,
                                AbortTransferException {
                            String value = ((IteratingXMLFileSource) model).getData().getAttribute(
                                CHARSET_ATTR);
                            return value != null ? value : DEFAULT_CHARSET;
                        }

                        public void setModelValue(Object model, Object value)
                                throws FailTransferException, AbortTransferException {
                            ((IteratingXMLFileSource) model).getData().setAttribute(
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

            } catch(Exception e) {
                Environment.getInstance().log(
                    "Couldn't create GUIContext for IteratingXMLFileSource", e);
            }
        }
    }

    public IteratingXMLFileSource(Object pipeComponentData) {
        setData((PipeComponentData) pipeComponentData);
    }
    
    private int getBlockSize() throws NumberFormatException {
        Integer size = getData().getIntegerAttribute(BLOCK_SIZE_ATTR);
        return size != null ? size.intValue() : 1;    
    }
    
    private void setBlockSize(int blockSize) {
        getData().setAttribute(BLOCK_SIZE_ATTR, blockSize);
    }
    
    private int getChopDepth() throws NumberFormatException {
        Integer depth = getData().getIntegerAttribute(CHOP_DEPTH_ATTR);
        return depth != null ? depth.intValue() : 1;    
    }
    
    private void setChopDepth(int chopDepth) {
        getData().setAttribute(CHOP_DEPTH_ATTR, chopDepth);
    }
    
    
    /* (non-Javadoc)
     * @see smilehouse.opensyncro.pipes.component.SourceIF#give(smilehouse.opensyncro.pipes.metadata.SourceInfo, smilehouse.opensyncro.pipes.log.MessageLogger)
     */
    public String[] give(SourceInfo info, MessageLogger logger) throws FailTransferException,
            AbortTransferException {
        try {
            String block =  chopper.getNext(blockSize);
            if(block != null)
            logger.logMessage("Read "+block.length()+" characters.", this, MessageLogger.DEBUG);
            return new String[] { block };
        }
        catch(ParseException pe) {
            logger.logMessage("Error while reading the source XML. "+pe.getMessage(), this, MessageLogger.ERROR);
            Environment.getInstance().log("Error while reading the source XML.", pe);
            PipeComponentUtils.failTransfer();
        }
        return null;
    }

    public File getFile(File directory, MessageLogger logger) throws FailTransferException {

        // Prefix
        String fileName = this.data.getAttribute(PREFIX_ATTR);
        if(fileName == null)
            fileName = "";
        // Date
        String dateFormatStr = this.data.getAttribute(DATEFORMAT_ATTR);
        if(dateFormatStr != null && dateFormatStr.length() != 0) {
            try {
                DateFormat dFormat = new SimpleDateFormat(dateFormatStr);
                fileName += dFormat.format(new Date());

            } catch(IllegalArgumentException ex) {
                logger.logMessage(
                    "Date format syntax error, should conform to java.text.SimpleDateFormat",
                    this,
                    MessageLogger.ERROR);
                PipeComponentUtils.failTransfer();
            }
        }
        // Extension
        String extension = this.data.getAttribute(EXTENSION_ATTR);
        if(extension != null && extension.length() > 0)
            fileName += "." + extension;

        if(directory != null)
            return new File(directory, fileName);
        else
            return new File(fileName);
    }
    
    
    /**
     * @see smilehouse.opensyncro.pipes.component.SourceIF#open(smilehouse.opensyncro.pipes.metadata.SourceInfo, smilehouse.opensyncro.pipes.log.MessageLogger)
     */
    public int open(SourceInfo info, MessageLogger logger) throws FailTransferException,
            AbortTransferException {
        
        // ------------------
        // Get the attributes
        // ------------------
        
        // Directory
        String dirName = data.getNonNullAttribute(DIRECTORY_ATTR, logger, "Directory not set!", MessageLogger.ERROR);
        File dir = new File(dirName);
        if(!dir.exists()) {
            logger.logMessage("Directory \""+dirName+"\" does not exist.", this, MessageLogger.ERROR);
            return ITERATION_OPEN_STATUS_ERROR;
        }
        // File
        File file = getFile(dir, logger);
        if(!file.exists()) {
            logger.logMessage("\""+file.getAbsolutePath()+" not found", this, MessageLogger.WARNING);
            PipeComponentUtils.abortTransfer();
        }
        if(!file.isFile()) {
            logger.logMessage("\""+file.getAbsolutePath()+"\" is not a file.", this, MessageLogger.ERROR);
            return ITERATION_OPEN_STATUS_ERROR;
        }
        if(!file.canRead()) {
            logger.logMessage("Cannot read from file \""+file.getAbsolutePath()+"\".", this, MessageLogger.ERROR);
            return ITERATION_OPEN_STATUS_ERROR;
        }        
        // Block size
        try {
            blockSize = getBlockSize();
        }
        catch(NumberFormatException nfe) {
            logger.logMessage("Invalid block size.", this, MessageLogger.ERROR);
            return ITERATION_OPEN_STATUS_ERROR;
        }        
        if(blockSize < 1) {
            logger.logMessage("Block size must be at least 1.", this, MessageLogger.ERROR);
            return ITERATION_OPEN_STATUS_ERROR;
        }        
        // Chop depth
        int chopDepth = 1;
        try {
            chopDepth = getChopDepth();
        }
        catch(NumberFormatException nfe) {
            logger.logMessage("Invalid chop depth.", this, MessageLogger.ERROR);
            return ITERATION_OPEN_STATUS_ERROR;
        }        
        if(chopDepth < 0) {
            logger.logMessage("Chop depth must be at least 0.", this, MessageLogger.ERROR);
            return ITERATION_OPEN_STATUS_ERROR;
        }        
        // Encoding
        String charsetName = this.data.getAttribute(CHARSET_ATTR);
        if(charsetName == null || charsetName.length() == 0)
            charsetName = DEFAULT_CHARSET;
        
        // ----------------------------------------
        // Open the file and initialize the chopper
        // ----------------------------------------
        try {
            logger.logMessage("Opening file "+file.getPath()+" for reading.", this, MessageLogger.DEBUG);
            in = new FileInputStream(file);
            InputStreamReader reader = new InputStreamReader(in, charsetName);
            chopper = new XMLChopper(reader, chopDepth);
        }
        catch(UnsupportedEncodingException uee) {
            logger.logMessage("Unsupported encoding: " + charsetName, this, MessageLogger.ERROR);
            return ITERATION_OPEN_STATUS_ERROR;
        }
        catch(FileNotFoundException fnfe) {
            logger.logMessage("File not found: " + file.getAbsolutePath(), this, MessageLogger.ERROR);
            return ITERATION_OPEN_STATUS_ERROR;
        }
        
        return ITERATION_OPEN_STATUS_OK;
    }

    
    
    /**
     * @see smilehouse.opensyncro.pipes.component.SourceIF#close(smilehouse.opensyncro.pipes.metadata.SourceInfo, smilehouse.opensyncro.pipes.log.MessageLogger)
     */
    public int close(SourceInfo info, MessageLogger logger) throws FailTransferException,
    	AbortTransferException {
        
        if(in != null) {
            try {
                in.close();
            }
            catch(IOException ioe) {
                String message = "IOException while trying to close the source file.";
                logger.logMessage(message, this, MessageLogger.ERROR);
                Environment.getInstance().log(message, ioe);
                return ITERATION_CLOSE_STATUS_ERROR;
            }
        }
        return ITERATION_CLOSE_STATUS_OK;
    }

    /**
     * @see smilehouse.opensyncro.pipes.component.SourceIF#lastBlockStatus(int)
     */
    public void lastBlockStatus(int statusCode) {
        // We are not interested...
    }

    /**
     * @see smilehouse.opensyncro.pipes.gui.GUIConfigurationIF#getGUIContext()
     */
    public GUIContext getGUIContext() {
        return gui.getGUIContext();
    }

    /* (non-Javadoc)
     * @see smilehouse.opensyncro.pipes.gui.GUIConfigurationIF#getGUITemplate()
     */
    public String getGUITemplate() {
        return "<table border=0 cellspacing=5>" +
        "<tr><td colspan=\"3\">$" + DIRECTORY_ATTR + "$</td></tr>" +
        "<tr><td>$" + PREFIX_ATTR + "$</td><td>$" + DATEFORMAT_ATTR + "$</td><td>$" + EXTENSION_ATTR + "$</td></tr>" +
        "<tr><td>$" + CHARSET_ATTR + "$</td><td>$" + BLOCK_SIZE_ATTR + "$</td><td>$" + CHOP_DEPTH_ATTR + "$</td></tr>" +
        "</table>";
    }

    /* (non-Javadoc)
     * @see smilehouse.opensyncro.pipes.component.PipeComponentIF#setData(smilehouse.opensyncro.pipes.component.PipeComponentData)
     */
    public void setData(PipeComponentData data) {
    	this.data = data;

    }

    /* (non-Javadoc)
     * @see smilehouse.opensyncro.pipes.component.PipeComponentIF#getData()
     */
    public PipeComponentData getData() {
        return this.data;
    }

    /* (non-Javadoc)
     * @see smilehouse.opensyncro.pipes.component.PipeComponentIF#getID()
     */
    public String getID() {
        return this.getClass().getName();
    }

    /* (non-Javadoc)
     * @see smilehouse.opensyncro.pipes.component.PipeComponentIF#getDescription(java.util.Locale)
     */
    public String getDescription(Locale locale) {
        return PipeComponentUtils.getDescription(locale, this.getClass());
    }

    /* (non-Javadoc)
     * @see smilehouse.opensyncro.pipes.component.PipeComponentIF#getName()
     */
    public String getName() {
        return "IteratingXMLFileSource";
    }

    /* (non-Javadoc)
     * @see smilehouse.opensyncro.pipes.component.PipeComponentIF#getType()
     */
    public int getType() {
        return TYPE_SOURCE;
    }

}
