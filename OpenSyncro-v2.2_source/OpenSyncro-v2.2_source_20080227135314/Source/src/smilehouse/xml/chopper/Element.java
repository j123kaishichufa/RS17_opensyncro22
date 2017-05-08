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

package smilehouse.xml.chopper;

import java.util.LinkedList;
import java.util.List;

/**
 * Class for saving data on elements in the current path in XMLChopper. 
 */
class Element {
    String name;
    List<Attribute> attributes;
 
    public Element(String name) {
        this.name = name;
        this.attributes = new LinkedList<Attribute>();
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public String getName() {
        return name;
    }

    public void addAttribute(Attribute a) {
        this.attributes.add(a);
    }
    
    /**
     * Get the most recently added attribute.
     * @return Latest attribute
     */
    public Attribute getLatestAttribute() {
        if(attributes.isEmpty()) {
            throw new IllegalStateException("No attributes!");
        }
        else
            return attributes.get(attributes.size()-1);
    }

    public void setName(String name) {
        this.name = name;
    }
    
    
    /**
     * Print the entire element tag including the attribute list, for example <element attr1="a" attr2="b">
     * @return XML style element string
     */
    public String toString() {
        StringBuffer buff = new StringBuffer("<").append(name);
        for(Attribute a : attributes) {
            buff.append(' ').append(a.getName()).append("=\"").append(a.getValue()).append('"');
        }
        buff.append('>');
        
        return buff.toString();
    }
    
    
}
