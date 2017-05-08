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

import http.utils.multipartrequest.MultipartRequest;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.ecs.html.B;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;

import smilehouse.opensyncro.system.Environment;


/**
 * ParameterManager.java Class for managing form parameters in a servlet. To use ParameterManager
 * your servlet (or some other class) must implement the ParameterInitializer-interface. That
 * contains a single method initParameters that should initialize the parameters with addParameter
 * calls. Fro example: <code>
 * public void initParameters(ParameterManager pm) {
 *     pm.addParameter("name", pm.STRING, "john smith");
 *     pm.addParameter("age",  pm.INT);
 * }
 * </code>
 * ParameterManager extracts parameters from HttpServletRequest and can also turn them into ECS
 * input elements. ParameterManager can also be kept in a session. TO DO: - Some kind of
 * getInput-method for arrays Created: Tue Feb 20 11:14:26 2001
 * 
 */
public class ParameterManager {
    

    
    /**
     * Constant for string type
     */
    public final static int STRING = 0;
    /**
     * Constant for integer type
     */
    public final static int INT = 1;
    /**
     * Constant for long type
     */
    public final static int LONG = 4;
    /**
     * Constant for double type
     */
    public final static int DOUBLE = 2;
    /**
     * Constant for boolean type
     */
    public final static int BOOLEAN = 3;
    /**
     * Constant for string array type
     */
    public final static int STRING_ARRAY = 5;
    /**
     * Constant for integer array type
     */
    public final static int INT_ARRAY = 6;
    /**
     * Constant for double array type
     */
    public final static int DOUBLE_ARRAY = 7;
    /**
     * Constant for float type
     */
    public final static int FLOAT = 8;

    /**
     * Constant for long array type
     */
    public final static int LONG_ARRAY = 9;


    /**
     * Description of the Field
     */
    public final static String SESSION_NAME = "parameters";

    // Modes for toString-method
    /**
     * Description of the Field
     */
    public final static int ALL = 0;
    /**
     * Description of the Field
     */
    public final static int GIVEN = 1;
    /**
     * Description of the Field
     */
    public final static int CHANGED = 2;

    /**
     * Description of the Field
     */
    public final static String[] TYPE_NAMES = {
            "STRING",
            "INT",
            "DOUBLE",
            "BOOLEAN",
            "LONG",
            "STRING_ARRAY",
            "INT_ARRAY",
            "DOUBLE_ARRAY",
            "FLOAT",
            "LONG_ARRAY"};

    Hashtable parameters;
    String owner;
    String description;

    String charset;

    /**
     * Inner class representing a single parameter.
     */
    class Parameter {
        int type;
        Object value;
        Object defaultValue;
        String parameterDescription;
        boolean wasGiven;
        boolean isChanged;
        boolean illegal;


        /**
         * Create parameter eith given type and default value.
         * 
         * @param type Parameter's type
         * @param defaultValue Parameter's default value
         * @param description Description of the Parameter
         */
        Parameter(int type, String description, Object defaultValue) {
            this.type = type;
            this.value = null;
            this.parameterDescription = description;
            this.wasGiven = false;
            this.isChanged = false;
            this.illegal = false;
            // If default value is not given, use default default value
            if(defaultValue == null) {
                switch(type) {
                case STRING:
                    this.defaultValue = "";
                    break;
                case INT:
                    this.defaultValue = new Integer(0);
                    break;
                case LONG:
                    this.defaultValue = new Long(0);
                    break;
                case DOUBLE:
                    this.defaultValue = new Double(0);
                    break;
                case BOOLEAN:
                    this.defaultValue = new Boolean(false);
                    break;
                case STRING_ARRAY:
                    this.defaultValue = new String[0];
                    break;
                case INT_ARRAY:
                    this.defaultValue = new int[0];
                    break;
                case DOUBLE_ARRAY:
                    this.defaultValue = new double[0];
                    break;
                case FLOAT:
                    this.defaultValue = new Float(0);
                    break;
                case LONG_ARRAY:
                    this.defaultValue = new long[0];
                    break;
                }
            } else {
                this.defaultValue = defaultValue;
            }
        }


