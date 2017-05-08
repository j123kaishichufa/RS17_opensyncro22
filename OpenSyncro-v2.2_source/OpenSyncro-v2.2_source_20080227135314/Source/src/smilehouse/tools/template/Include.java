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
 * Created on May 30, 2005
 */
package smilehouse.tools.template;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Represents a http-include structure in templates. URL-values can contain variables which can be
 * set by the surrounding templates setVariable-method. The content is retrieved from the
 * dynamically created url when toString is called.
 */
class Include extends Object {

    //url found in template
    String url;
    //variables parsed from url
    Set variables;
    //variable values set from surrounding template
    Map varValues;

    /**
     * Creates a new Include part from the url found when parsing the template
     */
    public Include(String url) {
        this.url = url;
        varValues = new HashMap();
        variables = parseVariables(url);
    }

    public void setVariablesFromSource(ValueSource source) throws Exception {
        for(Iterator iter = variables.iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String value = source.getValue(name);
            if(value != null)
                varValues.put(name, value);
        }
    }

    /**
     * @param value
     * @return true if variable was found and set. false if variable wasn't found.
     */
    public boolean setVariableValue(String name, String value) {
        if(variables.contains(name)) {
            varValues.put(name, value);
            return true;
        }
        return false;
    }

    /**
     * Evaluates the Include by getting the content from the url-address.
     */
    public String toString() {
        //modUrl is the dynamic uri based on variables in the original uri and values from the
        // varValues.
        String modUrl = url;
        for(Iterator iter = variables.iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            //logger.debug("var: "+name+" val:"+varValues.get(name));

            //$-chars must be escaped in regex
            String varName = "\\" + Parser.VARIABLE_CHAR + name + "\\" + Parser.VARIABLE_CHAR;
            //modUrl contains variables in from $varname$, replace them with values
            modUrl = modUrl.replaceFirst(varName, "" + varValues.get(name));
        }

        try {
            URL u = new URL(modUrl);
            return readContent(u);

        } catch(MalformedURLException mue) {
            String message = "<!-- include failed, invalid url address: '" + modUrl
                    + "', errormessage: '" + mue.getMessage()
                    + "'. See server log for details. -->";
            return message;
        } catch(IOException e) {
            String message = "<!-- include failed, error reading url content from '" + modUrl
                    + "', errormessage: '" + e.getMessage() + "'. See server log for details. -->";
            return message;
        }
    }

    /**
     * Retrieves the content from the given url-address.
     */
    private String readContent(URL u) throws IOException {              
        StringBuffer buffer = new StringBuffer();

        //get connection, doesn't open resources yet (there no con.close provided in the api, I
        // guess closing the stream suffices).
        URLConnection con = u.openConnection();
        //TODO after moving to jdk1.5 permanently we should remove the reflection stuff.
        try {
            trySetTimeouts(con);
        }catch(Throwable t) {
//          System.err.println("includes do not support timeouts, server might hang.\n" + t.toString());
        }
        
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            while((inputLine = in.readLine()) != null)
                buffer.append(inputLine);
        } finally {
            if(in != null)
                in.close();
        }
        /*
        if(buffer.length() == 0) {
            System.err.println("empty response from url '" + u.toString() + "'.");
        }
        */
        return buffer.toString();
    }

    /**
     * @param con
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    private void trySetTimeouts(URLConnection con) throws SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        callSetterWithInt(con, "setConnectTimeout", 2000);
        callSetterWithInt(con, "setReadTimeout", 3000);
    }

    private void callSetterWithInt(URLConnection con, String methodName, int value) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Class[] parTypes = { Integer.TYPE };
        Method m = con.getClass().getMethod(methodName, parTypes);
        Integer to = new Integer(value);
        Object[] pars = { to };
        m.invoke(con, pars);
    }

    /**
     * Parses a textline for variables
     */
    private Set parseVariables(String line) {
        //This somewhat overlaps with the Parser class and it's a bit ugly. Instead of
        // re-implementing the
        // parser with callbacks I chose to reimplement this parsing here due to time limitations
        // and fear of breaking previous behaviour. I also think that if there are any more new
        // functions requested for templates we should consider using a 3rd party library like
        // Velocity or something else, Tapestry's templates look nice.

        //Anyway, if this get's you into trouble, try to cover problems by using the unit tests. I
        // added them to ease maintenance of the template library.


        Set variables = new HashSet();
        final int TEXT = 0;
        final int VARIABLE = 1;
        int state = TEXT;

        StringBuffer buffer = new StringBuffer();
        try {
            StringReader reader = new StringReader(line);
            int charValue = reader.read();
            while(charValue != -1) {
                char c = (char) charValue;
                switch(state) {
                case (TEXT):
                    if(c == Parser.VARIABLE_CHAR) {
                        state = VARIABLE;
                        buffer.delete(0, buffer.length());
                    }
                    break;
                case (VARIABLE):
                    if(c == Parser.VARIABLE_CHAR) {
                        if(buffer.length() > 0) {
                            variables.add(buffer.toString());
                            buffer.delete(0, buffer.length());
                        }
                        state = TEXT;
                    } else
                        buffer.append(c);
                    break;
                }
                charValue = reader.read();
            }
        } catch(IOException ioe) {
            //Don't know when this could happen as String should'n throw IOException.. lets make
            // sure we know if this happens.
            throw new RuntimeException("Unexpected end of java string.", ioe);
        }
        return variables;
    }
}