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
 * Created on Nov 11, 2005
 */
package smilehouse.opensyncro.defaultcomponents.string;

import java.util.Locale;

import smilehouse.gui.html.fieldbased.GUIContext;
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

public class StringSource implements SourceIF, GUIConfigurationIF {

    private PipeComponentData data;
    private static final String SOURCE_KEY = "source";

    public StringSource(Object pipeComponentData) {
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
        return "StringSource";
    }
    
    public String getID() {
        return "smilehouse.opensyncro.defaultcomponents.string.StringSource";
    }
    
    // --------------
    // GUI definition
    // --------------
    protected static StringSourceGUI gui = new StringSourceGUI();
    private boolean dataGiven;

    protected static class StringSourceGUI extends GUIDefinition {
        public StringSourceGUI() {
            try {
                addSimpleTextAreaFieldForComponent(SOURCE_KEY,SOURCE_KEY, 80,10);               
            } catch(Exception e) {
                Environment.getInstance().log("Couldn't create GUIContext", e);
            }
        }
    }    
    public GUIContext getGUIContext() {
        return gui.getGUIContext();
    }

    public String getGUITemplate() {
        return "$"+SOURCE_KEY+"$";
    }


    /* (non-Javadoc)
     * @see smilehouse.opensyncro.pipes.component.SourceIF#give(smilehouse.opensyncro.pipes.metadata.SourceInfo, smilehouse.opensyncro.pipes.log.MessageLogger)
     */
    public String[] give(SourceInfo info, MessageLogger logger) throws FailTransferException, AbortTransferException {
        if(dataGiven) {
            return null;
        }else {
            String value=data.getAttribute(SOURCE_KEY);
            dataGiven=true;
            
            // Convert a null (non-specified) value to an empty String
            if(value == null) {
                value = new String("");
            }
            
            return new String[] {value};
        }
    }

    /* (non-Javadoc)
     * @see smilehouse.opensyncro.pipes.component.SourceIF#open(smilehouse.opensyncro.pipes.metadata.SourceInfo, smilehouse.opensyncro.pipes.log.MessageLogger)
     */
    public int open(SourceInfo info, MessageLogger logger) throws FailTransferException, AbortTransferException {
        this.dataGiven=false;
        return ITERATION_OPEN_STATUS_OK;
    }

    /* (non-Javadoc)
     * @see smilehouse.opensyncro.pipes.component.SourceIF#close(smilehouse.opensyncro.pipes.metadata.SourceInfo, smilehouse.opensyncro.pipes.log.MessageLogger)
     */
    public int close(SourceInfo info, MessageLogger logger) throws FailTransferException, AbortTransferException {
        this.dataGiven=false;
        return ITERATION_CLOSE_STATUS_OK;
    }

    /* (non-Javadoc)
     * @see smilehouse.opensyncro.pipes.component.SourceIF#lastBlockStatus(int)
     */
    public void lastBlockStatus(int statusCode) throws FailTransferException, AbortTransferException {
       /* no return channel here */
    }


    /* (non-Javadoc)
     * @see smilehouse.opensyncro.pipes.component.PipeComponentIF#getDescription(java.util.Locale)
     */
    public String getDescription(Locale locale) {
        return PipeComponentUtils.getDescription(locale, this.getClass());
    }




}