        /**
         * Returns parameter's value or the default value, if it's null
         * 
         * @return The value value
         */
        Object getValue() {
            if(value != null) {
                if(value instanceof String) {
                    try {
                        return (URLDecoder.decode((String) value, charset));
                    } catch(Exception iWouldBeVerySurprisedToCatchThisException) {
                        return value;
                    }
                } else {
                    return value;
                }
            } else {
                return defaultValue;
            }
        }


        /**
         * Sets the parameters value from a given String. Int and double parameters are tried to
         * convert. Boolean parameters are set to 'false' if newValue is null or a string "false"
         * and 'true' otherwise.
         * 
         * @param newValue Parameter's new value as String
         */
        void setValue(String newValue) {
            if(newValue == null && type != BOOLEAN) {
                throw new IllegalArgumentException(
                    "null set to ParameterManager.Parameter value. Only boolean can have a null value");
            }

            if(newValue != null && (type == STRING || type == STRING_ARRAY)) {
                try {
                    newValue = URLEncoder.encode(newValue, charset);
                } catch(java.io.UnsupportedEncodingException uee) {}
            }
            illegal = false;

            switch(type) {
            case STRING:
                value = newValue;
                break;
            case INT:
                try {
                    value = new Integer(newValue);
                } catch(NumberFormatException nfe) {
                    value = null;
                    illegal = true;
                }
                break;
            case LONG:
                try {
                    value = new Long(newValue);
                } catch(NumberFormatException nfe) {
                    value = null;
                    illegal = true;
                }
                break;
            case DOUBLE:
                try {
                    value = Utils.parseDouble(newValue);
                } catch(NumberFormatException nfe) {
                    value = null;
                    illegal = true;
                }
                break;
            case BOOLEAN:
                value = new Boolean(newValue != null && !newValue.equalsIgnoreCase("false"));
                break;
            case STRING_ARRAY:
            case INT_ARRAY:
            case DOUBLE_ARRAY:
            case LONG_ARRAY:
                String[] stringArray = new String[1];
                stringArray[0] = newValue;
                setValue(stringArray);
                break;
            case FLOAT:
                try {
                    value = new Float(newValue);
                } catch(NumberFormatException nfe) {
                    value = null;
                    illegal = true;
                }
                break;
            }
        }


        /**
         * Sets the value attribute of the Parameter object
         * 
         * @param values The new value value
         */
        void setValue(String[] values) {
            illegal = false;
            if(values != null && values.length != 0) {
                // If the parameters type is STRING_ARRAY we already have it
                if(type == STRING_ARRAY) {
                    value = values;
                }
                // If not, we'll have to construct the array
                else {
                    switch(type) {
                    case INT_ARRAY:
                        int[] intValues = new int[values.length];
                        for(int i = 0; i < values.length; i++) {
                            try {
                                intValues[i] = Integer.parseInt(values[i]);
                            } catch(NumberFormatException nfe) {
                                // If number couldn't be parsed we set the illegal true.
                                illegal = true;
                            }
                        }
                        value = intValues;
                        break;
                    case DOUBLE_ARRAY:
                        double[] doubleValues = new double[values.length];
                        for(int i = 0; i < values.length; i++) {
                            try {
                                doubleValues[i] = (Utils.parseDouble(values[i])).doubleValue();
                                //Double.parseDouble(values[i]);
                            } catch(NumberFormatException nfe) {
                                // If number couldn't be parsed we set the illegal true.
                                illegal = true;
                            }
                        }
                        value = doubleValues;
                        break;
                    case LONG_ARRAY:
                        long[] longValues = new long[values.length];
                        for(int i = 0; i < values.length; i++) {
                            try {
                                longValues[i] = Long.parseLong(values[i]);
                            } catch(NumberFormatException nfe) {
                                // If number couldn't be parsed we set the illegal true.
                                illegal = true;
                            }
                        }
                        value = longValues;
                        break;
                    default:
                        value = null;
                        illegal = true;
                    }
                }
            } else {
                value = null;
                illegal = true;
            }
        }

    }


    /**
     * Create and initialize ParameterManager tries to get the charset from the current Environment.
     * 
     * @param initializer Class responsible for initializing the Usually the servlet that uses the
     *        ParameterManager.
     */
    public ParameterManager(ParameterInitializer initializer) {
        this(initializer, Environment.getInstance().getCharsetWWW());
    }


