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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import smilehouse.util.LabelResource;

/**
 * Implementation for a containerfield. This extends the base-classes functionality by providing a
 * place to keep the created columnfields and information about a collections order in memory
 * between method calls. This information cannot be saved in Info-classes because they serve
 * multiple different users, and Field-classes are specifically used as container's on
 * request-specific data.
 */
class ContainerFieldImpl extends FieldImpl {
    /*Note that this class would be much simpler if a containerfield couldn't be set inside a containerfield.
     *Then we would just have the model's collection here, now we can have multiple collection's (the map).
     * 
     */
    
    List columnFields; //list of Fields for one row
    Map beanRows; //key: bean, value: List of Row objects

    /**
     * Constructor called by ContainerFieldInfo.
     */
    ContainerFieldImpl(ContainerFieldInfo fieldInfo,
                       Object model,
                       LabelResource resource,
                       HttpServletRequest request) {
        super(fieldInfo, model, resource, request);
        this.columnFields = new LinkedList();
        beanRows = new HashMap();
        createColumnFields(fieldInfo.getColumns());
    }

    /**
     * Creates the fields in the columns of this containerfield.
     */
    private void createColumnFields(Collection columns) {
        //create child fields
        for(Iterator iter = columns.iterator(); iter.hasNext();) {
            FieldInfo column = (FieldInfo) iter.next();
            try {
                //check for a container
                ContainerFieldInfo containerColumn = (ContainerFieldInfo) column;
                columnFields.add(new ContainerFieldImpl(containerColumn, null, super
                    .getResource(), super.getRequest()));
            } catch(ClassCastException cce) {
                //not a container, do a regular field
                columnFields.add(new FieldImpl(column, super.getModel(), super.getResource(), super
                    .getRequest()));
            }
        }
    }


    public void revert() {
        super.revert();
        for(Iterator iter = columnFields.iterator(); iter.hasNext();) {
            Field f = (Field) iter.next();
            f.revert();
        }
    }
    
    /**
     * Used by containereditor.
     */
    protected Collection getColumnFields() {
        return this.columnFields;
    }

    /**
     * Used by containereditor.
     */
    protected List getRows() {
        List l = (List) beanRows.get(getModel());
        //bugfix: if rows not set, then set them with the current model
        if(l==null) {
            setModel(getModel());
            l = (List) beanRows.get(getModel());
        }
        return l;
        
    }

    /**
     * Overrides the regular FieldImpl, adds an update of this classes internal state after the
     * superclasses method call.
     */
    public void setModel(Object model) {
        super.setModel(model);
        updateRows(model);
    }

    /**
     * Updates the Row indexes to represent the order read from the model
     * @param bean
     */
    private void updateRows(Object bean) {
        List rows = (List) beanRows.get(bean);
        if(rows == null) {
            //init rows and set their read values
            rows = new LinkedList();
            int index = 0;
            Iterator children = ((Collection) getModelValue()).iterator();
            while(children.hasNext()) {
                Object beanChild = children.next();
                rows.add(new ContainerRow(index, beanChild));
                index++;
            }
            beanRows.put(bean, rows);
        }
    }


}