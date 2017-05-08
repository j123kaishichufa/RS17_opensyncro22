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

import javax.servlet.http.HttpServletRequest;

import smilehouse.util.LabelResource;

/**
 * Interface to retrieve information required by Editors. Known implementations: FieldImpl and
 * ContainerFieldImpl.
 */
public interface EditorResources {
    /** Return the request that contains the editor editvalues */
    public HttpServletRequest getRequest();

    /** Return the model modified by this editor */
    public Object getModel();

    /** Return the language dependent resources according to the users preference. */
    public LabelResource getResource();

    /** @return the identifier of this field. */
    public String getId();

    /** @return the identifier used to get the current edit value from the request. */
    public String getReadId();

    /**
     * @return the identifier used when writing the editor to html. This might differ from ReadId
     *         when the field is in a container and it's row position has changed.
     */
    public String getWriteId();

    /**
     * Localized text used as the editors description of the field in the html-representation.
     */
    public String getDescription();

    /**
     * The modified Objects edited value.
     */
    public Object getModelValue();

    /**
     * @return localized error messages (as strings) that occured when processing the field in the
     *         request. These messages are shown in the html-editor. In case there are no error
     *         messages this returns an empty collection.
     */
    public Collection validate();

    /**
     * @return true if revert has been called on this field. This is needed to know, because if
     *         revert is called the edit value is thrown away and then in the editor's html
     *         representation the field's value will be the same as the object's value.
     */
    public boolean isReverted();

    /**
     * @return true if commit has been done on this field. This is needed to know so that the editor
     *         knows to show the edited value from the model, not the value from the request, which
     *         might be different.
     */
    public boolean isCommitted();
}