    /**
     * Create and initialize ParameterManager
     * 
     * @param initializer Class responsible for initializing the Usually the servlet that uses the
     *        ParameterManager.
     * @param charset Charset to be used when encoding and decoding parameters
     */
    public ParameterManager(ParameterInitializer initializer, String charset) {
        parameters = new Hashtable();
        description = null;
        this.charset = charset;
        initializer.initParameters(this);
    }


    /**
     * Create and initialize ParameterManager and extract values from a request
     * 
     * @param initializer Class responsible for initializing the Usually the servlet that uses the
     *        ParameterManager.
     * @param req HttpServletRequest where the parameter's values are to be extracted
     */
    public ParameterManager(ParameterInitializer initializer, HttpServletRequest req) {
        this(initializer, req, Environment.getInstance().getCharsetWWW());
    }

    /**
     * Create and initialize ParameterManager and extract values from a request tries to get the
     * charset from the current Environment.
     * 
     * @param initializer Class responsible for initializing the Usually the servlet that uses the
     *        ParameterManager.
     * @param req HttpServletRequest where the parameter's values are to be extracted
     */
    public ParameterManager(ParameterInitializer initializer, HttpServletRequest req, String charset) {
        parameters = new Hashtable();
        description = null;
        this.charset = charset;
        initializer.initParameters(this);
        extractParameters(req);
    }


    /**
     * Create and initialize ParameterManager and extract values from a multipartrequest Tries to
     * get the charset from the current Environment.
     * 
     * @param initializer Class responsible for initializing the ParameterManager. Usually the
     *        servlet that uses the ParameterManager.
     * @param multi MultipartRequest where the parameter's values are to be extracted
     */
    public ParameterManager(ParameterInitializer initializer, MultipartRequest multi) {
        parameters = new Hashtable();
        description = null;
        this.charset = Environment.getInstance().getCharsetWWW();
        initializer.initParameters(this);
        extractParameters(multi);
    }

    /**
     * Gets ParameterManager from session. If it's not there, new one is created and initialized.
     * 
     * Tries to get the charset from the current Environment.
     * 
     * @param session Session where the ParameterManager might be found
     * @param owner String identifying the ParameterManager's owner. Servlet's name or something
     *        like that.
     * @param initializer Initializer used to initialize a new ParameterManager if it's not found in
     *        the session
     * @return Description of the Return Value
     */
    public static ParameterManager fromSession(HttpSession session,
                                               String owner,
                                               ParameterInitializer initializer) {
        // Try to get ParameterManager from session
        ParameterManager pm = (ParameterManager) session.getAttribute(SESSION_NAME + "." + owner);
        // If it's not there, or it's from a different servlet, create new one
        if(pm == null
        /*
         * || pm.owner != owner
         */
        ) {
            pm = new ParameterManager(initializer);
            pm.owner = owner;
            session.setAttribute(SESSION_NAME + "." + owner, pm);
        }
        // Mark all parameters as not given
        Enumeration e = pm.parameters.elements();
        while(e.hasMoreElements()) {
            ((Parameter) e.nextElement()).wasGiven = false;
        }

        return pm;
    }


    /**
     * Gets ParameterManager from session and extracts values from request. If it's not in session,
     * new one is created and initialized.
     * 
     * @param session Session where the ParameterManager might be found
     * @param owner String identifying the ParameterManager's owner. Servlet's name or something
     *        like that.
     * @param initializer Initializer used to initialize a new ParameterManager if it's not found in
     *        the session
     * @param req HttpServletRequest where the parameter's values are to be extracted
     * @return Description of the Return Value
     */
    public static ParameterManager fromSession(HttpSession session,
                                               String owner,
                                               ParameterInitializer initializer,
                                               HttpServletRequest req) {
        ParameterManager pm = fromSession(session, owner, initializer);
        pm.extractParameters(req);
        return pm;
    }


