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

/**
 * TextAreaEditor corresponds to a html form textarea input. Cols and Rows can be changed.
 */
public class TextAreaEditor extends TextEditor {


    /**
     * Default constructor using a template from defaulttemplates.
     */
    public TextAreaEditor() {
        super(Template
            .load(
                TextAreaEditor.class.getResourceAsStream("defaulttemplates/textareaeditor.html"),
                DEFAULT_TEMPLATE_ENCODING));
        setCols(65);
        setRows(10);

    }

    /**
     * Constructor for changing the template
     */
    public TextAreaEditor(Template template) {
        super(template);
        setCols(65);
        setRows(10);
    }

    /**
     * set the cols for the textarea
     */
    public void setCols(int cols) {
        setProperty("cols", "" + cols);
    }

    /**
     * get the cols for the textarea
     */
    public int getCols() {
        return getIntProperty("cols");
    }

    /**
     * set the rows for the textarea
     */
    public void setRows(int rows) {
        setProperty("rows", "" + rows);
    }

    /**
     * get the rows for the textarea
     */
    public int getRows() {
        return getIntProperty("rows");
    }


}