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
import java.util.Collections;
import java.util.LinkedList;

import javax.servlet.http.HttpServletRequest;

import smilehouse.gui.html.fieldbased.model.ModelModifier;
import smilehouse.util.LabelResource;

/**
 * FieldInfo class for a containerfield. The container is configured by adding other fieldInfos as
 * columns.
 */
public class ContainerFieldInfo extends FieldInfo {
    ContainerEditor containerEditor;
    Collection columns;

    /**
     * Creates a new ContainerField
     * 
     * @param id Identifier for this field.
     * @param label the labelkey for this field's description
     * @param modifier the modifier used to change the values modified by this field.
     * @param editor The representation of this editor.
     *  
     */
    public ContainerFieldInfo(String id,
                              String label,
                              ModelModifier modifier,
                              ContainerEditor editor) throws Exception {
        super(id, label, modifier, editor);
        this.containerEditor = editor;
        this.columns = new LinkedList();
    }

    /**
     * Adds a field into the editor. Fields are added in the order of the add-calls. Columns are
     * always added as the last column. If move or delete-buttons are enabled they are shown after
     * the field columns.
     * 
     * @param column a new field shown as a column in the editor
     * @return Returns itself
     */
    public ContainerFieldInfo addColumn(FieldInfo column) {
        this.columns.add(column);
        return this;
    }

    /**
     * @return the columns added to this field.
     */
    protected Collection getColumns() {
        return this.columns;
    }


    /**
     * Returns the Field that is used in the processing of the given request.
     */
    protected Field createField(Object model, LabelResource resource, HttpServletRequest request) {
        return new ContainerFieldImpl(this, model, resource, request);
    }


    /**
     * Forwards to the editor, for a description see the Field Interface.
     */
    protected boolean hasBeenEdited(FieldImpl field) {
        return containerEditor.hasBeenEdited(field);
    }

    /**
     * Forwards to the editor, for a description see the Field Interface.
     */
    protected boolean isEditValid(FieldImpl field){
        return containerEditor.isEditValid(field);
    }

    /**
     * Forwards to the editor, for a description see the Field Interface.
     */
    protected boolean commit(FieldImpl field){
        return this.containerEditor.commit(field);
    }

    /**
     * Forwards to the editor, for a description see the Field Interface.
     */
    protected Collection validate(FieldImpl field) {
        //container is always valid
        return Collections.EMPTY_LIST;
    }

}