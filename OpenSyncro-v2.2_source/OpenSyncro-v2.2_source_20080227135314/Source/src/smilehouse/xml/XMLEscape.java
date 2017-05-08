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
 * Created on 15.12.2004
 */
package smilehouse.xml;

public class XMLEscape {
    
    /**
     * XML escapes a string. <br>
     * '<' --> &lt; <br>
     * '>' --> &gt; <br>
     * '&' --> &amp; <br>
     * '"' --> &quot; <br>
     * 
     * @param str A string that might contain some troublesome characters.
     * @return A string that is strikingly similar to the string which was given as the parameter 'str'
     *         except for these troublesome characters which are replaced by their XML entity counterparts.
     *         Might even be the very same string that was given as the parameter 'str' if it was found to
     *         be completely free of these troublesome characters.
     */
    public static String escape(String str) {
        // ------------------------------------------------
        // Check if our services are needed for this String
        // ------------------------------------------------
        boolean needsEscaping = false;
        for(int i=0; i<str.length(); i++) {
            char c = str.charAt(i);
            if(c == '<' || c == '>' || c == '&' || c == '"') {
                needsEscaping = true;
                break;
            }
        }
        if(!needsEscaping)
            return str;
        // ----------------
        // The great escape
        // ----------------
        StringBuffer buff = new StringBuffer();
        for(int i=0; i<str.length(); i++) {
            char c = str.charAt(i);
            switch(c) {
            	case '<' : buff.append("&lt;"); break;
            	case '>' : buff.append("&gt;"); break;
            	case '&' : buff.append("&amp;"); break;
            	case '"' : buff.append("&quot;"); break;
            	default : buff.append(c);
            }
        }
        return buff.toString();
    }
}