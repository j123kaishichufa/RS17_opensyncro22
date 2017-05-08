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

package smilehouse.gui.html.fieldbased.editor;


import smilehouse.gui.html.fieldbased.formatter.FormatterException;
import smilehouse.tools.template.Template;
import smilehouse.util.Utils;


/**
 * Editor showing a value in HTML definition list for display purposes only.
 */
public class ReadOnlyTextEditor extends AbstractEditor {


	/**
	 * Default constructor using a template from defaulttemplates.
	 */
	public ReadOnlyTextEditor() {
		super(Template.load(TextEditor.class
				.getResourceAsStream("defaulttemplates/readonlytexteditor.html"), DEFAULT_TEMPLATE_ENCODING));
	}


	/**
	 * Constructor with custom template.
	 */
	public ReadOnlyTextEditor(Template template) {
		super(template);
	}


	/**
	 * @return false
	 */
	public boolean hasEditValue(EditorResources editorResources) {
		return false;
	}


	/**
	 * @return The value of the bean
	 */
	public Object getEditValue(EditorResources editorResources) throws FormatterException {
		return editorResources.getModelValue();
	}


	protected String getEditor(EditorResources editorResources, Template template) {
		String value = getEditorValue(editorResources);
		value = Utils.htmlentities(value);
		template.setVariable("value", value);
		template.setVariable("description", editorResources.getDescription());
		template.write();
		return template.toString();
	}

}
