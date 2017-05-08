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


import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import smilehouse.gui.html.fieldbased.editor.EditorResources;


public class DateFormatter implements Formatter {


	private String errorLabel;


	public DateFormatter() {
		setErrorLabel("not_valid_date");
	}


	public String getErrorLabel() {
		return errorLabel;
	}


	private void setErrorLabel(String label) {
		this.errorLabel = label;
	}


	public Object stringToValue(String value, EditorResources editorResources) throws FormatterException {
		if(value == null)
			throw new FormatterException(
					editorResources.getResource(),
					getErrorLabel(),
					"Null not accepted."
				);
		try {
			DateFormat df = DateFormat.getInstance();
			return df.parse(value);
		} catch(ParseException e) {
			throw new FormatterException(
					editorResources.getResource(),
					getErrorLabel(),
					"Cannot format string to date: '" + value + "'"
				);
		}
	}

	public String valueToString(Object value, EditorResources editorResources) throws FormatterException {
		if(value == null)
			throw new FormatterException(
					editorResources.getResource(),
					getErrorLabel(),
					"Null not accepted."
				);
		try {
			Date date = (Date) value;
			return date.toString();
		} catch(ClassCastException e) {
			throw new FormatterException(
					editorResources.getResource(),
					getErrorLabel(),
					"Not a valid Date instance '" + value + "'"
				);
		}
	}

}
