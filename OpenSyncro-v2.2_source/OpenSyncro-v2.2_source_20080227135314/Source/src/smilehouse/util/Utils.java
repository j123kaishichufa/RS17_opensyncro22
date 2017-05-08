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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class contains miscellaneous static utility methods, mostly involving string manipulation.
 *
 * @created April 4, 2002
 */

public class Utils {

    /**
     * Splits a string using parameter <code>split</code> as a delimiter. If parameters are null,
     * returns an empty array.
     * 
     * @param string the <code>String</code> to be splitted
     * @param delim delimiter
     * @return an array containing the splitted parts
     */
    public static String[] split(String string, String delim) {
        if(string == null) {
            return (new String[0]);
        }
        if(delim == null) {
            throw new IllegalArgumentException("null delimiter is not accepted!");
        }

        Vector chunks = new Vector();
        int pos = 0;
        int next;
        while((next = string.indexOf(delim, pos)) >= 0) {
            chunks.addElement(string.substring(pos, next));
            pos = next + delim.length();
        }
        chunks.addElement(string.substring(pos));
        return (String[]) chunks.toArray(new String[chunks.size()]);
    }


    /**
     * Joins several strings using parameter <code>delim</code> as a delimiter. Delimiters
     * embedded in the string are not treated in any way - if this may be a problem, consider using
     * <code>CSVParser.buildLine()</code>.
     * 
     * @param parts the <code>String</code>s to be joined
     * @param delim delimiter
     * @return the string containing the merged parts
     */
    public static String join(String[] parts, String delim) {
        if(parts.length > 0) {
            String merged = parts[0];
            for(int i = 1; i < parts.length; i++) {
                merged += delim + parts[i];
            }
            return merged;
        } else {
            return "";
        }
    }

    /**
     * Replaces all occurences of a string with another one.
     * 
     * @param orig The string being changed.
     * @param oldstr All occurences of this string will be replaced.
     * @param newstr The replacement.
     * @return Corrected string.
     */
    public static String replace(String orig, String oldstr, String newstr) {
        StringBuffer ret=new StringBuffer();
        int oldLength = oldstr.length();
        int pos = 0;
        int next;
        while((next = orig.indexOf(oldstr, pos)) >= 0) {
            ret.append(orig.substring(pos, next));
            ret.append(newstr);
            pos = next + oldLength;
        }
        ret.append(orig.substring(pos));
        return ret.toString();
    }

    /**
     * Converts special characters to HTML entities and filters characters below ASCII
     * value 32, except tabulator and linefeed.
     * 
     * To be used for instance for escaping HTML form edit field values to prevent cross
     * site scripting exploits.
     * 
     * @param string the <code>String</code> to be converted
     * @return a <code>String</code> with character conversions made
     */
    public static String htmlentities(String string) {
        if(string != null) {
            String htmlChars = "\t\n\'\"&<>©";
            
            String[] htmlEntities = new String[] {
                    "\t",
                    "\n",
                    
                     // Use numeric entity for apostrophe since IE6 does not
                     // decode "&apos;" for some reason.   
                    "&#39;", 
                    
                    "&quot;",
                    "&amp;",
                    "&lt;",
                    "&gt;",
                    "&copy;"
            };
            return translate(string, htmlChars, htmlEntities, 0, 31);
        } else return null;
    }

    /**
     * Converts a string for use as a Javascript variable value by replacing
     * backslash, apostrophe, doublequote, linefeed and carriage return characters
     * with their escape sequences.
     * 
     * @param string to be escaped
     * @return escaped string
     */
    public static String escapeJavaScriptString(String string) {
        String searchChars = "\\\'\"\n\r";
        
        String[] replaceStrings = new String[] {
                "\\\\",
                "\\'",
                "\\\"",
                "\\n",
                "\\r"
        };
        
        if(string != null) {
            return translate(string, searchChars, replaceStrings, 0, 31);
        } else return null;
    }

