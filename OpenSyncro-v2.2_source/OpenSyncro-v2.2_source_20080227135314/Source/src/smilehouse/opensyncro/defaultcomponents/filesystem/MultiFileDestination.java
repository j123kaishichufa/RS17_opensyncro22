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

import smilehouse.gui.html.fieldbased.FieldInfo;
import smilehouse.gui.html.fieldbased.GUIContext;
import smilehouse.gui.html.fieldbased.editor.TextEditor;
import smilehouse.gui.html.fieldbased.model.DefaultModelModifier;
import smilehouse.gui.html.fieldbased.model.ModelModifier;
import smilehouse.opensyncro.pipes.component.AbortTransferException;
import smilehouse.opensyncro.pipes.component.FailTransferException;
import smilehouse.opensyncro.pipes.component.PipeComponentUtils;
import smilehouse.opensyncro.pipes.gui.GUIConfigurationIF;
import smilehouse.opensyncro.pipes.gui.GUIDefinition;
import smilehouse.opensyncro.pipes.log.MessageLogger;
import smilehouse.opensyncro.pipes.metadata.DestinationInfo;
import smilehouse.opensyncro.system.Environment;

/**
 * MultiFileDestination
 * 
 * Created on Aug 4, 2005
 */

public class MultiFileDestination extends LocalFileDestination implements GUIConfigurationIF {

    private static final int DEFAULT_COUNTERSTARTVALUE = 1;

    private static final String PREFIX_ATTR = "prefix";
    private static final String EXTENSION_ATTR = "extension";
    private static final String COUNTERSTARTVALUE_ATTR = "counterstartvalue";

    private int counterValue = DEFAULT_COUNTERSTARTVALUE;
    
    // --------------
    // GUI definition
    // --------------
    private static MultiFileDestinationGUI tsf_gui = new MultiFileDestinationGUI();


    private static class MultiFileDestinationGUI extends GUIDefinition {

        public MultiFileDestinationGUI() {
            try {
                addField(DIRECTORY_ATTR, LocalFileDestination.gui.getField(DIRECTORY_ATTR));
                addField(APPEND_ATTR, LocalFileDestination.gui.getField(APPEND_ATTR));
                addField(CHARSET_ATTR, LocalFileDestination.gui.getField(CHARSET_ATTR));
                {
                    //set unique id and description labelkey
                    String id = PREFIX_ATTR;

                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            String value = ((MultiFileDestination) model)
                                .data.getAttribute(PREFIX_ATTR);
                            return value != null ? value : "";
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            ((MultiFileDestination) model).data.setAttribute(
                                PREFIX_ATTR,
                                (String) value);
                        }
                    };

                    TextEditor editor = new TextEditor();
                    editor.setSize(30);

                    //and finally create the configurationObject
                    FieldInfo fieldInfo = new FieldInfo(id, id, modifier, editor);

                    //add the configuration to the context for usage in the http-requests.
                    addField(id, fieldInfo);
                }
                {
                    //set unique id and description labelkey
                    String id = COUNTERSTARTVALUE_ATTR;

                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            String value = ((MultiFileDestination) model)
                                .data.getAttribute(COUNTERSTARTVALUE_ATTR);
                            return value != null ? value : "";
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            ((MultiFileDestination) model).data.setAttribute(
                                COUNTERSTARTVALUE_ATTR,
                                (String) value);
                        }
                    };

                    TextEditor editor = new TextEditor();
                    editor.setSize(20);

                    //and finally create the configurationObject
                    FieldInfo fieldInfo = new FieldInfo(id, id, modifier, editor);

                    //add the configuration to the context for usage in the http-requests.
                    addField(id, fieldInfo);
                }
                {
                    //set unique id and description labelkey
                    String id = EXTENSION_ATTR;

                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            String value = ((MultiFileDestination) model)
                                .data.getAttribute(EXTENSION_ATTR);
                            return value != null ? value : "";
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            ((MultiFileDestination) model).data.setAttribute(
                                EXTENSION_ATTR,
                                (String) value);
                        }
                    };

                    TextEditor editor = new TextEditor();
                    editor.setSize(10);

                    //and finally create the configurationObject
                    FieldInfo fieldInfo = new FieldInfo(id, id, modifier, editor);

                    //add the configuration to the context for usage in the http-requests.
                    addField(id, fieldInfo);
                }
            } catch(Exception e) {
                Environment.getInstance().log(
                    "Couldn't create GUIContext for MultiFileDestination", e);
            }
        }
    }

    // ---
 
    
    public MultiFileDestination( Object pipeComponentData ) {
        super( pipeComponentData );
    }
    
    public String getName() {
        return "MultiFileDestination";
    }

    public String getID() {
        return "smilehouse.opensyncro.defaultcomponents.filesystem.MultiFileDestination";
    }
    
    // ---
    

    public int open(DestinationInfo info, MessageLogger logger) throws FailTransferException {
        String counterStartValue = this.data.getAttribute(COUNTERSTARTVALUE_ATTR);
 
        this.directory = getDirectory(logger);
        if(this.directory == null) {
            logger.logMessage(
                "No directory specified. Aborting",
                this,
                MessageLogger.ERROR);
            PipeComponentUtils.failTransfer();
        }

        if(counterStartValue == null || counterStartValue.length() == 0) {
            logger.logMessage(
                "No counter start value specified. Aborting",
                this,
                MessageLogger.ERROR);
            PipeComponentUtils.failTransfer();
        }
        
        try {
            this.counterValue = Integer.valueOf(this.data.getAttribute(COUNTERSTARTVALUE_ATTR)).intValue();
        } catch(NumberFormatException e) {
            logger.logMessage(
                "Invalid counter start value format, should be integer. Aborting",
                this,
                MessageLogger.ERROR);
            PipeComponentUtils.failTransfer();
        }
        
        //We will call getFile at the beginning of each take()
        //this.file = getFile(directory, logger);

        this.singleFileMode = false;
        
        return ITERATION_OPEN_STATUS_OK;
        
    }
    
    public int close(DestinationInfo info, MessageLogger logger) throws FailTransferException {
        return ITERATION_CLOSE_STATUS_OK;
    }
    

    public void take(String data, DestinationInfo info, MessageLogger logger)
    	throws FailTransferException, AbortTransferException {
        this.file = getFile(directory, logger);
        super.take(data, info, logger);
    }
    
    public File getFile(File directory, MessageLogger logger) throws FailTransferException {
        
        String fileName = this.data.getAttribute(PREFIX_ATTR);
        if(fileName == null)
            fileName = "";

        fileName += this.counterValue;
        this.counterValue++;
        
        String extension = this.data.getAttribute(EXTENSION_ATTR);
        if(extension != null && extension.length() > 0)
            fileName += "." + extension;

        if(directory != null)
            return new File(directory, fileName);
        else
            return new File(fileName);

    }

    public GUIContext getGUIContext() {
        return tsf_gui.getGUIContext();
    }


    public String getGUITemplate() {
        return "<table border=0 cellspacing=5><tr><td colspan=\"4\">$" + DIRECTORY_ATTR
                + "$</td></tr>" + "<tr><td>$" + PREFIX_ATTR + "$</td><td>$" + COUNTERSTARTVALUE_ATTR
                + "$</td><td>$" + EXTENSION_ATTR + "$</td><td>$" + APPEND_ATTR + "$</td></tr>"
                + "<tr><td colspan=\"4\">$" + CHARSET_ATTR + "$</td></tr></table>";
    }
}