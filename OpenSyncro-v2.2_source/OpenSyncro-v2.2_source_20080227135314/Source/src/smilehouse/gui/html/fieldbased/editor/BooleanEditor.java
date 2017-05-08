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

import javax.servlet.http.HttpServletRequest;

import smilehouse.tools.template.Template;
import smilehouse.util.Utils;

/**
 * Editor showing a html form checkbox.
 */
public class BooleanEditor extends AbstractEditor {
    /**
     * Default constructor using a template from defaulttemplates.
     */
    public BooleanEditor() {
        super(Template.load(BooleanEditor.class
            .getResourceAsStream("defaulttemplates/booleaneditor.html"), DEFAULT_TEMPLATE_ENCODING));

    }

    /**
     * Constructor for changing the template
     */
    public BooleanEditor(Template template) {
        super(template);
    }

    /**
     * @return html representation of the editor
     */
    public String getEditor(EditorResources editorResources, Template template) {
        writeProperties(template);

        template.setVariable("id", editorResources.getWriteId());

        template.setVariable("value", Boolean.TRUE.toString());

        template.setVariable("description", editorResources.getDescription());

        template.setVariable("cssClass", "");

        try {
            Object modelValue = editorResources.getModelValue();
            Boolean bool = (Boolean) modelValue;
            if(bool.booleanValue())
                template.setVariable("checked", "checked");
        } catch(Throwable e) {
            //this is an ugly precaution...
            template.setVariable("checked", "\">" + Utils.getStackTrace(e));
        }

        super.writeEditorErrors(template, editorResources);


        template.write();
        return template.toString();
    }

    /** Boolean always has an edit value so this always return's true. */
    public boolean hasEditValue(EditorResources editorResources) {
        return true;
    }

    /**
     * Returns Boolean.TRUE if the request has a value with the field's id. Otherwise
     * Boolean.FALSE..
     */
    public Object getEditValue(EditorResources editorResources) {
        HttpServletRequest request = editorResources.getRequest();
        String id = editorResources.getReadId();
        if(request.getParameter(id) != null)
            return Boolean.TRUE;
        else
            return Boolean.FALSE;
    }

    /** This isn't actually used in the boolean editor... throws a runtimeexception... */
    protected String getEditorValue(EditorResources editorResources) {
        throw new RuntimeException("BooleanEditor.getEditorValue shouldn't be called.");
    }

}