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
 * Represents a request-specific object of a field. Field objects are created from FieldInfos.
 * FieldInfo's can be usually created without request-specific information, and Field-objects can be
 * created from FieldInfos by giving them the request-specific information required.
 * 
 * Field is the public interface for accessing fields to the users of the GUI-package.
 */
public interface Field {
    /**
     * @return the id used to identify this field. Every id in a GUIContext must be unique.
     */
    public String getId();

    /**
     * @return true if there is a valid edit value and the value is different from the value of the
     *         model. Note that after commit has been called, this should always return true.
     */
    public boolean hasBeenEdited();

    /**
     * @return true if the field's new value (the edit value) is valid according to the formatter of
     *         this field (if one has been designated). Valid means that the value can be set to the
     *         object without an exception thrown in the process.
     */
    public boolean isEditValid();

    /**
     * Sets the edit value from the editor as the modified object's value. Only valid values will be
     * committed.
     */
    public void commit();

    /**
     * @return true if field has been committed (= commit has been called and field value was
     *         valid.)
     */
    public boolean isCommitted();

    /**
     * Discards the field's edit value. This will result the editor to show the Modified object's
     * value in the editor instead of the current edit value.
     */
    public void revert();

    /**
     * @return true if revert has been called on this field.
     */
    public boolean isReverted();

    /**
     * @return a html representation of this field's editor.
     */
    public String getEditor();

}