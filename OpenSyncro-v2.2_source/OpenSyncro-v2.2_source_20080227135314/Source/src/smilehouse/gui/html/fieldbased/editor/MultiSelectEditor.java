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
 * Editor showing a html form select with multiple selects enabled. Size can be changed, default
 * size is 5.
 */
public class MultiSelectEditor extends CheckboxGroupEditor {

    /**
     * Default constructor using a template from defaulttemplates.
     */
    public MultiSelectEditor() {
        super(Template.load(
            MultiSelectEditor.class.getResourceAsStream("defaulttemplates/multiselecteditor.html"),
            DEFAULT_TEMPLATE_ENCODING));
        setSize(5);

    }

    /**
     * Constructor for changing the template
     */
    public MultiSelectEditor(Template template) {
        super(template);
        setSize(5);
    }

    /**
     * set size of the select field.
     */
    public void setSize(int size) {
        setProperty("size", "" + size);
    }

    /**
     * get size of the select field.
     */
    public int getSize() {
        return getIntProperty("size");
    }


}