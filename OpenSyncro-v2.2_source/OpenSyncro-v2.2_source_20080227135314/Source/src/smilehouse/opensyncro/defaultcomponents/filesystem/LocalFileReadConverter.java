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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

import smilehouse.gui.html.fieldbased.FieldInfo;
import smilehouse.gui.html.fieldbased.GUIContext;
import smilehouse.gui.html.fieldbased.editor.BooleanEditor;
import smilehouse.gui.html.fieldbased.editor.DefaultSelectOption;
import smilehouse.gui.html.fieldbased.editor.SelectEditor;
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
 * LocalFileReadConverter.java
 * 
 * Created: Tue Oct 10 14:17:18 2006
 */

public class LocalFileReadConverter implements ConverterIF, GUIConfigurationIF {

    protected static final String FILENAME_ATTR = "filename";
    protected static final String CHARSET_ATTR = "charset";
    protected static final String DIRECTORY_ATTR = "directory";
    protected static final String APPEND_ATTR = "append";

    protected static final String DEFAULT_CHARSET = "UTF-8";

    protected static final int FILE_READBUFFER_SIZE = 1024;

    private static final String[] CHARSETS = PipeComponentUtils.getCharacterSetArray();
    
    // --------------
    // GUI definition
    // --------------
    protected static LocalFileReadConverterGUI gui = new LocalFileReadConverterGUI();

    protected static class LocalFileReadConverterGUI extends GUIDefinition {

        public LocalFileReadConverterGUI() {
            try {
                addSimpleTextFieldForComponent(DIRECTORY_ATTR, DIRECTORY_ATTR, 70);
                {
                    //set unique id and description labelkey
                    String id = FILENAME_ATTR;
                    addSimpleTextFieldForComponent(id, id, 40);
                }
                {

                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            return ((LocalFileReadConverter) model).getAppend();
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            ((LocalFileReadConverter) model).setAppend((Boolean) value);
                        }
                    };

                    BooleanEditor editor = new BooleanEditor();

                    //and finally create the configurationObject
                    FieldInfo fieldInfo = new FieldInfo(APPEND_ATTR, APPEND_ATTR, modifier, editor);

                    //add the configuration to the context for usage in the http-requests.
                    addField(APPEND_ATTR, fieldInfo);
                }
                {
                    //set unique id and description labelkey
                    String id = CHARSET_ATTR;

                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws FailTransferException,
                                AbortTransferException {
                            String value = ((LocalFileReadConverter) model).getData().getAttribute(
                                CHARSET_ATTR);
                            return value != null ? value : DEFAULT_CHARSET;
                        }

                        public void setModelValue(Object model, Object value)
                                throws FailTransferException, AbortTransferException {
                            ((LocalFileReadConverter) model).getData().setAttribute(
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
                    "Couldn't create GUIContext for LocalFileReadConverter", e);
            }
        }
    }

    public LocalFileReadConverter(Object pipeComponentData) {
        setData((PipeComponentData) pipeComponentData);
    }

    protected PipeComponentData data;

    public void setData(PipeComponentData data) {
        this.data = data;
    }

    public PipeComponentData getData() {
        return this.data;
    }

    public final int getType() {
        return TYPE_CONVERTER;
    }

    public String getName() {
        return "LocalFileReadConverter";
    }

    public String getID() {
        return this.getClass().getName();
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

    public void lastBlockStatus(int statusCode) {}

    // ---
    
    public void setAppend(Boolean append) {
        this.getData().setAttribute(APPEND_ATTR, append != null ? append.toString() : "false");
    }

    public Boolean getAppend() {
        return new Boolean(this.getData().getAttribute(APPEND_ATTR));
    }

    public File getFile(File directory, MessageLogger logger) throws FailTransferException,
            AbortTransferException {
        String fileName = this.data.getAttribute(FILENAME_ATTR);
        if(fileName == null || fileName.length() == 0) {
            logger.logMessage("File name is not set!", this, MessageLogger.ERROR);
            PipeComponentUtils.abortTransfer();
        }

        if(directory != null)
            return new File(directory, fileName);
        else
            return new File(fileName);
    }

    public File getDirectory(MessageLogger logger) throws FailTransferException,
            AbortTransferException {
        String directoryName = this.data.getAttribute(DIRECTORY_ATTR);
        if(directoryName == null || directoryName.length() == 0) {
            return null;
        } else {
            return new File(directoryName);
        }
    }
    
    public String[] convertAll(String[] data, ConversionInfo info, MessageLogger logger ) 
    throws FailTransferException, AbortTransferException {
        for(int i = 0; i < data.length; i++) {
            data = convert(data[i], info, logger);
        }
    	return data;
    }

    public String[] convert(String data, ConversionInfo info, MessageLogger logger) throws FailTransferException,
            AbortTransferException {
    

        File directory = getDirectory(logger);

        if(directory != null) {
            if(!directory.exists()) {
                logger.logMessage(
                    "Directory \"" + directory.getPath() + "\" does not exist",
                    this,
                    MessageLogger.ERROR);
                PipeComponentUtils.failTransfer();
            }

            if(!directory.canRead()) {
                logger.logMessage(
                    "Can't read directory \"" + directory.getPath() + "\"",
                    this,
                    MessageLogger.ERROR);
                PipeComponentUtils.failTransfer();
            }

            if(!directory.isDirectory()) {
                logger.logMessage(
                    "\"" + directory.getPath() + "\" is not a directory!",
                    this,
                    MessageLogger.ERROR);
                PipeComponentUtils.failTransfer();
            }
        }

        File file = getFile(directory, logger);

        if(!file.exists()) {
            logger.logMessage(
                "File \"" + file.getPath() + "\" does not exist",
                this,
                MessageLogger.ERROR);
            
            /** We only abort since in many cases it is normal that the file is not there */
            PipeComponentUtils.abortTransfer();
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
            
            if(getAppend().booleanValue())
            	strBuf = new StringBuffer(data);
            else
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
                + "$</td></tr>" + "<tr><td>$" + FILENAME_ATTR + "$</td><td>$" + APPEND_ATTR
                + "$</td></tr>" + "<tr><td colspan=\"2\">$" + CHARSET_ATTR + "$</td></tr></table>";
    }

} // LocalFileReadConverter

