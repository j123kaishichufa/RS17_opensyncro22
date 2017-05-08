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
 * Created on Jun 9, 2005 
 */
package smilehouse.opensyncro.system;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;

import smilehouse.opensyncro.pipes.component.ComponentClassNotFoundException;
import smilehouse.opensyncro.pipes.component.PipeComponentData;
import smilehouse.opensyncro.pipes.component.PipeComponentIF;
import smilehouse.opensyncro.servlets.PipeComponentCreationException;

/**
 * ComponentLoader handles loading of OpenSyncro's defaultcomponents and dynamic components. Dynamic
 * components are loaded from jar-files which are located in the component directory. Component
 * directory is specified in opensyncro-property file's component.dir property. Rescanning and
 * reloading of dynamic components is possible by calling the refresh-method. 
 * OpenSyncro is expected to create a single ComponentLoader-object. 
 * 
 * Created on Jun 9, 2005
 */

public class ComponentLoader {

    /** Finds all files ending with .jar */
    private static final class MyJarFilter implements FilenameFilter {
        public MyJarFilter() {}

        public boolean accept(File dir, String name) {
            return name.endsWith(".jar");
        }
    }

    //Key=className [String], Value= ClassLoader for the class
    private Map resourceMap;

    /**
     *  Creates a new componentloader and creates the classloaders by scanning the component dir's jar files.
     */
    public ComponentLoader() {
        super();
        refresh();
    }

    /**
     * Reloads the dynamic components.
     *
     */
    public void refresh() {
        this.resourceMap = new HashMap();
        loadDefaultComponents(resourceMap);
        loadDynamicComponents(resourceMap);
    }

    /**
     * 
     * @return Returns instances of all available OpenSyncro components.
     */
    public List loadComponents() {
        List list = new LinkedList();
        for(Iterator iter = resourceMap.keySet().iterator(); iter.hasNext();) {
            String className = (String) iter.next();
            PipeComponentIF pcimpl = getInstance(className);
            list.add(pcimpl);
        }

        return list;
    }

    /**
     * 
     * @param className
     * @return The Class object created with the component's classloader. Can be used for instantiating the component.
     * @throws ComponentClassNotFoundException
     */
    public Class getImplementationClass(String className) throws ComponentClassNotFoundException {
        try {
            ClassLoader cl = (ClassLoader) resourceMap.get(className);
            if(cl == null)
                throw new ComponentClassNotFoundException("Couldn't find class for component.");
            return Class.forName(className, true, cl);
        } catch(ClassNotFoundException cle) {
            throw new ComponentClassNotFoundException("Couldn't find class for component.", cle);
        }
    }

    /**
     * Checks if a component is available.
     * @param className
     * @return true if a component is available
     */
    public boolean implementationExists(String className) {
        try {
            getImplementationClass(className);
            return true;
        } catch(ComponentClassNotFoundException cce) {
            return false;
        }
    }

    /**
     * Creates the component object.
     * @param componentID
     * @return The created component object
     * @throws PipeComponentCreationException
     */
    public PipeComponentIF getInstance(String componentID) throws PipeComponentCreationException {
        return getInstance(componentID, null);
    }

    /**
     * Creates the component object.
     * 
     * @param componentID
     * @param data The data associated with this component
     * @return The created component object
     * @throws PipeComponentCreationException
     */
    public PipeComponentIF getInstance(String componentID, PipeComponentData data)
            throws PipeComponentCreationException {
        try {

            if(componentID == null) {
                Environment.getInstance().log("OpenSyncro internal error: Persister.getInstance called with" +
                    						  " a NULL componentID");
                return null;
            }

            // Initialize a new PipeComponentData if needed
            if(data == null) {
                data = new PipeComponentData();
                data.setAttributes(new HashMap());
            }

            // Get the constructor every component should implement
            Class implClass = getImplementationClass(componentID);
            Class[] paramTypes = {Object.class};
            Constructor cons = implClass.getConstructor(paramTypes);

            // Call the constructor to instantiate a new component
            Object[] params = {data};
            PipeComponentIF component = (PipeComponentIF) cons.newInstance(params);
            return component;

        } catch(ComponentClassNotFoundException cle) {
            throw new PipeComponentCreationException("Implementation class for Pipe Component "
                    + componentID + " not found.", cle);
        } catch(InstantiationException e) {
            throw new PipeComponentCreationException("Implementation class for Pipe Component "
                    + componentID + " cannot be instantiated.", e);
        } catch(IllegalAccessException e) {
            throw new PipeComponentCreationException("Implementation class of " + componentID
                    + " cannot be accessed.", e);
        } catch(NoSuchMethodException e) {
            throw new PipeComponentCreationException("NoSuchMethodException while trying to get a "
                    + "constructor for creating a new instance of Pipe Component " + componentID
                    + ".", e);
        } catch(InvocationTargetException e) {
            throw new PipeComponentCreationException("InvocationTargetException while trying to "
                    + "instantiate Pipe Component" + componentID + ".", e);
        }
    }



