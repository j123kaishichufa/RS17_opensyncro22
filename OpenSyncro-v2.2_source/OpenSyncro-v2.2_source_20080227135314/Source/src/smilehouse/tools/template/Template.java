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

package smilehouse.tools.template;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

/**
 * <b><tt>Template</tt> </b> is a tool for printing ascii output from a program. <br>
 * It can be used to output information from the program to a static templatefile which contains
 * predefined places for the information. <br>
 * <br>
 * The templatefile consists of templateblocks. Templateblocks are pieces of ascii text limited by
 * block identifiers. All templateblocks have a name, except for one templateblock, which is the
 * root templateblock and contains the whole templatefile. <br>
 * <br>
 * Templateblocks can contain static text, variables and other templateblocks. Templateblocks inside
 * another blocks are called 'subblocks'. The amount of block levels is not restricted. <br>
 * <br>
 * Variables have names and values. Variable's default value is an empty string. <br>
 * <br>
 * A templateblock can also contain a special element, a Breakpoint. A Breakpoint is a line of text
 * which is included in the templateblock only if it's condition matches. The condition is a modulus
 * of the amount of writes for the block. For example '#breakpoint 3 haha' would print 'haha' every
 * third time that the templateblock is written. A breakpoint is never included in the last write.
 * See about writing blocks below. <br>
 * 
 * The templateblocks and variables can be filled with content by the program using the
 * templatefile, with this Template library.
 * 
 * A Template object is a manifestation of a 'templateblock' in the templatefile. <br>
 * <br>
 * The program using this tool library first loads a templatefile to form a template object. <br>
 * Then it asks for blocks defined in the templatefile and sets the variables values in the
 * template. <br>
 * When they are set, the template should be written with the write-method. <br>
 * The write method evaluates the template with the current values and forms the final printout.
 * <br>
 * The printout can be retrieved with the toString-method. <br>
 * 
 * If a variable value isn't set by the program then the variable will disappear from the printout.
 * <br>
 * If a templateblock defined in the templatefile, isn't written by the program, then it disappears
 * from the printout. This means that the block can be used like an if-clause. The templateblocks
 * can also be written multiple times with different values set in it's variables each time. This
 * means that the block can be used like a loop. <br>
 * <br>
 */
public class Template {
//	name of the Template
    private String name; 
//  warnings found when parsing the template
    private StringBuffer warnings; 
    //parts of the template (template's structure)
    private Vector parts;
//  strings already written using this template
    private StringBuffer written;
//  number of writes done for this template
    private int writeCounter; 


    //  Parts requested from the template but not found in the
    // templatefile
    private Hashtable missingTemplates;

    //  Variables requested from the template but not found in the
    // templatefile
    private Set missingVariables;


    //  The constant variable INFO if it appears in the templatefile. This is
    // set only to the root Template.
    //It's set to the root Template by the parser when it sees the INFO variable.
    //It's used for efficiency, now we now directly if we have to build the info from the
    // template's structure.
    private Variable info;

    //if the last write had a Breakpoint, use these buffers to check if it was the last write.
    //buffer used if not the last write
    private StringBuffer bufferWithBreak;
//  buffer used if this was the last write
    private StringBuffer bufferNoBreak; 

    /*
     * Code documentation only private void inVariant(){ test(name!=null, "Name shouldn't be null");
     * test(warnings!=null && parts!=null && written!=null && missingTemplates!=null &&
     * missingVariables!=null, "Variable shouldn't be null."); } private void test(boolean b, String
     * message){ if(!b) throw new IllegalArgumentException("Assert failed! "+message); }
     */





    /**
     * Creates an empty Template
     * 
     * @param name Name of the template, cannot be null.
     */
    Template(String name) {
        if(name == null)
            throw new IllegalArgumentException(
                "Template cannot be created without a name (given name was null).");

        this.name = name;

        this.warnings = new StringBuffer();
        this.parts = new Vector();
        this.written = new StringBuffer();
        writeCounter = 0;
        this.missingTemplates = new Hashtable();
        this.missingVariables = new HashSet();

        info = null;

        bufferWithBreak = null;
        bufferNoBreak = null;
    }





    /** ** PUBLIC METHODS *** */

    /**
     * Creates a Template object from the given templatefile. Opens the given file and parses it to
     * a Template object.
     */
    public static Template load(String file, String charset) {
        Template template = new Template("");
        Parser p = new Parser();
        p.parse(template, file, charset);
        return template;
    }

