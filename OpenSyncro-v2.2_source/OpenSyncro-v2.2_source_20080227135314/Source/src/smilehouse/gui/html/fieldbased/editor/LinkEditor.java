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

import smilehouse.gui.html.fieldbased.formatter.FormatterException;
import smilehouse.tools.template.Template;

/**
 * LinkEditor.java
 */

public class LinkEditor implements WebEditor {

    private Template baseTemplate;
    private String href;
    private String parameterName;
    private String target;

    public LinkEditor() {
        this.baseTemplate = Template.load(
            TextEditor.class.getResourceAsStream("defaulttemplates/linkeditor.html"),
            AbstractEditor.DEFAULT_TEMPLATE_ENCODING);
        this.target = "_blank";
    }


    public void setHref(String href) {
        this.href = href;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public boolean hasEditValue(EditorResources editorResources) throws FormatterException {
        return false; // No, we don't have one.
    }

    public Object getEditValue(EditorResources editorResources) throws FormatterException {
        return editorResources.getModelValue(); // We don't have an edit value
    }

    public String getEditor(EditorResources editorResources) {
        Template template = baseTemplate.copyStructure();

        Object valueObject = null;
        valueObject = editorResources.getModelValue();

        if(valueObject != null) {
            String value = valueObject.toString();
            // TODO: URL encode value
            template.setVariable("value", value);
        }
        template.setVariable("href", href);
        template.setVariable("parameterName", parameterName);
        template.setVariable("linkText", editorResources.getDescription());
        template.setVariable("target", target);

        template.write();
        return template.toString();
    }

}// LinkEditor
