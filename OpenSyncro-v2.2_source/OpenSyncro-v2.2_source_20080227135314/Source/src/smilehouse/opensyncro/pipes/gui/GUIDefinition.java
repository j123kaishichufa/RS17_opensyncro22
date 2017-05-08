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

package smilehouse.opensyncro.pipes.gui;

import java.util.Hashtable;

import smilehouse.gui.html.fieldbased.FieldInfo;
import smilehouse.gui.html.fieldbased.GUIContext;
import smilehouse.gui.html.fieldbased.editor.TextAreaEditor;
import smilehouse.gui.html.fieldbased.editor.TextEditor;
import smilehouse.gui.html.fieldbased.model.ModelModifier;

/**
 * GUIDefinition.java
 * 
 * Created: Fri May 28 11:13:23 2004
 */

public abstract class GUIDefinition {
    private Hashtable fieldInfos = new Hashtable();
    private GUIContext guiContext = new GUIContext();

    protected void addField(String id, FieldInfo field) {
        fieldInfos.put(id, field);
        guiContext.addFieldInfo(field);
    }

    public FieldInfo getField(String id) {
        return (FieldInfo) fieldInfos.get(id);
    }

    public GUIContext getGUIContext() {
        return guiContext;
    }

    protected void addSimpleTextFieldForComponent(String attributeName, String label, int fieldSize)
            throws Exception {
        ModelModifier modifier = new ComponentAttributeModifier(attributeName);
        TextEditor editor = new TextEditor();
        editor.setSize(fieldSize);

        FieldInfo fieldInfo = new FieldInfo(attributeName, label, modifier, editor);

        addField(attributeName, fieldInfo);
    }

    protected void addSimpleTextAreaFieldForComponent(String attributeName, String label, int cols, int rows)
            throws Exception {
        ModelModifier modifier = new ComponentAttributeModifier(attributeName);
        TextAreaEditor editor = new TextAreaEditor();
        editor.setCols(cols);
        editor.setRows(rows);

        FieldInfo fieldInfo = new FieldInfo(attributeName, label, modifier, editor);

        addField(attributeName, fieldInfo);
    }

} // GUIDefinition