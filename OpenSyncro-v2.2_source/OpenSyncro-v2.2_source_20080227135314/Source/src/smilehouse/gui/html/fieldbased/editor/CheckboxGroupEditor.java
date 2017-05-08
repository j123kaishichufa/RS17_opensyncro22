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
import java.util.LinkedList;

import javax.servlet.http.HttpServletRequest;

import smilehouse.gui.html.fieldbased.formatter.FormatterException;
import smilehouse.tools.template.Template;


/**
 * Editor showing a bunch of checkboxes.
 */
public class CheckboxGroupEditor extends SelectEditor {

    private int cols;
    /**
     * Default constructor using a template from defaulttemplates.
     */
    public CheckboxGroupEditor() {
        super(Template.load(
            CheckboxGroupEditor.class
                .getResourceAsStream("defaulttemplates/checkboxgroupeditor.html"),
            DEFAULT_TEMPLATE_ENCODING));
        this.cols=1;
    }

    /**
     * Constructor for changing the template
     */
    public CheckboxGroupEditor(Template template) {
        super(template);
        this.cols=1;
    }

    /** return the selected values. */
    protected Collection getChosen(EditorResources editorResources){
        Collection chosen = (Collection) editorResources.getModelValue();
        return chosen;
    }

    protected int getColumns() {
        return this.cols;
    }
    public void setColumns(int cols) {
        this.cols=cols;
    }
    protected boolean useColumns() {
        return true;
    }
    /***********************************************************************************************
     * Overrides the AbstractEditor-implementation. This returns a collection of the selected
     * values. Selected values are created with the formatter if the formatter is set. If formatter
     * is null, the values will be String-objects.
     */
    public Object getEditValue(EditorResources editorResources) {
        HttpServletRequest request = editorResources.getRequest();

        String id = editorResources.getReadId();
        String[] reqValues = request.getParameterValues(id);

        Collection values = new LinkedList();
        for(int i = 0; reqValues != null && i < reqValues.length; i++) {
            if(formatter == null) {
                if(reqValues[i] != null)
                    values.add(reqValues[i]);
            } else {
                try {
                    if(reqValues[i] != null) {
                        Object o = formatter.stringToValue(reqValues[i], editorResources);
                        if(o != null)
                            values.add(o);
                    }
                } catch(FormatterException e) {
                    /*
                     * illegal value in options, well its not my problem, the error should be
                     * discovered before.
                     */
                }
            }
        }
        return values;
    }
}