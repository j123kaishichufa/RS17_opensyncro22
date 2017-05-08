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
 * Transition for a finite state machine. This class defines a bit more
 * complex sort of transition than usual, for it might read any number of characters.
 * The transition consists of the following:
 * 
 *  - match method that is used for recognizing which transition will be selected next.
 * 
 *  - doTransition-method that is called for the selected Transition. The method will
 *    probably read some number of characters and then return the next State for the
 *    state machine.  
 * 
 * For example the match method of XML "elementStart"-transition states that the first character must
 * be '<' and the second must be a letter.
 * The doTransition method for "elementStart" reads
 * letters and numbers (the name of the element) and stops when it encounters some other
 * kind of character. Then it returns the state waitParameters.
 */
interface Transition {
    
    /** 
     * Constant for the match-method.
     * match should return this value when it is absolutely sure that this transition doesn't match.
     * After returning this value, this transition will be discarded from the transition resolution.
     */
    public static final int NO_MATCH = -1;
    /** 
     * Constant for the match-method. 
     * match should return this value if the character in the index matches, but there are still some
     * unmatched characters.
     */
    public static final int PARTIAL_MATCH = 0;
    /** 
     * Constant for the match-method.
     * match should return this value if the character matches and there are now characters unmatched.
     * The transition will be selected after returning this. 
     */
    public static final int MATCH = 1;
    
    /**
     * Match the transition. Every transitions should always return either MATCH or NO_MATCH
     * for some finite index or else the state machine will get stuck in an infinite loop.
     * 
     * @param ch Character to match
     * @param index Character's index
     * @return One of the MATCH constants.
     * 
     * @see Transition#NO_MATCH
     * @see Transition#PARTIAL_MATCH
     * @see Transition#MATCH
     */
    public int match(int ch, int index);

    /**
     * Read the characters associated with this transition. If transition reads a character that doesn't
     * belong to it, it should be pushed back.
     * 
     * @param in The next character returned by this will be the character after the one for which
     *           this transition returned MATCH.
     * 
     * @return The next state, or null if the state should remain the same.
     */
    public State doTransition(CharInput in);
    
    /**
     * Return the name of the transition. For testing and debugging.
     * @return
     */
    public String getName();
}
