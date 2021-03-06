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
 * Created on Apr 13, 2005
 */
package smilehouse.opensyncro.servlets;

public class PipeComponentCreationException extends RuntimeException {

    public PipeComponentCreationException() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     */
    public PipeComponentCreationException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param cause
     */
    public PipeComponentCreationException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     */
    public PipeComponentCreationException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

}