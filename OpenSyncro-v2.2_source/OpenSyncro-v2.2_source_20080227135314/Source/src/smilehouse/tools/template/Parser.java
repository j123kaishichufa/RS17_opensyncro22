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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Stack;
import java.util.StringTokenizer;

class Parser {

    /** Constant for the open block command */
    public static final String COMMAND_OPEN_BLOCK = "#block";
    /** Constant for the close block commmand */
    public static final String COMMAND_CLOSE_BLOCK = "#endblock";
    /** Constant for the breakpoint command */
    public static final String COMMAND_BREAKPOINT = "#breakpoint";
    /** Constant for the include command */    
    public static final String COMMAND_INCLUDE = "#include";

    /** Constant for the character defining a variable */
    public static final char VARIABLE_CHAR = '$';
    /** Constant for the variable name which information about the template is printed in */
    public static final String INFO_VARIABLE = "INFO";


    //state information used in parsing
    private int row; //currently parsed rownumber
    private StringBuffer buffer; //buffer for read text
    private Stack openBlocks; //stack used when parsing blocks that have more blocks inside
    private Template lowestBlock; //The block that contains the whol templatefile.
    private Template current; //Currently parsed block




    /**
     * Basic constructor which sets the parsers state information to beginning state.
     */
    Parser() {
        row = 0;
        buffer = new StringBuffer();
        openBlocks = new Stack();
        lowestBlock = null;
        current = null;
    }



    /**
     * Method fills the parsed templatefile to the given template.
     */
    void parse(Template template, InputStream inStream, String charset) {
        try {
            lowestBlock = template;
            current = template;

            InputStreamReader inReader = new InputStreamReader(inStream, charset);
            BufferedReader bufferReader = new BufferedReader(inReader);
            parse(bufferReader);
            bufferReader.close();

        } catch(Exception e) {
            //add a warning to the template
            current.warn("Stream could not be parsed. Reason: " + getStackTrace(e));
            //add the info variable to the structure of the template:
            //set name to buffer, addVariable takes the name from the buffer.
            buffer.append(INFO_VARIABLE);
            addVariable();
        }
    }


    /**
     * Method fills the parsed templatefile to the given template.
     */
    void parse(Template template, String file, String charset) {
        try {
            lowestBlock = template;
            current = template;

            FileInputStream inStream = new FileInputStream(file);
            InputStreamReader inReader = new InputStreamReader(inStream, charset);
            BufferedReader bufferReader = new BufferedReader(inReader);
            parse(bufferReader);
            bufferReader.close();

        } catch(Exception e) {
            //add a warning to the template
            current.warn("File '" + file + "' could not be parsed. Reason: " + getStackTrace(e));
            //add the info variable to the structure of the template:
            //set name to buffer, addVariable takes the name from the buffer.
            buffer.append(INFO_VARIABLE);
            addVariable();
        }
    }

    void parseString(Template template, String templateString) {
        try {
            lowestBlock = template;
            current = template;

            StringReader stringReader = new StringReader(templateString);
            BufferedReader bufferReader = new BufferedReader(stringReader);
            parse(bufferReader);
            bufferReader.close();

        } catch(Exception e) {
            //add a warning to the template
            current.warn("String could not be parsed. Reason: " + getStackTrace(e));
            //add the info variable to the structure of the template:
            //set name to buffer, addVariable takes the name from the buffer.
            buffer.append(INFO_VARIABLE);
            addVariable();
        }
    }





    /**
     * Reads the file line by line and checks if a line is a command line or a textline, then gives
     * the parsing to separate methods.
     */
    private void parse(BufferedReader reader) throws Exception {
        String line = reader.readLine();
        while(line != null) {
            row++;

            if(line.length() == 0)
                buffer.append('\n');
            else {
                int index = 0;
                while(Character.isWhitespace(line.charAt(index)) && index < line.length() - 1) {
                    ++index;
                }
                if(line.charAt(index) == COMMAND_OPEN_BLOCK.charAt(0)) {
                    // Attempt to parse as a command line
                    if(parseCommandLine(line) == false) {
                        // No command was recognized so parse it as a text line
                        parseTextLine(line);
                    }
                } else {
                    parseTextLine(line);
                }
            }

            line = reader.readLine();
        }
        addText();

        if(openBlocks.size() != 0) {
            warn(openBlocks.size() + " open blocks in the template structure.");
            while(openBlocks.size() != 0) {
                current.write();
                closeBlock();
            }
        }

    }





