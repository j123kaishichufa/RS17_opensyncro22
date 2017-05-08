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
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import smilehouse.gui.html.fieldbased.editor.EditorResources;
import smilehouse.gui.html.fieldbased.formatter.FormatterException;
import smilehouse.util.LabelResource;

/**
 *  Default implementation of a Field.
 */
class FieldImpl implements Field, EditorResources, ContainableField {
    private FieldInfo fieldInfo;
    private LabelResource resource;
    private HttpServletRequest request;

    
    protected Object model;
    private boolean reverted;
    private Set committedModels;

    public FieldImpl(FieldInfo fieldInfo,
                     Object model,
                     LabelResource resource,
                     HttpServletRequest request) {
        this.fieldInfo = fieldInfo;
        this.model = model;
        this.resource = resource;
        this.request = request;

        this.reverted=false;
        this.committedModels=new HashSet();
    }



    /** Field interface implementation, see Field interface */
    public boolean hasBeenEdited() {
        try {
            return fieldInfo.hasBeenEdited(this);
        } catch(FormatterException le) {
            //not a valid edit, interface says to return false on this.
           return false;
        }
    }

    /** Field interface implementation, see Field interface */
    public boolean isEditValid() {
            return fieldInfo.isEditValid(this);
    }

    /** Field interface implementation, see Field interface */
    public void commit() {
        //if reverted, then commit shouldn't have any effect -> don't commit
       if(!isReverted()) {
           if(fieldInfo.commit(this)){
               this.markCommit();
           }
       }
    }
    
    public boolean isCommitted() {
        return committedModels.contains(model);
    }
    private void markCommit() {
        committedModels.add(model);
    }
    /** Field interface implementation, see Field interface */
    public void revert() {
        this.reverted=true;
    }

    /** Field interface implementation, see Field interface */
    public boolean isReverted() {
        return this.reverted;
    }

    /** Field interface implementation, see Field interface */
    public String getEditor() {
        return fieldInfo.getEditor(this);
    }





    /** ContainableField interface implementation, see ContainableField interface */
    public void setReadId(String id) {
        fieldInfo.setReadId(id);
    }

    /** ContainableField interface implementation, see ContainableField interface */
    public void setWriteId(String id) {
        fieldInfo.setWriteId(id);
    }

    /** ContainableField interface implementation, see ContainableField interface */
    public void setModel(Object model) {
        this.model = model;
    }




    /** EditorResources interface implementation, see EditorResources interface */
    public HttpServletRequest getRequest() {
        return this.request;
    }

    /** EditorResources interface implementation, see EditorResources interface */
    public Object getModel() {
        return this.model;
    }

    /** EditorResources interface implementation, see EditorResources interface */
    public LabelResource getResource() {
        return this.resource;
    }

    /** EditorResources interface implementation, see EditorResources interface */
    public String getId() {
        return fieldInfo.getId();
    }

    /** EditorResources interface implementation, see EditorResources interface */
    public String getReadId() {
        return fieldInfo.getReadId();
    }

    /** EditorResources interface implementation, see EditorResources interface */
    public String getWriteId() {
        return fieldInfo.getWriteId();
    }

    /** EditorResources interface implementation, see EditorResources interface */
    public String getDescription() {
        if(fieldInfo.getLabel() == null) {
            return "";
        }
        else {
            String label = resource.getLabel(fieldInfo.getLabel());
            if(label == null)
                return fieldInfo.getLabel();
            else
                return resource.getLabel(fieldInfo.getLabel());
        }
    }

    /** EditorResources interface implementation, see EditorResources interface */
    public Object getModelValue(){
        return this.fieldInfo.getModelValue(this);
    }

    /** EditorResources interface implementation, see EditorResources interface */
    public Collection validate() {
        return this.fieldInfo.validate(this);
    }



}