    /**
     * Gets ParameterManager from session and extracts values from a multipartrequest. If it's not
     * in session, new one is created and initialized.
     * 
     * @param session Session where the ParameterManager might be found
     * @param owner String identifying the ParameterManager's owner. Servlet's name or something
     *        like that.
     * @param initializer Initializer used to initialize a new ParameterManager if it's not found in
     *        the session
     * @param multi multipartRequest where the parameter's values are to be extracted
     * @return Description of the Return Value
     */
    public static ParameterManager fromSession(HttpSession session,
                                               String owner,
                                               ParameterInitializer initializer,
                                               MultipartRequest multi) {
        ParameterManager pm = fromSession(session, owner, initializer);
        pm.extractParameters(multi);
        return pm;
    }


    /**
     * Add new parameter to manager. Usually called from the initParameter method of the
     * initializer.
     * 
     * @param name Parameter's name
     * @param description Description of the parameter's usage
     * @param type Parameter's type. See the constants for type.
     * @param defaultValue Default value as an Object
     */
    public void addParameter(String name, String description, int type, Object defaultValue) {
        parameters.put(name, new Parameter(type, description, defaultValue));
    }


    /**
     * Add new parameter to manager. Usually called from the initParameter method of the
     * initializer.
     * 
     * @param name Parameter's name
     * @param type Parameter's type. See the constants for type.
     * @param defaultValue Default value as an Object
     */
    public void addParameter(String name, int type, Object defaultValue) {
        parameters.put(name, new Parameter(type, null, defaultValue));
    }


    /**
     * Add new parameter to manager. Usually called from the initParameter method of the
     * initializer.
     * 
     * @param name Parameter's name
     * @param description Description of the parameter's usage
     * @param type Parameter's type. See the constants for type.
     */
    public void addParameter(String name, String description, int type) {
        parameters.put(name, new Parameter(type, description, null));
    }


    /**
     * Add new parameter to manager. Usually called from the initParameter method of the
     * initializer.
     * 
     * @param name Parameter's name
     * @param type Parameter's type. See the constants for type.
     */
    public void addParameter(String name, int type) {
        parameters.put(name, new Parameter(type, null, null));
    }


    /**
     * Set the overall description of the parameters.
     * 
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }


    /**
     * Returns parameter's value as a String. Value will be converted to string even if it's of some
     * other type.
     * 
     * @param name Parameter's name
     * @return Parameter's value as a String
     */
    public String getString(String name) {
        Parameter p = (Parameter) parameters.get(name);
        if(p == null) {
            return null;
            /*
             * throw something...
             */
        }
        // We don't start complaining if it's not really a String...
        return p.getValue().toString();
    }


    /**
     * Returns value of a parameter of INT-type.
     * 
     * @param name Parameter's name
     * @return The int value
     */
    public int getInt(String name) {
        Parameter p = (Parameter) parameters.get(name);
        if(p == null) {
            return 0;
            /*
             * throw something...
             */
        }
        if(p.type != INT) {
            /*
             * throw something else...
             */
        }
        return ((Integer) p.getValue()).intValue();
    }


    /**
     * Returns value of a parameter of LONG-type.
     * 
     * @param name Parameter's name
     * @return The long value
     */
    public long getLong(String name) {
        Parameter p = (Parameter) parameters.get(name);
        if(p == null) {
            return 0;
            /*
             * throw something...
             */
        }
        if(p.type != LONG) {
            /*
             * throw something else...
             */
        }
        return ((Long) p.getValue()).longValue();
    }


    /**
     * Returns value of a parameter of DOUBLE-type.
     * 
     * @param name Parameter's name
     * @return The double value
     */
    public double getDouble(String name) {
        Parameter p = (Parameter) parameters.get(name);
        if(p == null) {
            /*
             * throw something...
             */
            return 0;
        }
        if(p.type != DOUBLE) {
            /*
             * throw something else...
             */
        }
        return ((Double) p.getValue()).doubleValue();
    }


    /**
     * Returns value of a parameter of BOOLEAN-type.
     * 
     * @param name Parameter's name
     * @return The boolean value
     */
    public boolean getBoolean(String name) {
        Parameter p = (Parameter) parameters.get(name);
        if(p == null) {
            /*
             * throw something...
             */
            return false;
        }
        if(p.type != BOOLEAN) {
            /*
             * throw something else...
             */
        }
        return ((Boolean) p.getValue()).booleanValue();
    }


