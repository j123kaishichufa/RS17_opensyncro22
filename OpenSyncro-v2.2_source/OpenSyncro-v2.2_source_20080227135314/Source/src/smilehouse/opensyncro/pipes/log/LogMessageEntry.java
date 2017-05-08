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

package smilehouse.opensyncro.pipes.log;

import java.io.Serializable;

public class LogMessageEntry implements Serializable{
	
	private Long id;
	private String message;
	private int index;
	private LogEntry log;
	private int messageType;
	
	public LogMessageEntry(String message,int index,LogEntry log,int type){
		this.message=message;
		this.index=index;
		this.log=log;
		this.messageType=type;
	}
	public LogMessageEntry(){}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	public LogEntry getLog() {
		return log;
	}
	public void setLog(LogEntry log) {
		this.log = log;
	}
	public int getMessageType() {
		return messageType;
	}
	public void setMessageType(int type) {
		this.messageType = type;
	}


}
