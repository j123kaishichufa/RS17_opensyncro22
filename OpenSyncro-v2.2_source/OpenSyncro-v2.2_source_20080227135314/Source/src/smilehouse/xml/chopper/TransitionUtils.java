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

package smilehouse.xml.chopper;

/**
 * Utility class to ease implementation of Transitions.
 */
class TransitionUtils {

        
    /**
     * Method to help matching the transition.
     * Can be used with transitions identified with a constant string (such as XML comment which always starts with "<!--").
     * 
     * @param string
     * @param ch
     * @param index
     * @return Some of the MATCH constants of the Transition interface.
     */
    public static int match(String string, int ch, int index) {
        if(index > string.length()-1)
            return Transition.NO_MATCH;
        boolean matches = string.charAt(index) == ch;
        if(matches) {
            return index < string.length()-1 ? Transition.PARTIAL_MATCH : Transition.MATCH;
        }
        else
            return Transition.NO_MATCH;
    }

    /**
     * Method for searching a terminating string.
     * Reads the input until the given string is found and not a character more.
     * 
     * @param string 
     * @param input
     * @return true if the string was found or false if end of the stream was encountered.
     */
    public static boolean search(String string, CharInput input) {
        
        // This is some kind of simple improvised brute force string search algorithm...
        // TODO: replace with faster algorithm, if you don't have anything better to do.
        
        // Buffer for the last read characters.
        char[] inputChars = new char[string.length()];
        
        // -------------------------------------------------------------------------------
        // First read the first characters to inputChars buffer, so we can start matching.
        // -------------------------------------------------------------------------------
        for(int i=0; i<inputChars.length; i++) {
            int ch = input.nextChar();
            if(ch == -1) {
                return false;
            }
            inputChars[i] = (char) ch;            
        }
        
        // Offset to inputChars buffer pointing to the index where to start matching
        int offset = 0;
        
        // -------------------------------------------------------------------------------------
        // Try to match and proceed to the next char until match is found or eos is encountered.
        // -------------------------------------------------------------------------------------
        while(true) {
            for(int i=0; i<inputChars.length; i++) {
                int index = (i+offset) % inputChars.length;
                if(string.charAt(i) != inputChars[index]) {
                    break;
                }
                else if(i == inputChars.length-1) {
                    return true; // MATCH!!
                }
            }
            int ch = input.nextChar();
            if(ch == -1)
                return false;
            inputChars[offset] = (char) ch;
            offset = (offset + 1) % inputChars.length;
        }
    }
}
