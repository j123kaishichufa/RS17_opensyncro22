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

package smilehouse.opensyncro.system;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smilehouse.util.LabelProvider;
import smilehouse.util.LabelResource;

/**
 * Environment.java
 * 
 * Created: Fri Feb 6 10:49:50 2004
 */

public abstract class Environment implements LabelProvider {

    private static Environment instance;

    /* Prefix for OpenSyncro properties files. Used only if the webapp name is other than
       the default "opensyncro".
    */
    private static final String PROPERTYFILENAME_PREFIX = "os_";
    
    private static final String LABEL_RESOURCE_BUNDLE = "smilehouse.opensyncro.system.labels";

    private static final String[] ENVIRONMENTS = {
            "smilehouse.opensyncro.system.StandaloneEnvironment"};

    private static ResourceBundle properties;

    protected static String getProperty(String propertyName) throws MissingResourceException {
        return properties.getString(propertyName);
    }

    /* Initializes the Environment. Must be called before accessing the other methods.
       If parameter webappName is not the default 'opensyncro', it gets appended to
       the OpenSyncro PROPERTYFILENAME_PREFIX. This allows multiple instances/versions of
       OpenSyncro on the same application server to read different properties files while
       maintaining backwards compatibility.
    */
    public static void initialize(String webappName) {

        String propertiesFilePath = "smilehouse.conf.";
        String propertiesFileName = "";
        
        if(!webappName.equals("opensyncro")) {
            propertiesFileName += PROPERTYFILENAME_PREFIX;
        }
        propertiesFileName += webappName;
        
        try {
            properties = ResourceBundle.getBundle(propertiesFilePath + propertiesFileName);
        } catch(RuntimeException e) {
            System.err.println("OpenSyncro: Cannot find file \"" + propertiesFileName 
                				+ ".properties\" in classpath, startup failed.");
        }
        
        getInstance();
    }
    
    public static Environment getInstance() {
        if(instance == null) {
            for(int i = 0; i < ENVIRONMENTS.length; i++) {
                try {
                    Class environmentClass = Class.forName(ENVIRONMENTS[i]);
                    instance = (Environment) environmentClass.newInstance();
                    break;
                } catch(ClassNotFoundException ignored) {} catch(InstantiationException ie) {
                    ie.printStackTrace();
                } catch(IllegalAccessException iae) {
                    iae.printStackTrace();
                }
            }
        }
        if(instance == null) {
            System.err
                .println("OpenSyncro: Couldn't get environment information, startup failed.");
            return null;
        } else
            return instance;
    }


    /**
     * This is same for all environments since labels shouldn't depend on the environment.
     */
    public String getLabel(String key, String lang) {

        String label = null;
        Locale locale = new Locale(lang);

        try {
            ResourceBundle resource = ResourceBundle.getBundle(LABEL_RESOURCE_BUNDLE, locale);
            label = resource.getString(key);
        } catch(Exception e) {
            log("Couldn't find label for key '" + key + "'");
        }

        return label;
    }

    public LabelResource getLabelResource(String lang) {
        return new LabelResource(this, lang);
    }

    public LabelResource getLabelResource(HttpSession session) {
        return new LabelResource(this, getLanguage(session));
    }

    public abstract Connection getConnection(String database) throws SQLException;
    
    public abstract Connection getConnectionSuppressException(String database) throws SQLException;

    public abstract void freeConnection(Connection conn) throws SQLException;

    public abstract void outputPage(int tabSetId,
                                    int tabId,
                                    String content,
                                    String toHead,
                                    PrintWriter out,
                                    HttpSession session) throws Exception;

    public abstract void outputErrorPage(Throwable e, PrintWriter out);

    public abstract HttpSession getSession(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException;


    public abstract String getContentType();

    public abstract String getCharsetWWW();

    public abstract String getDatabaseName(HttpSession session);

    public abstract String getLanguage(HttpSession session);

    public abstract void log(String message);
    
    public abstract void log(String message, Throwable t);

    public abstract void log(String message, Exception e);
    
    /**
     * Reads OpenSyncro database names and pipe execution queue resume mode settings from properties file
     * @return HashMap containing database names and responding pipe execution queue resume mode settings
     */
    public HashMap getDatabaseInfo(){
    	int i=1;
    	HashMap d=new HashMap();
    	String INIT="init";
    	String DATABASE="database";
    	String RESUMEMODE="resumemode";
    	while(true){
    		try{
    			String db=getProperty(INIT+i+"."+DATABASE);
    			String rm=getProperty(INIT+i+"."+RESUMEMODE);
    			d.put(db,rm);
        		i++;
    		}
    		catch(MissingResourceException mre){
    			//assuming entries are sequential
    			break;
    		}
    		
    	}
    	return d;
    }
    
} // Environment