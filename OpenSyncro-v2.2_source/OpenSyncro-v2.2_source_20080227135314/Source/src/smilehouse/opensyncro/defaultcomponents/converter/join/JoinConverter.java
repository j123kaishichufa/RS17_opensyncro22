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

package smilehouse.opensyncro.defaultcomponents.converter.join;

import java.util.Locale;

import smilehouse.gui.html.fieldbased.GUIContext;
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
import smilehouse.util.Utils;

/**
 * JoinConverter.java
 * 
 * Created: Thu Jun 3 15:28:04 2004
 */

public class JoinConverter implements ConverterIF, GUIConfigurationIF {

    private static final String DELIMITER_ATTR = "delimiter";

    private static GUIDefinition gui = new JoinGUI();

    private static class JoinGUI extends GUIDefinition {
        public JoinGUI() {
            try {
                addSimpleTextFieldForComponent(DELIMITER_ATTR, DELIMITER_ATTR, 50);
            } catch(Exception e) {
                Environment.getInstance().log("Couldn't initialize GUI", e);
            }
        }
    }

    public GUIContext getGUIContext() {
        return gui.getGUIContext();
    }

    public String getGUITemplate() {
        return null;
    }

    public JoinConverter( Object pipeComponentData ) {
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
        return "JoinConverter";
    }

    public String getID() {
        return "smilehouse.opensyncro.defaultcomponents.converter.join.JoinConverter";
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
    
    
    public String[] convert(String data, ConversionInfo info, MessageLogger logger)
            throws FailTransferException, AbortTransferException {
        throw new UnsupportedOperationException(
            "This method has no meaning in this class, use convertAll");
    }

    public String[] convertAll(String data[], ConversionInfo info, MessageLogger logger)
            throws FailTransferException, AbortTransferException {
        String delimiter = this.data.getAttribute(DELIMITER_ATTR);
        if(delimiter == null) {
            logger
                .logMessage("Delimiter not set, using empty string.", this, MessageLogger.WARNING);
            delimiter = "";
        }
        logger.logMessage("Joining " + data.length + " components", this, MessageLogger.DEBUG);
        return new String[] {Utils.join(data, delimiter)};
    }


}// JoinConverter
