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

/**
 * ParameterInitializer.java Servlets using ParameterManager-class should implement this interface.
 * When ParameterManager is instantiated, a reference to the instantiating class is given as
 * parameter. When parameters need to be initialized (they cannot be fetched from session)
 * initParameters method is called. Created: Fri Apr 6 09:39:43 2001
 * 
 */
public interface ParameterInitializer {
    /**
     * Initializes the parameters of given ParameterManager.
     * 
     * @param pm ParameterManager to be initialized
     */
    public void initParameters(ParameterManager pm);
} // ParameterInitializer