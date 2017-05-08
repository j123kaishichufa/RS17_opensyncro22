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

import smilehouse.gui.html.fieldbased.GUIContext;
import smilehouse.opensyncro.pipes.component.FailTransferException;
import smilehouse.opensyncro.pipes.component.PipeComponentUtils;
import smilehouse.opensyncro.pipes.gui.GUIConfigurationIF;
import smilehouse.opensyncro.pipes.gui.GUIDefinition;
import smilehouse.opensyncro.pipes.log.MessageLogger;
import smilehouse.opensyncro.system.Environment;


/**
 * TimestampFileSource.java
 * 
 * Created: Fri May 28 10:58:53 2004
 */

public class TimestampFileSource extends LocalFileSource implements GUIConfigurationIF {

    protected static final String PREFIX_ATTR = "prefix";
    protected static final String DATEFORMAT_ATTR = "dateformat";
    protected static final String EXTENSION_ATTR = "extension";

    private static final String DEFAULT_DATEFORMAT = "yyyy-MM-dd";

    // --------------
    // GUI definition
    // --------------
    protected static TimestampFileSourceGUI tsf_gui = new TimestampFileSourceGUI();

    protected static class TimestampFileSourceGUI extends GUIDefinition {

        public TimestampFileSourceGUI() {
            try {
                addField(CHARSET_ATTR, LocalFileSource.gui.getField(CHARSET_ATTR));
                addField(DIRECTORY_ATTR, LocalFileSource.gui.getField(DIRECTORY_ATTR));
                addSimpleTextFieldForComponent(PREFIX_ATTR, PREFIX_ATTR, 30);
                addSimpleTextFieldForComponent(DATEFORMAT_ATTR, DATEFORMAT_ATTR, 20);
                addSimpleTextFieldForComponent(EXTENSION_ATTR, EXTENSION_ATTR, 10);
            } catch(Exception e) {
                Environment.getInstance().log(
                    "Couldn't create GUIContext for TimestampFileSource", e);
            }
        }
    }

    // ---
     
    public TimestampFileSource( Object pipeComponentData ) {
        super( pipeComponentData );
    }

    
    public String getName() {
        return "TimestampFileSource";
    }

    public String getID() {
        return "smilehouse.opensyncro.defaultcomponents.filesystem.TimestampFileSource";
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
                + "$</td><td>$" + EXTENSION_ATTR + "$</td><td>$" + CHARSET_ATTR
                + "$</td></tr></table>";
    }

} // TimestampFileSource
