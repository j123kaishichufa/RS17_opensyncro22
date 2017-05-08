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

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * ResourceLabelProvider.java
 * 
 * Created: Mon May 24 14:59:14 2004
 */

public class ResourceLabelProvider implements LabelProvider {
    private String resourceName;
    private String prefix;

    public ResourceLabelProvider(String resourceName, String prefix) {
        this.resourceName = resourceName;
        this.prefix = prefix;
    }

    public ResourceLabelProvider(String resourceName) {
        this.resourceName = resourceName;
        this.prefix = null;
    }

    public String getLabel(String key, String lang) {
        try {
            ResourceBundle resource = ResourceBundle.getBundle(resourceName, new Locale(lang));

            return resource.getString(prefix != null ? prefix + key : key);
        } catch(Exception e) {
            return null;
        }
    }

} // ResourceLabelProvider