    /** Escapes a specified Character from the string by replacing all its
        occurences with two instances of the same Character. For example 'test'
        becomes ''test''.
        */
    public static String escapeChar(String string, char c) {
        String searchChar = Character.toString(c);
        
        String[] replaceStrings = new String[] {
                searchChar + searchChar
        };
        
        if(string != null) {
            return translate(string, searchChar, replaceStrings);
        } else return null;
    }

    
    
    /**
     * Filters out all invalid XML characters from a string, according to
     * XML 1.0 specification (http://www.w3.org/TR/REC-xml/#charsets):
     * Char	::= #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
     * 
     * @param string the <code>String</code> to be filtered
     * @return a <code>String</code> with only valid XML characters
     */
    public static String filterInvalidXMLCharacters(String string) {
        if(string != null) {
            String passThroughChars = "\t\n\r";
            String[] passThroughCharsAsStrings = new String[] {
                    "\t",
                    "\n",
                    "\r"
            };
            
        // Filter all characters except TAB, LF and CR from range 0-31
        string = translate(string, passThroughChars, passThroughCharsAsStrings, 0, 31);
       
        // Apply filters [#x20-#xD7FF], [#xE000-#xFFFD] and [#x10000-#x10FFFF]
        StringBuffer buf = new StringBuffer();
        for(int i = 0; i < string.length(); i++) {
            char ch = string.charAt(i);
            if( !(((ch > 0xD7FF) && (ch < 0xE000)) ||
                 ((ch > 0xFFFD) && (ch < 0x10000)) ||
                 (ch > 0x10FFFF))) {
                buf.append(ch);
            }
        }
        return buf.toString();
        } else
            return null;
    }

    /**
     * String translate method for replacing occurrences of a list of Characters with
     * equivalent Strings.
     * 
     * @param str Input String to be search/replaced
     * @param searchChars Characters to search (single chars concatenated to one String)  
     * @param replaceStrings Array of String replacements for searchChars (in the same order
     *                       as the characters in searchChars) 
     * @return Result String with all occurences of searchChars replaced with equivalent
     *         replaceStrings 
     */
    public static String translate(String str,
                                   String searchChars,
                                   String[] replaceStrings) {
        // Preconditions
        if(str == null) throw new IllegalArgumentException("Input string cannot be null");
        if(searchChars == null) throw new IllegalArgumentException("Search character string cannot be null");
        if(searchChars.length() == 0) throw new IllegalArgumentException("Search character string cannot be empty");
        if(searchChars.length() != replaceStrings.length)
            throw new IllegalArgumentException("Length of search characters (" + searchChars.length() + ") and " +
                "replace strings (" + replaceStrings.length + ") must be equal");
        // TODO: check precondition "searchChars must not contain multiple occurences of the same character"
        
        StringBuffer buf = new StringBuffer(str.length());
        for(int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            int index = searchChars.indexOf(ch);
            if(index >= 0) {
                if(index < replaceStrings.length) {
                    buf.append(replaceStrings[index]);
                }
            } else {
                buf.append(ch);
            }
        }
        
        // Postcondition
        if(buf == null) throw new IllegalStateException("Result string was null");
        
        return buf.toString();
    }
    
    /**
     * String translate method for replacing occurrences of a list of Characters with
     * equivalent Strings. Additionally a numeric range of Characters can be filtered out.
     * Characters listed in searchChars will never be filtered.
     * 
     * @param str Input String to be search/replaced
     * @param searchChars Characters to search (single chars concatenated to one String)  
     * @param replaceStrings Array of String replacements for searchChars (in the same order
     *                       as the characters in searchChars)
     * @param filterRangeStart Character filter range start
     * @param filterRangeEnd Character filter range end
     * @return Result String with all occurences of searchChars replaced with equivalent
     *         replaceStrings 
     */
    public static String translate(String str,
                                   String searchChars,
                                   String[] replaceStrings,
                                   int filterRangeStart,
                                   int filterRangeEnd) {
        StringBuffer buf = new StringBuffer(str.length());
        for(int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            int index = searchChars.indexOf(ch);
            if(index >= 0) {
                if(index < replaceStrings.length) {
                    buf.append(replaceStrings[index]);
                }
            } else {
                if( (ch < filterRangeStart) || (ch > filterRangeEnd) ) {
                    buf.append(ch);    
                }
            }
        }
        return buf.toString();
    }

