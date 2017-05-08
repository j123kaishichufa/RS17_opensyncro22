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

/**
 * Formatter for integer numbers. Value is of type java.lang.Integer. valueToString also accepts
 * Strings that contain a valid integer value.
 * 
 * The class can also be configured to accept only positive integers by using
 * acceptOnlyPositive-method.
 */
public class IntegerFormatter implements Formatter {
    private String acceptOnlyPositiveErrorCode;
    private String acceptOnlyStrictlyPositiveErrorCode;
    private String errorLabel;
    private boolean nullEqualsEmpty;
    
    /**
     * Creates a formatter with a default general errorcode (not_valid_integer).
     */
    public IntegerFormatter() {
        errorLabel="not_valid_integer";
        nullEqualsEmpty = false;
    }

    /** Creates a formatter with the given errorcode. */
    public IntegerFormatter(String errorLabel) {
        this.errorLabel=errorLabel;
    }

    /***********************************************************************************************
     * Configures the formatter to only accept positive integer values.
     * 
     * @param errorCode In case of a negative integer the given errorCode is used to show the user
     *        an errormessage. If the given label is null, also negative integers are excepted.
     */
    public IntegerFormatter acceptOnlyPositive(String errorCode) {
        this.acceptOnlyPositiveErrorCode = errorCode;
        return this;
    }
    
    /**
     * Configures the formatter to only accept strictly positive (meaning > 0) integer values.
     * This condition is clearly stricter than the one given in the method acceptOnlyPositive,
     * so the 'only positive'-condition makes no difference if this is in use.
     * 
     * @param errorCode In case of a negative or zero integer the given errorCode is used to show the user
     *        an errormessage. If the given label is null, also negative and zero integers are excepted.
     */
    public void acceptOnlyStrictlyPositive(String errorCode) {
        this.acceptOnlyStrictlyPositiveErrorCode = errorCode;
    }

    /**
     * If set to 'true', null obejcts are formatted as empty strings and vice versa.
     * Default is 'false', in which case null objects and empty strings cause FormatterExceptions to be thrown.
     * 
     * @param nullEqualsEmpty
     */
    public void setNullEqualsEmpty(boolean nullEqualsEmpty) {
        this.nullEqualsEmpty = nullEqualsEmpty;
    }
    
    /**
     * Parses the given String to a Integer-object.
     */
    public Object stringToValue(String value, EditorResources editorResources) throws FormatterException {
    	LabelResource labels = editorResources.getResource();
        if(value == null)
            throw new FormatterException(labels, errorLabel);
        if(value.length() == 0 && nullEqualsEmpty)
            return null;
        try {
            Number integer = parseNumber(value);

            if(this.acceptOnlyStrictlyPositiveErrorCode != null && integer.intValue() < 1)
                throw new FormatterException(labels, this.acceptOnlyStrictlyPositiveErrorCode);
            
            if(this.acceptOnlyPositiveErrorCode != null && (integer.intValue() < 0))
                throw new FormatterException(labels, this.acceptOnlyPositiveErrorCode);

            return integer;
        } catch(NumberFormatException e) {
            throw new FormatterException(labels, errorLabel);
        }
    }

    /**
     * @param value
     * @return
     */
    protected Number parseNumber(String value) throws NumberFormatException {
        return new Integer(value);
    }

    /**
     * Parses the given (Integer) value to a string. The given value can also be a String-object,
     * then the string is parsed and the resulting Integer.toString is returned.
     */
    public String valueToString(Object value, EditorResources editorResources) throws FormatterException {
    	LabelResource labels = editorResources.getResource();
        if(value == null) {
            if(nullEqualsEmpty)
                return "";
            else
                throw new FormatterException(labels, errorLabel);
        }
        try {
            Number integer = (Number) value;

            return integer.toString();
        } catch(ClassCastException e) {
            //let's try another trick.
            //we might have a string object, whose value is sane as an integer, let's try it
            try {
                String ret = (String) value;
                parseNumber(ret); //if this doesn't throw, we have an integer
                return ret;
            } catch(Exception se) {
                throw new FormatterException(labels, errorLabel, value);
            }
        }
    }

}





