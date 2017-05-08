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

import java.util.Iterator;

import smilehouse.opensyncro.pipes.Pipe;

/**
 *  PipeExecutionQueue.java Created: Fri May 12 2006
 *
 */
public class PipeExecutionQueue {

	private Persister persister;

	private Pipe pipe;

	private int queueLength;

	private Long pipeId;

	/**
	 *  Constructor for the PipeExecutionQueue object
	 *
	 *@param  persister  Persister to use 
	 *@param  pipeid     PipeId of the pipe this queue concerns
	 */
	public PipeExecutionQueue(Persister persister, Long pipeid) {
		this.persister = persister;
		this.pipe = null;
		this.pipeId = pipeid;
		this.queueLength = 0;

	}

	/**
	 *  Determine whether there are requests for execution of the current
	 *  pipe
	 *
	 *@return    true when PipeExecutionRequest queue is empty
	 */
	public synchronized boolean isQueueEmpty() {
		int size = persister.getPipeExecutionRequests(pipeId).size();

		if (size > 0) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 *  Give the oldest PipeExecutionRequest from the queue
	 *
	 *@return    PipeExecutionRequest with the oldest timestamp in the queue
	 */
	public synchronized PipeExecutionRequest getFirst() {
		PipeExecutionRequest per = null;
		Iterator requestListIt = persister
				.getPipeExecutionRequests(this.pipeId).iterator();
		if (requestListIt.hasNext()) {
			per = (PipeExecutionRequest) requestListIt.next();

			if (this.pipe == null) {
				this.pipe = per.getPipe();
			}
		}
		return per;
	}

	/**
	 *  Adds a PipeExecutionRequest to the queue
	 *
	 *@param  per  PipeExecutionRequest to add to the queue
	 */
	public synchronized void addToQueue(PipeExecutionRequest per) {
		this.persister.save(per);
		this.queueLength++;
		if (this.pipe == null) {
			this.pipe = per.getPipe();
		}
	}

	/**
	 *  Delete the PipeExecutionRequest 
	 *
	 *@param  perID  PipeExecutionRequest to delete
	 */
	public synchronized void deleteRequest(Long perID) {
		this.persister.deletePipeExecutionRequest(perID);
		this.queueLength--;
	}

	/**
	 *  Deletes all pipe execution requests from the queue
	 */
	public synchronized void deleteAllRequests() {
		this.persister.deleteAllPipeExecutionRequests(this.pipeId);

	}

	/**
	 *  Update the pipe
	 */
	public synchronized void updatePipe() {
		this.persister.update(pipe);
	}

	
	/**
	 *  Update given PipeExecutionRequest
	 *
	 *@param  per  PipeExecutionRequest to update
	 */
	public synchronized void updatePERequest(PipeExecutionRequest per) {
		this.persister.update(per);
	}

	/**
	 *  Returns the current queue length. Do not use for determining whether
	 *  queue is empty or not. Use isQueueEmpty() instead.
	 *
	 *@return    Current queue length
	 */
	public int getQueueLength() {
		return this.queueLength;
	}
}