    /**
     * Parses a command line for a command and uses separate methods for filling the template.
     * Returns true if a command was detected, otherwise returns false. 
     */
    private boolean parseCommandLine(String line) {
        StringTokenizer words = new StringTokenizer(line);
        String firstWord = "";
        if(words.hasMoreTokens())
            firstWord = words.nextToken();

        if(firstWord.equals(COMMAND_OPEN_BLOCK)) {
            if(!words.hasMoreTokens())
                warn("Block doesn't have a name.");
            else {
                if(buffer.length() > 0)
                    addText();

                buffer.append(words.nextToken());
                openBlock();
            }

        } else if(firstWord.equals(COMMAND_CLOSE_BLOCK)) {

            if(openBlocks.empty())
                warn("Closing of an unknown block. Check that the beginning is correctly defined.");
            else {
                if(buffer.length() > 0)
                    addText();

                closeBlock();
            }
        } else if(firstWord.equals(COMMAND_BREAKPOINT)) {
            if(!words.hasMoreTokens())
                warn("Breakpoint interval not defined.");
            else {
                int interval = 0;
                try {
                    interval = Integer.parseInt(words.nextToken());
                } catch(NumberFormatException e) {
                    warn("Breakpoint interval not defined correctly. (NaN).");
                }

                String interV = "" + interval;
                String breakData = line.substring(line.indexOf(interV) + interV.length(), line
                    .length());

                if(buffer.length() > 0)
                    addText();
                addBreakpoint(interval, breakData + '\n');

            }
        }else if(firstWord.equals(COMMAND_INCLUDE)) {
            if(!words.hasMoreTokens())
                warn("Include url not defined.");
            else {
                String nTok = words.nextToken();
                addInclude(nTok);
            }
        } else {
            // No command was recognized
            return false;
        }
        return true;
    }





    /**
     * Parses a textline for variables and uses separate methods for filling the template.
     */
    private void parseTextLine(String line) throws Exception {
        final int TEXT = 0;
        final int VARIABLE = 1;
        int state = TEXT;

        StringReader reader = new StringReader(line);
        int charValue = reader.read();
        while(charValue != -1) {
            char c = (char) charValue;

            switch(state) {
            case (TEXT):
                if(c == VARIABLE_CHAR) {
                    addText();
                    state = VARIABLE;
                } else
                    buffer.append(c);
                break;

            case (VARIABLE):
                if(Character.isWhitespace(c)) {
                    warn("Possible variable was rejected, it didn't end with character '"
                            + VARIABLE_CHAR + "'.");
                    //undo/backup
                    buffer.append(c);
                    buffer.insert(0, VARIABLE_CHAR);
                    state = TEXT;
                } else if(c == VARIABLE_CHAR) {
                    addVariable();
                    state = TEXT;
                } else
                    buffer.append(c);
                break;
            }


            charValue = reader.read();
        }

        if(state == VARIABLE) {
            warn("Possible variable was rejected, it didn't end with character '" + VARIABLE_CHAR
                    + "'.");
            buffer.insert(0, VARIABLE_CHAR);
            state = TEXT;
        }
        buffer.append('\n');
    }





    /** ********************************************* */
    /* methods that fill the template. */
    /* All read from the buffer for data */
    /** ********************************************* */



    /** Adds a parsing warning to the template */
    private void warn(String warning) {
        current.warn("row " + row + ": " + warning);
    }

    /** Adds a subblock to the current template */
    private void openBlock() {
        openBlocks.push(current);
        current = new Template(buffer.toString());
        buffer = new StringBuffer();
    }

    /** Closes the current block and sets the parent of the block as the new current block */
    private void closeBlock() {
        Template temp = current;
        current = (Template) openBlocks.pop();
        current.add(temp);
    }

    /** Adds a breakpoint to the template with the given interval and the given data */
    private void addBreakpoint(int interval, String data) {
        Breakpoint p = new Breakpoint(interval, data);
        current.add(p);
    }
    

    /** Adds an include to the template's structure     */
    private void addInclude(String url) {
       Include i = new Include(url);
       current.add(i);
    }


    /** Adds a text piece to the current template's structure */
    private void addText() {
        current.add(buffer.toString());
        buffer = new StringBuffer();
    }

    /** Adds a variable to the current template's structure */
    private void addVariable() {
        if(buffer.length() <= 0)
            warn("Empty variable name");
        else {
            Variable m = new Variable(buffer.toString());
            current.add(m);
            buffer = new StringBuffer();

            if(m.getName().equals(INFO_VARIABLE))
                lowestBlock.setInfo(m);

        }
    }




    /** This helper method transforms the stacktrace of a Throwable to a Strign */
    private static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        sw.flush();
        return sw.toString();
    }

}