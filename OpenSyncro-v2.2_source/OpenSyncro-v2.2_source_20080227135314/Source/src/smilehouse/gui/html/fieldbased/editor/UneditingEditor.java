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

/**
 * UneditingEditor.java
 * 
 * An editor that doesn't edit anything. Just displays the model value.
 * 
 * Created: Thu Mar 11 14:07:41 2004
 */

public class UneditingEditor implements WebEditor {
    public UneditingEditor() {

    }

    public boolean hasEditValue(EditorResources editorResources) throws FormatterException {
        return false; // No, we don't have one.
    }

    public Object getEditValue(EditorResources editorResources) throws FormatterException {
        return editorResources.getModelValue(); // We DON'T HAVE an edit value! Ever! GO AWAY!
    }

    public String getEditor(EditorResources editorResources) {
        Object value = null;
        try {
            value = editorResources.getModelValue();
        } catch(Exception hardcoreCodersCatchEverything) {
            //TODO who did this and why. This requires a comment, or should this be removed?
        }

        return value != null ? value.toString() : "null";
    }

} // UneditingEditor