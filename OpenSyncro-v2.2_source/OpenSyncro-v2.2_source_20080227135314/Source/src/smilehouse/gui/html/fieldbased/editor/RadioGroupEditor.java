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
 * Editor showing a bunch of radio-inputs.
 *  
 */
public class RadioGroupEditor extends SelectEditor {

    /**
     * Default constructor using a template from defaulttemplates.
     */
    public RadioGroupEditor() {
        super(Template.load(
            RadioGroupEditor.class.getResourceAsStream("defaulttemplates/radiogroupeditor.html"),
            DEFAULT_TEMPLATE_ENCODING));

    }

    /**
     * Constructor for changing the template
     */
    public RadioGroupEditor(Template template) {
        super(template);
    }

}