    /**
     * Returns value of a parameter of STRING_ARRAY-type.
     * 
     * @param name Parameter's name
     * @return The stringArray value
     */
    public String[] getStringArray(String name) {
        Parameter p = (Parameter) parameters.get(name);
        if(p == null) {
            /*
             * throw something...
             */
            return null;
        }
        if(p.type != STRING_ARRAY) {
            /*
             * throw something else...
             */
        }
        return (String[]) p.getValue();
    }


    /**
     * Returns value of a parameter of INT_ARRAY-type.
     * 
     * @param name Parameter's name
     * @return The intArray value
     */
    public int[] getIntArray(String name) {
        Parameter p = (Parameter) parameters.get(name);
        if(p == null) {
            /*
             * throw something...
             */
            return null;
        }
        if(p.type != INT_ARRAY) {
            /*
             * throw something else...
             */
        }
        return (int[]) p.getValue();
    }


    /**
     * Returns value of a parameter of DOUBLE_ARRAY-type.
     * 
     * @param name Parameter's name
     * @return The doubleArray value
     */
    public double[] getDoubleArray(String name) {
        Parameter p = (Parameter) parameters.get(name);
        if(p == null) {
            /*
             * throw something...
             */
            return null;
        }
        if(p.type != DOUBLE_ARRAY) {
            /*
             * throw something else...
             */
        }
        return (double[]) p.getValue();
    }

    /**
     * Returns value of a parameter of LONG_ARRAY-type.
     * 
     * @param name Parameter's name
     * @return The long array value
     */
    public long[] getLongArray(String name) {
        Parameter p = (Parameter) parameters.get(name);
        if(p == null) {
            /*
             * throw something...
             */
            return null;
        }
        if(p.type != LONG_ARRAY) {
            /*
             * throw something else...
             */
        }
        return (long[]) p.getValue();
    }


    /**
     * Returns value of a parameter of FLOAT-type.
     * 
     * @param name Parameter's name
     * @return The float value
     */
    public float getFloat(String name) {
        Parameter p = (Parameter) parameters.get(name);
        if(p == null) {
            return 0;
            /*
             * throw something...
             */
        }
        if(p.type != FLOAT) {
            /*
             * throw something else...
             */
        }
        return ((Float) p.getValue()).floatValue();
    }


    /**
     * Returns the parameter's type.
     * 
     * @param name Parameter's name
     * @return Parameter's type. See the constants for types
     */
    public int getType(String name) {
        Parameter p = (Parameter) parameters.get(name);
        if(p == null) {
            return -1;
        }
        return p.type;
    }


    /**
     * Extracts parameters from a request.
     * 
     * @param req HttpServletRequest where to get the parameters
     */
    public void extractParameters(HttpServletRequest req) {
        Enumeration e = parameters.keys();
        while(e.hasMoreElements()) {
            String name = (String) e.nextElement();
            handleExtractedParameter(name, req.getParameterValues(name));
            /*
             * Parameter p = (Parameter) parameters.get(name); boolean isArray = p.type ==
             * STRING_ARRAY || p.type == INT_ARRAY || p.type == DOUBLE_ARRAY; // If parameter is
             * given in the request, extract it and mark as given if (req.getParameter(name) !=
             * null) { if (isArray) { p.setValue(req.getParameterValues(name)); } else {
             * p.setValue(req.getParameter(name)); } p.wasGiven = true; p.isChanged = true; } else { //
             * ...mark it as not given if (p.type == BOOLEAN) { p.setValue(req.getParameter(name)); }
             * p.wasGiven = false; p.isChanged = false; }
             */
        }
    }


    /**
     * Extracts parameters from a multipart request.
     * 
     * @param multi MultipartRequest where to get the parameters
     */
    public void extractParameters(MultipartRequest multi) {
        Enumeration e = parameters.keys();
        Vector valueVector = new Vector();
        while(e.hasMoreElements()) {
            String name = (String) e.nextElement();
            Enumeration values = multi.getURLParameters(name);
            String[] valueArray = null;
            if(values.hasMoreElements()) {
                valueVector.clear();
                while(values.hasMoreElements())
                    valueVector.add(values.nextElement());
                valueArray = (String[]) valueVector.toArray(new String[valueVector.size()]);
            }

            handleExtractedParameter(name, valueArray);
        }
    }

