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

/*
 * Created on Oct 7, 2004
 */
package smilehouse.tools.template;

import java.util.HashMap;
import java.util.Map;

/**
 * ValueSourceIndexer is a helper class that is used for removing the tedious if-elseif-structures
 * that occur from searching for the correct tag value from ValueSource implementations. It also
 * offers a simple way to express synonym tags.
 */
public class ValueSourceIndexer {
    Map productTagIndexes;

    /**
     * Creates a new ValueSourceIndexer
     * 
     * @param tags Tags for indexing
     * @param synonyms synonyms that are matched to the given tags. format is
     *        {{"synonym","actualtag"},{"synonym2", "actualtag2"}};
     */
    public ValueSourceIndexer(String[] tags, String[][] synonyms) {
        productTagIndexes = new HashMap();
        for(int i = 0; i < tags.length; i++)
            productTagIndexes.put(tags[i], new Integer(i));

        // -----------------------------------------
        // Some synonyms for backward compatibility
        // -----------------------------------------
        for(int i = 0; i < synonyms.length; i++)
            productTagIndexes.put(synonyms[i][0], productTagIndexes.get(synonyms[i][1]));

    }

    /**
     * 
     * @param tag name
     * @return tag's index, ie. the index position of the tag in the tags-array.
     */
    public int getTagIndex(String tag) {
        Integer integer = (Integer) productTagIndexes.get(tag);
        if(integer == null)
            return -1;
        else
            return integer.intValue();
    }

}