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

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import smilehouse.opensyncro.pipes.Pipe;
import smilehouse.opensyncro.pipes.PipeWrapper;
import smilehouse.opensyncro.pipes.metadata.TransferInfo;
import smilehouse.opensyncro.servlets.SyncroHttpStart;

/**
 * PipeExecutionThread.java Created: Fri May 12 2006
 *
 */
public class PipeExecutionThread extends Thread {

	private PipeExecutionQueue peq;

	private TransferInfo ti;
	
	private Map pipesThreads;
	
	private Persister pers;

	public static final String REMOTE_EXECUTER = "Remote";


	/**
	 * Full constructor
	 * @param pt Hashmap with the pipe list
	 * @param pipeid Id of the pipe for this thread
	 * @param persister Persiter to be used by the thread
	 */
	public PipeExecutionThread(Map pt,long pipeid,Persister persister) {
		this.pers= persister;
		this.peq = new PipeExecutionQueue(pers,pipeid);
		this.ti = new TransferInfo(persister.getDatabaseName(),REMOTE_EXECUTER);
		this.pipesThreads=pt;
	}

	public void run() {
		Environment en = Environment.getInstance();
		Pipe pipe=null;
		try {
			// Is queue empty?
			while (!this.peq.isQueueEmpty()) {
				// Get first PipeExecutionRequest in queue
				PipeExecutionRequest per = peq.getFirst();
				en.log("Getting pipe from request queue");
				pipe=per.getPipe();
				en.log("Got pipe: "+pipe.getName());
				// Set PipeExecutionRequest start timestamp
				per.setStartedDate(new Date());
				en.log("Set execution start time and updating pipe: "+pipe.getName());
				// Update PipeExecutionRequst
				peq.updatePERequest(per);
				en.log("Starting execution of pipe: "+pipe.getName());
				// Start pipe transfer with request creation date
				pipe.transfer(this.ti, per.getCreatedDate());
				en.log("Finished execution of pipe: "+pipe.getName());
				// Update pipe after execution
				peq.updatePipe();
				en.log("Updated pipe "+pipe.getName());
				// Remove completed PipeExecutionRequest from queue
				peq.deleteRequest(per.getId());
				en.log("Removed pipe "+ pipe.getName()+" from execution queue");
				
				
			}
			
		}
		catch(Exception e){
			
			en.log(e.getMessage(), e);
		}
		finally {
			// Remove persister from persisters, otherwise repeated execution of pipe may fail
			SyncroHttpStart.persisters.remove(pers.getDatabaseName()+pipe.getName());

            // Remove pipe and thread from webapp context (because queue is empty now)
            LinkedList<PipeWrapper> pipesThreadsToRemove = new LinkedList<PipeWrapper>();
			Iterator pipeWrapperIt = pipesThreads.keySet().iterator();
			while (pipeWrapperIt.hasNext()){
				PipeWrapper pw = (PipeWrapper)pipeWrapperIt.next();
				Pipe p = pw.getPipe();
				if(p.equals(pipe) && pw.getDatabase().equals(pers.getDatabaseName())){
					pipesThreadsToRemove.add(pw);
				}
			}
            Iterator ptRemoveIt = pipesThreadsToRemove.iterator();            
            while (ptRemoveIt.hasNext()){
                pipesThreads.remove(ptRemoveIt.next());
            }
			close();
		}
	}


	/**
	 * Gives the PipeExecutionQueue used by this thread
	 * @return PipeExecutionQueue used by the thread
	 */
	public PipeExecutionQueue getPipeExecutionQueue() {
		return this.peq;
	}
	
	/**
	 * Closes the persister used by this PipeExecutionThread
	 */
	public void close() {
		pers.close();
		
	}
	
	/**Gives the name of the database for the persister used by this PipeExecutionThread
	 * @return database name for the persister
	 */
	public String getDatabaseName(){
		return this.pers.getDatabaseName();
	}

}
