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

package smilehouse.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

/**
 * RecallingList.java <br>
 * A list that remembers which elements have been added or removed.
 * 
 * NOTES: This class doesn't expect that you add and remove the same element.
 * 
 * Created: Mon Mar 8 16:18:34 2004
 */

public class RecallingList extends LinkedList {

    Set added;
    Set removed;

    public RecallingList() {
        super();
        added = new HashSet();
        removed = new HashSet();
    }

    public RecallingList(Collection coll) {
        // --------------------------------------------------------------------------
        // I don't want to call super(coll) because it would call this class's addAll
        // and cause all kinds of trouble
        // --------------------------------------------------------------------------
        super();
        super.addAll(coll);

        added = new HashSet();
        removed = new HashSet();
    }

    public int getNumberOfAdded() {
        return added.size();
    }

    public int getNumberOfRemoved() {
        return removed.size();
    }

    public Iterator addedIterator() {
        return added.iterator();
    }

    public Iterator removedIterator() {
        return removed.iterator();
    }

    public void forget() {
        added.clear();
        removed.clear();
    }

    public boolean add(Object element) {
        added.add(element);
        return super.add(element);
    }

    public void add(int index, Object element) {
        super.add(index, element);
        added.add(element);
    }

    public void addFirst(Object o) {
        super.addFirst(o);
        added.add(o);
    }

    public void addLast(Object o) {
        super.addLast(o);
        added.add(o);
    }

    public boolean addAll(Collection c) {
        added.addAll(c);
        return super.addAll(c);
    }

    public boolean addAll(int index, Collection c) {
        if(added == null)
            added = new HashSet();
        added.addAll(c);
        return super.addAll(index, c);
    }

    public Object remove(int index) {
        Object removedElement = super.remove(index);
        removed.add(removedElement);
        return removedElement;
    }

    public boolean remove(Object o) {
        boolean contained = super.remove(o);
        if(contained)
            removed.add(o);
        return contained;
    }

    /**
     * NOTE: If some of the elements are contained in the list but some aren't the RecallingList
     * still recalls all the elements in c as removed.
     */
    public boolean removeAll(Collection c) {
        boolean changed = super.removeAll(c);
        if(changed)
            removed.addAll(c);
        return changed;
    }

    public boolean retainAll(Collection c) {
        throw new UnsupportedOperationException("Sorry... I was too lazy to code this.");
    }

    public void clear() {
        removed.add(this);
        super.clear();
    }

} // RecallingList