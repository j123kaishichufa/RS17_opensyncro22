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

/**
 * Formatter is used by a WebEditor to transform the values from the request to values that the
 * edited object uses. The methods throw a FormatterException if there is a problem transforming the
 * value. Some formatters also do other validation in addition to the transformation. Validation
 * problems are also reported through FormatterExceptions. FormatterExceptions define a label and will
 * be searched from the labelresource, so the problems validating are reported in the editor field, with
 * the user's language.
 *  
 */
public interface Formatter {
    /**
     * Used to transform a value from request (string) to a value accepted by the modifier object.
     * 
     * @param value Value to transform
     * @param editorResources Can be used to process language dependent transformations and it allows to
     * access the context of the transformation.
     */
    public Object stringToValue(String value, EditorResources editorResources) throws FormatterException;

    /**
     * Used to transform modified object's value type to a string used in the editor for request
     * processing.
     * 
     * @param value Value to transform
     * @param editorResources Can be used to process language dependent transformations and it allows to
     * access the context of the transformation.
     */
    public String valueToString(Object value, EditorResources editorResources) throws FormatterException;
}