    /**
     * Creates a Template object from the given inputstream. Reads the stream and parse it to a
     * Template object.
     */
    public static Template load(InputStream stream, String charset) {
        Template template = new Template("");
        Parser p = new Parser();
        p.parse(template, stream, charset);
        return template;
    }

    /**
     * Creates a Template object from the given String.
     */
    public static Template createTemplate(String templateString) {
        Template template = new Template("");
        Parser p = new Parser();
        p.parseString(template, templateString);
        return template;
    }


    /**
     * Makes a deep copy of the template. <br>
     * <br>
     * The method is solely designed for improving efficiency of multiple prints by skipping the
     * reading of the templatefile. <br>
     * How to use:
     * <ul>
     * <li>load the template file with the load-method.
     * <li>Set it somewhere where you can keep it between prints.
     * <li>Now everytime you need a new print with the template, just get a copy of the one stored
     * between prints.
     * <li>It should be much faster than using the load-method.
     * </ul>
     */
    public Template copyStructure() {
        return copyStructure(null);
    }


    /**
     * Searches this template for a subtemplate. The first one with the given name is returned.
     * 
     * @param templateName Name of the template to search for.
     * @return The first found template that matches the given name. Cannot return <tt>null</tt>.
     *         If a matching template is not found, it will be created to gather information about
     *         how the program uses the template.
     */
    public Template getBlock(String templateName) {
        //if already found missing
        if(missingTemplates.get(templateName) != null) {
            return (Template) missingTemplates.get(templateName);
        }

        Enumeration e = parts.elements();
        while(e.hasMoreElements()) {
            Object part = e.nextElement();

            if(part instanceof Template) {
                Template block = (Template) part;
                if(block.getName().equals(templateName))
                    return block;
            }
        }

        //block not found in templatefile.
        //if block hasn't been called before, create it
        if(missingTemplates.get(templateName) == null) {
            //make a warning first
            String missingWhere = "";
            if(this.getName().length() > 0)
                missingWhere = "from block '" + this.getName() + "'";
            warn("Block called '" + templateName + "' missing " + missingWhere);

            Template missing = new Template(templateName);
            missingTemplates.put(templateName, missing);
        }
        return (Template) missingTemplates.get(templateName);
    }

    /**
     * Sets the given value to all variables in this block with the given name.
     * 
     * @param name variable name
     * @param value variable's new value
     */
    public void setVariable(String name, String value) {
        //search the variable only if the variable exists (=is not in the missingVariables set).
        if(!missingVariables.contains(name)) {
            boolean set = false;
            Enumeration e = parts.elements();
            while(e.hasMoreElements()) {
                Object part = e.nextElement();

                if(part instanceof Variable) {
                    Variable var = (Variable) part;
                    if(var.getName().equals(name)) {
                        var.setValue(value);
                        set = true;
                    }
                }
                if(part instanceof Include) {
                    Include i = (Include) part;
                    if(i.setVariableValue(name, value))
                        set=true;
                }
            }

            //this variable is missing
            if(!set) {
                //make a warning first
                String missingWhere = "";
                if(this.getName().length() > 0)
                    missingWhere = "from block '" + this.getName() + "'";
                warn("Variable called '" + name + "' missing " + missingWhere);

                //add as missing so now further attemps to a value to it won't cause searching or
                // warning messages.
                missingVariables.add(name);
            }
        }
    }

    public void setVariablesFromSource(ValueSource source) throws Exception {
        Enumeration e = parts.elements();
        while(e.hasMoreElements()) {
            Object part = e.nextElement();

            if(part instanceof Variable) {
                Variable var = (Variable) part;
                String value = source.getValue(var.getName());
                if(value != null) {
                    var.setValue(value);
                }
            } if(part instanceof Include) {
                Include i = (Include) part;
                i.setVariablesFromSource(source);
            }
        }
    }

///TESTING GLOBAL VERSIONS

    public void setGlobalVariable(String name, String value) {
//      search the variable only if the variable exists (=is not in the missingVariables set).
        if(!missingVariables.contains(name)) {
            boolean set = false;
            Enumeration e = parts.elements();
            while(e.hasMoreElements()) {
                Object part = e.nextElement();

                if(part instanceof Variable) {
                    Variable var = (Variable) part;
                    if(var.getName().equals(name)) {
                        var.setValue(value);
                        set = true;
                    }
                }
                if(part instanceof Template) {
                    Template t = (Template) part;
                    t.setGlobalVariable(name, value);
                }
            }

            //this variable is missing
            if(!set) {
                //make a warning first
                String missingWhere = "";
                if(this.getName().length() > 0)
                    missingWhere = "from block '" + this.getName() + "'";
                warn("Variable called '" + name + "' missing " + missingWhere);

                //add as missing so now further attemps to a value to it won't cause searching or
                // warning messages.
                missingVariables.add(name);
            }
        }
    }
    

