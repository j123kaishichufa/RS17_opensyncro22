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
import java.util.LinkedList;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import smilehouse.gui.html.fieldbased.editor.WebEditor;
import smilehouse.gui.html.fieldbased.formatter.FormatterException;
import smilehouse.gui.html.fieldbased.model.ModelModifier;
import smilehouse.util.LabelResource;

/**
 * FieldInfo represents the information required for configuring a field. From a configured field
 * info, the actual fields for a request can be got by giving the getField-method the required
 * request-specific information.
 * 
 *  
 */
public class FieldInfo {
    private String id;
    private String readId;
    private String writeId;
    private String label;
    private ModelModifier modifier;
    private WebEditor editor;
    private String beanId;

    /**
     * Creates a new Field
     * 
     * @param id Identifier for this field, this should be unique in a request.
     * @param label the labelkey for this field's description
     * @param modifier the modifier used to change the values modified by this field.
     * @param editor The representation of this editor.
     *  
     */
    public FieldInfo(String id, String label, ModelModifier modifier, WebEditor editor)
            throws Exception {
        this.id = id;
        this.readId = id;
        this.writeId = id;
        this.label = label;
        this.modifier = modifier;
        this.editor = editor;
        this.beanId = null;
    }


    protected Field createField(Object model, LabelResource resource, HttpServletRequest request) {
    	return new FieldImpl(this, model, resource, request);
    }

    /**
     * @return the request-specific Field-object
     */
    public Field getField(Object model, LabelResource resource, HttpServletRequest request) {
    	Object bean = model;
    	if (getBeanId() != null) {
    		bean = ((Map) model).get(getBeanId());
    	}
    	return createField(bean, resource, request);
    }


    protected String getLabel() {
        return this.label;
    }

    protected String getId() {
        return this.id;
    }

    protected String getReadId() {
        return this.readId;
    }

    protected String getWriteId() {
        return this.writeId;
    }

    //called trough the Field class, containereditor uses these
    protected void setReadId(String id) {
        this.readId = id;
    }

    protected void setWriteId(String id) {
        this.writeId = id;
    }
    
    public String getBeanId() {
    	return beanId;
    }
    
    public FieldInfo setBeanId(String beanId) {
    	this.beanId = beanId;
    	return this;
    }

    /**
     * Decorator methods called by the decorator FieldImpl
     */
    protected boolean hasBeenEdited(FieldImpl field) throws FormatterException {
        if(!editor.hasEditValue(field))
            return false;
        Object model = field.getModel();
        Object modelValue = modifier.getValue(model);
        Object editValue = editor.getEditValue(field);
        if(editValue == null) {
            if(modelValue == null)
                return false;
            else
                return true;
        } else
            return !editValue.equals(modelValue);
    }

    /**
     * Decorator methods called by the decorator FieldImpl
     */
    protected boolean isEditValid(FieldImpl field) {
        Collection errors = this.validate(field);
        return (errors.isEmpty());
    }


    /**
     * Decorator methods called by the decorator FieldImpl
     */
    protected boolean commit(FieldImpl field) {
        //not edited, then it's not committed either
        if(field.hasBeenEdited()==false)
            return false;
        
        if(isEditValid(field)) {
            Object model = field.getModel();
            try {
                this.modifier.setValue(model, editor.getEditValue(field));
            } catch(FormatterException fe) {
                //I'll assume that this shouldn't happen, let's make sure though.
                //Reason is that isEditValid should return false if editor.getEditValue(field)
                // throws FormatterException.
                throw new RuntimeException(
                    "Unexpected error settings the edit value to the model",
                    fe);
            }
            return true;
        }
        return false;
    }

    /**
     * Decorator methods called by the decorator FieldImpl
     */
    protected String getEditor(FieldImpl field) {
        return this.editor.getEditor(field);
    }


    /**
     * Decorator methods called by the decorator FieldImpl
     */
    protected Collection validate(FieldImpl field) {
        Collection messages = new LinkedList();
        try {
            //editor should have a value when validate is called
            if(!this.editor.hasEditValue(field))
                throw new RuntimeException("validate called even if no editValue is present which to validate");
            
            
            //if a field is already reverted no need to check it's validity
            if(!field.isReverted()) {
                //validates with editor's formatter
                editor.getEditValue(field);
                //no problems, if we got this far
            }
        } catch(FormatterException e) {
            messages.add(e.getMessage());
        }
        return messages;
    }

    /**
     * Decorator methods called by the decorator FieldImpl
     */
    protected Object getModelValue(FieldImpl field) {
        Object model = field.getModel();
        return this.modifier.getValue(model);
    }
}