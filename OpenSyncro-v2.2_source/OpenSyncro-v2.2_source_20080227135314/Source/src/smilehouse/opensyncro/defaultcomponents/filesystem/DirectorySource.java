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
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import smilehouse.gui.html.fieldbased.FieldInfo;
import smilehouse.gui.html.fieldbased.GUIContext;
import smilehouse.gui.html.fieldbased.editor.DefaultSelectOption;
import smilehouse.gui.html.fieldbased.editor.SelectEditor;
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

/**
 * DirectorySource.java
 *
 * Reads all files from a specified directory. 
 *  
 * TODO: Recursive directory scan (include subdirectories)
 * 
 * Created: Thu Aug 11 10:39:18 2005
 */

public class DirectorySource implements SourceIF, GUIConfigurationIF {

    protected static final String FILENAMEREGEXP_ATTR = "filenameregexp";
    protected static final String CHARSET_ATTR = "charset";
    protected static final String DIRECTORY_ATTR = "directory";

    protected static final String DEFAULT_CHARSET = "UTF-8";

    protected static final int FILE_READBUFFER_SIZE = 1024;

    private static final String[] CHARSETS = PipeComponentUtils.getCharacterSetArray();

   
    // DirectorySource component parameters provided by the OpenSyncro framework
    private PipeComponentData data;

    private List fileNameList; 
    private Iterator fileNameListIterator;
    
    
    
    // --------------
    // GUI definition
    // --------------
    protected static DirectorySourceGUI gui = new DirectorySourceGUI();

    protected static class DirectorySourceGUI extends GUIDefinition {