    private void handleExtractedParameter(String name, String[] values) {

        Parameter p = (Parameter) parameters.get(name);
        boolean isArray = p.type == STRING_ARRAY || p.type == INT_ARRAY || p.type == DOUBLE_ARRAY
                || p.type == LONG_ARRAY;
        // If parameter is given in the request, extract it and mark as given
        if(values != null && values.length > 0) {
            if(isArray) {
                p.setValue(values);
            } else {
                p.setValue(values[0]);
            }
            p.wasGiven = true;
            p.isChanged = true;
        } else {
            // ...mark it as not given
            if(p.type == BOOLEAN) {
                p.setValue((String) null);
            }
            p.wasGiven = false;
            p.isChanged = false;
        }
    }


    /**
     * Sets parameter's value.
     * 
     * @param name Parameter's name
     * @param value New value as a String. If parameter's type is not STRING conversion is
     *        attempted.
     */
    public void setParameter(String name, String value) {
        Parameter p = (Parameter) parameters.get(name);
        if(p != null) {
            p.setValue(value);
            p.isChanged = true;
        }
    }


    /**
     * Sets the value of a parameter of INT-type.
     * 
     * @param name Parameter's name
     * @param value New value as int
     */
    public void setParameter(String name, int value) {
        Parameter p = (Parameter) parameters.get(name);
        if(p != null) {
            switch(p.type) {
            case INT:
                p.value = new Integer(value);
                p.illegal = false;
                break;
            case LONG:
                p.value = new Long(value);
                p.illegal = false;
            case DOUBLE:
                p.value = new Double(value);
                p.illegal = false;
            case FLOAT:
                p.value = new Float(value);
                p.illegal = false;
            default:
                p.value = null;
                p.illegal = true;
            }
            p.isChanged = true;
        }
    }


    /**
     * Sets the value of a parameter of LONG-type.
     * 
     * @param name Parameter's name
     * @param value New value as int
     */
    public void setParameter(String name, long value) {
        Parameter p = (Parameter) parameters.get(name);
        if(p != null) {
            if(p.type == LONG) {
                p.value = new Long(value);
                p.illegal = false;
            } else {
                p.value = null;
                p.illegal = true;
            }
            p.isChanged = true;
        }
    }


    /**
     * Sets the value of a parameter of DOUBLE-type.
     * 
     * @param name Parameter's name
     * @param value New value as double
     */
    public void setParameter(String name, double value) {
        Parameter p = (Parameter) parameters.get(name);
        if(p != null) {
            if(p.type == DOUBLE) {
                p.value = new Double(value);
                p.illegal = false;
            } else {
                p.value = null;
                p.illegal = true;
            }
            p.isChanged = true;
        }
    }


    /**
     * Sets the value of a parameter of BOOLEAN-type.
     * 
     * @param name Parameter's name
     * @param value New value as boolean
     */
    public void setParameter(String name, boolean value) {
        Parameter p = (Parameter) parameters.get(name);
        if(p != null) {
            if(p.type == BOOLEAN) {
                p.value = new Boolean(value);
                p.illegal = false;
            } else {
                p.value = null;
                p.illegal = true;
            }
            p.isChanged = true;
        }
    }


    /**
     * Method for setting array parameter's value. All the array versions of setParameter use this.
     * 
     * @param name Parameter's name
     * @param value New value. Should be an array of the right type, but it isn't checked here.
     * @param type Parameter's type
     */
    public void setArrayParameter(String name, Object value, int type) {
        Parameter p = (Parameter) parameters.get(name);
        if(p != null) {
            if(p.type == type) {
                p.value = value;
                p.illegal = false;
            } else {
                p.value = null;
                p.illegal = true;
            }
            p.isChanged = true;
        }
    }


    /**
     * Sets the parameter attribute of the ParameterManager object
     * 
     * @param name The new parameter value
     * @param value The new parameter value
     */
    public void setParameter(String name, String[] value) {
        setArrayParameter(name, value, STRING_ARRAY);
    }


