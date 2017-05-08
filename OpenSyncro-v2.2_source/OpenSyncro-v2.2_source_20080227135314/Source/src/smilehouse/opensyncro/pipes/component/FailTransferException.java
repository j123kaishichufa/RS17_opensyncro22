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

package smilehouse.opensyncro.pipes.component;

/**
 * Used by components to tell the pipe to fail the current transfer.
 * 
 * Created: Thu Jun 3 17:15:29 2004
 */

public class FailTransferException extends Exception {
    public FailTransferException() {
        super();
    }
    
    public FailTransferException(String message) {
        super(message);
    }

    public FailTransferException(String message, Exception e) {
        super(message, e);
    }


} // FailTransferException