    /** Returns the input XML document String without the leading XML
     *  declaration line.
     * 
     * @param xmlData XML document
     * @return XML document without the XML declaration line
     */
    public static String stripXMLdeclaration(String xmlData) {
        
		Pattern p = Pattern.compile("<\\?xml .*\\?>\\s*");
        Matcher m = p.matcher(xmlData);

        if( m.find() ) {
            // XML declaration found, return the String without it
            return m.replaceFirst("");
        } else {
            // No XML declaration found, return the String as is
            return xmlData;
        }
    }

    /** Returns the XML declaration from input XML document.
     * 
     * @param xmlData XML document
     * @return XML declaration
     */
    public static String getXMLdeclaration(String xmlData) {
        
		Pattern p = Pattern.compile("<\\?xml .*\\?>");
        Matcher m = p.matcher(xmlData);

        if( m.find() ) {
            // XML declaration found, return it
            return xmlData.substring(m.start(), m.end());
        } else {
            // No XML declaration found, return null
            return null;
        }
    }
    
    /**
     * Helper method for parsing Double's. We accept both '.' and ',' characters as
     * decimalseparators in a decimalnumber. This helper method checks wich one is used by the
     * system and then converts the given decimalnumber-string to the correct format. Then the
     * java.lang.Double constructor is used.
     * 
     * @param value String representation of a decimalnumber that should be converted to a double.
     * @return Double if the value is a correct decimalnumber.
     * @exception NumberFormatException If the Double cannot be parsed even after the filtering.
     */
    public static Double parseDouble(String value) throws NumberFormatException {
        if(value != null) {
            value = Utils.replace(value, ",", ".");
            //convert all ',' to '.' because it is the right decimalSeparator
        }

        return new Double(value);
    }


    /**
     * Gets the stackTrace attribute of the Utils class
     * 
     * @param e Description of the Parameter
     * @return The stackTrace value
     */
    public static String getStackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        sw.flush();
        return sw.toString();
    }

    /**
     * Unescapes Strings that have been escaped in java style.
     * Does not support unicode escape sequences. 
     * 
     * @param escaped Escaped String
     * @return Unescaped String
     */
    public static String javaStyleUnescape(String escaped) {
        if(escaped.indexOf('\\') == -1)
            return escaped;
        StringBuffer unescaped = new StringBuffer();
        for(int i=0; i<escaped.length(); i++) {
            char c = escaped.charAt(i);
            if(c == '\\') {
                i++;
                if(i>=escaped.length()) {
                    // The string is escaped wrong, but I don't think it's worth
                    // throwing any exceptions...
                    break;
                }
                char c2 = escaped.charAt(i);
                switch(escaped.charAt(i)) {
            		case 't' : c = '\t'; break;
                	case 'n' : c = '\n'; break;
                	case 'r' : c = '\r'; break;
                	case 'b' : c = '\b'; break;
                	case 'f' : c = '\f'; break;
                	default  : c = c2;
                }
            }
            unescaped.append(c);
        }        
        return unescaped.toString();
    }
    
    /**
     * Returns name of a Class without the package name.
     * 
     * @param c Class
     * @return Name of the Class without package name
     */
    public static String getClassName(Class c) {
        int classNameStart;
        String fullClassName;

        // Preconditions
        if(c == null) throw new IllegalArgumentException("Class cannot be null");
        
        fullClassName = c.getName();
        if(fullClassName == null) throw new IllegalStateException("Class name was null");
        
        classNameStart = fullClassName.lastIndexOf('.');
        if(classNameStart != -1) {
            return fullClassName.substring(classNameStart + 1);
        } else {
            return fullClassName;
        }
    }
    
    /**
     * Get name of a throwable without the leading Class package.  
     * 
     * @param t Throwable
     * @return Name of the Throwable
     */
    public static String getThrowableName(Throwable t) {

        // Precondition
        if(t == null) throw new IllegalArgumentException("Throwable cannot be null");
        
        return getClassName(t.getClass());
    }

    
}