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
 * Created on 8.11.2004
 */
package smilehouse.opensyncro.servlets;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import smilehouse.opensyncro.pipes.PipeWrapper;
import smilehouse.opensyncro.system.Environment;
import smilehouse.opensyncro.system.Persister;
import smilehouse.opensyncro.system.PipeExecutionRequest;
import smilehouse.opensyncro.system.PipeExecutionThread;
import smilehouse.opensyncro.system.Persister.DatabaseConnectionException;

public class SyncroHttpStart extends HttpServlet {

    
	public static final String PIPES_AND_THREADS = "pipethreadmap";
	private static final String PARAM_DB = "database";
    private static final String PARAM_PASSWORD = "password";
    private static final String PARAM_PIPE = "pipe";
    private static final String PARAM_MODE = "mode";

    private static final String PARAM_VALUE_SYNCHRONOUS="sync";
    private static final String PARAM_VALUE_ASYNCHRONOUS="async";

    public static HashMap persisters=null;

    // Throws also ServletException?
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {

        String database = req.getParameter(PARAM_DB);
        String password = req.getParameter(PARAM_PASSWORD);
        String pipe = req.getParameter(PARAM_PIPE);
        String mode = req.getParameter(PARAM_MODE);
        String respMessage = null;   
        try {
            respMessage = start(database, password, pipe, mode);
        } catch(Exception e) {
            Environment.getInstance().log("Exception in HttpStart", e);
            respMessage = "Exception while trying to start the pipe: " + e.toString();
        }

        res.setContentType("text/plain");
        Writer out = res.getWriter();
        try {
            out.write(respMessage);
            out.write('\n');
            out.flush();
        } finally {
            if(out != null)
                out.close();
        }
    }

    private String start(String database, String startPassword, String pipeName, String mode) throws Exception {

        // ------------
        // Sanity check
        // ------------
        if(database == null || database.length() == 0)
            return "ERROR: Database not given";
        if(pipeName == null || pipeName.length() == 0)
            return "ERROR: Pipe not given";
        if(startPassword == null || startPassword.length() == 0)
            return "ERROR: Start password not given";
        //Default to syncronized operation mode
        boolean isSynced=true;

        if(mode!=null && mode.length()>0){
        	
        	if(mode.equalsIgnoreCase(PARAM_VALUE_SYNCHRONOUS))
        		isSynced=true;
        	else if(mode.equalsIgnoreCase(PARAM_VALUE_ASYNCHRONOUS))
        		isSynced=false;
        	else
        		return "ERROR: Mode parameter must be either 'sync' or 'async'";
        }
       
        // HashMap persisters contains a persister for each active pipe that is started via HttpStart.
       if(persisters == null)
			persisters = new HashMap();
        
        Persister pers=(Persister)persisters.get(database + pipeName);
        if(pers==null||pers.isClosed()){
        	try {
				pers = new Persister(database);
				//unique pipe identification by database name and pipename
				persisters.put(database + pipeName, pers);
			} catch (DatabaseConnectionException e) {
				return("ERROR: Invalid database name");
			}
        }
        
		int queueLength = 0;
		try {
			PipeWrapper pipe = null;

			Map pipesAndThreads = null;

			// Get pipesAndThreads object from webapp context. 
			// This is where running pipes and threads are listed 
			pipesAndThreads = (Map) this.getServletContext().getAttribute(PIPES_AND_THREADS);
			boolean newThread = false;

			if (pipesAndThreads == null) {
				// No pipesAndThreads object exists -> no pipes have been started via HttpStart 
				// nor has SyncroStartup started any.
				// Create synchronized map, as it may be modified simultaneously by several threads
				pipesAndThreads = Collections.synchronizedMap(new HashMap());
				this.getServletContext().setAttribute(PIPES_AND_THREADS,
						pipesAndThreads);
			}
			
			Iterator pipesIt=pipesAndThreads.keySet().iterator();
			boolean newPipe=true;
			// Iterate through active pipes to see if the pipe the incoming request refers to
			// is already executing

			while(pipesIt.hasNext()){
				PipeWrapper p = (PipeWrapper)pipesIt.next();
				String db=((PipeExecutionThread)pipesAndThreads.get(p)).getDatabaseName();
				//Since two databases could have pipes with the same name it is necessary to
				//check that in addition to pipe name database name also matches.
				if(p.getPipe().getName().equals(pipeName) && db.equals(database)){
					//Pipe was found. No need to create a new pipe
					newPipe=false;
					pipe=p;
				}
			}
			if(newPipe)
				pipe = new PipeWrapper(pers.findPipeByName(pipeName), database, database + pipeName);
			
			if(pipe == null || pipe.getPipe().getStartPassword() == null
					|| !pipe.getPipe().getStartPassword().equals(startPassword)) {
				return "ERROR: Incorrect pipe name or start password";
			}
			if(!pipe.getPipe().isHttpStartEnabled()) {
				return "ERROR: HTTP start is not enabled for this pipe";
			}

			//If pipesAndThreads already contains the pipe, get the PipeExecutionThread from it
			pipesIt=pipesAndThreads.keySet().iterator();
			PipeExecutionThread pet = null;
			while(pipesIt.hasNext()){
				PipeWrapper p = (PipeWrapper)pipesIt.next();
				String db=((PipeExecutionThread)pipesAndThreads.get(p)).getDatabaseName();
				if(p.getPipe().getName().equals(pipe.getPipe().getName()) && db.equals(pipe.getDatabase())){
					pet = (PipeExecutionThread) pipesAndThreads.get(p);
				}
			}

			Long pipeId=pipe.getPipe().getId();
			
			if (pet == null || !pet.getDatabaseName().equals(database)) {
				// Create new thread if no execution thread exists.
				pet = new PipeExecutionThread(pipesAndThreads,pipeId,pers);
				// Add the created thread to webapp context
				pipesAndThreads.put(pipe, pet);
				//Remember that new thread was created and this has to be started
				newThread = true;
			}

			// Add the new request to the threads execution queue
			pet.getPipeExecutionQueue().addToQueue(
					new PipeExecutionRequest(pipe.getPipe(), new Date()));
			if (newThread)
				pet.start();
			if (isSynced) {
				// If synced mode was specified, wait for the thread to finish
				pet.join();
			} else
				// Otherwise get the queue length to return in the response
				queueLength = pet.getPipeExecutionQueue().getQueueLength();
			
		} catch (Exception e) {
			Environment en=Environment.getInstance();
			en.log("Exception while processing requests in pipe execution queue for pipe: " + pipeName);
			en.log(e.getMessage(),e);
            return "ERROR: Exception while processing pipe execution request for pipe: " + pipeName;
		}
		
        if(isSynced)
        	return "OK";
        else
        	return "QUEUED (queue length: "+queueLength+")";
    }
}