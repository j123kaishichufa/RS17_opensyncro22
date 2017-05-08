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
 * State is a collection of possible Transitions.
 * The doTransition method selects one transition based on the character input,
 * executes it and returns the new state.
 */
public class State {
    private Transition[] transitions;
    private String name;
    
    /**
     * 
     * @param name Name of this state. For testing and debugging purposes.
     */
    public State(String name) {
        this.transitions = new Transition[0];
        this.name = name;
    }
   
    /**
     * Set the transitions that are possible from this state.
     * 
     * @param transitions
     */
    public void setTransitions(Transition[] transitions) {
        this.transitions = transitions;
    }
    
    /**
     * Tries to select one of the state's transitions based on the character input.
     * If a transition is found, it's doTransition method is called.
     * 
     * @param in
     * @return The state returned by the transition or this state, if the transition didn't return state.
     * 
     * @throws NoMatchingTransitionException None of the transitions from this state matched the input.
     *                                       The input is probably malformed.
     */
    public State doTransition(CharInput in) throws NoMatchingTransitionException {
        
        if(transitions == null)
            throw new IllegalStateException("No transitions set!");
                
        int discardedCount = 0;
        boolean[] discarded = new boolean[transitions.length];        
        for(int i=0; i<discarded.length; i++)
            discarded[i] = false;
        
        int index = 0;
        while(discardedCount < transitions.length) {
            int ch = in.nextChar();
            for(int i=0; i<transitions.length; i++) {
                if(!discarded[i]) {
                    int matchCode = transitions[i].match(ch, index);
                    switch(matchCode) {
                    
                    	case Transition.MATCH :
                    	    // Match! Do the transition
                    	    State next = transitions[i].doTransition(in);
                    		return next != null ? next : this;
                    	
                    	case Transition.NO_MATCH :
                    	    // No match, discard the transition
                    	    discarded[i] = true;
                    		discardedCount++;
                    		break;
                        case Transition.PARTIAL_MATCH :
                            // Just continue...
                            break;
                        default:
                            // Some transition is acting weird! 
                            throw new RuntimeException("Transition "+transitions[i].getName()+" returned unknown match code: "
                                + matchCode);
                    }
                }
            }
            index++;
        }
        
        // ---------------------------------------------
        // None of the transitions matched, report error
        // ---------------------------------------------
        StringBuffer message = new StringBuffer("Could not match any of the expected: ");
        for(int i=0; i<transitions.length; i++) {
            if(i>0) {
                if(i == transitions.length-1)
                    message.append(" or ");
                else
                    message.append(", ");
            }
            message.append(transitions[i].getName());
        }
        
        throw new NoMatchingTransitionException(message.toString());
    }
    
    /**
     * Return the name of this state.
     * 
     * @return Human readable name for the state.
     */
    public String getName() {
        return name;
    }
}
