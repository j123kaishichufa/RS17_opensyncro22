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

import smilehouse.opensyncro.pipes.Pipe;

/**
 *  PipeExecutionRequest.java Created: Fri May 12 2006
 *
 */
public class PipeExecutionRequest {

	private Long id;

	private Date createdDate;

	private Date startedDate;

	private Pipe pipe;

	/**
	 *  Empty constructor for the PipeExecutionRequest object
	 */
	public PipeExecutionRequest() {
	}

	/**
	 *  Constructor for the PipeExecutionRequest object
	 *
	 *@param  pipe          Description of the Parameter
	 *@param  creationDate  Description of the Parameter
	 */
	public PipeExecutionRequest(Pipe pipe, Date creationDate) {
		this.pipe = pipe;
		this.createdDate = creationDate;

	}

	/**
	 *  Gets the createdDate attribute of the PipeExecutionRequest object
	 *
	 *@return    The createdDate value
	 */
	public Date getCreatedDate() {
		return createdDate;
	}

	/**
	 *  Sets the createdDate attribute of the PipeExecutionRequest object
	 *
	 *@param  created  The new createdDate value
	 */
	public void setCreatedDate(Date created) {
		this.createdDate = created;
	}

	/**
	 *  Gets the id attribute of the PipeExecutionRequest object
	 *
	 *@return    The id value
	 */
	public Long getId() {
		return id;
	}

	/**
	 *  Sets the id attribute of the PipeExecutionRequest object
	 *
	 *@param  id  The new id value
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 *  Gets the pipe attribute of the PipeExecutionRequest object
	 *
	 *@return    The pipe value
	 */
	public Pipe getPipe() {
		return pipe;
	}

	/**
	 *  Sets the pipe attribute of the PipeExecutionRequest object
	 *
	 *@param  pipe  The new pipe value
	 */
	public void setPipe(Pipe pipe) {
		this.pipe = pipe;
	}

	/**
	 *  Gets the startedDate attribute of the PipeExecutionRequest object
	 *
	 *@return    The startedDate value
	 */
	public Date getStartedDate() {
		return startedDate;
	}

	/**
	 *  Sets the startedDate attribute of the PipeExecutionRequest object
	 *
	 *@param  started  The new startedDate value
	 */
	public void setStartedDate(Date started) {
		this.startedDate = started;
	}

}
