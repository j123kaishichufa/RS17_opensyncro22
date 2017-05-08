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

import smilehouse.tools.template.Template;
import smilehouse.util.Utils;

/**
 * Editor showing a html form input type text. Size can be changed, default size is 65.
 */
public class TextEditor extends AbstractEditor {

    /**
     * Default constructor using a template from defaulttemplates.
     */
    public TextEditor() {
        super(Template.load(TextEditor.class
            .getResourceAsStream("defaulttemplates/texteditor.html"), DEFAULT_TEMPLATE_ENCODING));
        setSize(65);
    }

    /**
     * Constructor for changing the template
     */
    public TextEditor(Template template) {
        super(template);
        setSize(65);
    }

    /**
     * set size of the input field.
     */
    public TextEditor setSize(int size) {
        setProperty("size", "" + size);
        return this;
    }

    /**
     * get size of the input field.
     */
    public int getSize() {
        return getIntProperty("size");
    }

    /**
     * @return html representation of the editor
     */
    public String getEditor(EditorResources editorResources, Template template) {
        writeProperties(template);

        template.setVariable("id", editorResources.getWriteId());

        String value = getEditorValue(editorResources);
        value = Utils.htmlentities(value);
        template.setVariable("value", value);

        template.setVariable("description", editorResources.getDescription());

        template.setVariable("cssClass", "");

        super.writeEditorErrors(template, editorResources);


        template.write();
        return template.toString();
    }


}