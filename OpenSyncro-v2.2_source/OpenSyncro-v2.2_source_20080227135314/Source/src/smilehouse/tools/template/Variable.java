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

package smilehouse.tools.template;

/**
 * A class that represents a variable, variable is a structure in the template file. The class is a
 * protected helper class and it's objects are used by the Template object.
 */
class Variable {
    /** Variable's name */
    private String name;

    /** Variable's value, default value is an empty string. */
    private String value;


    /**
     * Creates a new Variable
     * 
     * @param name The name of the variable. Cannot be null or an empty string.
     */
    Variable(String name) {
        if(name == null)
            throw new IllegalArgumentException("Variable name cannot be a null value.");
        if(name.length() == 0)
            throw new IllegalArgumentException("Variable name cannot be an empty string.");

        this.name = name;
        this.value = "";
    }

    /**
     * Sets the Variable's value
     * 
     * @param value Variable's value. null values will be ignored.
     */
    void setValue(String value) {
        if(value != null)
            this.value = value;
    }

    /**
     * Returns the variable's value
     */
    public String toString() {
        return this.value;
    }


    /**
     * Returns the variable's name
     */
    String getName() {
        return this.name;
    }
}