    private void loadDefaultComponents(Map m) {
        ClassLoader syncroBase = getSyncroBaseClassLoader();

        m.put("smilehouse.opensyncro.defaultcomponents.converter.ascii.ASCIItoXMLConverter", syncroBase);
        m.put("smilehouse.opensyncro.defaultcomponents.converter.csv.CSVtoXMLConverter", syncroBase);
        m.put("smilehouse.opensyncro.defaultcomponents.converter.join.JoinConverter", syncroBase);
        m.put("smilehouse.opensyncro.defaultcomponents.converter.split.SplitConverter", syncroBase);
        m.put("smilehouse.opensyncro.defaultcomponents.converter.xml.groupexpander.XMLGroupExpander", syncroBase);
        m.put("smilehouse.opensyncro.defaultcomponents.converter.xml.xslt.XSLTConverter", syncroBase);
        m.put("smilehouse.opensyncro.defaultcomponents.converter.xml.xslt.XSLT20Converter", syncroBase);
        m.put("smilehouse.opensyncro.defaultcomponents.filesystem.LocalFileDestination", syncroBase);
        m.put("smilehouse.opensyncro.defaultcomponents.filesystem.LocalFileSource", syncroBase);
        m.put("smilehouse.opensyncro.defaultcomponents.filesystem.DirectorySource", syncroBase);
        m.put("smilehouse.opensyncro.defaultcomponents.filesystem.TimestampFileDestination", syncroBase);
        m.put("smilehouse.opensyncro.defaultcomponents.filesystem.TimestampFileSource", syncroBase);
        m.put("smilehouse.opensyncro.defaultcomponents.filesystem.MultiFileDestination", syncroBase);
        m.put("smilehouse.opensyncro.defaultcomponents.filesystem.IteratingFileSource", syncroBase);
        m.put("smilehouse.opensyncro.defaultcomponents.filesystem.IteratingXMLFileSource", syncroBase);
        m.put("smilehouse.opensyncro.defaultcomponents.filesystem.LocalFileReadConverter", syncroBase);
        m.put("smilehouse.opensyncro.defaultcomponents.filesystem.LocalFileWriteConverter", syncroBase);
        m.put("smilehouse.opensyncro.defaultcomponents.ftp.FTPDestination", syncroBase);
        m.put("smilehouse.opensyncro.defaultcomponents.ftp.FTPSource", syncroBase);
        m.put("smilehouse.opensyncro.defaultcomponents.http.HTTPSource", syncroBase);
        m.put("smilehouse.opensyncro.defaultcomponents.http.HTTPConverter", syncroBase);
        m.put("smilehouse.opensyncro.defaultcomponents.http.HTTPDestination", syncroBase);
        m.put("smilehouse.opensyncro.defaultcomponents.jdbc.JDBCSource", syncroBase);
        m.put("smilehouse.opensyncro.defaultcomponents.jdbc.JDBCConverter", syncroBase);
        m.put("smilehouse.opensyncro.defaultcomponents.jdbc.JDBCDestination", syncroBase);
        m.put("smilehouse.opensyncro.defaultcomponents.workspace.WorkspaceHQLSource", syncroBase);
        m.put("smilehouse.opensyncro.defaultcomponents.workspace.WorkspaceHQLOrderSource", syncroBase);
        m.put("smilehouse.opensyncro.defaultcomponents.workspace.RemoteOrderSource", syncroBase);
        m.put("smilehouse.opensyncro.defaultcomponents.workspace.RemoteCustomerSource", syncroBase);
        m.put("smilehouse.opensyncro.defaultcomponents.workspace.WorkspaceHQLResultConverter", syncroBase);
        m.put("smilehouse.opensyncro.defaultcomponents.workspace.RemoteCustomerDestination", syncroBase);
        m.put("smilehouse.opensyncro.defaultcomponents.workspace.RemotePriceListDestination", syncroBase);
        m.put("smilehouse.opensyncro.defaultcomponents.workspace.RemoteProductDestination", syncroBase);
        m.put("smilehouse.opensyncro.defaultcomponents.workspace.RemoteOrderDestination", syncroBase);
        m.put("smilehouse.opensyncro.defaultcomponents.workspace.RemoteOrderConverter", syncroBase);
        
        m.put("smilehouse.opensyncro.defaultcomponents.string.StringSource", syncroBase);
    }

