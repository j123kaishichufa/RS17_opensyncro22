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

package smilehouse.opensyncro.servlets;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import smilehouse.opensyncro.pipes.Pipe;
import smilehouse.opensyncro.pipes.PipeWrapper;
import smilehouse.opensyncro.system.Environment;
import smilehouse.opensyncro.system.Persister;
import smilehouse.opensyncro.system.PipeExecutionThread;
import smilehouse.opensyncro.system.Persister.DatabaseConnectionException;

/**
 * SyncroStartup.java
 * 
 * Loaded on startup. Initializes the environment.
 * 
 * Created: Tue May 25 10:50:02 2004
 */

public class SyncroStartup extends HttpServlet {

	public static String CLEAR_MODE ="CLEAR";
	public static String EXECUTE_MODE ="RUN";
	
    public void init(ServletConfig config) //throws ServletException
    {
        String webappName = null;

        try {

            // Get webapp URL path
            String webappURL = config.getServletContext().getResource("/").toString();

            // Remove trailing slash character from the path (if exists)
            if(webappURL.charAt(webappURL.length() - 1) == '/') {
                webappURL = webappURL.substring(0, webappURL.length() - 1);
            }

            // Extract the last path component as webappName
            webappName = webappURL.substring(webappURL.lastIndexOf('/') + 1);
            
        } catch(Exception e) {
            System.err
                .println("OpenSyncro: Unable to get the webapp path name to determine which "
                        + "properties file to load for this instance - defaulting to opensyncro.properties.");
            webappName = "opensyncro";
        }

        // Initialize the environment
        Environment.initialize(webappName); 

        // Initialize XSLT TransformerFactory to Xalan (XSLT 1.0).
        // This prevents Saxon (or other XSLT processors) to be selected
        // for initialization time XSL transformations.
        System.setProperty(
            "javax.xml.transform.TransformerFactory",
            "org.apache.xalan.processor.TransformerFactoryImpl");
        
        Map pipesAndThreads=null;
        if(config.getServletContext().getAttribute(SyncroHttpStart.PIPES_AND_THREADS)==null){
        	//create synchronized HashMap
			pipesAndThreads=Collections.synchronizedMap(new HashMap());
			//Add the pipesAndThreads map to servlet context so that requests received during execution of
			//pipes (via SyncroHttpStart) would be aware of executing pipes and would be queued instead of 
			//creating a new executor thread
			config.getServletContext().setAttribute(SyncroHttpStart.PIPES_AND_THREADS, pipesAndThreads);
		}
        //Go through each database listed in the properties file and, depending on the settings, start or remove
        //found pipe execution request.
        HashMap db=Environment.getInstance().getDatabaseInfo();
        Iterator it=db.keySet().iterator();
        while(it.hasNext()){
        	Persister pers;
        	String database=(String)it.next();
        	try{
        		pers=new Persister(database);
        	}
        	catch(DatabaseConnectionException dce){
        		Environment.getInstance().log(dce.getMessage());
        		continue;
        	}
        	String mode=(String)db.get(database);
        	//Database listed, but no resumemode specified
        	
        	if(mode==null){
        		Environment.getInstance().log("Mode not specified for database "+database);
        	}
        	//Delete all execution request from this database
        	else if(mode.equalsIgnoreCase(CLEAR_MODE)){
        		pers.deleteAllPipeExecutionRequests(null);
        	}
        	//Execute found execution requests
        	else if(mode.equalsIgnoreCase(EXECUTE_MODE)){
        		Iterator pipeIt=pers.loadAllPipes().iterator();
        		
        		//Iterate through all the pipes found in the database
        		while (pipeIt.hasNext()) {
        			Persister perPipePersister = new Persister(database);
					Pipe p = perPipePersister.findPipeByName(((Pipe) pipeIt.next()).getName());
					String dbase = perPipePersister.getDatabaseName();
					PipeWrapper pw = new PipeWrapper(p, dbase, dbase + p.getName());
					PipeExecutionThread pet = new PipeExecutionThread(pipesAndThreads,
							p.getId(),perPipePersister);
					//PipeExecutionQueue peq = new PipeExecutionQueue(perPipePersister, 
						//	p.getId());
					//If the there are PipeExecutionRequests in the database for this pipe, 
					//execute them.
					if (!pet.getPipeExecutionQueue().isQueueEmpty()) {
						pipesAndThreads.put(pw, pet);
						pet.start();
        			}
        			else{
        				pet.close();
        			}
					
				}
        	}
        	else{
        		Environment.getInstance().log("Invalid mode - "+mode+" - for database "+database+" specified");
        	}
        	pers.close();
        }
        
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException
    {}

    public void doPost(HttpServletRequest req, HttpServletResponse res)throws IOException, ServletException
    {}


} // SyncroStartup