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


public class BooleanFormatter implements Formatter {


	String errorLabel;


	public BooleanFormatter() {
		setErrorLabel("not_valid_boolean");
	}


	public String getErrorLabel() {
		return errorLabel;
	}


	public void setErrorLabel(String label) {
		this.errorLabel = label;
	}


	public Object stringToValue(String value, EditorResources editorResources) throws FormatterException {
		if(value == null)
			return Boolean.valueOf(false);
		return Boolean.valueOf(value);
	}


	public String valueToString(Object value, EditorResources editorResources) throws FormatterException {
		if(value == null)
			return Boolean.valueOf(false).toString();
		try {
			Boolean bool = (Boolean) value;
			return bool.toString();
		} catch(ClassCastException e) {
			throw new FormatterException(
					editorResources.getResource(),
					getErrorLabel(),
					"Not a valid Boolean instance '" + value + "'"
				);
		}
	}

}
