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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import smilehouse.gui.html.fieldbased.formatter.Formatter;
import smilehouse.gui.html.fieldbased.formatter.FormatterException;
import smilehouse.tools.template.Template;

public abstract class AbstractEditor implements WebEditor {
    Template baseTemplate;
    Formatter formatter;
    Map properties;

    public static final String DEFAULT_TEMPLATE_ENCODING = "ISO-8859-1";

    protected AbstractEditor(Template template) {
        this.baseTemplate = template;
        this.properties = new HashMap();
    }

    public AbstractEditor setFormatter(Formatter formatter) {
        this.formatter = formatter;
        return this;
    }

    protected void setProperty(String name, String value) {
        properties.put(name, value);
    }

    protected String getProperty(String name) {
        return (String) properties.get(name);
    }

    protected int getIntProperty(String name) {
        String ret = getProperty(name);
        try {
            return Integer.parseInt(ret);
        } catch(Exception e) {
            throw new RuntimeException(
                "Editor hasn't been coded properly, problem getting a property that should be always available.");
        }
    }

    public String getEditor(EditorResources editorResources) {
        Template template = baseTemplate.copyStructure();
        return this.getEditor(editorResources, template);
    }



    public boolean hasEditValue(EditorResources editorResources) {
        if(editorResources.isCommitted() || editorResources.isReverted())
            return false;
        try {
            return (getEditValue(editorResources) != null);
        } catch(FormatterException e) {
            //something caused formatter exception so there should be an edit value
            return true;
        }
    }

    public Object getEditValue(EditorResources editorResources) throws FormatterException {
        String val = getRawEditValue(editorResources);
        if(val == null)
            return null; //there is no edit value.
        if(this.formatter == null)
            return val;
        Object ret = formatter.stringToValue(val, editorResources);
        return ret;
    }


    protected abstract String getEditor(EditorResources editorResources, Template template);


    protected void writeProperties(Template template) {
        Set entries = properties.entrySet();
        for(Iterator iter = entries.iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            template.setVariable((String) entry.getKey(), (String) entry.getValue());
        }
    }


    protected String getEditorValue(EditorResources editorResources) {
        try {
            //this can prove us there is no edit value
            boolean hasEditValue = hasEditValue(editorResources);
            if(hasEditValue)
                return getEditValueString(editorResources);
            else
                return getModelValueString(editorResources);
        } catch(FormatterException le) {
            /* if we got here, there is an edit value, it's just invalid, return the invalid string. */
            return getRawEditValue(editorResources);
        }
    }


    public void writeEditorErrors(Template template, EditorResources editorResources) {
        if(this.hasEditValue(editorResources)) {
            Collection errors = editorResources.validate();

            if(!errors.isEmpty()) {
                template.setVariable("cssClass", "error");
                Template errBlock = template.getBlock("errors");
                for(Iterator iter = errors.iterator(); iter.hasNext();) {
                    Template err = errBlock.getBlock("error");
                    err.setVariable("message", ((String) iter.next()));
                    err.write();
                }
                errBlock.write();
            }
        }
    }
    /*
    public static void writeEditorErrorMessage(Template template, String message) {
        if(message != null) {
            template.setVariable("cssClass", "error");
            Template errBlock = template.getBlock("errors");
            Template err = errBlock.getBlock("error");
            err.setVariable("message", message);
            err.write();
            errBlock.write();
        }
    }
    */
    

    /* Return the raw string from the request (assumes not array) */
    private String getRawEditValue(EditorResources editorResources) {
        HttpServletRequest request = editorResources.getRequest();
        String id = editorResources.getReadId();
        return request.getParameter(id);
    }

    /* Return the request string formatted (string->object->string) with our formatter */
    private String getEditValueString(EditorResources editorResources) throws FormatterException {
        Object val = getEditValue(editorResources);
        if(val == null)
            return null;
        if(this.formatter == null)
            return val.toString();
        return formatter.valueToString(val, editorResources);
    }
    
    private String getModelValueString(EditorResources editorResources) {
        try {
            Object value = editorResources.getModelValue();
            if(this.formatter != null)
                return formatter.valueToString(value, editorResources);
            if(value == null)
                return "null";
            return value.toString();
        } catch(FormatterException le) {
            //if we can't get any value, it means the model value is not correct for this editor's
            // formatter, then report the exception message in the field value (bad?)
            return le.getMessage();
        }
    }
    
}