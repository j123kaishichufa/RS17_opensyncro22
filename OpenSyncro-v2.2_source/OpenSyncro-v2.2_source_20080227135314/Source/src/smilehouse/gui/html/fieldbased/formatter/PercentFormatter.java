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

import java.text.NumberFormat;

import smilehouse.gui.html.fieldbased.editor.EditorResources;
import smilehouse.util.Utils;

public class PercentFormatter implements Formatter {


	String errorLabel;
	NumberFormat formatter;


	public PercentFormatter() {
		this.errorLabel = "not_valid_double";
		this.formatter = NumberFormat.getInstance();
		this.formatter.setMinimumFractionDigits(2);
		this.formatter.setMaximumFractionDigits(2);
	}


	public String getErrorLabel() {
		return this.errorLabel;
	}


	public void setErrorLabel(String label) {
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
			return Utils.parseDouble(value);
		} catch(NumberFormatException e) {
			throw new FormatterException(
					editorResources.getResource(),
					getErrorLabel(),
					"Cannot parse number '" + value + "'"
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
			Double n = (Double) value;
			return formatter.format(n.doubleValue());
		} catch(ClassCastException e) {
			throw new FormatterException(
					editorResources.getResource(),
					getErrorLabel(), 
					"Not a valid Double instance '" + value + "'"
				);
		}
	}

}
