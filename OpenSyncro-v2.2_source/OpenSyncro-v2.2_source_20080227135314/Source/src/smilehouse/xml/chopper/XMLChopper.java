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

import java.io.Reader;
import java.io.StringReader;
import java.text.ParseException;
import java.util.Stack;

import org.apache.xerces.util.XMLChar;

/**
 * Reads XML in valid blocks.
 */
public class XMLChopper {

    // --------------------------------
    // States for the XML state machine
    // --------------------------------
    private State waitDeclaration;
    private State waitDocType;
    private State waitRoot;
    private State waitAttributes;
    private State waitAttrAssign;
    private State waitAttrValue;
    private State waitElement;
    private State waitEnd;
    private State theEnd;
    
    // -------------------------------------
    // Transitions for the XML state machine
    // -------------------------------------
    private Transition whiteSpace;
    private Transition comment;
    private Transition xmlDeclaration;
    private Transition docType;
    private Transition elementStart;
    private Transition attributeStart;
    private Transition attrAssign;
    private Transition attrValue;
    private Transition elementTagEnd;
    private Transition emptyElementTagEnd;
    private Transition elementText;
    private Transition cdata;
    private Transition elementEnd;
    private Transition eos;
    
    
    private ChopperCharInput charInput;
    private Stack<Element> elementStack;
    private int elementCount;
    private State currentState;
    private int chopDepth;
    private String xmlDeclarationStr;
    private String docTypeStr;
    
    /**
     * 
     * @param reader Reader for reading the XML
     * @param chopDepth Should be at least 0, meaning that the chopper will separate root elements (in that special case
     *                               input XML is not valid, though, and you must chop elements one by one for the resulting XML to be valid)
     */
    public XMLChopper(Reader reader, int chopDepth) {
        if(reader == null) {
            throw new IllegalArgumentException("reader cannot be null!");
        }
        if(chopDepth < 0) {
            throw new IllegalArgumentException("Chop depth must be at least 0!");
        }        
        elementStack = new Stack<Element>();
        elementCount = 0;
        initStateMachine();
        charInput = new ChopperCharInput(reader);
        this.chopDepth = chopDepth;
    }
    
