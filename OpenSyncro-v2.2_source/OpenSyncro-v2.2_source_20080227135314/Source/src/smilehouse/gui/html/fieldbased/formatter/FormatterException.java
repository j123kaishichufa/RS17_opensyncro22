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

package smilehouse.gui.html.fieldbased.formatter;

import java.text.MessageFormat;

import smilehouse.util.LabelResource;


/**
 * Exception wrapper for exceptions that happen during formatter transformations.
 */
public class FormatterException extends Exception {
    LabelResource labels;
    private String errorLabel;
    private Object[] args;

    
    /**
     * Basic constructor for creating a Formatter error message.
     * 
     * @param errorLabel Key for getting the errormessage from the resource
     */
    public FormatterException(LabelResource labels, String errorLabel, Object[] args) {
        this.labels = labels;
        this.errorLabel = errorLabel;
        this.args = args;
    }

    /**
     * Constructor for creating a Formatter error message.
     * 
     * @param errorLabel Key for getting the errormessage from the resource
     */
    public FormatterException(LabelResource labels, String errorLabel) {
        this(labels, errorLabel, new Object[0]);
    }

    /**
     * Constructor for creating a Formatter error message.
     * 
     * @param errorLabel Key for getting the errormessage from the resource
     * @param arg1 Argument1 for the messageformat
     */
    public FormatterException(LabelResource labels, String errorLabel, Object arg1) {
        this(labels, errorLabel);
        Object[] tmp = new Object[1];
        tmp[0] = arg1;
        this.args = tmp;
    }

    /**
     * Overriden now returns the errorcode of the instance.
     */
    public String getMessage() {
        String message = labels.getLabel(errorLabel);
        if(message==null)
            return null;
        MessageFormat mf = new MessageFormat(message);
        return mf.format(this.args);
    }

}