        public DirectorySourceGUI() {
            try {
                addSimpleTextFieldForComponent(DIRECTORY_ATTR, DIRECTORY_ATTR, 70);
                {
                    //set unique id and description labelkey
                    String id = FILENAMEREGEXP_ATTR;
                    addSimpleTextFieldForComponent(id, id, 40);
                }
                {
                    //set unique id and description labelkey
                    String id = CHARSET_ATTR;

                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws FailTransferException,
                                AbortTransferException {
                            String value = ((DirectorySource) model).getData().getAttribute(
                                CHARSET_ATTR);
                            return value != null ? value : DEFAULT_CHARSET;
                        }

                        public void setModelValue(Object model, Object value)
                                throws FailTransferException, AbortTransferException {
                            ((DirectorySource) model).getData().setAttribute(
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
                    "Couldn't create GUIContext for DirectorySource", e);
            }
        }
    }

    public DirectorySource(Object pipeComponentData) {
        setData((PipeComponentData) pipeComponentData);
    }

    public void setData(PipeComponentData data) {
        this.data = data;
    }

    public PipeComponentData getData() {
        return this.data;
    }

    public final int getType() {
        return TYPE_SOURCE;
    }

    public String getName() {
        return "DirectorySource";
    }

    public String getID() {
        return "smilehouse.opensyncro.defaultcomponents.filesystem.DirectorySource";
    }

    public String getDescription(Locale locale) {
        return PipeComponentUtils.getDescription(locale, this.getClass());
    }

    /** Open iteration session: prepare a list of filenames and initialize filename Iterator */
    public int open(SourceInfo info, MessageLogger logger) throws FailTransferException,
    AbortTransferException {
        
        File directory = getDirectory(logger);

        if(testDirectory(directory, logger) == false) {
            PipeComponentUtils.failTransfer();
        }

        String fileNameRegExp = getFileNameRegExp(logger);
        
        this.fileNameList = getFileNameList(directory, fileNameRegExp);

        initializeFileNameListIterator();
        
        return ITERATION_OPEN_STATUS_OK;
    }

    public int close(SourceInfo info, MessageLogger logger) throws FailTransferException {

        this.fileNameListIterator = null;
        this.fileNameList = null;
        
        return ITERATION_CLOSE_STATUS_OK;
    }

    /**
     * Tests that a File object is (not-null) readable directory. Writes
     * error messages to Transfer Log and returns false if any of the tests
     * fail. 
     * 
     * @param logger
     * @param directory
     * @return true if File object passed all tests, otherwise false 
     */
    private boolean testDirectory(File directory, MessageLogger logger) {
        if(directory != null) {
            if(!directory.exists()) {
                logger.logMessage(
                    "Directory \"" + directory.getPath() + "\" does not exist",
                    this,
                    MessageLogger.ERROR);
                return false;
            }

            if(!directory.isDirectory()) {
                logger.logMessage(
                    "\"" + directory.getPath() + "\" is not a directory!",
                    this,
                    MessageLogger.ERROR);
                return false;
            }
            
            if(!directory.canRead()) {
                logger.logMessage(
                    "Can't read directory \"" + directory.getPath() + "\"",
                    this,
                    MessageLogger.ERROR);
                return false;
            }
        } else {
            logger.logMessage(
                "Directory not specified",
                this,
                MessageLogger.ERROR);
            return false;
        }
        return true;
    }
    
    public void lastBlockStatus(int statusCode) {}

    // ---
    
    public File getDirectory(MessageLogger logger) throws FailTransferException,
            AbortTransferException {
        String directoryName = this.data.getAttribute(DIRECTORY_ATTR);
        if(directoryName == null || directoryName.length() == 0) {
            return null;
        } else {
            return new File(directoryName);
        }
    }

    public String getFileNameRegExp(MessageLogger logger) throws FailTransferException,
    AbortTransferException {
        String fileNameRegExp = this.data.getAttribute(FILENAMEREGEXP_ATTR);
        /* If regular expression is not specified, return a match-all pattern */
        if(fileNameRegExp == null || fileNameRegExp.length() == 0) {
            return ".*";
        } else {
            return fileNameRegExp;
        }
}

    /* FilenameFilter class used to select files by matching a pre-compiled
     * Java Regular Expression Pattern against the filename
     */
    private class RegExpFilter implements FilenameFilter {
        
        private Pattern pattern;  
        
        public RegExpFilter(String regExp) {
            this.pattern = Pattern.compile(regExp);
        }
        
        public boolean accept(File dir, String name) {
            File file = new File(dir, name);
            if(file.isDirectory()) {
                // Skip directories
                return false;
            } else {
                // Match filename with a regular expression
                Matcher m = pattern.matcher(name);
                return m.matches();
            }
        }
    }

    private List getFileNameList(File directory, String fileNameRegExp) {

        String[] fileNames = directory.list(new RegExpFilter(fileNameRegExp));

        // Insert full path before each filename
        for( int i = 0; i < fileNames.length; i++ ) {
            //System.out.println(directory.toString() + File.separatorChar + fileNames[i]);
            fileNames[i] = directory.toString() + File.separatorChar + fileNames[i];
        }

        return Arrays.asList(fileNames);
        
    }

    private void initializeFileNameListIterator() {
        this.fileNameListIterator = this.fileNameList.iterator();
    }
    
    private String getNextFileName() {
        String nextFileName;
        if(this.fileNameListIterator.hasNext()) {
            return (String) this.fileNameListIterator.next();
        } else {
            return null;
        }
    }
    
    public String[] give(SourceInfo info, MessageLogger logger) throws FailTransferException,
            AbortTransferException {

        String fileName = getNextFileName();
        
        if(fileName == null) {
            // No more files to read, we're done
            return null;
        }

        //logger.logMessage("Opening file \"" + fileName + "\"", this, MessageLogger.DEBUG);
        File file = new File(fileName);
        
        if(!file.exists()) {
            logger.logMessage(
                "File \"" + file.getPath() + "\" does not exist",
                this,
                MessageLogger.ERROR);
            
            PipeComponentUtils.failTransfer();
        }

        if(!file.canRead()) {
            logger
                .logMessage("Can't read file \"" + file.getPath() + "\"", this, MessageLogger.ERROR);
            PipeComponentUtils.failTransfer();
        }

        if(!file.isFile()) {
            logger.logMessage("\"" + file.getPath() + "\" is not a file!", this, MessageLogger.ERROR);
            PipeComponentUtils.failTransfer();
        }

        String charsetName = this.data.getAttribute(CHARSET_ATTR);
        if(charsetName == null || charsetName.length() == 0)
            charsetName = DEFAULT_CHARSET;

        StringBuffer strBuf = null;
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            InputStreamReader reader = new InputStreamReader(in, charsetName);

            char[] buffer = new char[FILE_READBUFFER_SIZE];
            int read = 0;
            strBuf = new StringBuffer();
            while((read = reader.read(buffer)) != -1) {
                strBuf.append(buffer, 0, read);
            }
            logger.logMessage("Read " + strBuf.length() + " characters from file '"
                    + file.getPath() + "'", this, MessageLogger.DEBUG);

        } catch(UnsupportedEncodingException e) {
            
            logger.logMessage(
                "Unsupported encoding: " + charsetName + ", aborting",
                this,
                MessageLogger.ERROR);
            PipeComponentUtils.failTransfer();
            
        } catch(IOException e) {
            
            logger.logMessage("IOException while reading from source file \"" + file.getPath()
                + "\", aborting", this, MessageLogger.ERROR);
            PipeComponentUtils.failTransfer();
            
        } finally {
            if(in != null)
                try {
                    in.close();
                } catch(IOException e) {
                    logger.logMessage("IOException while closing source file \""
                            + file.getPath() + "\", aborting", this, MessageLogger.ERROR);
                    PipeComponentUtils.failTransfer();
                }
        }
        return new String[] {strBuf.toString()};
    }


    public GUIContext getGUIContext() {
        return gui.getGUIContext();
    }

    public String getGUITemplate() {
        return "<table border=0 cellspacing=5><tr><td colspan=\"2\">$" + DIRECTORY_ATTR
                + "$</td></tr>" + "<tr><td>$" + FILENAMEREGEXP_ATTR + "$</td><td>$" + CHARSET_ATTR
                + "$</td></tr></table>";
    }

}// DirectorySource
