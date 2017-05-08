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
 * Created on 17.10.2005
 */
package smilehouse.opensyncro.defaultcomponents.filesystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
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
import smilehouse.util.Utils;

/**
 * This component is like LocalFileSource, except it does not read all the data into memory at once.
 * Instead it reads the file in multiple blocks thus enabling iterative processing of arbitrary
 * large files. <br><br>
 * 
 * The following attributes are used for splitting the input:
 * <ul>
 *  <li><b>Delimiter</b>. The delimiter that is used to divide the file into components. For example this could be "\n" for a CSV file,
 * 	                      in which case the component would be one row. 
 *                        The delimiter can also be a String, for example "\nNEXT\n".</li>
 *  <li><b>Block size</b>. Block size tells how many pieces one iteration block contains in maximum</li>
 *  <li><b>Quote char</b>. If the quote char is defined, delimiters inside quoted regions are ignored.</li>
 *  <li><b>Escape char</b>. If the escape char is defined, quote chars preceded by (unescaped) escape char are ignored.</li>
 * </ul>
 * The java style escape sequences '\n', '\r', '\f', '\t', '\\' can by used in the delimiter and as quote and escape chars.
 * 
 */
public class IteratingFileSource implements SourceIF, GUIConfigurationIF {

    // ---------------
    // Attribute names
    // ---------------
    private static final String DIR_ATTR  = "dir";
    private static final String FILE_ATTR = "file";
    private static final String CHARSET_ATTR = "charset";

    private static final String DELIMITER_ATTR = "delim";
    private static final String QUOTE_CHAR_ATTR = "quote";
    private static final String ESCAPE_CHAR_ATTR = "escape";
    private static final String BLOCK_SIZE_ATTR = "size";

    private static final String[] CHARSETS = PipeComponentUtils.getCharacterSetArray();

    protected static final String DEFAULT_CHARSET = "UTF-8";
    
    private Splitter splitter;
    private Reader reader;
    protected PipeComponentData data;
    
    //  --------------
    // GUI definition
    // --------------
    protected static GUI gui = new GUI();

    protected static class GUI extends GUIDefinition {

