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

import smilehouse.util.LabelResource;

/**
 * SelectOption is used in editors that are used to choose values from predefined options. These
 * kind of editors are for example select,multiselect,radiogroup,checkboxgroup -editors.
 */
public interface SelectOption {
    /**
     * Used to get the value-attribute to the html-option.
     */
    public Object getValue();

    /**
     * Used to get the description text to the html-option.
     */
    public String getLabel(LabelResource resource);
}