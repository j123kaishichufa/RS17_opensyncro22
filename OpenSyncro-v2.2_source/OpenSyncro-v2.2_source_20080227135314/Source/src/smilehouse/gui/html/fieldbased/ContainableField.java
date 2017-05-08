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

/**
 * Describes requirements for a field that can be inserted into a ContainerEditor. <br>
 * <br>
 * 
 * These methods are used by the ContainerEditor. All fields that can be set in a ContainerEditor
 * musy implement this interface. A default implementation is provided with FieldImpl.
 * 
 * These are required because the container contains only fields for one row, so these methods are
 * used to configure the field to operate with multiple rows.
 */
public interface ContainableField {

    /**
     * Sets the id used for reading the editvalue of the editor.
     */
    public void setReadId(String id);

    /**
     * Sets the id used to identify the editor's value on the next request. This id will be written
     * to the page when rendering the editor to html. <br>
     * <br>
     * Note: This might be different from readId if the containerEditor's collection of values has
     * changed (changed order, deleted items or added items.)
     */
    public void setWriteId(String id);

    /**
     * Sets the currently used model for the Editor, with a container this changes for every row.
     */
    public void setModel(Object model);
}