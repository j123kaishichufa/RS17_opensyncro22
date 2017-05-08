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

import smilehouse.gui.html.fieldbased.editor.EditorResources;
import smilehouse.util.LabelResource;
import smilehouse.util.Utils;

/**
 * Formatter for double-precision floating point numbers. value is of type java.lang.Double.
 * valueToString also accepts Strings that contain a valid floating point value.
 */
public class DoubleFormatter implements Formatter {

    private static final Double NAN_DOUBLE = new Double(Double.NaN);
    private String errorLabel;
    private boolean acceptEmptyValues = false;

    /**
     */
    public DoubleFormatter() {
        this.errorLabel="not_valid_double";
    }

    /**
     * Creates a formatter with the given errorLabel.
     */
    public DoubleFormatter(String errorLabel) {
       this.errorLabel = errorLabel;
    }

    public void setAcceptEmptyValues(boolean acceptEmptyValues) {
        this.acceptEmptyValues = acceptEmptyValues;
    }

    /**
     * Parses the given String to a Double-object. Uses Smilehouse.util.Utils.parseDouble for the
     * conversion. It accepts both . and , characters as the decimalseparator.
     */
    public Object stringToValue(String value, EditorResources editorResources) throws FormatterException {
    	LabelResource labels = editorResources.getResource();
        // Return NaN for null or an empty string if empty values are to be accepted.
        if(acceptEmptyValues && (value == null || value.length() == 0))
            return NAN_DOUBLE;
        if(value == null)
            throw new FormatterException(labels, errorLabel, value);
        try {
            return Utils.parseDouble(value);
        } catch(NumberFormatException e) {
            throw new FormatterException(labels, errorLabel, value);
        }
    }

    /**
     * Parses the given (Double) value to a string. The given value can also be a String-object,
     * then the string is parsed and the resulting Double.toString is returned. So this always
     * return's a . character as the decimal separator.
     */
    public String valueToString(Object value, EditorResources editorResources) throws FormatterException {
    	LabelResource labels = editorResources.getResource();
        if(value == null) {
            if(acceptEmptyValues)
                return "";
            else
                throw new FormatterException(labels, errorLabel);
        }
        try {
            Double doub = (Double) value;
            if(doub.isNaN() && acceptEmptyValues)
                return "";
            return doub.toString();
        } catch(ClassCastException e) {
            //let's try another trick. we might have a string object,
            //whose value is sane as a double, let's try it
            try {
                String ret = (String) value;
                if(acceptEmptyValues && ret.length() == 0)
                    return ret;
                Double doub = Utils.parseDouble(ret);

                return doub.toString();
            } catch(Exception se) {
                throw new FormatterException(labels, errorLabel, value);
            }
        }
    }
}