    private ClassLoader getSyncroBaseClassLoader() {
        ClassLoader syncroBase = this.getClass().getClassLoader();
        return syncroBase;
    }

    public class ComponentInitException extends Exception {
        public ComponentInitException(String message) {
            super(message);
        }

        public ComponentInitException(String message, Throwable t) {
            super(message, t);
        }
    }

    /**
     * @param resourceMap
     */
    private void loadDynamicComponents(Map resourceMap) {
        File componentDir = readComponentDir();
        if(componentDir != null) {
            File[] jars = componentDir.listFiles(new MyJarFilter());
            for(int i = 0; i < jars.length; i++) {
                File jar = jars[i];
                try {
                    initJarFile(jar, resourceMap);
                } catch(ComponentInitException cie) {
                    Environment env = Environment.getInstance();
                    env
                        .log(
                            "Component initialization failed for jar: " + jar.toString() + ".",
                            cie);
                }
            }
        }
    }

    private void initJarFile(File jar, Map resourceMap) throws ComponentInitException {
        if(!jar.exists())
            throw new ComponentInitException("FAILED: component file not found.");
        if(!jar.canRead())
            throw new ComponentInitException("FAILED: component file not readable.");
        if(!jar.isFile())
            throw new ComponentInitException(
                "FAILED: component file should be a jar file, this is not a file.");

        //Init loader
        JarClassLoader loader = null;
        try {
            loader = new JarClassLoader(jar, this.getClass().getClassLoader());
        } catch(MalformedURLException mue) {
            throw new ComponentInitException("FAILED: Problem reading the component jar file.", mue);
        }

        //read component information
        Properties componentProperties = new Properties();
        try {
            URL info = loader.getResource("component.properties");
            componentProperties.load(info.openStream());
        } catch(IOException ioe) {
            throw new ComponentInitException(
                "FAILED: component.properties not found in the component.",
                ioe);
        }

        int index = 0;
        
        // componentClass property is actually componentID, rename ASAP!
        
        while(componentProperties.getProperty("componentClass" + index) != null) {
            //System.err.println("putting dynamic class
            // "+componentProperties.getProperty("componentClass" + index));
            resourceMap.put(componentProperties.getProperty("componentClass" + index), loader);
            index++;
        }

    }

    /**
     * @return component directory if exists and is readable, null if not.
     */
    private File readComponentDir() {
        Environment env = Environment.getInstance();
        try {
            String componentDirProperty = Environment.getProperty("component.dir");
            if(componentDirProperty == null) {
                env
                    .log("WARN Component directory isn't defined (the properties file doesn't contain component.dir property). Dynamic component loading disabled");
                return null;
            }
            File componentDir = new File(componentDirProperty);
            if(!componentDir.exists()) {
                env.log("WARN Component directory (" + componentDir.toString()
                        + ") doesn't exist. Dynamic component loading disabled");
                return null;
            }
            if(!componentDir.canRead()) {
                env.log("WARN Component directory (" + componentDir.toString()
                        + ") isn't readable. Dynamic component loading disabled");
                return null;
            }
            if(!componentDir.isDirectory()) {
                env.log("WARN Component directory (" + componentDir.toString()
                        + ") invalid (not a directory). Dynamic component loading disabled");
                return null;
            }
            return componentDir;
        } catch(MissingResourceException mre) {
            env
                .log("WARN Component directory isn't defined (the properties file doesn't contain component.dir property). Dynamic component loading disabled");
            return null;
        }
    }

    /**
     * @param component
     * @return true if the component's class loader isn't OpenSyncro base class loader
     */
    public boolean isDynamicComponent(PipeComponentIF component) {
        if(component==null)
            throw new NullPointerException("component cannot be null");
        
        ClassLoader componentLoader = (ClassLoader) resourceMap.get(component.getID());
        ClassLoader syncroBase = getSyncroBaseClassLoader();
        
        return (componentLoader != syncroBase);
    }
}