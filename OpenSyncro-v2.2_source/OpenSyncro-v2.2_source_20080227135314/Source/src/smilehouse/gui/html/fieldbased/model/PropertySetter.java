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
 * PropertySetter can be used to set a property to a model. Property here means the types of
 * properties known in JavaBeans. JavaBeans-based PropertySetter using Introspection can be created
 * with the IntrospectionPropertySetter. It's easy but suffers a penalty from using Introspection.
 * For faster implementations of PropertySetter you should provide your own for your specific
 * javabean. The IntrospectionPropertySetter is suitable for testing.
 *  
 */
public interface PropertySetter {
    /**
     * Sets the given property's value to the given value.
     * 
     * @param model the actual bean that contains the property
     * @param property the name of the property by javaBean specification.
     * @param value the value set to the model's property
     */
    public void setValue(Object model, String property, Object value);

    /**
     * Gets the given property's value
     * 
     * @param model the actual bean that contains the property
     * @param property the name of the property by javaBean specification.
     */
    public Object getValue(Object model, String property);

    /**
     * Gets the type of the given property
     * 
     * @param property the name of the property by javaBean specification.
     */
    public Class getType(String property);

    /**
     * Gets the type of the javaBean that can be edited with this PropertySetter.
     */
    public Class getType();
}