    public void setGlobalVariablesFromSource(ValueSource source) throws Exception {
        Enumeration e = parts.elements();
        while(e.hasMoreElements()) {
            Object part = e.nextElement();

            if(part instanceof Variable) {
                Variable var = (Variable) part;
                String value = source.getValue(var.getName());
                if(value != null) {
                    var.setValue(value);
                }
            }
            if(part instanceof Template) {
                Template t = (Template) part;
                t.setGlobalVariablesFromSource(source);
            }
        }
    }

    
    
    /**
     * Evaluates the current contents of the template and appends it to the written buffer. The
     * written buffer can be retrieved with the template's toString-method. Variables and
     * subtemplate's written parts will be cleaned between consecutive writes.
     */
    public void write() {

        //it wasn't the last write for the block, commit the buffer with the breakpoints included.
        commitBuffer(false);

        //try to do this here before block are evaluated
        if(this.info != null)
            info.setValue(makeInfo(null, null, ""));

        //start the write
        ++writeCounter;
        boolean containsBreakpoint = containsBreakpoint();

        Enumeration e = parts.elements();
        //if breakpoints were found, write to temp buffers, one with and one without breakpoints
        if(containsBreakpoint) {
            bufferWithBreak = new StringBuffer();
            bufferNoBreak = new StringBuffer();


            while(e.hasMoreElements()) {
                Object part = e.nextElement();

                if(part instanceof Template) {
                    String s = part.toString();
                    bufferWithBreak.append(s);
                    bufferNoBreak.append(s);
                    ((Template) part).empty();

                } else if(part instanceof Variable) {
                    String s = part.toString();
                    bufferWithBreak.append(s);
                    bufferNoBreak.append(s);
                    ((Variable) part).setValue("");

                } else if(part instanceof Breakpoint) {
                    if(((Breakpoint) part).breakpoint(writeCounter))
                        bufferWithBreak.append(part.toString());
                } else {
                    String s = part.toString();
                    bufferWithBreak.append(s);
                    bufferNoBreak.append(s);
                }
            }

        }
        //no breakpoints found, write directly to written
        else {

            while(e.hasMoreElements()) {
                Object part = e.nextElement();

                if(part instanceof Template) {

                    written.append(part.toString());
                    ((Template) part).empty();

                } else if(part instanceof Variable) {

                    written.append(part.toString());
                    ((Variable) part).setValue("");

                } else if(part instanceof Breakpoint) {
                    /**
                     * don't do anything because if the breakpoint was now, then this shouldn't be
                     * executed.
                     */
                } else
                    written.append(part.toString());
            }

        }
    }


    /**
     * returns the template's written parts.
     * 
     * @return The template's written parts.
     */
    public String toString() {
        commitBuffer(true); //it really was the last write, commit possible buffers without the
                            // breakpoints
        return written.toString();
    }





    /** ** PROTECTED METHODS *** */

    /** adds a warning to this template */
    void warn(String warning) {
        warnings.append(warning + '\n');
    }


    /** Adds a new part to this template */
    void add(String s) {
        add((Object) s);
    }

    /** Adds a new part to this template */
    void add(Variable m) {
        add((Object) m);
    }

    /** Adds a new part to this template */
    void add(Template t) {
        add((Object) t);
    }

    /** Adds a new part to this template */
    void add(Breakpoint p) {
        add((Object) p);
    }
	/** Adds a new include part to this template */
    void add(Include i) {
        add((Object) i);
    }

    /** Adds a new part to this template */
    private void add(Object o) {
        parts.addElement(o);
    }

    /** Sets the information variable to this template */
    void setInfo(Variable m) {
        this.info = m;
    }





    /** ** PRIVATE METHODS *** */


    /**
     * Commits possible buffers, should be called before evaluating the Template and before more
     * writes.
     */
    private void commitBuffer(boolean last) {
        if(bufferWithBreak != null && bufferNoBreak != null) {
            if(last)
                written.append(bufferNoBreak.toString());
            else
                written.append(bufferWithBreak.toString());
        }
        bufferWithBreak = null;
        bufferNoBreak = null;
    }

