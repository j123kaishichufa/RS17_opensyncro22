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

package smilehouse.opensyncro.pipes;

public class PipeWrapper{
	public String id;
	private Pipe pipe;
	public String database;
	/**
	 * PipeWrapper.java
	 * 
	 * Enables adding database name to a pipe and using several databases simultaneously.
	 * 
	 * Created: Tue Nov 21 10:50:02 2006
	 */
	public PipeWrapper(Pipe pipe, String database, String id) {
		this.id = id;
		this.pipe = pipe;
		this.database = database;
	}
	
	/**
	 * @return Returns the database.
	 */
	public String getDatabase() {
		return database;
	}
	/**
	 * @param database The database to set.
	 */
	public void setDatabase(String database) {
		this.database = database;
	}
	/**
	 * @return Returns the pipe.
	 */
	public Pipe getPipe() {
		return pipe;
	}
	/**
	 * @param pipe The pipe to set.
	 */
	public void setPipe(Pipe pipe) {
		this.pipe = pipe;
	}

	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id The id to set.
	 */
	public void setId(String id) {
		this.id = id;
	}

}