    /**
     * Sets the parameter attribute of the ParameterManager object
     * 
     * @param name The new parameter value
     * @param value The new parameter value
     */
    public void setParameter(String name, int[] value) {
        setArrayParameter(name, value, INT_ARRAY);
    }


    /**
     * Sets the parameter attribute of the ParameterManager object
     * 
     * @param name The new parameter value
     * @param value The new parameter value
     */
    public void setParameter(String name, double[] value) {
        setArrayParameter(name, value, DOUBLE_ARRAY);
    }


    public void setParameter(String name, long[] value) {
        setArrayParameter(name, value, LONG_ARRAY);
    }


    /**
     * Sets the value of a parameter of FLOAT-type.
     * 
     * @param name Parameter's name
     * @param value New value as float
     */
    public void setParameter(String name, float value) {
        Parameter p = (Parameter) parameters.get(name);
        if(p != null) {
            if(p.type == FLOAT) {
                p.value = new Float(value);
                p.illegal = false;
            } else {
                p.value = null;
                p.illegal = true;
            }
            p.isChanged = true;
        }
    }


    /**
     * Was the parameter given in the request.
     * 
     * @param name Parameter's name
     * @return true, if parameter was given in the request and false if it wasn't
     */
    public boolean wasGiven(String name) {
        Parameter p = (Parameter) parameters.get(name);
        if(p != null) {
            return p.wasGiven;
        }
        return false;
    }


    /**
     * Has parameter's value been changed, either being given in the request or with
     * setParameter-method.
     * 
     * @param name Parameter's name
     * @return true, if parameter was given in the request and false if it wasn't
     */
    public boolean isChanged(String name) {
        Parameter p = (Parameter) parameters.get(name);
        if(p != null) {
            return p.isChanged;
        }
        return false;
    }


    /**
     * Was parameter given an illegal value. STRING and BOOLEAN parameters are never illegal. If INT
     * or DOUBLE parameter's value can't be parsed, parameter is set to illegal.
     * 
     * @param name Parameter's name
     * @return true, if parameter was given an illegal value
     */
    public boolean wasIllegal(String name) {
        Parameter p = (Parameter) parameters.get(name);
        if(p != null) {
            return p.illegal;
        }
        return false;
    }


    /**
     * Gets the input attribute of the ParameterManager object
     * 
     * @param inputType Description of the Parameter
     * @param name Description of the Parameter
     * @return The input value
     */
    public Input getInput(String inputType, String name) {
        return getInput(inputType, name, 0);
    }


    /**
     * Makes an ECS Input-object from the parameter. It is recommended that the method is used only
     * to make text and hidden elements, and checkbox fields ONLY if the parameter is of type
     * BOOLEAN. Other kind of usage may give weird results.
     * 
     * @param name Name of the parameter
     * @param inputType Type of the ECS Input-object
     * @param index Description of the Parameter
     * @return ECS input element of given type having the name and value of the parameter
     */
    public Input getInput(String inputType, String name, int index) {
        Parameter p = (Parameter) parameters.get(name);
        if(p == null) {
            return null;
        }
        // Special treatment for checkboxes
        if(inputType.equals(Input.CHECKBOX)) {
            // If it's an boolean value, this is easy
            if(p.type == BOOLEAN) {
                return new Input(Input.CHECKBOX, name, "true").setChecked(((Boolean) p.getValue())
                    .booleanValue());
            } else {
                // If it's not this gets bit complicated. Why would anyone make a checkbox of a
                // non-boolean parameter anyway?
                // Let's give him something, but I don't know if this makes any sense. Not
                // recommended to use the method this way...
                String value = p.getValue().toString();
                if(value.equals("")) {
                    // Now we don't even know what the value should be...
                    return new Input(Input.CHECKBOX, name, "true").setChecked(false);
                } else {
                    return new Input(Input.CHECKBOX, name, Utils.htmlentities(value))
                        .setChecked(true);
                }
            }
        }

        String value = "";
        switch(p.type) {
        case STRING_ARRAY:
            value = ((String[]) p.getValue())[index];
            break;
        case INT_ARRAY:
            value = String.valueOf(((int[]) p.getValue())[index]);
            break;
        case DOUBLE_ARRAY:
            value = String.valueOf(((double[]) p.getValue())[index]);
            break;
        case LONG_ARRAY:
            value = String.valueOf(((long[]) p.getValue())[index]);
            break;
        default:
            value = p.getValue().toString();
        }
        return new Input(inputType, name, Utils.htmlentities(value));
    }