        public GUI() {
            try {
                // ------------------
                // Simple text fields
                // ------------------
                addSimpleTextFieldForComponent(DIR_ATTR, DIR_ATTR, 70);
                addSimpleTextFieldForComponent(FILE_ATTR, FILE_ATTR, 20);
                addSimpleTextFieldForComponent(DELIMITER_ATTR, DELIMITER_ATTR, 10);
                addSimpleTextFieldForComponent(QUOTE_CHAR_ATTR, QUOTE_CHAR_ATTR, 2);
                addSimpleTextFieldForComponent(ESCAPE_CHAR_ATTR, ESCAPE_CHAR_ATTR, 2);
                {
                    // --------------
                    // Iteration size
                    // --------------
                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            return new Integer( ((IteratingFileSource) model).getBlockSize() );
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            ((IteratingFileSource) model).setBlockSize( ((Integer) value).intValue() );
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
                    // --------------
                    // Charset select
                    // --------------
                    String id = CHARSET_ATTR;
                    
                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            return ((IteratingFileSource) model).getCharset();
                        }
                        
                        public void setModelValue(Object model, Object value) throws Exception {
                            ((IteratingFileSource) model).getData().setAttribute(
                                CHARSET_ATTR,
                                (String) value);
                        }
                    };
                    
                    SelectEditor editor = new SelectEditor();
                    for(int i = 0; i < CHARSETS.length; i++)
                        editor.addOption(new DefaultSelectOption(CHARSETS[i], CHARSETS[i]));
                    
                    FieldInfo fieldInfo = new FieldInfo(id, id, modifier, editor);
                    
                    addField(id, fieldInfo);
                }
            } catch(Exception e) {
                Environment.getInstance().log(
                    "Couldn't create GUIContext for IteratingFileSource", e);
            }
        }

    }
    
    public IteratingFileSource(Object pipeComponentData) {
        setData((PipeComponentData) pipeComponentData);
    }
    
    private int getBlockSize() throws NumberFormatException {
        Integer size = getData().getIntegerAttribute(BLOCK_SIZE_ATTR);
        return size != null ? size.intValue() : 1;    
    }
    
    private void setBlockSize(int blockSize) {
        getData().setAttribute(BLOCK_SIZE_ATTR, blockSize);
    }
    
    private String getCharset() {
        String charset = getData().getAttribute(CHARSET_ATTR);
        return charset != null && charset.length() > 0 ? charset : DEFAULT_CHARSET;
    }
    
    /**
     * @see smilehouse.opensyncro.pipes.component.SourceIF#give(smilehouse.opensyncro.pipes.metadata.SourceInfo, smilehouse.opensyncro.pipes.log.MessageLogger)
     */
    public String[] give(SourceInfo info, MessageLogger logger) throws FailTransferException,
            AbortTransferException {
        try {
            String block = splitter.getNextBlock();
            String debugMessage = block != null
                    ? "Read "+block.length()+" characters."
                    : "No more data.";
            logger.logMessage(debugMessage, this, MessageLogger.DEBUG);
            return new String[] { block };
        }
        catch(IOException ioe) {
            String message = "IOException while reading the file!";
            logger.logMessage(message, this, MessageLogger.ERROR);
            Environment.getInstance().log(message, ioe);
            throw new AbortTransferException();
        }
    }

    /**
     * Opens the file for reading and initializes the splitter.
     * 
     * @see smilehouse.opensyncro.pipes.component.SourceIF#open(smilehouse.opensyncro.pipes.metadata.SourceInfo, smilehouse.opensyncro.pipes.log.MessageLogger)
     */
    public int open(SourceInfo info, MessageLogger logger) throws FailTransferException,
            AbortTransferException {
        // -------------
        // Check the dir
        // -------------
        String dirName = getData().getNonNullAttribute(DIR_ATTR, logger, "Directory name not set!", MessageLogger.ERROR);
        File dir = new File(dirName);
        if(!dir.exists()) {
            logger.logMessage("Directory '"+dirName+"' does not exist!", this, MessageLogger.ERROR);
            return ITERATION_OPEN_STATUS_ERROR;
        }
        if(!dir.isDirectory()) {
            logger.logMessage("'"+dirName+"' is not a directory!", this, MessageLogger.ERROR);
            return ITERATION_OPEN_STATUS_ERROR;
        }
        // --------------
        // Check the file
        // --------------
        String fileName = getData().getNonNullAttribute(FILE_ATTR, logger, "File name not set!", MessageLogger.ERROR);
        File file = new File(dir, fileName);
        if(!file.exists()) {
            logger.logMessage("Directory '"+fileName+"' does not exist!", this, MessageLogger.ERROR);
            return ITERATION_OPEN_STATUS_ERROR;
        }
        if(file.isDirectory()) {
            logger.logMessage("'"+fileName+"' is a directory!", this, MessageLogger.ERROR);
            return ITERATION_OPEN_STATUS_ERROR;
        }
        if(!file.canRead()) {
            logger.logMessage("Cannot read the file'"+fileName+"'!", this, MessageLogger.ERROR);
            return ITERATION_OPEN_STATUS_ERROR;            
        }        
        // -----------------
        // Get the delimiter
        // -----------------
        String delimiter = getData().getAttribute(DELIMITER_ATTR);
        if(delimiter == null || delimiter.length() == 0) {
            logger.logMessage("Delimiter not set!", this, MessageLogger.ERROR);
            return ITERATION_OPEN_STATUS_ERROR;
        }        
        // ------------------
        // Get the block size
        // ------------------
        int blockSize = 1;
        String blockSizeStr = getData().getAttribute(BLOCK_SIZE_ATTR);
        if(blockSizeStr == null || blockSizeStr.length() == 0) {
            logger.logMessage("Block size not given, using 1", this, MessageLogger.WARNING);
        }
        else {
            try {
                blockSize = Integer.parseInt(blockSizeStr);
            }
            catch(NumberFormatException nfe) {
                logger.logMessage("Block size '"+blockSizeStr+"' not a valid integer, using 1", this, MessageLogger.WARNING);
            }
            if(blockSize < 1) {
                blockSize = 1;
                logger.logMessage("Invalid block size, using 1", this, MessageLogger.WARNING);
            }
        }
        // ----------------------------------------
        // Open the file and configure the splitter
        // ----------------------------------------
        String encoding = getCharset();
        try {
            reader = new InputStreamReader(new FileInputStream(file), encoding);
            this.splitter = new Splitter(reader, Utils.javaStyleUnescape(delimiter));
            // Quote char
            String quoteStr = getData().getAttribute(QUOTE_CHAR_ATTR);
            if(quoteStr != null) { 
                quoteStr = Utils.javaStyleUnescape(quoteStr);
                if(quoteStr.length() > 0)
                    splitter.setQuoteChar( new Character(quoteStr.charAt(0)) );
            }
            // Escape char
            String escapeStr = getData().getAttribute(ESCAPE_CHAR_ATTR);
            if(escapeStr != null) {
                escapeStr = Utils.javaStyleUnescape(escapeStr);
                if(escapeStr.length() > 0)
                    splitter.setEscapeChar( new Character(escapeStr.charAt(0)) );
            }
            splitter.setBlockSize(blockSize);

            return ITERATION_CLOSE_STATUS_OK;
        }
        catch(FileNotFoundException fnfe) {
            logger.logMessage("File not found: " + file.getAbsolutePath(), this, MessageLogger.ERROR);
            return ITERATION_OPEN_STATUS_ERROR;
        }
        catch(UnsupportedEncodingException uee) {
            logger.logMessage("Unsupported encoding: " + encoding, this, MessageLogger.ERROR);
            return ITERATION_OPEN_STATUS_ERROR;
        }
        
    }

    /**
     * Closes the file.
     * 
     * @see smilehouse.opensyncro.pipes.component.SourceIF#close(smilehouse.opensyncro.pipes.metadata.SourceInfo, smilehouse.opensyncro.pipes.log.MessageLogger)
     */
    public int close(SourceInfo info, MessageLogger logger) throws FailTransferException,
            AbortTransferException {
        if(reader != null) {
            try {
                reader.close();
                reader = null;
            }
            catch(IOException ioe) {
                String message = "IOException while trying to close the reader";
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
    public void lastBlockStatus(int statusCode) throws FailTransferException, AbortTransferException {
        // Not much use for this...
    }

    /**
     * @see smilehouse.opensyncro.pipes.gui.GUIConfigurationIF#getGUIContext()
     */
    public GUIContext getGUIContext() {
        return gui.getGUIContext();
    }

    /**
     * @see smilehouse.opensyncro.pipes.gui.GUIConfigurationIF#getGUITemplate()
     */
    public String getGUITemplate() {
        return "<table>"
        	+"<tr><td colspan=\"2\">$"+DIR_ATTR+"$</td><td>$"+FILE_ATTR+"$</td></tr>" 
        	+"<tr><td>$"+DELIMITER_ATTR+"$</td><td>$"+QUOTE_CHAR_ATTR+"$</td><td>$"+ESCAPE_CHAR_ATTR+"$</td>"
        	+"<td>$"+BLOCK_SIZE_ATTR+"$</td></tr>"
        	+"<tr><td colspan=\"3\">$"+CHARSET_ATTR+"$</td></tr>"
        	+"</table>";
    }

    /**
     * @see smilehouse.opensyncro.pipes.component.PipeComponentIF#setData(smilehouse.opensyncro.pipes.component.PipeComponentData)
     */
    public void setData(PipeComponentData data) {
        this.data = data;
    }

    /**
     * @see smilehouse.opensyncro.pipes.component.PipeComponentIF#getData()
     */
    public PipeComponentData getData() {
        return data;
    }
    



    /**
     * @see smilehouse.opensyncro.pipes.component.PipeComponentIF#getID()
     */
    public String getID() {
        return this.getClass().getName();
    }

    /**
     * @see smilehouse.opensyncro.pipes.component.PipeComponentIF#getDescription(java.util.Locale)
     */
    public String getDescription(Locale locale) {
        return PipeComponentUtils.getDescription(locale, this.getClass());
    }

    /**
     * @see smilehouse.opensyncro.pipes.component.PipeComponentIF#getName()
     */
    public String getName() {
        return "IteratingFileSource";
    }

    /**
     * @see smilehouse.opensyncro.pipes.component.PipeComponentIF#getType()
     */
    public int getType() {
        return TYPE_SOURCE;
    }
 
    
}

class Splitter {
    
    private Reader in;
    private char[] delimiter;
    /** An array for saving information on matches.
     *  If lastMatched[i] == true, then i+1 first characters of 
     *  the searched string (delimiter) match the i+1 last read characters.
     *  Actually the algorithm is just a kind of a variant of bruteforcing
     *  where the characters are read one at a time and matched simultaneously
     *  to all the characters in the searched string.
     */ 
    private boolean[] lastMatched;
    private Character quoteChar;
    private Character escapeChar;
    private int blockSize = 1;
    private boolean stop = false;
    
    
    public Splitter(Reader in, String delim) {
        this.in = in;
        this.delimiter = delim.toCharArray();
        this.lastMatched = new boolean[delimiter.length -1];
    }
    
    public void setEscapeChar(Character escapeChar) {
        this.escapeChar = escapeChar;
    }
    public void setQuoteChar(Character quoteChar) {
        this.quoteChar = quoteChar;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }
    
    public String getNextBlock() throws IOException {
        if(stop)
            return null;
        StringBuffer buffer = new StringBuffer();
        for(int i=0; i<blockSize && !stop; i++)
            readNext(buffer);
        
        return buffer.toString();
    }
    
    private void readNext(StringBuffer buffer) throws IOException {
        for(int i=0; i<lastMatched.length; i++)
            lastMatched[i] = false;
        int c = 0;
        boolean quoted = false;
        boolean escaped = false;
        
        match:
        while( (c = in.read()) != -1) {
            char ch = (char) c;
            buffer.append(ch);
            if(!escaped) {
                // ----------------------------
                // Is this the quote character?
                // ----------------------------
                if(quoteChar != null && c == quoteChar.charValue()) {
                    if(quoted) {
                        // Quote begins
                        quoted = false;
                        for(int i=0; i<lastMatched.length; i++)
                            lastMatched[i] = false;
                    }
                    else {
                        // Quote ends
                        quoted = true;
                    }
                }
                // -----------------------------
                // Is this the escape character?
                // -----------------------------
                if(escapeChar != null && c == escapeChar.charValue())
                    escaped = true;
            }
            else
                escaped = false;
            
            if(!quoted) {
                for(int i=delimiter.length-1; i>=0; i--) {
                    if( (i == 0 || lastMatched[i-1]) && delimiter[i] == c) {
                        if(i<lastMatched.length)
                            lastMatched[i] = true;
                        else
                            break match;
                    }
                    else if(i < lastMatched.length)
                        lastMatched[i] = false;                    
                }
            }
            
        }
        if(c == -1)
            stop = true;                
    }
}
