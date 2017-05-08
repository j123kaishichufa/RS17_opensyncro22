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

package smilehouse.gui.html.fieldbased;


import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import smilehouse.tools.template.Template;
import smilehouse.util.LabelResource;

/**
 * Container for a bunch of fields. Adds some convenience methods that would propably otherwise be
 * repeated in multiple servlets. Usually a GUIContext can be created in the servlet's init-method
 * as an instance variable of the servlet.
 */
public class GUIContext {

    private Map fieldInfos;

    /**
     * Creates a new guicontext
     */
    public GUIContext() {
        this.fieldInfos = new LinkedHashMap();

    }

    /**
     * Creates a new guiContext and sets the given map of FieldInfo-objects to this context.
     */
    public GUIContext(Map fieldInfos) {
        this.fieldInfos = fieldInfos;
    }

    /**
     * Adds a FieldInfo to this context
     */
    public void addFieldInfo(FieldInfo fieldInfo) {
        this.fieldInfos.put(fieldInfo.getId(), fieldInfo);
    }





    /**
     * Creates the fields for this GUIContext. Called when processing the request.
     */
    public Map makeFields(Object model, LabelResource labels, HttpServletRequest req) {
        Map fields = new LinkedHashMap();
        for(Iterator iter = fieldInfos.values().iterator(); iter.hasNext();) {
            FieldInfo info = (FieldInfo) iter.next();
            fields.put(info.getId(), info.getField(model, labels, req));
        }
        return fields;
    }


    /**
     * Writes the field editors to the given template. editors are written as variables with the
     * field ids.
     */
    public void writeEditors(Map fields, Template template) {

        for(Iterator iter = fields.values().iterator(); iter.hasNext();) {
            Field field = (Field) iter.next();
            template.setVariable(field.getId(), field.getEditor());
        }

    }



    /**
     * @return true if any of the fields value has been edited (ie. the value has changed).
     */
    public boolean hasBeenEdited(Map fields) {
        for(Iterator iter = fields.values().iterator(); iter.hasNext();) {
            Field field = (Field) iter.next();
            if(field.hasBeenEdited())
                return true;
        }
        return false;
    }

    /**
     * tries to commit the edit values from the given pages fields to the bean object.
     * 
     * @return true if all fields were valid and committed. false if any of the fields were invalid.
     */
    public boolean commitFields(Map fields) {
        boolean valid = true;

        for(Iterator iter = fields.values().iterator(); iter.hasNext();) {
            Field field = (Field) iter.next();
            //System.out.println(field.getId());
            if(field.hasBeenEdited() && !field.isEditValid()) {
                valid = false;
                //System.out.println("not valid edit.");
            }
            field.commit();

        }
        return valid;
    }

    /* Reverts all fields to the value of the bean. */
    public void revertFields(Map fields) {
        for(Iterator iter = fields.values().iterator(); iter.hasNext();) {
            Field field = (Field) iter.next();
            field.revert();
        }
    }


}