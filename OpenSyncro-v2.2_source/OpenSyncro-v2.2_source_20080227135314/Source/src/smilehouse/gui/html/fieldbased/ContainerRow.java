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
 * A protected class used by the containerfields. Collection of row-instances are stored in the
 * ContainerFieldImpl. This is required because the Field objects are recycled, so one field's
 * status and indexing must be kept aside.
 */
class ContainerRow {
    private int readIndex;
    private Object bean;

    /**
     * Creates a new ContainerRow
     * 
     * @param readIndex the current index of the given object in the collection
     * @param bean a list item in the collection modified by an editor.
     */
    public ContainerRow(int readIndex, Object bean) {
        this.readIndex = readIndex;
        this.bean = bean;
    }

    /**
     * @return the index of this bean object before changing the collection.
     */
    public int getReadIndex() {
        return this.readIndex;
    }

    /**
     * @return The bean object whose index needed to be stored.
     */
    public Object getBean() {
        return this.bean;
    }
}