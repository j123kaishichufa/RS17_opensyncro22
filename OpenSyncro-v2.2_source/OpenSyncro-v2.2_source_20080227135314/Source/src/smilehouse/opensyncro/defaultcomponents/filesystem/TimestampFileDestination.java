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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import smilehouse.gui.html.fieldbased.FieldInfo;
import smilehouse.gui.html.fieldbased.GUIContext;
import smilehouse.gui.html.fieldbased.editor.TextEditor;
import smilehouse.gui.html.fieldbased.model.DefaultModelModifier;
import smilehouse.gui.html.fieldbased.model.ModelModifier;
import smilehouse.opensyncro.pipes.component.FailTransferException;
import smilehouse.opensyncro.pipes.component.PipeComponentUtils;
import smilehouse.opensyncro.pipes.gui.GUIConfigurationIF;
import smilehouse.opensyncro.pipes.gui.GUIDefinition;
import smilehouse.opensyncro.pipes.log.MessageLogger;
import smilehouse.opensyncro.system.Environment;

/**
 * TimestampFileDestination
 * 
 * Created: Thu May 27 12:06:58 2004
 */

public class TimestampFileDestination extends LocalFileDestination implements GUIConfigurationIF {

    private static final String DEFAULT_DATEFORMAT = "yyyy-MM-dd";

    private static final String PREFIX_ATTR = "prefix";
    private static final String EXTENSION_ATTR = "extension";
    private static final String DATEFORMAT_ATTR = "dateformat";

    // --------------
    // GUI definition
    // --------------
    private static TimestampFileDestinationGUI tsf_gui = new TimestampFileDestinationGUI();


    private static class TimestampFileDestinationGUI extends GUIDefinition {

        public TimestampFileDestinationGUI() {
            try {
                addField(DIRECTORY_ATTR, LocalFileDestination.gui.getField(DIRECTORY_ATTR));
                addField(APPEND_ATTR, LocalFileDestination.gui.getField(APPEND_ATTR));
                addField(CHARSET_ATTR, LocalFileDestination.gui.getField(CHARSET_ATTR));
                {
                    //set unique id and description labelkey
                    String id = PREFIX_ATTR;

                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            String value = ((TimestampFileDestination) model)
                                .data.getAttribute(PREFIX_ATTR);
                            return value != null ? value : "";
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            ((TimestampFileDestination) model).data.setAttribute(
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
                    String id = DATEFORMAT_ATTR;

                    ModelModifier modifier = new DefaultModelModifier() {
                        public Object getModelValue(Object model) throws Exception {
                            String value = ((TimestampFileDestination) model)
                                .data.getAttribute(DATEFORMAT_ATTR);
                            return value != null ? value : DEFAULT_DATEFORMAT;
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            ((TimestampFileDestination) model).data.setAttribute(
                                DATEFORMAT_ATTR,
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
                            String value = ((TimestampFileDestination) model)
                                .data.getAttribute(EXTENSION_ATTR);
                            return value != null ? value : "";
                        }

                        public void setModelValue(Object model, Object value) throws Exception {
                            ((TimestampFileDestination) model).data.setAttribute(
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
                    "Couldn't create GUIContext for TimestampFileDestination", e);
            }
        }
    }

    // ---
    
    public TimestampFileDestination( Object pipeComponentData ) {
        super( pipeComponentData );
    }
    
    public String getName() {
        return "TimestampFileDestination";
    }

    public String getID() {
        return "smilehouse.opensyncro.defaultcomponents.filesystem.TimestampFileDestination";
    }
    
    // ---
    
    
    public File getFile(File directory, MessageLogger logger) throws FailTransferException {
        String fileName = this.data.getAttribute(PREFIX_ATTR);
        if(fileName == null)
            fileName = "";

        String dateFormatStr = this.data.getAttribute(DATEFORMAT_ATTR);
        if(dateFormatStr == null || dateFormatStr.length() == 0)
            dateFormatStr = DEFAULT_DATEFORMAT;

        try {

            DateFormat dFormat = new SimpleDateFormat(dateFormatStr);
            fileName += dFormat.format(new Date());

        } catch(IllegalArgumentException ex) {
            logger.logMessage(
                "Timestamp format syntax error, should conform to java.text.SimpleDateFormat",
                this,
                MessageLogger.ERROR);
            PipeComponentUtils.failTransfer();
        }

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
                + "$</td></tr>" + "<tr><td>$" + PREFIX_ATTR + "$</td><td>$" + DATEFORMAT_ATTR
                + "$</td><td>$" + EXTENSION_ATTR + "$</td><td>$" + APPEND_ATTR + "$</td></tr>"
                + "<tr><td colspan=\"4\">$" + CHARSET_ATTR + "$</td></tr></table>";
    }
}