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

/**
 * MessageLogger.java
 * 
 * Created: Tue Mar 23 13:39:13 2004
 */

public interface MessageLogger {

    /** Constants for log message verbosity levels. For use by PipeComponents. */
    public static final int ERROR = 0;
    public static final int WARNING = 1;
    public static final int DEBUG = 2;
    
    /** Constant for verbosity level.
     *  Note/FIXME: DYNAMIC level should NOT be used by PipeComponents for writing log messages.
     *  It is intended to be used only by the Pipe framework. */
    
    /** Transfer log verbosity constants*/
    public static final int LOG_ERROR=0;
    public static final int LOG_WARNING=1;
    public static final int LOG_DEBUG=2;
    public static final int LOG_DYNAMIC = 3;
    
    /** Notification mail verbosity constants*/
    public static final int MAIL_ERROR=0;
    public static final int MAIL_WARNING=1;
    public static final int MAIL_DEBUG=2;
    public static final int MAIL_NONE=4;

    public void logMessage(String message, Object creator, int verbosityLevel);

} // MessageLogger