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
 * a default implementation for SelectOption. If only the value is given, the label is created by
 * using value.toString().
 */
public class DefaultSelectOption implements SelectOption {
    private Object value;
    private String labelKey;
    private String label;

    /**
     * Creates a SelectOption with the value, labelKey will be created by value.toString().
     */
    public DefaultSelectOption(Object value) {
        this.value = value;
        if(value != null)
            this.label = value.toString();
        this.labelKey = null;
    }

    /**
     * Creates a SelectOption with the given values.
     */
    public DefaultSelectOption(Object value, String labelKey) {
        this.value = value;
        this.labelKey = labelKey;
        this.label = null;
    }

    /**
     * @return the value given in the constructor.
     */
    public Object getValue() {
        return this.value;
    }

    /**
     * @return the value given in the constructor, or by using value.toString().
     */
    public String getLabel(LabelResource resource) {
        if(this.label != null)
            return label;
        String desc = resource.getLabel(labelKey);
        if(desc == null)
            desc = labelKey;
        return desc;

    }
}