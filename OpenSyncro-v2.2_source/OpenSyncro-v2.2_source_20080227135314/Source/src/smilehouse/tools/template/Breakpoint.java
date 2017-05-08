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

package smilehouse.tools.template;

/**
 * This class represents a breakpoint in the template structure. A Breakpoint is a text line that is
 * included in the template conditionally. a Breakpoint has an interval number for specifiend when
 * it should be included and the data to be included. A single template can be written multiple
 * times with different values in variables, the writes are calculated by a counter. If the
 * counter's value is equally divided by the interval of a breakpoint, if( counter%interval == 0 ).
 */
class Breakpoint {

    private String data;
    private int interval;

    Breakpoint(int interval, String data) {
        this.data = data;
        this.interval = interval;
    }

    public String toString() {
        return this.data;
    }

    boolean breakpoint(int currentPoint) {
        if(interval==0)
            return false;
        else
            return (currentPoint > 0 && (currentPoint % interval) == 0);
    }

    int getInterval() {
        return interval;
    }
}