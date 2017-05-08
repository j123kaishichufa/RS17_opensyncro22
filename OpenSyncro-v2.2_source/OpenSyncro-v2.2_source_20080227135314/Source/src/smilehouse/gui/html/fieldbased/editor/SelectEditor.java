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
import java.util.Iterator;
import java.util.LinkedList;

import smilehouse.gui.html.fieldbased.formatter.FormatterException;
import smilehouse.tools.template.Template;
import smilehouse.util.LabelResource;

/**
 * Editor showing a html form select-input.
 */
public class SelectEditor extends AbstractEditor {
    private Collection options;

    /**
     * Default constructor using a template from defaulttemplates.
     */
    public SelectEditor() {
        super(Template.load(SelectEditor.class
            .getResourceAsStream("defaulttemplates/selecteditor.html"), DEFAULT_TEMPLATE_ENCODING));

        this.options = new LinkedList();
    }

    /**
     * Constructor for changing the template
     */
    public SelectEditor(Template template) {
        super(template);
        this.options = new LinkedList();
    }

    /**
     * Add options to the select-editor. These are the possible values of the editor.
     */
    public void addOption(SelectOption option) {
        this.options.add(option);
    }

    /**
     * Adds multiple options at once, the Collection should contain SelectOption-instances.
     */
    public void addOptions(Collection opts) {
        this.options.addAll(opts);
    }




    /** returns the only selected value. */
    protected Collection getChosen(EditorResources editorResources){
        Collection chosen = new LinkedList();
        chosen.add(editorResources.getModelValue());
        return chosen;
    }

    /**
     * @return html representation of the editor
     */
    public String getEditor(EditorResources editorResources, Template template) {
        writeProperties(template);
        LabelResource labelResource = editorResources.getResource();
        template.setVariable("id", editorResources.getWriteId());

        template.setVariable("description", editorResources.getDescription());

        template.setVariable("cssClass", "");
        if(useColumns()) {
            template.setVariable("cols", ""+getColumns());
        }

            Collection chosen = getChosen(editorResources);


            Template optionBlock = template.getBlock("option");
            Iterator iter = this.options.iterator();
            for(int i=0; i<this.options.size(); i++) {
                SelectOption opt = (SelectOption) iter.next();
                optionBlock.setVariable("id", editorResources.getWriteId());

                //write the option's description
                String desc = opt.getLabel(labelResource);
                optionBlock.setVariable("description", desc);
                try {
                    //write the option's value
                    String optValue;
                    if(formatter == null)
                        optValue = opt.getValue().toString();
                    else
                        optValue = formatter.valueToString(opt.getValue(), editorResources);
                    optionBlock.setVariable("value", optValue);



                    //check if this option is currently selected/chosen
                    //here's a small trick, in some cases the opts values
                    //might not be directly of same type as the
                    //propertytype, example the optvalues might be Strings
                    //containing integers and propertytype might be
                    //integer. So we use the formatter here before
                    //comparison to convert the optString's to correct format.
                    if(formatter == null && chosen.contains(opt.getValue()))
                        optionBlock.getBlock("selected").write();
                    else if(formatter != null) {
                        String tmp = formatter.valueToString(opt.getValue(), editorResources);
                        Object tmp2 = formatter.stringToValue(tmp, editorResources);
                        if(chosen.contains(tmp2))
                            optionBlock.getBlock("selected").write();
                    }

                } catch(FormatterException e) {
                    //mark option as erroneus, set error message to the description
                    optionBlock.setVariable("cssClass", "error");
                    optionBlock.setVariable("description", desc + " :"
                            + e.getMessage());
                }
                
                //useful for CheckBoxGroupEditor
                if(useColumns() && (i + 1) % getColumns() == 0) {
                    optionBlock.getBlock("rowchange").write();
                }
                
                optionBlock.write();
            }
            super.writeEditorErrors(template, editorResources);

        template.write();
        return template.toString();
    }

    protected int getColumns() {
        return 1;
    }
    protected boolean useColumns() {
        return false;
    }
    
}