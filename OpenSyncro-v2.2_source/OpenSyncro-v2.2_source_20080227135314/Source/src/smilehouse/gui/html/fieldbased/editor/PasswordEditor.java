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
 * PasswordEditor.java
 * 
 * Created: Thu Apr 22 15:22:09 2004
 */

public class PasswordEditor extends TextEditor {

    /**
     * Default constructor using a template from defaulttemplates.
     */
    public PasswordEditor() {
        super(Template
            .load(
                TextEditor.class.getResourceAsStream("defaulttemplates/passwordeditor.html"),
                DEFAULT_TEMPLATE_ENCODING));
    }

}// PasswordEditor