    /**
     * Gets the parameterNames attribute of the ParameterManager object
     * 
     * @return The parameterNames value
     */
    public Enumeration getParameterNames() {
        return parameters.keys();
    }


    /**
     * Constructs a string that can be appended in a URL in GET-request.
     * 
     * @param mode Defines which parameters are included in the string. Either ALL, GIVEN or
     *        CHANGED.
     * @return Description of the Return Value
     */
    public String toString(int mode) {
        String urlString = "";
        char symbol = '?';
        Enumeration e = parameters.keys();
        while(e.hasMoreElements()) {
            String name = (String) e.nextElement();
            Parameter p = (Parameter) parameters.get(name);
            // No need to include parameter, if it's value is the default value
            if(p.value == null || p.value.equals(p.defaultValue)) {
                continue;
            }
            // Skip the parameters, that are not to be included in the string
            if(mode == GIVEN && !p.wasGiven) {
                continue;
            }
            if(mode == CHANGED && !p.isChanged) {
                continue;
            }
            // Append the parameter to the string
            try {
                urlString += symbol + name + '='
                        + (URLEncoder.encode(p.getValue().toString(), charset));
            } catch(java.io.UnsupportedEncodingException uee) {}
            // Change the symbol if this was the first parameter in the string
            if(symbol == '?') {
                symbol = '&';
            }
        }

        return urlString;
    }


    /**
     * Description of the Method
     * 
     * @return Description of the Return Value
     */
    public String toString() {
        return toString(ALL);
    }


    /**
     * Returns a description of used parameters.
     * 
     * @return The description value
     */
    public String getDescription() {
        StringBuffer desc = new StringBuffer();
        // Overall description
        if(this.description != null) {
            desc.append(description);
        }
        Enumeration e = parameters.keys();
        while(e.hasMoreElements()) {
            String name = (String) e.nextElement();
            Parameter p = (Parameter) parameters.get(name);
            // Type and name
            desc.append(TYPE_NAMES[p.type]).append(' ').append(name);
            // Default
            if(p.defaultValue != null) {
                desc.append(" (default: ").append(p.defaultValue.toString()).append(')');
            }
            // Parameter description
            if(p.parameterDescription != null) {
                desc.append(" - ").append(p.parameterDescription);
            }
            desc.append('\n');
        }
        return desc.toString();
    }


    /**
     * Returns a table containing a description of the parameters.
     * 
     * @return ECS-table
     */
    public Table getDescriptionTable() {
        // -----
        // Title
        // -----
        Table descTable = new Table().setBorder(1).addElement(
            new TR().addElement(new TD().setColSpan(4).addElement(new B("Parameter description"))));
        // ------------------------------
        // Overall description (if given)
        // ------------------------------
        if(this.description != null) {
            descTable.addElement(new TR().addElement(new TD().setColSpan(4).addElement(
                this.description)));
        }
        // ------------------------------------
        // Parameter description column headers
        // ------------------------------------
        descTable.addElement(new TR()
            .addElement(new TD().addElement(new B("Name")))
            .addElement(new TD().addElement(new B("Type")))
            .addElement(new TD().addElement(new B("Default")))
            .addElement(new TD().addElement(new B("Description"))));
        // ----------------------------
        // Print parameter descriptions
        // ----------------------------
        Enumeration e = parameters.keys();
        while(e.hasMoreElements()) {
            String name = (String) e.nextElement();
            Parameter p = (Parameter) parameters.get(name);
            descTable.addElement(new TR()
                .addElement(new TD().addElement(name))
                .addElement(new TD().addElement(TYPE_NAMES[p.type]))
                .addElement(
                    new TD().addElement(p.defaultValue != null ? p.defaultValue.toString() : "-"))
                .addElement(new TD().addElement(p.parameterDescription != null ? p.parameterDescription : "-")));
        }
        return descTable;
    }
} // ParameterManager