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

package smilehouse.gui.html.fieldbased.model;

/**
 * ModelModifier based on a PropertySetter.
 * 
 *  
 */
public class PropertyBasedModelModifier implements ModelModifier {
    private String property;
    private PropertySetter setter;

    /**
     * Creates a modelmofier that modifies the given property using the given propertysetter.
     * 
     * @param property String identifying the modifier property
     * @param setter PropertySetter for modifying the model of this ModelModifier.
     */
    public PropertyBasedModelModifier(String property, PropertySetter setter) {
        this.property = property;
        this.setter = setter;
    }

    /** @see ModelModifier */
    public void setValue(Object model, Object value) {
        setter.setValue(model, property, value);
    }

    /** @see ModelModifier */
    public Object getValue(Object model) {
        return setter.getValue(model, property);
    }
}