    private void initStateMachine() {
        
        // -------------------------------------------------------------
        // First create the states. We cannot yet set their transitions,
        // as we haven't created them yet...
        // -------------------------------------------------------------
        waitDeclaration = new State("waitDeclaration");
        waitDocType = new State("waitDocType");
        waitRoot = new State("waitRoot");
        waitAttributes = new State("waitAttributes");
        waitAttrAssign = new State("waitAttrAssign");
        waitAttrValue = new State("waitAttrValue");
        waitElement = new State("waitElement");
        waitEnd = new State("waitEnd");
        theEnd = new State("theEnd");
        
        // ------------------
        // Create transitions
        // ------------------
        
        // Whitespace transition
        whiteSpace = new Transition() {
            
            public String getName() { return "whiteSpace"; }
            
            public int match(int ch, int index) {
                if(ch != -1 && Character.isWhitespace((char) ch))
                    return MATCH;
                else
                    return NO_MATCH;
            }

            public State doTransition(CharInput in) {
                while(true) {
                    int ch = in.nextChar();
                    if(ch == -1 || !Character.isWhitespace( (char) ch)) {
                        break;
                    }
                }
                // Adjust in so that the first non-whitespace char will be next.
                in.pushBack();
                // No state change.
                return null; 
            }            
        };
        
        // Comment transition
        comment = new Transition() {

            public String getName() { return "comment"; }
            
            public int match(int ch, int index) {
                return TransitionUtils.match("<!--", ch, index);
            }

            public State doTransition(CharInput in) {
                TransitionUtils.search("-->", in);
                return null;
            }            
        };
        
        // XML declaration transition
        xmlDeclaration = new Transition() {

            public String getName() { return "xmlDeclaration"; }
            
            public int match(int ch, int index) {
                return TransitionUtils.match("<?", ch, index);
            }

            /** Also saves the declatation for later use... */
            public State doTransition(CharInput in) {
                StringBuffer decBuff = new StringBuffer("<?");
                while(true) {
                    int ch = in.nextChar();
                    if(ch == '>') {
                        decBuff.append('>');
                        xmlDeclarationStr = decBuff.toString();
                        break;
                    }
                    if(ch == -1)
                        break;
                    decBuff.append((char)ch);
                }
                // Declaration read, change to 'waitDocType' state
                return waitDocType;
            }            
        };
        
        // DOCTYPE transition
        docType = new Transition() {

            public String getName() { return "docType"; }
            
            public int match(int ch, int index) {
                return TransitionUtils.match("<!DOCTYPE", ch, index);
            }

            /** Also saves the DOCTYPE declaration for later use... */
            public State doTransition(CharInput in) {
                StringBuffer docTypeBuff = new StringBuffer("<!DOCTYPE");
                while(true) {
                    int ch = in.nextChar();
                    if(ch == '>') {
                        docTypeBuff.append('>');
                        docTypeStr = docTypeBuff.toString();
                        break;
                    }
                    if(ch == -1)
                        break;
                    docTypeBuff.append((char)ch);
                }
                // DocType read, change to 'waitRoot' state
                return waitRoot;
            }            
        };
        
        // Element Start transition
        elementStart = new Transition() {

            public String getName() { return "elementStart"; }
            
            public int match(int ch, int index) {
                switch(index) {
                	case 0:
                	    return (ch == '<') ? PARTIAL_MATCH : NO_MATCH;
                	case 1:
                	    return XMLChar.isNCNameStart(ch) ? MATCH : NO_MATCH;
                }
                return NO_MATCH;
            }

            /**
             * Reads the name of the element and pushes it to the element stack.
             */
            public State doTransition(CharInput in) {
                StringBuffer name = new StringBuffer();
                // The first char of the element name was read during the match, so we have to push it back...
                in.pushBack();
                while(true) {
                    int ch = in.nextChar();
                    if(ch == -1) {
                        // Ouch! EOS!
                        break; 
                    }
                    // Also accept colon character as we want to include the namespace prefix
                    // to the element name
                    if((XMLChar.isNCName(ch) || ch == ':')) {
                        name.append((char) ch);
                    }
                    else {
                        in.pushBack();
                        startElement(name.toString());
                        break;
                    }
                        
                }
                
                return waitAttributes;
            }            
        };
        
        // Attribute start transition
        attributeStart = new Transition() {

            public String getName() { return "attributeStart"; }
            
            public int match(int ch, int index) {
                if(XMLChar.isNCNameStart(ch))
                    return MATCH;
                else
                    return NO_MATCH;
            }

            public State doTransition(CharInput in) {                
                in.pushBack();
                StringBuffer name = new StringBuffer();
                while(true) {
                    int ch = in.nextChar();
                    if(ch == -1) {
                        // Ouch! EOS!
                        break; 
                    }
                    // Also accept colon character as we want to include the namespace prefix
                    // to the attribute name
                    if(XMLChar.isNCName(ch) || ch == ':') {
                        name.append((char) ch);
                    }
                    else {
                        in.pushBack();
                        startAttribute( name.toString() );
                        break;
                    }
                }
                return waitAttrAssign;
            }            
        };
        
        // Attribute assign sign transition
        attrAssign = new Transition() {

            public String getName() { return "attrAssign"; }
            
            public int match(int ch, int index) {
                return ch == '=' ? MATCH : NO_MATCH;
            }

            public State doTransition(CharInput in) {
                return waitAttrValue;
            }            
        };
        
        // Attribute value transition
        attrValue = new Transition() {

            public String getName() { return "attrValue"; }
            
            public int match(int ch, int index) {
                return ch == '"' ? MATCH : NO_MATCH;
            }

            public State doTransition(CharInput in) {
                StringBuffer value = new StringBuffer();
                while(true) {
                    int ch = in.nextChar();
                    if(ch != '"' && ch != -1) {
                        value.append((char) ch);
                    }
                    else {
                        valueForAttribute(value.toString());
                        break;
                    }
                }
                return waitAttributes;
            }            
        };
        
        // Element tag end transition ( plain '>' )
        elementTagEnd = new Transition() {

            public String getName() { return "elementTagEnd"; }
            
            public int match(int ch, int index) {
                return ch == '>' ? MATCH : NO_MATCH;
            }

            public State doTransition(CharInput in) {
                return waitElement;
            }
            
        };

        // Empty element tag end transition ("/>")
        emptyElementTagEnd = new Transition() {

            public String getName() { return "emptyElementTagEnd"; }
            
            public int match(int ch, int index) {
                return TransitionUtils.match("/>", ch, index);
            }

            public State doTransition(CharInput in) {
                endElement();
                // If we are back to root level, wait for end...
                if(getDepth() == 0)
                    return waitEnd;
                else
                    return waitElement;
            }            
        };
        
        // Element text transition
        elementText = new Transition() {

            public String getName() { return "elementText"; }
            
            public int match(int ch, int index) {                
                return ch != '<' && ch != '>'  && ch != -1? MATCH : NO_MATCH;
            }

            public State doTransition(CharInput in) {
                while(true) {
                    int ch = in.nextChar();
                    if(ch == '<' || ch == '>' || ch == -1) {
                        in.pushBack();
                        break;
                    }
                }
                return null;
            }
            
        };
        
        // CDATA transition
        cdata = new Transition() {

            public String getName() { return "cdata"; }
            
            public int match(int ch, int index) {
                return TransitionUtils.match("<![CDATA[", ch, index);
            }

            public State doTransition(CharInput in) {
                TransitionUtils.search("]]>", in);
                return null;
            }
        };
        
        // Element end tag transition
        elementEnd = new Transition() {

            public String getName() { return "elementEnd"; }
            
            public int match(int ch, int index) {
                return TransitionUtils.match("</", ch, index);
            }

            public State doTransition(CharInput in) {
                // We could check the element name too,
                // but it would be too much effort for monday afternoon...
                while(true) {
                    int ch = in.nextChar();
                    if(ch == '>' || ch == -1) {
                        endElement();
                        // If we are back to root level, wait for end...
                        if(getDepth() == 0)
                            return waitEnd;
                        break;
                    }
                    if(ch == -1) {
                        break;
                    }
                }
                
                return waitElement;
            }
            
        };
        
        // End of stream transition
        eos = new Transition() {

            public String getName() { return "eos"; }
            
            public int match(int ch, int index) {
                return ch == -1 ? MATCH : NO_MATCH;
            }

            public State doTransition(CharInput in) {
                return theEnd;
            }
            
        };
        
        
        // ------------------------------------------
        // Ok, now set the transitions for the states
        // ------------------------------------------
        waitDeclaration.setTransitions( new Transition[] { whiteSpace, comment, xmlDeclaration, elementStart, docType } );
        waitDocType.setTransitions( new Transition[] { whiteSpace, comment, docType, elementStart } );
        waitRoot.setTransitions( new Transition[] { whiteSpace, comment, elementStart } );
        waitAttributes.setTransitions( new Transition[] { whiteSpace, attributeStart, elementTagEnd, emptyElementTagEnd } );
        waitAttrAssign.setTransitions( new Transition[] { whiteSpace, attrAssign } );
        waitAttrValue.setTransitions( new Transition[] { whiteSpace, attrValue } );
        waitElement.setTransitions( new Transition[] { elementText, comment, cdata, elementStart, elementEnd } );
        waitEnd.setTransitions( new Transition[] { whiteSpace, comment, eos,
                // These following transitions are not useful for valid XML with only one root element,
                // but it might be nice to be able to read XML with multiple root elements also
                // (chopping it to one root element sized bits would make it valid)
                xmlDeclaration, elementStart, docType } );
        
        // (no transitions for the theEnd-state, of course...)
        
        // Ta-da! Now our XML eating state machine should be ready for action!
        currentState = waitDeclaration;
    }
    
    
  
