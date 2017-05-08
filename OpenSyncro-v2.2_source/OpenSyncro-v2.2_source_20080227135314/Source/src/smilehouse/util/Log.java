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

package smilehouse.util;

// smilehouse.util

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Log.java
 * 
 * Created: Wed Sep 20 15:04:55 2000
 * 
 * Class for writing simple log-files.
 */
public class Log {
    PrintWriter logWriter = null;
    SimpleDateFormat formatter;


    /**
     * Constructor for the Log object
     * 
     * @param logFile Description of the Parameter
     * @exception FileNotFoundException Description of the Exception
     */
    public Log(String logFile) throws FileNotFoundException {
        logWriter = new PrintWriter(new FileOutputStream(logFile, true), true);
        formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS");
    }


    /**
     * Description of the Method
     */
    public void finalize() {
        if(logWriter != null) {
            logWriter.close();
        }
    }


    /**
     * Description of the Method
     * 
     * @param message Description of the Parameter
     */
    public void write(String message) {
        logWriter.println("[" + formatter.format(new Date()) + "] " + message);
    }
} // Log