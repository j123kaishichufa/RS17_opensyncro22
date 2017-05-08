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

/*
 * Created on May 9, 2005
 */
package smilehouse.gui.html.fieldbased.editor;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import smilehouse.gui.html.fieldbased.editor.AbstractEditor;
import smilehouse.gui.html.fieldbased.editor.EditorResources;
import smilehouse.gui.html.fieldbased.editor.TextEditor;
import smilehouse.gui.html.fieldbased.editor.WebEditor;
import smilehouse.gui.html.fieldbased.formatter.FormatterException;
import smilehouse.tools.template.Template;

/**
 * ConverterEditButtonEditor.java
 *
 * LinkButtonEditor with multiple HTTP query parameters 
 */

public class ConverterEditButtonEditor implements WebEditor {

    private final String PARAMETER_ENCODING = "UTF-8";
    private Template baseTemplate;
    private String href;
    private List parameterNames;
    private String target;

    public ConverterEditButtonEditor() {
        this.baseTemplate = Template.load(
            TextEditor.class.getResourceAsStream("defaulttemplates/convertereditbuttoneditor.html"),
            AbstractEditor.DEFAULT_TEMPLATE_ENCODING);
        this.target = "_blank";
    }

    // Create ConverterLinkEditor with template HTML passed as a String parameter
    public ConverterEditButtonEditor(String editorTemplate) {
        this.baseTemplate = Template.load(
            new ByteArrayInputStream(editorTemplate.getBytes()),
            AbstractEditor.DEFAULT_TEMPLATE_ENCODING);
        this.target = "_blank";
    }

    public void setHref(String href) {
        this.href = href;
    }

    public void setParameterNames(List parameterNames) {
        this.parameterNames = parameterNames;
    }
    
    public void setTarget(String target) {
        this.target = target;
    }

    public boolean hasEditValue(EditorResources editorResources) throws FormatterException {
        return false; // No, we don't have one.
    }

    public Object getEditValue(EditorResources editorResources) throws FormatterException {
        return editorResources.getModelValue(); // We DON'T HAVE an edit value! Ever! GO AWAY!
    }

    public String getEditor(EditorResources editorResources) {
        Template template = baseTemplate.copyStructure();

        Object valueObject = null;

        valueObject = editorResources.getModelValue();

        if(valueObject != null) {

            Map params = (HashMap) valueObject;

            for(Iterator iter = params.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();
                try {
                    template.setVariable(URLEncoder.encode(key, PARAMETER_ENCODING),
                        				 URLEncoder.encode(value, PARAMETER_ENCODING));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(
                        "Internal error: Unable to encode servlet parameters with "
                                + PARAMETER_ENCODING);
                }
            }
        }
        template.setVariable("href", href);
        template.setVariable("buttonText", editorResources.getDescription());
        template.setVariable("target", target);

        template.write();
        return template.toString();
    }

}// ConverterEditButtonEditor