    /**
     * Called by the elementStart transition to inform that a new element start tag was encountered.
     * @param elementName Name of the element
     */
    private void startElement(String elementName) {
        elementStack.push( new Element(elementName) );
    }
        
    /**
     * Called by elementEnd and emptyElementTagEnd transitions to inform that end of an element was encountered.
     *
     */
    private void endElement() {
        elementStack.pop();
        if(elementStack.size() == chopDepth)
            elementCount++;
    }
    
    /**
     * Called by attributeStart transition to inform that new attribute name was encountered.
     * 
     * @param attributeName
     */
    private void startAttribute(String attributeName) {
        // Add new attribute to the element on top of the stack.
        elementStack.peek().addAttribute( new Attribute(attributeName) );
    }
    
    /**
     * Called by attributeValue transition to inform that a value was read for an attribute.
     * @param value
     */
    private void valueForAttribute(String value) {
        // Set value for the most recent attribute in the element on top of the stack.
        elementStack.peek().getLatestAttribute().setValue(value);
    }
    
    /**
     * Returns the depth of the element stack.
     * @return Current depth in the XML document.
     */
    private int getDepth() {
        return elementStack.size();
    }
    
    
    /**
     * Return valid XML containing the next elements. 
     * 
     * @param max The maximum number of elements in the chop depth
     * @return XML block or null if no more elements in the chop depth are available.
     * 
     * @throws ParseException Something unexpected found in the input.
     */
    public String getNext(int max) throws ParseException {
        
        // Make the starting tags for this XML-block
        StringBuffer block = new StringBuffer();        
        if(xmlDeclarationStr != null && chopDepth > 0) // Don't append XML declaration when using chopDepth 0, there might be one in the input... 
            block.append(xmlDeclarationStr).append('\n');
        // Doc type, if any.
        if(docTypeStr != null)
            block.append(docTypeStr).append('\n');
        // Opening tags for the elements that are open.
        for(Element element : elementStack) {            
            block.append( element.toString() ).append('\n');
        }
        
        // Skip white space on chopDepth 0 
        if(chopDepth == 0) {
            while(Character.isWhitespace(charInput.nextChar())) { }
            charInput.pushBack();
        }
        
        charInput.clearRead();
        elementCount = 0;
        
        try {
            // Jump around the states until we've got enough elements or the end state is reached.
            while(currentState != theEnd && elementCount < max) {
                currentState = currentState.doTransition(charInput);
            }
        }
        catch(NoMatchingTransitionException nmte) {
            // The state machine doesn't know what to do, meaning that the XML is probably malformed.
            String message = "Row: "+charInput.getRow()+", column: " +
                (charInput.getColumn() != -1 ? String.valueOf(charInput.getColumn()) : "[unknown]") + ", " + nmte.getMessage();
            throw new ParseException(message, charInput.getPosition());
        }
        if(elementCount == 0)
            return null;
        
        block.append(charInput.getRead());
        
        // ---------------
        // Append end tags
        // ---------------
        for(int i=elementStack.size()-1; i>=0; i--) {
            block.append('\n').append("</").append(elementStack.elementAt(i).getName()).append('>');
        }
        
        return block.toString();        
    }
   
}




    


