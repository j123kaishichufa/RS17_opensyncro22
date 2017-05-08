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

package smilehouse.opensyncro.system;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * ClassLoader that can load resources and classes from the given Jar-file. Used to load plugin
 * jars. This classloader isn't used in the background automatically, but should be used to
 * explicitly for loadind classes.
 */
class JarClassLoader extends URLClassLoader {
    /**
     * Constructs a classloader for the given jar file.
     */
    JarClassLoader(File jarFile, ClassLoader parent) throws MalformedURLException {
        super(new URL[] {jarFile.toURL()}, parent);
    }

    /**
     * Overrides superclasses method for making it visible to users of this classloader.
     */
    public Class findClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }
}