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
 * All Destination-type Pipe components implement this interface.
 */
package smilehouse.opensyncro.pipes.component;

import smilehouse.opensyncro.pipes.log.MessageLogger;
import smilehouse.opensyncro.pipes.metadata.DestinationInfo;

public interface DestinationIF extends PipeComponentIF {

    // Change result type of 'take' methods from void to String.
    // This way the Destination component may return an ID with which the
    // data block was stored in the destination system. If no ID is available,
    // Destination component may return null value.

    public void take(String data, DestinationInfo info, MessageLogger logger)
            throws FailTransferException, AbortTransferException;

    public void takeAll(String[] data, DestinationInfo info, MessageLogger logger)
            throws FailTransferException, AbortTransferException;

    /** Open iteration session */
    public int open(DestinationInfo info, MessageLogger logger) throws FailTransferException,
            AbortTransferException;

    /** Close iteration session */
    public int close(DestinationInfo info, MessageLogger logger) throws FailTransferException,
            AbortTransferException;

}