    /**
     * @return true if this block contains a breakpoint that should be included to this write (the
     *         modulus matches).
     */
    private boolean containsBreakpoint() {
        //check for breakpoint
        Enumeration e = parts.elements();
        while(e.hasMoreElements()) {
            Object part = e.nextElement();
            if(part instanceof Breakpoint) {
                if(((Breakpoint) part).breakpoint(writeCounter))
                    return true;
            }
        }
        return false;
    }

    /**
     * @return Name of the current template
     */
    private String getName() {
        return name;
    }

    /** clears the written buffer of this template */
    private void empty() {
        written = new StringBuffer();
        writeCounter = 0;
    }

    /**
     * Used to copy a template's structure. This method is used recursively to copy all
     * substructures contained in the original template.
     * 
     * @param copyOfRoot The copy's root is given only for setting the copy of the possible INFO
     *        variable, otherwise it should be ignored in the method.
     */
    private Template copyStructure(Template copyOfRoot) {
        //make a new template, and append any parse warnings from the original
        Template copy = new Template(this.name);
        copy.warnings.append(this.warnings.toString());
        copy.written.append(this.written.toString());

        //go trough parts and copy each one to the new template
        Enumeration e = this.parts.elements();
        while(e.hasMoreElements()) {
            Object part = e.nextElement(); //original part
            Object copyPart = null; //the new part
            if(part instanceof Template) {
                if(copyOfRoot == null)
                    copyPart = ((Template) part).copyStructure(copy); //copy templates using
                                                                      // recursion
                else
                    copyPart = ((Template) part).copyStructure(copyOfRoot);
            } else if(part instanceof Variable) {
                Variable v = (Variable) part;

                Variable copyVariable = new Variable(v.getName()); //create new variables with same
                                                                   // names as before
                copyVariable.setValue(v.toString());

                //if this is the root and info is in the root, then also set the info
                if(copyVariable.getName().equals(Parser.INFO_VARIABLE) && copyOfRoot == null)
                    copy.info = copyVariable;
                //if this is the info variable, and it's not set in the root already, set it to the
                // root.
                else if(copyVariable.getName().equals(Parser.INFO_VARIABLE)
                        && copyOfRoot.info == null)
                    copyOfRoot.info = copyVariable;

                copyPart = copyVariable;
            } else if(part instanceof Include) {
                Include i = (Include) part;
                part = new Include(i.url);
            } else
                copyPart = part; //these structures can't be changed so only a shallow copy is used
                                 // to save some processing and resources.

            //now add the part to the copy
            copy.add(copyPart);
        }
        return copy;
    }





    /**
     * goes trough all the template's beneath this template and gathers the information to the info
     * variable.(subtemplates form a tree) The order is a recursive left-to-right-bottom-first.
     */
    private String makeInfo(StringBuffer structure, StringBuffer var, String indent) {
        //if this is the first call, create buffers
        if(structure == null && var == null) {
            structure = new StringBuffer();
            var = new StringBuffer();
            structure.append("Structure\n--\n");
            var.append("Warnings\n--\n");
        }
        var.append(this.warnings);

        //go trough parts and gather info
        Enumeration e = parts.elements();
        while(e.hasMoreElements()) {
            Object part = e.nextElement();
            if(part instanceof Template) {
                Template p = (Template) part;
                structure.append(indent + Parser.COMMAND_OPEN_BLOCK + " " + p.getName() + "\n");
                //use a recursive call to go trough the subtemplate
                p.makeInfo(structure, var, indent + "  ");
                structure.append(indent + Parser.COMMAND_CLOSE_BLOCK + "\n");
            }
            if(part instanceof Breakpoint) {
                Breakpoint b = (Breakpoint) part;
                structure.append(indent + Parser.COMMAND_BREAKPOINT + " " + b.getInterval() + "\n");
            }
            if(part instanceof Variable) {
                Variable m = (Variable) part;
                structure.append(indent + Parser.VARIABLE_CHAR + m.getName() + Parser.VARIABLE_CHAR
                        + "\n");
            }
            if(part instanceof Include) {
                Include i = (Include) part;
                structure.append(indent + "Include: "+i.url+ "\n");
        }
        }
        //Go trough parts missing from this block
        e = missingTemplates.elements();
        while(e.hasMoreElements()) {
            Object part = e.nextElement();
            if(part instanceof Template) {
                Template p = (Template) part;
                p.makeInfo(structure, var, indent + "  ");
            }
        }
        return (structure.toString() + "\n" + var.toString());

    }

}