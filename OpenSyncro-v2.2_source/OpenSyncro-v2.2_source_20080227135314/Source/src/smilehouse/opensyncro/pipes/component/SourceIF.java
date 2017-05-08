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
 * Created on Apr 26, 2005
 * 
 * All Source-type Pipe components implement this interface.
 * 
 * TODO: AbortCurrentIterationException, FailCurrentIterationException?
 */
package smilehouse.opensyncro.pipes.component;

import smilehouse.opensyncro.pipes.log.MessageLogger;
import smilehouse.opensyncro.pipes.metadata.SourceInfo;

public interface SourceIF extends PipeComponentIF {
    
    public String[] give(SourceInfo info, MessageLogger logger) throws FailTransferException, AbortTransferException;
 
    /** Open iteration session */
    public int open(SourceInfo info, MessageLogger logger) throws FailTransferException, AbortTransferException;

    /** Close iteration session */
    public int close(SourceInfo info, MessageLogger logger) throws FailTransferException, AbortTransferException;
    
    /**
     * OpenSyncro uses this method to inform the Source component whether the last data block
     * received from get() was successfully processed and stored in the Destination
     */
    public void lastBlockStatus(int statusCode) throws FailTransferException, AbortTransferException;
    
}
